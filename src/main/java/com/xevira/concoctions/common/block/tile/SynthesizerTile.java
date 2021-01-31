package com.xevira.concoctions.common.block.tile;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.block.tile.MixerTile.MixerReservoir;
import com.xevira.concoctions.common.container.SynthesizerContainer;
import com.xevira.concoctions.common.handlers.*;
import com.xevira.concoctions.common.utils.Utils;
import com.xevira.concoctions.setup.Catalysts;
import com.xevira.concoctions.setup.Catalysts.Catalyst;
import com.xevira.concoctions.setup.Config;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class SynthesizerTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider, ITilePotionRenamer {
	public static final int OFF_WHEN_POWERED = 0;
	public static final int ON_WHEN_POWERED = 1;
	public static final int IGNORE_REDSTONE = 2;

	public static final int SYNTH_TIME = 400;
	public static final int SYNTH_POWER_TIME = 1200;	// Power is SLOW

	private final SynthesizerTank inputTank;
	private final SynthesizerTank outputTank;
	private final LazyOptional<OutputItemStackHandler> output = LazyOptional.of(() -> new OutputItemStackHandler(2));
	private final SynthesizerCharge energy;
	private final LazyOptional<InputItemStackHandler> catalyst = LazyOptional.of(() -> new InputItemStackHandler(1));
	
	private int redstoneBehavior = OFF_WHEN_POWERED;
	private int synthesizingTime = 0;
	private int synthesizingMaxTime = 0;
	private IConsumeCatalyst consumeCatalyst = null;
	private String newPotionName = "";
	
    public final IIntArray synthesizerData = new IIntArray() {
        @Override
        public int get(int index) {
            switch (index) {
            case 0:
            	return SynthesizerTile.this.synthesizingTime;
            case 1:
            	return SynthesizerTile.this.synthesizingMaxTime;
            case 2:
            	return SynthesizerTile.this.energy.getEnergyStored();
            case 3:
            	return SynthesizerTile.this.energy.getMaxEnergyStored();
            case 4:
            	return SynthesizerTile.this.energy.getBurnTime();
            case 5:
            	return SynthesizerTile.this.energy.getBurnMaxTime();
            case 6:
            	return SynthesizerTile.this.redstoneBehavior;
            default:
                throw new IllegalArgumentException("Invalid index: " + index);
            }
        }

        @Override
        public void set(int index, int value) {
            throw new IllegalStateException("Cannot set values through IIntArray");
        }

        @Override
        public int size() {
            return 7;
        }
    };
	
	public SynthesizerTile() {
		super(Registry.SYNTHESIZER_TILE.get());
		
		this.inputTank = new SynthesizerTank(this, 1, false);
		this.outputTank = new SynthesizerTank(this, 10, true);
		this.energy = new SynthesizerCharge(this, 1000000);
	}
	
	public void markContainingBlockForUpdate(@Nullable BlockState newState)
	{
		if(this.world!=null)
			markBlockForUpdate(getPos(), newState);
	}

	public void markBlockForUpdate(BlockPos pos, @Nullable BlockState newState)
	{
		BlockState state = world.getBlockState(pos);
		if(newState==null)
			newState = state;
		world.notifyBlockUpdate(pos, state, newState, 3);
		world.notifyNeighborsOfStateChange(pos, newState.getBlock());
	}
	
    @Override
    public void read(BlockState stateIn, CompoundNBT nbt) {
        super.read(stateIn, nbt);

        this.inputTank.deserializeNBT(nbt.getCompound("inputTank"));
        this.outputTank.deserializeNBT(nbt.getCompound("outputTank"));
        this.energy.deserializeNBT(nbt.getCompound("energyStorage"));
        this.output.ifPresent(o -> { o.deserializeNBT(nbt.getCompound("outputItems")); });
        this.catalyst.ifPresent(c -> { c.deserializeNBT(nbt.getCompound("catalyst")); });
        this.synthesizingTime = nbt.getInt("synthesizingTime");
        this.synthesizingMaxTime = nbt.getInt("synthesizingMaxTime");
        
        this.consumeCatalyst = null;
        if(nbt.contains("consumeCatalyst"))
        {
        	if(nbt.getBoolean("consumeCatalyst"))
        		this.consumeCatalyst = new ConsumeCatalystEnergy(this.energy.lazy);
        	else
        		this.consumeCatalyst = new ConsumeCatalystItem(this.catalyst);
        }
        
        this.newPotionName = nbt.getString("newPotionName");
        this.redstoneBehavior = nbt.getInt("redstoneBehavior");
    }
    
    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
    	nbt.put("inputTank", this.inputTank.serializeNBT());
    	nbt.put("outputTank", this.outputTank.serializeNBT());
    	nbt.put("energyStorage", this.energy.serializeNBT());
    	this.output.ifPresent(o -> { nbt.put("outputItems", o.serializeNBT()); });
    	this.catalyst.ifPresent(c -> { nbt.put("catalyst", c.serializeNBT()); });
    	
    	if( this.synthesizingTime > 0)
    	{
	    	nbt.putInt("synthesizingTime", this.synthesizingTime);
	    	nbt.putInt("synthesizingMaxTime", this.synthesizingMaxTime);
    	}
    	
    	if(this.consumeCatalyst != null)
    		nbt.putBoolean("consumeCatalyst", this.consumeCatalyst.isEnergy());
    	
    	nbt.putString("newPotionName", this.newPotionName);
    	nbt.putInt("redstoneBehavior", this.redstoneBehavior);

    	return super.write(nbt);
    }
    

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		// Vanilla uses the type parameter to indicate which type of tile entity (command block, skull, or beacon?) is receiving the packet, but it seems like Forge has overridden this behavior
		return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return write(new CompoundNBT());
	}

	@Override
	public void handleUpdateTag(BlockState stateIn, CompoundNBT tag) {
		read(stateIn, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		read(this.getBlockState(), pkt.getNbtCompound());
	}



	@Override
	public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player)
	{
		assert world != null;
		return new SynthesizerContainer(this, this.synthesizerData, i, inventory);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("tile.concoctions.synthesizer");
	}
	
	private boolean synthesizeFluid(int amount, FluidAction action)
	{
		FluidStack inFluid = this.inputTank.getFluid().copy();
		if(inFluid.isEmpty())
			return false;
		
		inFluid.setAmount(amount);

		return this.outputTank.fillInternal(inFluid, action) == amount;
	}
	
	private IConsumeCatalyst getConsumeCatalyst()
	{
		// Item catalyst
		if(this.catalyst.isPresent())
		{
			ItemStack stack = this.catalyst.resolve().get().getStackInSlot(0);
			
			if(!stack.isEmpty())
			{
				if(Catalysts.getCatalyst(stack) != null)
					return new ConsumeCatalystItem(this.catalyst);
			}
		}

		if(this.energy.getEnergyStored() >= Config.SYNTHESIZER_POWER_RATIO.get())
			return new ConsumeCatalystEnergy(this.energy.lazy);
		
		return null;
	}
	
	private ItemStack fillInputTank(FluidTank tank, ItemStack inStack, ItemStack outStack, FluidStack inFluid, ItemStack resultStack)
	{
		if( resultStack.isEmpty() )
		{
			//Concoctions.GetLogger().info("resultStack.isEmpty() {}", resultStack.isEmpty());
			return null;
		}
		
		// Check if result and current output stack are compatible
		if( !outStack.isEmpty() && !Utils.areItemStacksEqual(outStack, resultStack))
		{
			//Concoctions.GetLogger().info("!outStack.isEmpty() {} && !areItemStacksEqual(outStack, resultStack) {}", !outStack.isEmpty(), !areItemStacksEqual(outStack, resultStack));
			return null;
		}
		
		// Check if the output pile can handle more items
		int newSize = outStack.getCount() + 1; 
		if( newSize > resultStack.getMaxStackSize() )
		{
			//Concoctions.GetLogger().info("newSize {} > resultStack.getMaxStackSize() {}", newSize, resultStack.getMaxStackSize());
			return null;
		}
		
		// Can they even stack properly?
		if( !outStack.isEmpty() && (!outStack.isStackable() || !resultStack.isStackable()))
		{
			//Concoctions.GetLogger().info("!outStack.isEmpty() {} && (!outStack.isStackable() {} || !resultStack.isStackable() {})", !outStack.isEmpty(), !outStack.isStackable(), !resultStack.isStackable());
			return null;
		}
		
		FluidStack tankFluid = tank.getFluid();
		
		if( !tankFluid.isEmpty() && !tankFluid.isFluidEqual(inFluid) )
		{
			//Concoctions.GetLogger().info("!tankFluid.isEmpty() {} && !tankFluid.isFluidEqual(inFluid) {}", !tankFluid.isEmpty(), !tankFluid.isFluidEqual(inFluid));
			return null;
		}

		
		int res = tank.fill(inFluid, FluidAction.SIMULATE);
		if( res < inFluid.getAmount() )
		{
			//Concoctions.GetLogger().info("res {} < inFluid.getAmount() {}", res, inFluid.getAmount());
			return null;
		}
		
		tank.fill(inFluid, FluidAction.EXECUTE);
		
		inStack.shrink(1);
		if( outStack.isEmpty() )
			outStack = resultStack.copy();
		else
			outStack.grow(1);
		
		return outStack;
	}

	private boolean tryFillInputTank(int slot, FluidTank tank, ItemStack inStack, OutputItemStackHandler output)
	{
		ItemStack outStack = output.getStackInSlot(slot);
		
		Item item = inStack.getItem();
		FluidStack fluidStack = FluidStack.EMPTY;
		ItemStack outputStack = ItemStack.EMPTY;
		
		
		if( item == Items.POTION ) {
			fluidStack = Utils.getPotionFluidFromNBT(inStack.getTag());
			if(fluidStack.getFluid() != Registry.POTION_FLUID.get())
				return false;
			
			outputStack = new ItemStack(Items.GLASS_BOTTLE, 1);
		}

		// Filling
		if( !fluidStack.isEmpty() && !outputStack.isEmpty()) {
			outStack = fillInputTank(tank, inStack, outStack, fluidStack, outputStack);
			
			if( outStack != null )
			{
				output.setStackInSlot(slot, outStack);
				this.markDirty();
				this.markContainingBlockForUpdate(null);
				
				return true;
			}
		}
		
		return false;
	}

	private ItemStack emptyInputTank(FluidTank tank, ItemStack inStack, ItemStack outStack, FluidStack inFluid, ItemStack resultStack)
	{
		if( resultStack.isEmpty())
			return null;

		FluidStack tankFluid = tank.getFluid();
		
		if( !tankFluid.isEmpty() && !inFluid.isEmpty() && tankFluid.getFluid() != inFluid.getFluid())
			return null;	// Not the same fluid
		
		int volume = FluidAttributes.BUCKET_VOLUME;
		if( !inFluid.isEmpty() )
			volume = inFluid.getAmount();
		
		FluidStack result = tank.drain(volume, FluidAction.SIMULATE);
		if( result.isEmpty() || result.getAmount() < volume )
			return null;	// Not enough room
		
		Utils.addPotionEffectsToItemStack(result, resultStack);

		// Check if result and current output stack are compatible
		if( !outStack.isEmpty() && !Utils.areItemStacksEqual(outStack, resultStack))
			return null;	// Not the same result
		
		// Check if the output pile can handle more items
		int newSize = outStack.getCount() + 1; 
		if( newSize > resultStack.getMaxStackSize() )
				return null;
		
		// Can they even stack properly?
		if( !outStack.isEmpty() && (!outStack.isStackable() || !resultStack.isStackable()))
			return null;
		
		tank.drain(volume, FluidAction.EXECUTE);

		inStack.shrink(1);
		if( outStack.isEmpty())
		{
			outStack = resultStack.copy();
			outStack.setCount(1);
		}
		else
			outStack.grow(1);
		
		return outStack;
	}

	private boolean tryEmptyInputTank(int slot, FluidTank tank, ItemStack inStack, OutputItemStackHandler output)
	{
		ItemStack outStack = output.getStackInSlot(slot);
		
		Item item = inStack.getItem();
		FluidStack fluidStack = FluidStack.EMPTY;
		ItemStack outputStack = ItemStack.EMPTY;

		FluidStack tankFluid = tank.getFluid();
		if( tankFluid.isEmpty() )
			return false;
		
		if( item == Items.SPONGE)
		{
			fluidStack = new FluidStack(Registry.POTION_FLUID.get(), Math.min(tankFluid.getAmount(),FluidAttributes.BUCKET_VOLUME));
			outputStack = new ItemStack(Items.WET_SPONGE, 1);
		}
		else if( item == Items.GLASS_BOTTLE ) {
			if( tankFluid.getFluid() != Registry.POTION_FLUID.get())
				return false;

			outputStack = new ItemStack(Items.POTION, 1);
		}
		
		if( !outputStack.isEmpty() ) {
			outStack = emptyInputTank(tank, inStack, outStack, fluidStack, outputStack);
			
			if( outStack != null ) {
				output.setStackInSlot(slot, outStack);
				this.markDirty();
				this.markContainingBlockForUpdate(null);

				return true;
			}
		}
		
		return false;
	}

	private boolean processInputTank()
	{
		if(!this.inputTank.tank.isPresent())
			return false;
		
		if(!this.inputTank.input.isPresent())
			return false;
		
		if(!this.output.isPresent())
			return false;
		
		ItemStack inStack = this.inputTank.input.resolve().get().getStackInSlot(0);
		FluidTank inTank = this.inputTank.tank.resolve().get();
		OutputItemStackHandler outHandler = this.output.resolve().get();
		
		if(tryFillInputTank(0, inTank, inStack, outHandler))
			return true;
		if(tryEmptyInputTank(0, inTank, inStack, outHandler))
			return true;
		
		return false;
	}

	
	private ItemStack emptyOutputTank(FluidTank tank, ItemStack inStack, ItemStack outStack, FluidStack inFluid, ItemStack resultStack)
	{
		if( resultStack.isEmpty())
			return null;

		FluidStack tankFluid = tank.getFluid();
		
		if( !tankFluid.isEmpty() && !inFluid.isEmpty() && tankFluid.getFluid() != inFluid.getFluid())
			return null;	// Not the same fluid
		
		int volume = FluidAttributes.BUCKET_VOLUME;
		if( !inFluid.isEmpty() )
			volume = inFluid.getAmount();
		
		FluidStack result = tank.drain(volume, FluidAction.SIMULATE);
		if( result.isEmpty() || result.getAmount() < volume )
			return null;	// Not enough room
		
		Utils.addPotionEffectsToItemStack(result, resultStack);
		renameItemName(resultStack);

		// Check if result and current output stack are compatible
		if( !outStack.isEmpty() && !Utils.areItemStacksEqual(outStack, resultStack))
			return null;	// Not the same result
		
		// Check if the output pile can handle more items
		int newSize = outStack.getCount() + 1; 
		if( newSize > resultStack.getMaxStackSize() )
				return null;
		
		// Can they even stack properly?
		if( !outStack.isEmpty() && (!outStack.isStackable() || !resultStack.isStackable()))
			return null;
		
		tank.drain(volume, FluidAction.EXECUTE);

		inStack.shrink(1);
		if( outStack.isEmpty())
		{
			outStack = resultStack.copy();
			outStack.setCount(1);
		}
		else
			outStack.grow(1);
		
		return outStack;
	}

	private void tryEmptyOutputTank(FluidTank tank, ItemStack inStack, OutputItemStackHandler output)
	{
		ItemStack outStack = output.getStackInSlot(1);
		
		Item item = inStack.getItem();
		FluidStack fluidStack = FluidStack.EMPTY;
		ItemStack outputStack = ItemStack.EMPTY;

		FluidStack tankFluid = tank.getFluid();
		if( tankFluid.isEmpty() )
			return;
		
		if( item == Items.SPONGE)
		{
			fluidStack = new FluidStack(Registry.POTION_FLUID.get(), Math.min(tankFluid.getAmount(),FluidAttributes.BUCKET_VOLUME));
			outputStack = new ItemStack(Items.WET_SPONGE, 1);
		}
		else if( item == Items.GLASS_BOTTLE ) {
			if( tankFluid.getFluid() == Fluids.WATER )
			{
				fluidStack = new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
				outputStack = new ItemStack(Items.POTION, 1);
			}
			else if( tankFluid.getFluid() == Registry.POTION_FLUID.get())
			{
				outputStack = new ItemStack(Items.POTION, 1);
			}
			
		} else if( item == Registry.SPLASH_BOTTLE.get() ) {
			if( tankFluid.getFluid() == Fluids.WATER )
			{
				fluidStack = new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
				outputStack = new ItemStack(Items.SPLASH_POTION, 1);
			}
			else if( tankFluid.getFluid() == Registry.POTION_FLUID.get())
			{
				outputStack = new ItemStack(Items.SPLASH_POTION, 1);
			}
			
		} else if( item == Registry.LINGERING_BOTTLE.get() ) {
			if( tankFluid.getFluid() == Fluids.WATER )
			{
				fluidStack = new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
				outputStack = new ItemStack(Items.LINGERING_POTION, 1);
			}
			else if( tankFluid.getFluid() == Registry.POTION_FLUID.get())
			{
				outputStack = new ItemStack(Items.LINGERING_POTION, 1);
			}
			
		} else if( item == Items.ARROW ) {
			if( tankFluid.getFluid() == Fluids.WATER )
			{
				fluidStack = new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
				outputStack = new ItemStack(Items.LINGERING_POTION, 1);
			}
			else if( tankFluid.getFluid() == Registry.POTION_FLUID.get())
			{
				fluidStack = new FluidStack(Registry.POTION_FLUID.get(), 125);	// BUCKET_VOLUME / 8
				outputStack = new ItemStack(Items.TIPPED_ARROW, 1);
			}
		}
		
		if( !outputStack.isEmpty() ) {
			outStack = emptyOutputTank(tank, inStack, outStack, fluidStack, outputStack);
			
			if( outStack != null ) {
				output.setStackInSlot(1, outStack);
				
				this.markDirty();
				this.markContainingBlockForUpdate(null);

				return;
			}
		}
	}

	private void processOutputTank()
	{
		if(!this.outputTank.tank.isPresent())
			return;
		
		if(!this.outputTank.input.isPresent())
			return;
		
		if(!this.output.isPresent())
			return;
		
		ItemStack inStack = this.outputTank.input.resolve().get().getStackInSlot(0);
		FluidTank inTank = this.outputTank.tank.resolve().get();
		OutputItemStackHandler outHandler = this.output.resolve().get();
		
		tryEmptyOutputTank(inTank, inStack, outHandler);
	}

	private boolean isActive()
	{
		if(this.hasWorld())
		{
			boolean powered = this.world.isBlockPowered(this.getPos());
			
			if(redstoneBehavior == OFF_WHEN_POWERED)
				return !powered;
			
			if(redstoneBehavior == ON_WHEN_POWERED)
				return powered;
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void tick() {

		this.energy.tick();
		
		processInputTank();
		processOutputTank();

		if(isActive())
		{
			if(this.synthesizingTime > 0)
			{
				if(this.consumeCatalyst == null || !this.consumeCatalyst.isValid())
				{
					this.consumeCatalyst = null;
					this.synthesizingTime = 0;
				}
				else
				{
					int strength = this.consumeCatalyst.getStrength();
	
					if(synthesizeFluid(strength, FluidAction.SIMULATE))
					{
						this.synthesizingTime--;
						
						if(this.synthesizingTime == 0)
						{
							if((this.outputTank.getFluidAmount() + strength) <= this.outputTank.getCapacity())
							{
								synthesizeFluid(strength, FluidAction.EXECUTE);
								this.consumeCatalyst.consume();
								this.consumeCatalyst = null;
							}
						}
					}
					else
					{
						this.consumeCatalyst = null;
						this.synthesizingTime = 0;
					}
				}
			}
			else 
			{
				this.synthesizingTime = 0;
				this.consumeCatalyst = getConsumeCatalyst();
				if(this.consumeCatalyst != null)
				{
					if(synthesizeFluid(this.consumeCatalyst.getStrength(), FluidAction.SIMULATE))
					{
						this.synthesizingMaxTime = this.synthesizingTime = this.consumeCatalyst.getTime();
						
					}
					else
					{
						this.consumeCatalyst = null;
					}
					
				}
			}
		}
	}
	
	private void setCustomPotionName(ItemStack stack, String prefix)
	{
		if( StringUtils.isBlank(this.newPotionName))
		{
			boolean isBasePotion = false;
			if(stack.hasTag())
			{
				CompoundNBT root = stack.getTag();
				if( root.contains("CustomPotionName") )
					root.remove("CustomPotionName");
				
				isBasePotion = root.contains("Potion");
			}
			
			if( isBasePotion )
			{
				stack.clearCustomName();
			}
			else if (stack.getItem() == Items.TIPPED_ARROW )
			{
				stack.setDisplayName(new TranslationTextComponent("item.concoctions.tipped_arrow.solution"));
			}
			else
			{
				stack.setDisplayName(new TranslationTextComponent("item.concoctions.solution"));
			}
		}
		else
		{
			CompoundNBT root = stack.getOrCreateTag();
			root.putString("CustomPotionName", this.newPotionName);
			
			stack.setDisplayName(new TranslationTextComponent(prefix, this.newPotionName));
		}
	}

	
	private void renameItemName(ItemStack stack)
	{
		if( stack.isEmpty() )
			return;
		
		if( stack.getItem() == Items.POTION )
		{
			setCustomPotionName(stack, "item.concoctions.potion.prefix");
		}
		else if( stack.getItem() == Items.SPLASH_POTION )
		{
			setCustomPotionName(stack, "item.concoctions.splash_potion.prefix");
		}
		else if( stack.getItem() == Items.LINGERING_POTION )
		{
			setCustomPotionName(stack, "item.concoctions.lingering_potion.prefix");
		}
		else if( stack.getItem() == Items.TIPPED_ARROW )
		{
			setCustomPotionName(stack, "item.concoctions.tipped_arrow.prefix");
		}
	}


	private void renameItemName()
	{
		ItemStackHandler inv = this.output.orElse(null);
		
		if( inv != null )
		{
			ItemStack stack = inv.getStackInSlot(1);
			renameItemName(stack);
		}
	}
	
	private void updatePotionName(FluidStack fluid, String name)
	{
		if(fluid.getFluid() == Registry.POTION_FLUID.get())
		{
			CompoundNBT root = fluid.getTag();
			if(StringUtils.isBlank(name))
			{
				if(root.contains("CustomPotionName"))
					root.remove("CustomPotionName");
			}
			else
			{
				root.putString("CustomPotionName", name);
			}
		}
	}

	@Override
	public String getPotionName() {
		return this.newPotionName;
	}

	@Override
	public void updatePotionName(String name)
	{
		if(this.hasWorld())
		{
			this.newPotionName = name;
			renameItemName();

			this.inputTank.tank.ifPresent(t -> {
				updatePotionName(t.getFluid(), name);
			});

			this.outputTank.tank.ifPresent(t -> {
				updatePotionName(t.getFluid(), name);
			});
		}
		
	}
	
	
	private void handlerDropItems(IItemHandler handler, World worldIn, BlockPos pos)
	{
		for (int i = 0; i < handler.getSlots(); i++)
			InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(),
					handler.getStackInSlot(i));
	}
	
	public void dropItems(World worldIn, BlockPos pos)
	{
		this.inputTank.input.ifPresent(handler -> handlerDropItems(handler, worldIn, pos));
		this.outputTank.input.ifPresent(handler -> handlerDropItems(handler, worldIn, pos));
		this.catalyst.ifPresent(handler -> handlerDropItems(handler, worldIn, pos));
		this.energy.input.ifPresent(handler -> handlerDropItems(handler, worldIn, pos));
		this.output.ifPresent(handler -> handlerDropItems(handler, worldIn, pos));
	}

	public void getSynthesizingInformation(List<ITextComponent> tooltip)
	{
		if(this.synthesizingTime > 0 && this.consumeCatalyst != null)
		{
			tooltip.add(new StringTextComponent(""));
			
			int seconds = (this.synthesizingTime + 19) / 20;
			if(seconds > 1)
				tooltip.add(new TranslationTextComponent("gui.concoctions.synthesizing.time.prefix").mergeStyle(TextFormatting.BLUE)
						.append(new TranslationTextComponent("gui.concoctions.synthesizing.time.seconds", seconds).mergeStyle(TextFormatting.YELLOW)));
			else
				tooltip.add(new TranslationTextComponent("gui.concoctions.synthesizing.time.prefix").mergeStyle(TextFormatting.BLUE)
						.append(new TranslationTextComponent("gui.concoctions.synthesizing.time.second").mergeStyle(TextFormatting.YELLOW)));
			
			tooltip.add(new StringTextComponent(""));
			this.consumeCatalyst.addInformation(tooltip);
			tooltip.add(new StringTextComponent(""));
			int amount = this.consumeCatalyst.getStrength();
			tooltip.add(new TranslationTextComponent("gui.concoctions.synthesizing.fluid").mergeStyle(TextFormatting.BLUE)
					.append(new TranslationTextComponent("gui.concoctions.synthesizing.fluid.value", amount).mergeStyle(TextFormatting.WHITE)));
			
		}
	}
	
	
	public SynthesizerTank getInputTank()
	{
		return this.inputTank;
	}
	
	public ItemStackHandlerEx getInputTankItem()
	{
		return this.inputTank.input.orElse(new ItemStackHandlerEx(1));
	}
	
	public SynthesizerTank getOutputTank()
	{
		return this.outputTank;
	}
	
	public ItemStackHandlerEx getOutputTankItem()
	{
		return this.outputTank.input.orElse(new ItemStackHandlerEx(1));
	}
	
	public OutputItemStackHandler getOutputs()
	{
		return this.output.orElse(new OutputItemStackHandler(2));
	}
	
	public InputItemStackHandler getCatalyst()
	{
		return this.catalyst.orElse(new InputItemStackHandler(1));
	}

	public InputItemStackHandler getEnergyInput()
	{
		return this.energy.input.orElse(new InputItemStackHandler(1));
	}
	
	public FluidStack getInputFluid()
	{
		return this.inputTank.getFluid();
	}
	
	public int getInputCapacity()
	{
		return this.inputTank.getCapacity();
	}
	
	public FluidStack getOutputFluid()
	{
		return this.outputTank.getFluid();
	}
	
	public int getOutputCapacity()
	{
		return this.outputTank.getCapacity();
	}
	
	public int getRedstoneBehavior()
	{
		return this.redstoneBehavior;
	}
	
	public void setRedstoneBehavior(int behavior)
	{
		this.redstoneBehavior = MathHelper.clamp(behavior, 0, 2);
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}
	
	public static class SynthesizerTank implements IFluidHandler, IFluidTank, INBTSerializable<CompoundNBT>
	{
		public final LazyOptional<FluidTank> tank;
		public final LazyOptional<ItemStackHandlerEx> input;
		private final boolean output;

		public final LazyOptional<SynthesizerTank> lazy;
		
		private final SynthesizerTile tile;
		
		public SynthesizerTank(SynthesizerTile tile, int capacity, boolean output)
		{
			this.tank = LazyOptional.of(() -> new FluidTank(capacity * FluidAttributes.BUCKET_VOLUME));
			if(output)
				this.input = LazyOptional.of(() -> new SynthesizerEmptyingItemStackHandler());
			else
				this.input = LazyOptional.of(() -> new SynthesizerFillingItemStackHandler());
			this.output = output;
			
			this.lazy = LazyOptional.of(() -> this);
			this.tile = tile;
		}
		
		public int fillInternal(FluidStack resource, FluidAction action)
		{
			if(this.tank.isPresent())
			{
				return this.tank.resolve().get().fill(resource, action);
			}

			return 0;
		}

		public FluidStack drainInternal(FluidStack resource, FluidAction action)
		{
			if(this.tank.isPresent())
			{
				return this.tank.resolve().get().drain(resource, action);
			}

			return FluidStack.EMPTY;
		}

		public FluidStack drainInternal(int maxDrain, FluidAction action)
		{
			if(this.tank.isPresent())
			{
				return this.tank.resolve().get().drain(maxDrain, action);
			}

			return FluidStack.EMPTY;
		}
		
		//////////////////////////////
		// IFluidHandler

		@Override
		public int getTanks() {
			return this.tank.isPresent() ? 1 : 0;
		}

		@Override
		public FluidStack getFluidInTank(int tank) {
			if(this.tank.isPresent())
			{
				return this.tank.resolve().get().getFluidInTank(tank);
			}
			
			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank) {
			if(this.tank.isPresent())
			{
				return this.tank.resolve().get().getTankCapacity(tank);
			}

			return 0;
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			if(this.tank.isPresent())
			{
				return this.tank.resolve().get().isFluidValid(tank, stack);
			}
			return false;
		}
		
		@Override
		public int fill(FluidStack resource, FluidAction action)
		{
			if(!this.output)
			{
				return fillInternal(resource, action);
			}

			return 0;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action)
		{
			if(this.output)
			{
				return drainInternal(resource, action);
			}

			return FluidStack.EMPTY;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action)
		{
			if(this.output)
			{
				return drainInternal(maxDrain, action);
			}

			return FluidStack.EMPTY;
		}
		
		//////////////////////////////
		// IFluidTank

		@Override
		public FluidStack getFluid() {
			if(this.tank.isPresent())
			{
				return this.tank.resolve().get().getFluid();
			}
			return FluidStack.EMPTY;
		}

		@Override
		public int getFluidAmount() {
			if(this.tank.isPresent())
			{
				return this.tank.resolve().get().getFluidAmount();
			}
			return 0;
		}

		@Override
		public int getCapacity() {
			if(this.tank.isPresent())
			{
				return this.tank.resolve().get().getCapacity();
			}
			return 0;
		}

		@Override
		public boolean isFluidValid(FluidStack stack) {
			if(this.tank.isPresent())
			{
				return this.tank.resolve().get().isFluidValid(stack);
			}
			return false;
		}
		
		
		//////////////////////////////
		// INBTSerializable<CompoundNBT>

		@Override
		public CompoundNBT serializeNBT()
		{
			CompoundNBT nbt = new CompoundNBT();
			
			this.tank.ifPresent(t -> { nbt.put("t", t.writeToNBT(new CompoundNBT())); });
			this.input.ifPresent(i -> { nbt.put("i", i.serializeNBT()); });
			
			return nbt;
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt)
		{
			this.tank.ifPresent(t -> { t.readFromNBT(nbt.getCompound("t")); });
			this.input.ifPresent(i -> { i.deserializeNBT(nbt.getCompound("i")); });
		}
	}
	
	public static class SynthesizerCharge implements IEnergyStorage, IItemHandler, IItemHandlerModifiable, INBTSerializable<CompoundNBT>
	{
		public final LazyOptional<InputItemStackHandler> input;
		private final int capacity;
		
		public final LazyOptional<SynthesizerCharge> lazy;

		private int energy;
		private int burningTime;
		private int burningMaxTime;
		
		private final SynthesizerTile tile;

		public SynthesizerCharge(SynthesizerTile tile, int capacity)
		{
			this.input = LazyOptional.of(() -> new SynthesizerEnergyInputHandler(1));

			this.burningTime = 0;
			this.burningMaxTime = 0;
			this.energy = 0;
			this.capacity = capacity;
			
			this.lazy = LazyOptional.of(() -> this);
			
			this.tile = tile;
		}

		public boolean consumeEnergy(int maxExtract)
		{
			int consumed = Math.min(energy, maxExtract);
			this.energy -= consumed;
			return this.energy == 0;
		}
		
		public boolean isBurning()
		{
			return this.burningTime > 0;
		}
		
		public int getBurnTime()
		{
			return this.burningTime;
		}
		
		public int getBurnMaxTime()
		{
			return this.burningMaxTime;
		}
		
		public void tick()
		{
			if(this.burningTime > 0)
			{
				if( this.energy < capacity)
				{
					this.burningTime--;
					this.energy = Math.min(this.energy + Config.FUEL_TO_POWER.get(), capacity);
					this.tile.markDirty();
					this.tile.markContainingBlockForUpdate(null);
				}
			}
			else if(this.input.isPresent())
			{
				ItemStack stack = this.input.resolve().get().getStackInSlot(0);
				
				LazyOptional<IEnergyStorage> storage = stack.getCapability(CapabilityEnergy.ENERGY);
				
				if(storage.isPresent())
				{
					int power = storage.resolve().get().extractEnergy(Config.MAX_POWER_TRANSFER.get(), true);
					
					if( power > 0 )
					{
						power = Math.min(capacity - energy, power);
						
						if(power > 0)
						{
							storage.resolve().get().extractEnergy(power, false);
							this.energy += power;
							this.tile.markDirty();
							this.tile.markContainingBlockForUpdate(null);
						}
					}
				}
				else
				{
					int burn = ForgeHooks.getBurnTime(stack);
					
					if( burn > 0)
					{
						this.burningMaxTime = this.burningTime = burn;
						ItemStack container = stack.getContainerItem();
						
						if(container.isEmpty())
						{
							stack.shrink(1);
							this.input.resolve().get().setStackInSlot(0, stack);
						}
						else
							this.input.resolve().get().setStackInSlot(0, container);
							
						this.tile.markDirty();
						this.tile.markContainingBlockForUpdate(null);
					}
				}
			}
		}
		
		//////////////////////////////
		// IEnergyStorage

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if(canReceive())
			{
				int received = Math.min(capacity - energy, maxReceive);
				if(!simulate)
					this.energy += received;
				return received;
			}

			return 0;
		}
		
		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			// No extract
			return 0;
		}

		@Override
		public int getEnergyStored() {
			return energy;
		}

		@Override
		public int getMaxEnergyStored() {
			return capacity;
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public boolean canReceive() {
			return true;
		}
		
		
		
		//////////////////////////////
		// IItemHandler

		@Override
		public int getSlots() {
			if(this.input.isPresent())
				return this.input.resolve().get().getSlots();
			
			return 0;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			if(this.input.isPresent())
				return this.input.resolve().get().getStackInSlot(slot);

			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if(this.input.isPresent())
				return this.input.resolve().get().insertItem(slot, stack, simulate);

			return stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if(this.input.isPresent())
				return this.input.resolve().get().extractItem(slot, amount, simulate);

			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot) {
			if(this.input.isPresent())
				return this.input.resolve().get().getSlotLimit(slot);

			return 0;
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			if(this.input.isPresent())
				return this.input.resolve().get().isItemValid(slot, stack);
				
			return false;
		}

		//////////////////////////////
		// IItemHandlerModifiable

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			if(this.input.isPresent())
				this.input.resolve().get().setStackInSlot(slot, stack);
			
		}
		
		//////////////////////////////
		// INBTSerializable<CompoundNBT>

		@Override
		public CompoundNBT serializeNBT() {
			CompoundNBT nbt = new CompoundNBT();
			
			this.input.ifPresent(i -> { nbt.put("i", i.serializeNBT()); });
			nbt.putInt("e", this.energy);
			nbt.putInt("bt", this.burningTime);
			
			return nbt;
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt)
		{
			this.input.ifPresent(i -> { i.deserializeNBT(nbt.getCompound("i")); });
			this.energy = nbt.getInt("e");
			this.burningTime = nbt.getInt("bt");
		}
		
	}

	public static interface IConsumeCatalyst
	{
		boolean isEnergy();
		boolean isValid();
		boolean consume();
		int getStrength();
		int getTime();
		void addInformation(List<ITextComponent> tooltip);
	}
	
	public static class ConsumeCatalystItem implements IConsumeCatalyst
	{
		private final LazyOptional<InputItemStackHandler> input;
		private final int itemStrength;
		private final int itemTime;
		
		public ConsumeCatalystItem(LazyOptional<InputItemStackHandler> input)
		{
			this.input = input;
			
			if(this.input.isPresent())
			{
				ItemStack stack = this.input.resolve().get().getStackInSlot(0);
				Catalyst catalyst = Catalysts.getCatalyst(stack);
				if(catalyst != null)
				{
					this.itemStrength = catalyst.strength;
					this.itemTime = catalyst.time;
				}
				else
				{
					this.itemStrength = 0;
					this.itemTime = 0;
				}
			}
			else
			{
				this.itemStrength = 0;
				this.itemTime = 0;
			}
		}
		
		@Override
		public boolean isEnergy() {
			return false;
		}

		@Override
		public boolean isValid() {
			if(this.itemStrength < 1 || this.itemTime < 1)
				return false;
			
			if(this.input.isPresent())
			{
				ItemStack stack = this.input.resolve().get().getStackInSlot(0);
				
				if(stack.isEmpty())
					return false;
				
				Catalyst catalyst = Catalysts.getCatalyst(stack);
				if( catalyst == null)
					return false;
				
				return catalyst.strength == this.itemStrength &&
						catalyst.time == this.itemTime;
			}

			return false;
		}


		@Override
		public boolean consume()
		{
			if(this.input.isPresent())
			{
				ItemStack stack = this.input.resolve().get().getStackInSlot(0);
				
				if(!stack.isEmpty())
				{
					stack.shrink(1);
					this.input.resolve().get().setStackInSlot(0, stack);
					return true;
				}
			}
			
			return false;
		}

		@Override
		public int getStrength() {
			return this.itemStrength;
		}
		
		@Override
		public int getTime() {
			return this.itemTime;
		}

		@Override
		public void addInformation(List<ITextComponent> tooltip)
		{
			if(this.input.isPresent())
			{
				ItemStack stack = this.input.resolve().get().getStackInSlot(0);
				tooltip.add(new TranslationTextComponent("gui.concoctions.synthesizing.consume.item").mergeStyle(TextFormatting.BLUE)
							.append(new StringTextComponent(stack.getDisplayName().getString()).mergeStyle(TextFormatting.WHITE)));
			}
		}
	}
	
	public static class ConsumeCatalystEnergy implements IConsumeCatalyst
	{
		private final LazyOptional<SynthesizerCharge> energy;
		
		public ConsumeCatalystEnergy(LazyOptional<SynthesizerCharge> energy)
		{
			this.energy = energy;
		}
		
		@Override
		public boolean isEnergy() {
			return true;
		}

		@Override
		public boolean isValid()
		{
			if(this.energy.isPresent())
			{
				return this.energy.resolve().get().getEnergyStored() >= Config.SYNTHESIZER_POWER_RATIO.get();
			}
			
			return false;
		}

		@Override
		public boolean consume() {
			if(this.energy.isPresent())
			{
				this.energy.resolve().get().consumeEnergy(Config.SYNTHESIZER_POWER_RATIO.get());
				return true;
			}
			
			return false;
		}

		@Override
		public int getStrength() {
			return Config.SYNTHESIZER_STRENGTH_RATIO.get();
		}

		@Override
		public int getTime() {
			return SYNTH_POWER_TIME;
		}

		@Override
		public void addInformation(List<ITextComponent> tooltip)
		{
			if(this.energy.isPresent())
			{
				tooltip.add(new TranslationTextComponent("gui.concoctions.synthesizing.consume.energy").mergeStyle(TextFormatting.BLUE)
						.append(new TranslationTextComponent("gui.concoctions.synthesizing.consume.energy.value", Config.SYNTHESIZER_POWER_RATIO.get()).mergeStyle(TextFormatting.GREEN)));
			}

			
		}
	}

}

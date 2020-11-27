/*
 * TODO: Add fluid handling support (need a way to transport them)
 * 
 * TODO: Get the custom potion name to propagate when the potion fluid changes either from using a bottled potion or via fluid input
*/
package com.xevira.concoctions.common.block.tile;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.xevira.concoctions.common.container.BrewingStationContainer;
import com.xevira.concoctions.common.fluids.PotionFluid;
import com.xevira.concoctions.common.handlers.*;
import com.xevira.concoctions.common.utils.Utils;
import com.xevira.concoctions.setup.BrewingRecipes;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class BrewingStationTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
	public static final int INV_SLOTS = Slots.TOTAL.getId();
	public static final int FUEL_SLOTS = 1;
	public static final int ITEM_SLOTS = Slots.QUEUE5.getId() - Slots.ITEM.getId() + 1;
	public static final int BOTTLE_IN_SLOTS = 1;
	public static final int BOTTLE_OUT_SLOTS = 1;
	
	public enum Slots {
		FUEL(0),
		ITEM(1),
		QUEUE1(2),
		QUEUE2(3),
		QUEUE3(4),
		QUEUE4(5),
		QUEUE5(6),
		BOTTLE_IN(7),
		BOTTLE_OUT(8),
		TOTAL(9);
		
		int id;
		
		Slots(int n) {
			id = n;
		}
		
		public int getId() {
			return id;
		}
	}
	
	public int fluidColor;
	public LazyOptional<FluidTank> tank;
	public FluidTank tankStorage;
//	private LazyOptional<ItemStackHandler> inventory  = LazyOptional.of(() -> new ItemStackHandler(BrewingStationTile.INV_SLOTS));
	
	private LazyOptional<BrewingFuelItemStackHandler> invFuel = LazyOptional.of(() -> new BrewingFuelItemStackHandler());
	private LazyOptional<BrewingQueueItemStackHandler> invItems = LazyOptional.of(() -> new BrewingQueueItemStackHandler());
	private LazyOptional<BrewingBottleInItemStackHandler> invBottleIn = LazyOptional.of(() -> new BrewingBottleInItemStackHandler());
	private LazyOptional<BrewingBottleOutItemStackHandler> invBottleOut = LazyOptional.of(() -> new BrewingBottleOutItemStackHandler());
	
	
	private ItemStack ingredient = ItemStack.EMPTY;
	private int brewTime;
	private int maxBrewTime;
	private int fuelRemaining;
	private String newPotionName;
	
    // Handles tracking changes, kinda messy but apparently this is how the cool kids do it these days
    public final IIntArray brewingStationData = new IIntArray() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                	return BrewingStationTile.this.brewTime;
                case 1:
                	return BrewingStationTile.this.fuelRemaining;
                case 2:
                	return BrewingStationTile.this.fluidColor;
                case 3:
                	return BrewingStationTile.this.tankStorage.getFluidAmount();
                case 4:
                	return BrewingStationTile.this.maxBrewTime;
                case 5:
                	return (BrewingStationTile.this.tankStorage.getFluid().getFluid() == Registry.POTION_FLUID.get()) ? 1 : 0;
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
            return 6;
        }
    };
	
	public BrewingStationTile() {
		super(Registry.BREWING_STATION_TILE.get());
		
		this.tankStorage = new FluidTank(10 * FluidAttributes.BUCKET_VOLUME);
		this.tank = LazyOptional.of(() -> this.tankStorage);
		this.brewTime = 0;
		this.maxBrewTime = 0;
	}

	private FluidStack getPotionFluidFromNBT(CompoundNBT root)
	{
		FluidStack fluidStack = new FluidStack(Registry.POTION_FLUID.get(), FluidAttributes.BUCKET_VOLUME);
		
		if(root == null) return fluidStack;
		
		// Add NBT data
		List<EffectInstance> effects = PotionUtils.getEffectsFromTag(root);
		CompoundNBT tag = new CompoundNBT();

		if( root.contains("Potion")) {
			String basePotion = root.getString("Potion");
			
			Potion potion = Potion.getPotionTypeForName(basePotion);
			if( potion == Potions.WATER)
			{
				// This is water... have it as water, instead of the "potion" water
				return new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
			}
			
			tag.putString("BasePotion", basePotion);
		}
		ListNBT listNBT = new ListNBT();
		for(EffectInstance effect : effects) {
			CompoundNBT tagEffect = new CompoundNBT();
				
//				Concoctions.GetLogger().info("Potion: {} {} {}", effect.getEffectName(), effect.getAmplifier(), effect.getDuration());
			
			effect.write(tagEffect);
			listNBT.add(tagEffect);
		}
		tag.put("CustomPotionEffects", listNBT);
		
		if( root.contains("DyedPotion") && root.contains("CustomPotionColor"))
		{
			if( root.getBoolean("DyedPotion"))
			{
				tag.putInt("CustomPotionColor", root.getInt("CustomPotionColor"));
				tag.putBoolean("DyedPotion", true);
			}
		}
		
		if( root.contains("CustomPotionName") )
			tag.putString("CustomPotionName", root.getString("CustomPotionName"));

		fluidStack.setTag(tag);
		return fluidStack;

	}
	
	private void addPotionEffectsToItemStack(FluidStack inFluid, ItemStack outStack)
	{
		if( !Utils.isPotionItemStack(outStack) )
			return;
		
		if( inFluid.getFluid() == Fluids.WATER )
		{
			// Shortcut if the fluid is water and the output is to be a potion (ie. water bottle)
			PotionUtils.addPotionToItemStack(outStack, Potions.WATER);
			return;
		}
		
		CompoundNBT root = outStack.getOrCreateTag();
		
		if( inFluid.getTag().contains("BasePotion")) {
			String basePotion = inFluid.getTag().getString("BasePotion");
			root.putString("Potion", basePotion);
		}
		else
		{
			if( inFluid.getTag().contains("CustomPotionEffects", 9))
			{
				ListNBT effects = inFluid.getTag().getList("CustomPotionEffects", 10);
				root.put("CustomPotionEffects", effects.copy());
				
				Fluid fluid = inFluid.getFluid();
				if(fluid instanceof PotionFluid) {
					int col = fluid.getAttributes().getColor(inFluid);
					
					root.putInt("CustomPotionColor", col);
					root.putBoolean("DyedPotion", false);	// Used to differentiate between raw custom potions and dyed potions 
				}
			}
		}

		if( inFluid.getTag().contains("CustomPotionColor")) {
			root.putInt("CustomPotionColor", inFluid.getTag().getInt("CustomPotionColor"));
			root.putBoolean("DyedPotion", true);
		}

		renameItemName(outStack);
	}
	
	private boolean areItemStacksEqual(ItemStack a, ItemStack b) {
		// same functionality as ItemStack.areItemStackEqual(a,b) but ignoring item count
		if( a.isEmpty() && b.isEmpty() ) {
			return true;
		} else if (a.getItem() != b.getItem()) {
			return false;
		} else if (a.getTag() == null && b.getTag() != null) {
			return false;
		} else {
			return (a.getTag() == null || a.getTag().equals(b.getTag())) && a.areCapsCompatible(b);
		}
	}

	private ItemStack fillTank(ItemStack inStack, ItemStack outStack, FluidStack inFluid, ItemStack resultStack)
	{
		if( resultStack.isEmpty() )
		{
			//Concoctions.GetLogger().info("resultStack.isEmpty() {}", resultStack.isEmpty());
			return null;
		}
		
		// Check if result and current output stack are compatible
		if( !outStack.isEmpty() && !areItemStacksEqual(outStack, resultStack))
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
		
		FluidStack tankFluid = this.tankStorage.getFluid();
		
		if( !tankFluid.isEmpty() && !tankFluid.isFluidEqual(inFluid) )
		{
			//Concoctions.GetLogger().info("!tankFluid.isEmpty() {} && !tankFluid.isFluidEqual(inFluid) {}", !tankFluid.isEmpty(), !tankFluid.isFluidEqual(inFluid));
			return null;
		}

		
		int res = this.tankStorage.fill(inFluid, FluidAction.SIMULATE);
		if( res < inFluid.getAmount() )
		{
			//Concoctions.GetLogger().info("res {} < inFluid.getAmount() {}", res, inFluid.getAmount());
			return null;
		}
		
		this.tankStorage.fill(inFluid, FluidAction.EXECUTE);
		
		inStack.shrink(1);
		if( outStack.isEmpty() )
			outStack = resultStack.copy();
		else
			outStack.grow(1);
		
		return outStack;
	}
	
	private boolean tryFillTank(ItemStackHandler inv, ItemStack inStack, ItemStack outStack) {
		Item item = inStack.getItem();
		FluidStack fluidStack = FluidStack.EMPTY;
		ItemStack outputStack = ItemStack.EMPTY;
		
		
		if( item == Items.WATER_BUCKET ) {
			fluidStack = new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
			outputStack = new ItemStack(Items.BUCKET, 1);
		} else if( item == Items.LAVA_BUCKET ) {
			fluidStack = new FluidStack(Fluids.LAVA, FluidAttributes.BUCKET_VOLUME);
			outputStack = new ItemStack(Items.BUCKET, 1);
		} else if( item == Items.POTION ) {
			fluidStack = getPotionFluidFromNBT(inStack.getTag());
			outputStack = new ItemStack(Items.GLASS_BOTTLE, 1);
		}

		// Filling
		if( !fluidStack.isEmpty() && !outputStack.isEmpty()) {
			outStack = fillTank(inStack, outStack, fluidStack, outputStack);
			
			if( outStack != null )
			{
				inv.setStackInSlot(0, outStack);
				this.markDirty();
				this.markContainingBlockForUpdate(null);
				
				return true;
			}
		}
		
		return false;
	}

	private ItemStack emptyTank(ItemStack inStack, ItemStack outStack, FluidStack inFluid, ItemStack resultStack)
	{
		if( resultStack.isEmpty())
			return null;

		FluidStack tankFluid = this.tankStorage.getFluid();
		
		if( !tankFluid.isEmpty() && !inFluid.isEmpty() && tankFluid.getFluid() != inFluid.getFluid())
			return null;	// Not the same fluid
		
		int volume = FluidAttributes.BUCKET_VOLUME;
		if( !inFluid.isEmpty() )
			volume = inFluid.getAmount();
		
		FluidStack result = this.tankStorage.drain(volume, FluidAction.SIMULATE);
		if( result.isEmpty() || result.getAmount() < volume )
			return null;	// Not enough room
		
		addPotionEffectsToItemStack(result, resultStack);

		// Check if result and current output stack are compatible
		if( !outStack.isEmpty() && !areItemStacksEqual(outStack, resultStack))
			return null;	// Not the same result
		
		// Check if the output pile can handle more items
		int newSize = outStack.getCount() + 1; 
		if( newSize > resultStack.getMaxStackSize() )
				return null;
		
		// Can they even stack properly?
		if( !outStack.isEmpty() && (!outStack.isStackable() || !resultStack.isStackable()))
			return null;
		
		this.tankStorage.drain(volume, FluidAction.EXECUTE);

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

	
	private void tryEmptyTank(ItemStackHandler inv, ItemStack inStack, ItemStack outStack) {
		Item item = inStack.getItem();
		FluidStack fluidStack = FluidStack.EMPTY;
		ItemStack outputStack = ItemStack.EMPTY;

		FluidStack tankFluid = this.tankStorage.getFluid();
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
			
		} else if( item == Items.BUCKET ) {
			if( tankFluid.getFluid() == Fluids.WATER ) {
				fluidStack = new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
				outputStack = new ItemStack(Items.WATER_BUCKET, 1);
				
			} else if( tankFluid.getFluid() == Fluids.LAVA ) {
				fluidStack = new FluidStack(Fluids.LAVA, FluidAttributes.BUCKET_VOLUME);
				outputStack = new ItemStack(Items.LAVA_BUCKET, 1);
			}
		}
		
		if( !outputStack.isEmpty() ) {
			outStack = emptyTank(inStack, outStack, fluidStack, outputStack);
			
			if( outStack != null ) {
				inv.setStackInSlot(0, outStack);
				this.markDirty();
				this.markContainingBlockForUpdate(null);

				return;
			}
		}
	}
	
	private int canBrew(ItemStackHandler inv)
	{
		ItemStack stack = inv.getStackInSlot(0);
		FluidStack fluid = this.tankStorage.getFluid();
		
		if( stack.isEmpty() || fluid.isEmpty() ) return 0;
		
		return BrewingRecipes.canBrew(stack, fluid);
	}
	
	private void brewPotion(ItemStackHandler inv)
	{
		ItemStack stack = inv.getStackInSlot(0);
		FluidStack fluid = this.tankStorage.getFluid();
		
		FluidStack result = BrewingRecipes.getBrewingRecipe(stack, fluid);
		
		if( result != null )
		{
			result = getPotionFluidFromNBT(result.getTag());
			result.setAmount(fluid.getAmount());
			
			if( fluid.hasTag() )
			{
				CompoundNBT root = fluid.getTag();
				
				if(root.contains("CustomPotionName"))
					result.getOrCreateTag().putString("CustomPotionName", root.getString("CustomPotionName"));
			}
			
			this.tankStorage.drain(this.tankStorage.getCapacity(), FluidAction.EXECUTE);
			this.tankStorage.fill(result, FluidAction.EXECUTE);
			
			BlockPos blockpos = this.getPos();
			if (stack.hasContainerItem()) {
				ItemStack container = stack.getContainerItem();
				stack.shrink(1);
				if (stack.isEmpty()) {
					stack = container;
				} else if (!this.world.isRemote) {
					InventoryHelper.spawnItemStack(this.world, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), container);
				}
			} else
				stack.shrink(1);
		      
			inv.setStackInSlot(0, stack);
			this.world.playEvent(1035, blockpos, 0);		// Play brewing sound
		}
		
	}
	
	@Override
	public void tick() {
		BrewingFuelItemStackHandler invF = this.invFuel.orElse(null); 
		BrewingQueueItemStackHandler invQ = this.invItems.orElse(null);
		BrewingBottleInItemStackHandler invBIn = this.invBottleIn.orElse(null);
		BrewingBottleOutItemStackHandler invBOut = this.invBottleOut.orElse(null);
		// Check for Blaze Powder
		if( invF != null ) {
			ItemStack stack = invF.getStackInSlot(0);
			if( this.fuelRemaining <= 0 && stack != null && !stack.isEmpty() && stack.getItem() == Items.BLAZE_POWDER) {
				this.fuelRemaining = 20;
				stack.shrink(1);
				this.markDirty();
			}
		}
		
		
		if( invQ != null)
			invQ.collapseQueue();
		
		// Check for Fluid Inputs, only process fluids if nothing is being brewed
		if( invBIn != null && invBOut != null && this.brewTime <= 0) {
			ItemStack inStack = invBIn.getStackInSlot(0);
			ItemStack outStack = invBOut.getStackInSlot(0);
			
			if( inStack != null && !inStack.isEmpty()) {
				if( !tryFillTank(invBOut, inStack, outStack))
					tryEmptyTank(invBOut, inStack, outStack);
			}
		}
		
		if( invQ != null )
		{
			int cb = canBrew(invQ);				// Can Brew?
			boolean ib = this.brewTime > 0;			// Is Brewing?
			if( ib ) {
				--this.brewTime;
				boolean db = this.brewTime == 0;	// Done Brewing?
				if( cb > 0 && db ) {
					this.brewPotion(invQ);
					this.markDirty();
					this.markContainingBlockForUpdate(null);
				} else if( cb <= 0 ) {
					this.brewTime = 0;
					this.markDirty();
					this.markContainingBlockForUpdate(null);
				} else if ( !this.ingredient.isItemEqual(invQ.getStackInSlot(0)) ) {
					this.brewTime = 0;
					this.markDirty();
					this.markContainingBlockForUpdate(null);
				}
			} else if( cb > 0 && this.fuelRemaining > 0 ) {
				--this.fuelRemaining;
				this.brewTime = cb;
				this.maxBrewTime = cb;
				this.ingredient = invQ.getStackInSlot(0).copy();
				this.markDirty();
				this.markContainingBlockForUpdate(null);
			}
		}
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
    public void read(BlockState stateIn, CompoundNBT compound) {
        super.read(stateIn, compound);

        this.brewTime = compound.getInt("brew");
        this.maxBrewTime = compound.getInt("maxbrew");
        this.fuelRemaining = compound.getInt("fuel");
        this.invFuel.ifPresent(h -> h.deserializeNBT(compound.getCompound("invF")));
        this.invItems.ifPresent(h -> h.deserializeNBT(compound.getCompound("invQ")));
        this.invBottleIn.ifPresent(h -> h.deserializeNBT(compound.getCompound("invBIn")));
        this.invBottleOut.ifPresent(h -> h.deserializeNBT(compound.getCompound("invBOut")));
        this.tank.ifPresent(h -> {
        	h.readFromNBT(compound.getCompound("tank"));
        	
        	FluidStack fluid = h.getFluid();
        	if( fluid.getFluid() == Registry.POTION_FLUID.get() && fluid.hasTag() )
        	{
        		if(fluid.getTag().contains("CustomPotionName"))
        			this.newPotionName = fluid.getTag().getString("CustomPotionName");
        	}
        });
    	updateFluidColor();
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound) {

        compound.putInt("brew", this.brewTime);
        compound.putInt("maxbrew", this.maxBrewTime);
        compound.putInt("fuel", this.fuelRemaining);
        
        invFuel.ifPresent(h ->  compound.put("invF", h.serializeNBT()));
        invItems.ifPresent(h ->  compound.put("invI", h.serializeNBT()));
        invBottleIn.ifPresent(h ->  compound.put("invBIn", h.serializeNBT()));
        invBottleOut.ifPresent(h ->  compound.put("invBOut", h.serializeNBT()));
        
        this.tank.ifPresent(h -> {
        	CompoundNBT tag = new CompoundNBT();
        	h.writeToNBT(tag);
        	compound.put("tank", tag);
        });
       
        return super.write(compound);
    }
	    
	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent("Brewing Station Tile");
	}
	
	@Nullable
	@Override
	public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
		assert world != null;
		return new BrewingStationContainer(this, this.brewingStationData, i, playerInventory,
				this.invFuel.orElse(new BrewingFuelItemStackHandler()),
				this.invItems.orElse(new BrewingQueueItemStackHandler()),
				this.invBottleIn.orElse(new BrewingBottleInItemStackHandler()),
				this.invBottleOut.orElse(new BrewingBottleOutItemStackHandler()));
	}
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			if( side == Direction.UP )
				return invItems.cast();
			
			else if( side == Direction.SOUTH)
				return invFuel.cast();
			
			else if( side == Direction.EAST || side == Direction.WEST )
				return invBottleIn.cast();
			
			else if( side == Direction.DOWN)
				return invBottleOut.cast();
		
			return LazyOptional.empty();
		}
		
		if( cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
			return this.tank.cast();
	
		return super.getCapability(cap, side);
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
	public void remove() {
		invFuel.invalidate();
		invItems.invalidate();
		invBottleIn.invalidate();
		invBottleOut.invalidate();
		super.remove();
	}
	
	private void updateFluidColor()
	{
		this.fluidColor = 3694022;	// Water
		
		this.tank.ifPresent(h -> {
			if( h.getFluidAmount() > 0 )
			{
				CompoundNBT tag = h.getFluid().getTag();
				List<EffectInstance> effects = PotionUtils.getEffectsFromTag(tag);
				this.fluidColor = PotionUtils.getPotionColorFromEffectList(effects);
			}
		});
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
				
				isBasePotion = root.contains("BasePotion") || root.contains("Potion");
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
				stack.setDisplayName(new TranslationTextComponent("text.concoctions.solution"));
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
		ItemStackHandler inv = invBottleOut.orElse(null);
		
		if( inv != null )
		{
			ItemStack stack = inv.getStackInSlot(0);
			renameItemName(stack);
		}
	}
	
	public void updatePotionName(String name)
	{
		if( this.hasWorld() )
		{
			this.newPotionName = name;
			this.renameItemName();
			
			this.tank.ifPresent(t -> {
				FluidStack fluid = t.getFluid();
				
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
				
			});
			
		}
	}
	
	public String getPotionName()
	{
		return this.newPotionName;
	}
}

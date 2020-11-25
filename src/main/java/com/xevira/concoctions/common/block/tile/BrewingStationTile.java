package com.xevira.concoctions.common.block.tile;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.container.BrewingStationContainer;
import com.xevira.concoctions.common.fluids.PotionFluid;
import com.xevira.concoctions.setup.BrewingRecipes;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class BrewingStationTile extends TileEntity implements ITickableTileEntity, ISidedInventory, INamedContainerProvider {
	public static final int INV_SLOTS = 4;
	public enum Slots {
		FUEL(0),
		ITEM(1),
		BOTTLE_IN(2),
		BOTTLE_OUT(3);
		
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
	private LazyOptional<ItemStackHandler> inventory  = LazyOptional.of(() -> new ItemStackHandler(BrewingStationTile.INV_SLOTS));
	
	private ItemStack ingredient = ItemStack.EMPTY;
	private int brewTime;
	private int maxBrewTime;
	private int fuelRemaining;
	
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
            return 5;
        }
    };
	
	public BrewingStationTile() {
		super(Registry.BREWING_STATION_TILE.get());
		
		this.tankStorage = new FluidTank(10 * FluidAttributes.BUCKET_VOLUME);
		this.tank = LazyOptional.of(() -> this.tankStorage);
		this.brewTime = 0;
		this.maxBrewTime = 0;

		// Make sure inventory has empty stacks
		clear();
	}

	@Override
	public int getSizeInventory() {
		return INV_SLOTS;
	}

	@Override
	public boolean isEmpty() {
		ItemStackHandler h = inventory.orElse(null);
		
		if(h != null ) 
		{
			for(int i = h.getSlots() - 1; i >= 0; i--)
			{
				ItemStack stack = h.getStackInSlot(i);
				if( stack != null && !stack.isEmpty())
					return false;
			}
		}
		
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		ItemStackHandler h = inventory.orElse(null);
		
		return (h != null && index >= 0 && index < h.getSlots()) ? h.getStackInSlot(index) : null;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStackHandler h = inventory.orElse(null);
		
		if( h == null || index < 0 || index >= h.getSlots())
			return ItemStack.EMPTY;
		
		ItemStack stack = h.getStackInSlot(index);
		if( stack == null || stack.isEmpty())
			return ItemStack.EMPTY;
		
		return stack.split(count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStackHandler h = inventory.orElse(null);
		
		if( h == null || index < 0 || index >= h.getSlots())
			return ItemStack.EMPTY;
		
		ItemStack stack = h.getStackInSlot(index);
		if( stack == null || stack.isEmpty())
			return ItemStack.EMPTY;

		h.setStackInSlot(index, ItemStack.EMPTY);
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		ItemStackHandler h = inventory.orElse(null);
		
		if( h == null || index < 0 || index >= h.getSlots())
			return;

		h.setStackInSlot(index, stack);
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return true;
	}

	@Override
	public void clear() {
		ItemStackHandler h = inventory.orElse(null);
		
		if( h == null )
			return;

		for(int i = h.getSlots() - 1; i >= 0; i--)
			h.setStackInSlot(i, ItemStack.EMPTY);
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		// TODO Add hopper support
		return null;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
		// TODO: Add hopper support
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
		// TODO: Add hopper support
		return false;
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
		

		fluidStack.setTag(tag);
		return fluidStack;

	}
	
	/*
	private FluidStack _getPotionFluidFromItemStack(ItemStack stack)
	{
		FluidStack fluidStack = new FluidStack(Registry.POTION_FLUID.get(), FluidAttributes.BUCKET_VOLUME);
		
		if(!stack.hasTag()) return fluidStack;
		
		CompoundNBT root = stack.getTag();
		
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
				tag.putInt("CustomPotionColor", root.getInt("CustomPotionColor"));
		}
		

		fluidStack.setTag(tag);
		return fluidStack;
	}
	*/
	
	private void addPotionEffectsToItemStack(FluidStack inFluid, ItemStack outStack)
	{
		if( outStack.getItem() != Items.POTION && outStack.getItem() != Items.SPLASH_POTION && outStack.getItem() != Items.LINGERING_POTION )
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
			outStack.setDisplayName(new TranslationTextComponent("text.concoctions.solution"));

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
				inv.setStackInSlot(Slots.BOTTLE_OUT.id, outStack);
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
		
		if( !tankFluid.isEmpty() && !inFluid.isEmpty() && !tankFluid.isFluidEqual(inFluid))
			return null;	// Not the same fluid
		
		FluidStack result = this.tankStorage.drain(FluidAttributes.BUCKET_VOLUME, FluidAction.SIMULATE);
		if( result.isEmpty() || result.getAmount() < FluidAttributes.BUCKET_VOLUME )
			return null;	// Not enough room
		
		addPotionEffectsToItemStack(result, resultStack);

		// Check if result and current output stack are compatible
		if( !outStack.isEmpty() && !ItemStack.areItemStacksEqual(outStack, resultStack))
			return null;	// Not the same result
		
		// Check if the output pile can handle more items
		int newSize = outStack.getCount() + 1; 
		if( newSize > resultStack.getMaxStackSize() )
				return null;
		
		// Can they even stack properly?
		if( !outStack.isEmpty() && (!outStack.isStackable() || !resultStack.isStackable()))
			return null;
		
		this.tankStorage.drain(FluidAttributes.BUCKET_VOLUME, FluidAction.EXECUTE);

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
		
		if( item == Items.GLASS_BOTTLE ) {
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
				inv.setStackInSlot(Slots.BOTTLE_OUT.id, outStack);
				this.markDirty();
				this.markContainingBlockForUpdate(null);

				return;
			}
		}
	}
	
	private int canBrew(ItemStackHandler inv)
	{
		ItemStack stack = inv.getStackInSlot(Slots.ITEM.id);
		FluidStack fluid = this.tankStorage.getFluid();
		
		if( stack.isEmpty() || fluid.isEmpty() ) return 0;
		
		
		return BrewingRecipes.canBrew(stack, fluid);
	}
	
	private void brewPotion(ItemStackHandler inv)
	{
		ItemStack stack = inv.getStackInSlot(Slots.ITEM.id);
		FluidStack fluid = this.tankStorage.getFluid();
		
		FluidStack result = BrewingRecipes.getBrewingRecipe(stack, fluid);
		
		if( result != null )
		{
			result = getPotionFluidFromNBT(result.getTag());
			result.setAmount(fluid.getAmount());
			
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
		      
			inv.setStackInSlot(Slots.ITEM.id, stack);
			this.world.playEvent(1035, blockpos, 0);		// Play brewing sound
		}
		
	}
	
	@Override
	public void tick() {
		ItemStackHandler inv = inventory.orElse(null);
		// Check for Blaze Powder
		if( inv != null ) {
			ItemStack stack = inv.getStackInSlot(Slots.FUEL.id);
			if( this.fuelRemaining <= 0 && stack != null && !stack.isEmpty() && stack.getItem() == Items.BLAZE_POWDER) {
				this.fuelRemaining = 20;
				stack.shrink(1);
				this.markDirty();
			}
		}
		
		// Check for Fluid Inputs, only process fluids if nothing is being brewed
		if( inv != null && this.brewTime <= 0) {
			ItemStack inStack = inv.getStackInSlot(Slots.BOTTLE_IN.id);
			ItemStack outStack = inv.getStackInSlot(Slots.BOTTLE_OUT.id);
			
			if( inStack != null && !inStack.isEmpty()) {
				if( !tryFillTank(inv, inStack, outStack))
					tryEmptyTank(inv, inStack, outStack);
			}
		}
		
		// TODO: Attempt to brew
		if( inv != null )
		{
			int cb = canBrew(inv);				// Can Brew?
			boolean ib = this.brewTime > 0;			// Is Brewing?
			if( ib ) {
				--this.brewTime;
				boolean db = this.brewTime == 0;	// Done Brewing?
				if( cb > 0 && db ) {
					this.brewPotion(inv);
					this.markDirty();
					this.markContainingBlockForUpdate(null);
				} else if( cb <= 0 ) {
					this.brewTime = 0;
					this.markDirty();
					this.markContainingBlockForUpdate(null);
				} else if ( !this.ingredient.isItemEqual(inv.getStackInSlot(Slots.ITEM.id)) ) {
					this.brewTime = 0;
					this.markDirty();
					this.markContainingBlockForUpdate(null);
				}
			} else if( cb > 0 && this.fuelRemaining > 0 ) {
				--this.fuelRemaining;
				this.brewTime = cb;
				this.maxBrewTime = cb;
				this.ingredient = inv.getStackInSlot(Slots.ITEM.id).copy();
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
        this.inventory.ifPresent(h -> h.deserializeNBT(compound.getCompound("inv")));
        this.tank.ifPresent(h -> h.readFromNBT(compound.getCompound("tank")));
    	updateFluidColor();
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound) {

        compound.putInt("brew", this.brewTime);
        compound.putInt("maxbrew", this.maxBrewTime);
        compound.putInt("fuel", this.fuelRemaining);
        
        inventory.ifPresent(h ->  compound.put("inv", h.serializeNBT()));
        
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
		return new BrewingStationContainer(this, this.brewingStationData, i, playerInventory, this.inventory.orElse(new ItemStackHandler(BrewingStationTile.INV_SLOTS)));
	}
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
			return inventory.cast();
		
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
		inventory.invalidate();
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
}

package com.xevira.concoctions.common.block.tile;

import java.util.HashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.block.MixerBlock;
import com.xevira.concoctions.common.container.MixerContainer;
import com.xevira.concoctions.common.handlers.*;
import com.xevira.concoctions.common.network.PacketHandler;
import com.xevira.concoctions.common.network.packets.PacketMixerValveChanges;
import com.xevira.concoctions.common.utils.Utils;
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
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class MixerTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider
{
	public static final int NORTH_TANK = 0;
	public static final int SOUTH_TANK = 1;
	public static final int EAST_TANK = 2;
	public static final int WEST_TANK = 3;
	public static final int TOTAL_INPUTS = 4;
	public static final int CENTER_TANK = 4;
	public static final int TOTAL_TANKS = 5;
	
	public static final int TOTAL_DATA = TOTAL_INPUTS + 3;
	
	public static final int ERROR_NONE = 0;
	public static final int ERROR_TOO_MANY_EFFECTS = 1;
	
	private static final String NBT_FIELDS[] = new String[] {
		"north",
		"south",
		"east",
		"west",
		"center"
	};
	
	private static final HashMap<Direction, int[]> FACINGS = new HashMap<Direction, int[]>();
	
	static {
		FACINGS.put(Direction.NORTH, new int[] {NORTH_TANK, SOUTH_TANK, EAST_TANK, WEST_TANK, CENTER_TANK});
		FACINGS.put(Direction.SOUTH, new int[] {SOUTH_TANK, NORTH_TANK, WEST_TANK, EAST_TANK, CENTER_TANK});
		FACINGS.put(Direction.EAST,  new int[] {EAST_TANK, WEST_TANK, NORTH_TANK, SOUTH_TANK, CENTER_TANK});
		FACINGS.put(Direction.WEST,  new int[] {WEST_TANK, EAST_TANK, SOUTH_TANK, NORTH_TANK, CENTER_TANK});
	}
	
	private final MixerReservoir tanks[];
	private final LazyOptional<MixerOutputItemStackHandler> output;		// Outputs are shared across all tank I/O
	private final int valves[];
	private int mixingTime = 0;
	private FluidStack mixingTarget;
	private boolean centerValveOpen = false;
	private int mixingStatus = 0;
	
    public final IIntArray mixerData = new IIntArray() {
        @Override
        public int get(int index) {
            switch (index) {
                case NORTH_TANK:
                case SOUTH_TANK:
                case EAST_TANK:
                case WEST_TANK:
                	return valves[index];
                case TOTAL_INPUTS:
                	return MixerTile.this.mixingTime;
                case TOTAL_INPUTS+1:
                	return MixerTile.this.centerValveOpen ? 1 : 0;
                case TOTAL_INPUTS+2:
                	return MixerTile.this.mixingStatus;
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
            return TOTAL_DATA;
        }
    };

	
	public MixerTile()
	{
		super(Registry.MIXER_TILE.get());
		
		tanks = new MixerReservoir[TOTAL_TANKS];
		tanks[CENTER_TANK] = new MixerReservoir(10, true);
		tanks[NORTH_TANK] = new MixerReservoir(10, false);
		tanks[SOUTH_TANK] = new MixerReservoir(10, false);
		tanks[EAST_TANK] = new MixerReservoir(10, false);
		tanks[WEST_TANK] = new MixerReservoir(10, false);
		
		output = LazyOptional.of(() -> new MixerOutputItemStackHandler());
		
		valves = new int[TOTAL_INPUTS];
		valves[NORTH_TANK] = 0;
		valves[SOUTH_TANK] = 0;
		valves[EAST_TANK] = 0;
		valves[WEST_TANK] = 0;
		
		mixingTime = 0;
		mixingTarget = FluidStack.EMPTY;
		
		centerValveOpen = false;
	}

	@Override
	public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player)
	{
		assert world != null;
		return new MixerContainer(this, i, inventory);
	}
	
	public MixerReservoir[] getReservoirs()
	{
		return this.tanks;
	}
	
	public MixerOutputItemStackHandler getOutputItemHandler()
	{
		return this.output.orElse(new MixerOutputItemStackHandler());
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("tile.concoctions.mixer");
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
	
	private ItemStack fillInputTank(FluidTank tank, ItemStack inStack, ItemStack outStack, FluidStack inFluid, ItemStack resultStack)
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

	private boolean tryFillInputTank(int slot, FluidTank tank, ItemStack inStack, MixerOutputItemStackHandler output)
	{
		ItemStack outStack = output.getStackInSlot(slot);
		
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
			fluidStack = Utils.getPotionFluidFromNBT(inStack.getTag());
			outputStack = new ItemStack(Items.GLASS_BOTTLE, 1);
		}

		// Filling
		if( !fluidStack.isEmpty() && !outputStack.isEmpty()) {
			outStack = fillInputTank(tank, inStack, outStack, fluidStack, outputStack);
			
			if( outStack != null )
			{
				output.setStackInSlot(0, outStack);
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
		if( !outStack.isEmpty() && !areItemStacksEqual(outStack, resultStack))
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

	private void tryEmptyInputTank(int slot, FluidTank tank, ItemStack inStack, MixerOutputItemStackHandler output)
	{
		ItemStack outStack = output.getStackInSlot(slot);
		
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
		}
		
		if( !outputStack.isEmpty() ) {
			outStack = emptyInputTank(tank, inStack, outStack, fluidStack, outputStack);
			
			if( outStack != null ) {
				output.setStackInSlot(slot, outStack);
				this.markDirty();
				this.markContainingBlockForUpdate(null);

				return;
			}
		}
	}

	
	private void processInputTank(int tank)
	{
		if(tank < 0 || tank >= TOTAL_INPUTS)
			return;
		
		MixerReservoir reservoir = this.tanks[tank];
		
		if(!reservoir.tank.isPresent())
			return;
		
		if(!reservoir.input.isPresent())
			return;
		
		if(!this.output.isPresent())
			return;
		
		ItemStack inStack = reservoir.input.resolve().get().getStackInSlot(0);
		FluidTank inTank = reservoir.tank.resolve().get();
		MixerOutputItemStackHandler outHandler = this.output.resolve().get();
		
		if(!tryFillInputTank(tank, inTank, inStack, outHandler))
			tryEmptyInputTank(tank, inTank, inStack, outHandler);
	}
	
	@Override
	public void tick()
	{
		// TODO
		
		for(int i = 0; i < TOTAL_INPUTS; i++)
			processInputTank(i);
		
		if(this.centerValveOpen)
		{
			
			
		}
	}
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			if(side == Direction.DOWN)
				return output.cast();
			
			if(side == Direction.UP)
				return tanks[CENTER_TANK].input.cast();
			
			if(side == Direction.NORTH)
				return tanks[NORTH_TANK].input.cast();

			if(side == Direction.SOUTH)
				return tanks[SOUTH_TANK].input.cast();

			if(side == Direction.EAST)
				return tanks[EAST_TANK].input.cast();

			if(side == Direction.WEST)
				return tanks[WEST_TANK].input.cast();
		}
		else if( cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
		{
			if(side == Direction.DOWN)
				return tanks[CENTER_TANK].lazy.cast();
			
			if(side == Direction.NORTH)
				return tanks[NORTH_TANK].lazy.cast();

			if(side == Direction.SOUTH)
				return tanks[SOUTH_TANK].lazy.cast();

			if(side == Direction.EAST)
				return tanks[EAST_TANK].lazy.cast();

			if(side == Direction.WEST)
				return tanks[WEST_TANK].lazy.cast();

		}
		
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
    public void read(BlockState stateIn, CompoundNBT compound)
    {
        super.read(stateIn, compound);
        
        this.mixingTime = compound.getInt("time");
        this.mixingTarget = FluidStack.loadFluidStackFromNBT(compound.getCompound("target"));
        this.centerValveOpen = compound.getBoolean("open");
        
        this.output.ifPresent(o -> { o.deserializeNBT(compound.getCompound("output")); });

        for(int i = 0; i < TOTAL_TANKS; i++)
        	this.tanks[i].deserializeNBT(compound.getCompound(NBT_FIELDS[i]));

        for(int i = 0; i < TOTAL_INPUTS; i++)
        	valves[i] = compound.getInt("valve_" + NBT_FIELDS[i]);
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
    	compound.putInt("time", this.mixingTime);
    	compound.put("target", this.mixingTarget.writeToNBT(new CompoundNBT()));
    	compound.putBoolean("open", this.centerValveOpen);
    	
    	this.output.ifPresent(o -> { compound.put("output", o.serializeNBT()); });
    	
    	for(int i = 0; i < TOTAL_TANKS; i++)
    		compound.put(NBT_FIELDS[i], this.tanks[i].serializeNBT());

        for(int i = 0; i < TOTAL_INPUTS; i++)
        	compound.putInt("valve_" + NBT_FIELDS[i], valves[i]);

        return super.write(compound);
    }

	@Override
	public void remove() {
		for(MixerReservoir tank : this.tanks)
		{
			tank.tank.invalidate();
			tank.input.invalidate();
		}
		this.output.invalidate();
		super.remove();
	}
	
	public int[] getCapacities()
	{
		assert this.world != null;
		
		int[] caps = new int[TOTAL_TANKS];
		
		BlockState state = this.world.getBlockState(this.pos);
		int[] order = FACINGS.get(state.get(MixerBlock.FACING));
		
		for(int i = 0; i < TOTAL_TANKS; i++)
			caps[i] = this.tanks[order[i]].getCapacity();
		
		return caps;
	}
	
	public FluidStack[] getFluids()
	{
		assert this.world != null;
		
		FluidStack[] stacks = new FluidStack[TOTAL_TANKS];
		
		BlockState state = this.world.getBlockState(this.pos);
		int[] order = FACINGS.get(state.get(MixerBlock.FACING));
		
		for(int i = 0; i < TOTAL_TANKS; i++)
			stacks[i] = this.tanks[order[i]].getFluid();
		
		return stacks;
	}
	
	private void handlerDropItems(IItemHandler handler, World worldIn, BlockPos pos)
	{
		for (int i = 0; i < handler.getSlots(); i++)
			InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(),
					handler.getStackInSlot(i));
	}
	
	public void dropItems(World worldIn, BlockPos pos)
	{
		for(MixerReservoir tank : this.tanks)
		{
			tank.input.ifPresent(handler -> handlerDropItems(handler, worldIn, pos));
		}
	}
	
	public void setValve(int valve, int value)
	{
		Concoctions.GetLogger().info("[{}] setValve({},{})", this.world.isRemote?"CLIENT":"SERVER", valve, value);
		
		if(valve >= 0 && valve < TOTAL_INPUTS)
		{
			this.valves[valve] = MathHelper.clamp(value, 0, 100);
			this.markContainingBlockForUpdate(null);
			
			if(this.world.isRemote)
				PacketHandler.sendToServer(new PacketMixerValveChanges((byte)valve, (byte)this.valves[valve]));
		}
		else if( valve == CENTER_TANK)
		{
			this.centerValveOpen = value != 0;
			this.mixingTime = 0;
			this.markContainingBlockForUpdate(null);

			if(this.world.isRemote)
				PacketHandler.sendToServer(new PacketMixerValveChanges((byte)valve, (byte)(this.centerValveOpen ? 1 : 0)));
		}
	}

	
	public static class MixerReservoir implements IFluidHandler, IFluidTank
	{
		public final LazyOptional<FluidTank> tank;
		public final LazyOptional<ItemStackHandlerEx> input;
		private final boolean isOutput;
		public final LazyOptional<MixerReservoir> lazy;
		
		public MixerReservoir(int capacity, boolean isOutput)
		{
			this.tank = LazyOptional.of(() -> new FluidTank(capacity * FluidAttributes.BUCKET_VOLUME));
			if(isOutput)
			{
				this.input = LazyOptional.of(() -> new MixerEmptyingItemStackHandler());
			}
			else
			{
				this.input = LazyOptional.of(() -> new MixerFillingItemStackHandler());
			}
			this.isOutput = isOutput;
			this.lazy = LazyOptional.of(() -> this);
		}
		
		public void deserializeNBT(CompoundNBT nbt)
		{
			this.tank.ifPresent(t -> { t.readFromNBT(nbt.getCompound("tank")); });
			this.input.ifPresent(i -> { i.deserializeNBT(nbt.getCompound("inv"));});
		}
		
		public CompoundNBT serializeNBT()
		{
			CompoundNBT nbt = new CompoundNBT();
			
			this.tank.ifPresent(t -> { nbt.put("tank", t.writeToNBT(new CompoundNBT())); });
			this.input.ifPresent(i -> { nbt.put("inv", i.serializeNBT()); });
			
			return nbt;
		}

		@Override
		public FluidStack getFluid() {
			if(this.tank.isPresent())
				return this.tank.resolve().get().getFluid();
			
			return FluidStack.EMPTY;
		}

		@Override
		public int getFluidAmount() {
			if(this.tank.isPresent())
				return this.tank.resolve().get().getFluidAmount();

			return 0;
		}

		@Override
		public int getCapacity() {
			if(this.tank.isPresent())
				return this.tank.resolve().get().getCapacity();

			return 0;
		}

		@Override
		public boolean isFluidValid(FluidStack stack) {
			if(this.tank.isPresent())
				return this.tank.resolve().get().isFluidValid(stack);

			return false;
		}

		@Override
		public int getTanks() {
			if(this.tank.isPresent())
				return this.tank.resolve().get().getTanks();

			return 0;
		}

		@Override
		public FluidStack getFluidInTank(int tank) {
			if(this.tank.isPresent())
				return this.tank.resolve().get().getFluidInTank(tank);

			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank) {
			if(this.tank.isPresent())
				return this.tank.resolve().get().getTankCapacity(tank);

			return 0;
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			if(this.tank.isPresent())
				return this.tank.resolve().get().isFluidValid(tank, stack);

			return false;
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			if(!this.isOutput && this.tank.isPresent())
				return this.tank.resolve().get().fill(resource, action);

			return 0;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			if(this.isOutput && this.tank.isPresent())
				return this.tank.resolve().get().drain(resource, action);

			return FluidStack.EMPTY;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			if(this.isOutput && this.tank.isPresent())
				return this.tank.resolve().get().drain(maxDrain, action);

			return FluidStack.EMPTY;
		}
	}
}

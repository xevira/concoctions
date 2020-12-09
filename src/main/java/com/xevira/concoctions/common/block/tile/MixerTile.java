package com.xevira.concoctions.common.block.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.xevira.concoctions.common.handlers.*;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
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
	
	public static final int TOTAL_DATA = TOTAL_INPUTS + 1;
	
	private static final String NBT_FIELDS[] = new String[] {
		"north",
		"south",
		"east",
		"west",
		"center"
	};
	
	private final MixerReservoir tanks[];
	private final LazyOptional<ItemStackHandlerEx> output;		// Outputs are shared across all tank I/O
	private final int valves[];
	private int mixingTime = 0;
	private FluidStack mixingTarget;
	
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
	}

	@Override
	public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player)
	{
		assert world != null;
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("tile.concoctions.mixer");
	}
	
	@Override
	public void tick()
	{
		// TODO Auto-generated method stub
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

    @Override
    public void read(BlockState stateIn, CompoundNBT compound)
    {
        super.read(stateIn, compound);
        
        this.mixingTime = compound.getInt("time");
        this.mixingTarget = FluidStack.loadFluidStackFromNBT(compound.getCompound("target"));
        
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

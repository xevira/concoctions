package com.xevira.concoctions.common.fluids;

import java.util.function.Predicate;

import javax.annotation.Nonnull;

import com.xevira.concoctions.common.utils.Utils;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class TargetedFluidTank implements IFluidHandler, IFluidTank, IItemHandler, IItemHandlerModifiable, INBTSerializable<CompoundNBT> {
	
    @Nonnull
	private ItemStack target = ItemStack.EMPTY;

    @Nonnull
	private FluidStack targetFluid = FluidStack.EMPTY;

	protected Predicate<FluidStack> validator;
    @Nonnull
    protected FluidStack fluid = FluidStack.EMPTY;
    protected int capacity;
    
    public TargetedFluidTank(int capacity)
    {
    	this(capacity, e -> true);
    }

    public TargetedFluidTank(int capacity, Predicate<FluidStack> validator)
    {
        this.capacity = capacity;
        this.validator = validator;
    }

    public TargetedFluidTank setCapacity(int capacity)
    {
        this.capacity = capacity;
        return this;
    }

    public TargetedFluidTank setValidator(Predicate<FluidStack> validator)
    {
        if (validator != null) {
            this.validator = validator;
        }
        return this;
    }
    
    
	public int forceFill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(resource))
            return 0;

        if (action.simulate())
        {
            if (fluid.isEmpty())
            {
                return Math.min(capacity, resource.getAmount());
            }
            if (!fluid.isFluidEqual(resource))
            {
                return 0;
            }
            return Math.min(capacity - fluid.getAmount(), resource.getAmount());
        }
        if (fluid.isEmpty())
        {
            fluid = new FluidStack(resource, Math.min(capacity, resource.getAmount()));
            return fluid.getAmount();
        }
        if (!fluid.isFluidEqual(resource))
        {
            return 0;
        }
        int filled = capacity - fluid.getAmount();

        if (resource.getAmount() < filled)
        {
            fluid.grow(resource.getAmount());
            filled = resource.getAmount();
        }
        else
        {
            fluid.setAmount(capacity);
        }
        return filled;
	}
	
	public FluidStack forceDrain(FluidStack resource, FluidAction action)
	{
        if (resource.isEmpty() || !resource.isFluidEqual(fluid))
            return FluidStack.EMPTY;

        return forceDrain(resource.getAmount(), action);
	}


	public FluidStack forceDrain(int maxDrain, FluidAction action)
	{
        int drained = maxDrain;
        if (fluid.getAmount() < drained)
            drained = fluid.getAmount();

        FluidStack stack = new FluidStack(fluid, drained);
        if (action.execute() && drained > 0)
            fluid.shrink(drained);

        return stack;
	}
	
    protected void validateSlotIndex(int slot)
    {
        if (slot != 0)
            throw new RuntimeException("Slot " + slot + " not in valid range - [0,1)");
    }
	
    protected int getStackLimit(int slot, @Nonnull ItemStack stack)
    {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

	
	public boolean isTargeting()
	{
		return !this.targetFluid.isEmpty();
	}
	
	public boolean isFull()
	{
		return !this.fluid.isEmpty() && (this.fluid.getAmount() == this.capacity);
	}


	////////////////////////////////////////
	// IFluidHandler methods
	@Override
	public int getTanks() {
		return 1;
	}


	@Override
	public FluidStack getFluidInTank(int tank) {
		return this.fluid;
	}


	@Override
	public int getTankCapacity(int tank) {
		return getCapacity();
	}


	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return isFluidValid(stack);
	}


	@Override
	public int fill(FluidStack resource, FluidAction action)
	{
		if(this.targetFluid.isEmpty())
			return 0;
		
		return forceFill(resource, action);
	}
	
	@Override
	public FluidStack drain(FluidStack resource, FluidAction action)
	{
        if (resource.isEmpty() || !resource.isFluidEqual(fluid))
            return FluidStack.EMPTY;

        return drain(resource.getAmount(), action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action)
	{
		if(this.targetFluid.isEmpty() || !this.targetFluid.isFluidEqual(fluid))
			return FluidStack.EMPTY;

		return forceDrain(maxDrain, action);
	}


	////////////////////////////////////////
    // IFluidTank methods
	@Override
	@Nonnull
	public FluidStack getFluid() {
		
		return fluid;
	}


	@Override
	public int getFluidAmount() {
		return this.fluid.getAmount();
	}


	@Override
	public int getCapacity() {
		return this.capacity;
	}


	@Override
	public boolean isFluidValid(FluidStack stack)
	{
        return validator.test(stack);
	}


	////////////////////////////////////////
	// IItemHandler methods

	@Override
	public int getSlots()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return this.target;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
        if (stack.isEmpty())
            return ItemStack.EMPTY;
            
        if (!isItemValid(slot, stack))
            return stack;

        validateSlotIndex(slot);

        ItemStack existing = this.target;

        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty())
        {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate)
        {
            if (existing.isEmpty())
            {
            	setStackInSlot(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            }
            else
            {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()- limit) : ItemStack.EMPTY;

	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = this.target;

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract)
        {
            if (!simulate)
            {
                this.target = ItemStack.EMPTY;
                this.targetFluid = FluidStack.EMPTY;
                return existing;
            }
            else
            {
                return existing.copy();
            }
        }
        else
        {
            if (!simulate)
            {
                this.target = ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract);
            }

            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
	}

	@Override
	public int getSlotLimit(int slot) {
		return 1;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return stack.getItem() == Items.POTION || stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.LINGERING_POTION;
	}
	
	////////////////////////////////////////
	// IItemHandlerModifiable methods

	@Override
	public void setStackInSlot(int slot, ItemStack stack)
	{
		validateSlotIndex(slot);
		if(stack.isEmpty())
		{
			this.target = ItemStack.EMPTY;
			this.targetFluid = FluidStack.EMPTY;
		}
		else if(isItemValid(slot, stack))
		{
			this.target = stack.copy();
			this.targetFluid = Utils.getPotionFluidFromNBT(stack.getTag());
		}
		else
		{
			this.target = stack.copy();
			this.targetFluid = FluidStack.EMPTY;
		}
	}
	
	////////////////////////////////////////
 	// INBTSerializable<CompoundNBT> methods

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		
		nbt.put("fluid", this.fluid.writeToNBT(new CompoundNBT()));
		nbt.put("targetItem", this.target.write(new CompoundNBT()));
		nbt.put("targetFluid", this.targetFluid.writeToNBT(new CompoundNBT()));

		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("fluid"));
		this.target = ItemStack.read(nbt.getCompound("targetItem"));
		this.targetFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("targetFluid"));
	}
}

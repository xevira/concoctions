package com.xevira.concoctions.common.handlers;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public class InputItemStackHandler extends ItemStackHandlerEx
{
	public InputItemStackHandler(int slots)
	{
		super(slots);
	}
	
    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
    	return ItemStack.EMPTY;
    }


}

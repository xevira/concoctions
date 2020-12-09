package com.xevira.concoctions.common.handlers;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class MixerFillingItemStackHandler extends ItemStackHandlerEx
{
	public MixerFillingItemStackHandler()
	{
		super(1);
	}
	
	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack)
	{
		return !stack.isEmpty() && stack.getItem() == Items.POTION;
	}

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
    	return ItemStack.EMPTY;
    }

}

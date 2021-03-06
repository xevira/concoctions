package com.xevira.concoctions.common.handlers;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class SynthesizerFillingItemStackHandler extends ItemStackHandlerEx
{
	public SynthesizerFillingItemStackHandler() {
		super(1);
	}
	
	
	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack)
	{
		return !stack.isEmpty() &&
				(stack.getItem() == Items.POTION ||
				stack.getItem() == Items.GLASS_BOTTLE ||
	    		//stack.getItem() == Items.BUCKET ||
        		stack.getItem() == Items.SPONGE);
	}

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
    	return ItemStack.EMPTY;
    }

}

package com.xevira.concoctions.common.handlers;

import javax.annotation.Nonnull;

import com.xevira.concoctions.setup.Registry;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class SynthesizerEmptyingItemStackHandler extends ItemStackHandlerEx
{
	public SynthesizerEmptyingItemStackHandler() {
		super(1);
	}
	
	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack)
	{
		return !stack.isEmpty() && (
	    		stack.getItem() == Items.GLASS_BOTTLE ||
				stack.getItem() == Registry.SPLASH_BOTTLE.get() ||
				stack.getItem() == Registry.LINGERING_BOTTLE.get() ||
				stack.getItem() == Items.ARROW ||
	    		stack.getItem() == Items.POTION ||
	    		stack.getItem() == Items.BUCKET ||
        		stack.getItem() == Items.SPONGE);
	}

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
    	return ItemStack.EMPTY;
    }
}

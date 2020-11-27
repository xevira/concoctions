package com.xevira.concoctions.common.handlers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

public class ItemStackHandlerEx extends ItemStackHandler
{
    public ItemStackHandlerEx()
    {
        this(1);
    }

    public ItemStackHandlerEx(int size)
    {
        super(size);
    }

    public ItemStackHandlerEx(NonNullList<ItemStack> stacks)
    {
        super(stacks);
    }
	
    public ItemStack forceExtractItem(int slot, int amount, boolean simulate)
    {
    	return super.extractItem(slot, amount, simulate);
    }

}

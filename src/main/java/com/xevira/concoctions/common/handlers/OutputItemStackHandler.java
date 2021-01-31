package com.xevira.concoctions.common.handlers;

import javax.annotation.Nonnull;

import com.xevira.concoctions.common.block.tile.MixerTile;

import net.minecraft.item.ItemStack;

public class OutputItemStackHandler extends ItemStackHandlerEx
{
	public OutputItemStackHandler(int slots)
	{
		super(slots);
	}
	
	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack)
	{
		// OUTPUT only
		return false;
	}
}

package com.xevira.concoctions.common.handlers;

import javax.annotation.Nonnull;

import com.xevira.concoctions.common.block.tile.MixerTile;

import net.minecraft.item.ItemStack;

public class MixerOutputItemStackHandler extends ItemStackHandlerEx
{
	public MixerOutputItemStackHandler()
	{
		super(MixerTile.TOTAL_TANKS);
	}
	
	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack)
	{
		// OUTPUT only
		return false;
	}
}

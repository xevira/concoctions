package com.xevira.concoctions.common.handlers;

import javax.annotation.Nonnull;
import com.xevira.concoctions.common.block.tile.BrewingStationTile;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class BrewingBottleOutItemStackHandler extends ItemStackHandlerEx
{
	public BrewingBottleOutItemStackHandler()
	{
		super(BrewingStationTile.BOTTLE_OUT_SLOTS);
	}
	
	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack)
	{
		// OUTPUT only
		return false;
	}

}

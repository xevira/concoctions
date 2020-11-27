package com.xevira.concoctions.common.handlers;

import javax.annotation.Nonnull;
import com.xevira.concoctions.common.block.tile.BrewingStationTile;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.items.ItemStackHandler;

public class BrewingFuelItemStackHandler extends ItemStackHandlerEx
{
	public BrewingFuelItemStackHandler()
	{
		super(BrewingStationTile.FUEL_SLOTS);
	}
	
	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack)
	{
		return !stack.isEmpty() && (stack.getItem() == Items.BLAZE_POWDER);
	}
	
    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
    	return ItemStack.EMPTY;
    }
}

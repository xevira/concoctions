package com.xevira.concoctions.common.handlers;

import javax.annotation.Nonnull;
import com.xevira.concoctions.common.block.tile.BrewingStationTile;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.items.ItemStackHandler;

public class BrewingBottleInItemStackHandler extends ItemStackHandlerEx
{
	public BrewingBottleInItemStackHandler()
	{
		super(BrewingStationTile.BOTTLE_IN_SLOTS);
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
        		stack.getItem() == Items.LAVA_BUCKET ||
        		stack.getItem() == Items.WATER_BUCKET ||
        		stack.getItem() == Items.SPONGE);
	}
	
    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
    	return ItemStack.EMPTY;
    }

	

}

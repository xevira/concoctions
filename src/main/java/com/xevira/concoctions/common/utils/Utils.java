package com.xevira.concoctions.common.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class Utils
{
	public static boolean isPotionItemStack(ItemStack stack)
	{
		if( stack.isEmpty())
			return false;
		else
			return (stack.getItem() == Items.POTION ||
					stack.getItem() == Items.SPLASH_POTION ||
					stack.getItem() == Items.LINGERING_POTION ||
					stack.getItem() == Items.TIPPED_ARROW);
	}
}

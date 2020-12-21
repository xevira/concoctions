package com.xevira.concoctions.common.items;

import java.util.List;

import javax.annotation.Nullable;

import com.xevira.concoctions.setup.Registry;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IncenseItem extends Item {
	public IncenseItem() {
		super(new Item.Properties().group(ItemGroup.MISC));
	}
	
	/////////////////////////////////////////
	// Copied from TippedArrowItem
	
	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
	{
		if (this.isInGroup(group))
		{
			for(Potion potion : net.minecraft.util.registry.Registry.POTION)
			{
				if (!potion.getEffects().isEmpty())
				{
					items.add(PotionUtils.addPotionToItemStack(new ItemStack(this), potion));
				}
			}
		}
	}
	
	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		PotionUtils.addPotionTooltip(stack, tooltip, 0.125F);
	}

	/**
	 * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
	 * different names based on their damage or NBT.
	 */
	@Override
	public String getTranslationKey(ItemStack stack)
	{
		return PotionUtils.getPotionFromItem(stack).getNamePrefixed(this.getTranslationKey() + ".effect.");
	}
}

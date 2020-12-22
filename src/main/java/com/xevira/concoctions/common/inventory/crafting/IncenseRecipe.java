package com.xevira.concoctions.common.inventory.crafting;

import com.xevira.concoctions.common.utils.Utils;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class IncenseRecipe extends SpecialRecipe {

	public IncenseRecipe(ResourceLocation idIn) {
		super(idIn);
	}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn)
	{
		if (inv.getWidth() == 3 && inv.getHeight() == 3)
		{
			for(int i = 0; i < inv.getWidth(); ++i)
			{
				for(int j = 0; j < inv.getHeight(); ++j)
				{
					ItemStack itemstack = inv.getStackInSlot(i + j * inv.getWidth());
					if (itemstack.isEmpty())
					{
						return false;
					}
					
					Item item = itemstack.getItem();
					if (i == 1 && j == 1)
					{
						if (item != Items.LINGERING_POTION)
							return false;
					}
					else if (item != Items.TORCH)
					{
						return false;
					}
				}
			}
			return true;
		}

		return false;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv)
	{
		ItemStack itemstack = inv.getStackInSlot(1 + inv.getWidth());
		if (itemstack.getItem() != Items.LINGERING_POTION)
			return ItemStack.EMPTY;

		ItemStack itemstack1 = new ItemStack(Registry.INCENSE_ITEM.get(), 8);
		PotionUtils.addPotionToItemStack(itemstack1, PotionUtils.getPotionFromItem(itemstack));
		PotionUtils.appendEffects(itemstack1, PotionUtils.getFullEffectsFromItem(itemstack));
		
		CompoundNBT root = itemstack.getTag();
		CompoundNBT dest = itemstack1.getTag();
		if( root != null && dest != null )
		{
			if(root.contains("CustomPotionColor"))
				dest.putInt("CustomPotionColor", root.getInt("CustomPotionColor"));

			if(root.contains("CustomPotionName"))
				dest.putString("CustomPotionName", root.getString("CustomPotionName"));
		}
		
		Utils.renamePotionStack(itemstack1);
		
		return itemstack1;
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return width >= 2 && height >= 2;
	}

	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return Registry.INCENSE_RECIPE.get();
	}
}

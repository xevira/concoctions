package com.xevira.concoctions.setup;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.fluids.FluidStack;

public class ImbuingRecipes
{
	private static final ArrayList<ImbuingRecipe> RECIPES = new ArrayList<ImbuingRecipe>();
	
	public static void postInit()
	{
		RECIPES.add(new ImbuingRecipe(Items.LILY_OF_THE_VALLEY, Registry.LAMENTING_LILY_ITEM.get(), Effects.REGENERATION, 1, 3, true));
		RECIPES.add(new ImbuingRecipe(Items.POPPY, Registry.FIREBLOSSOM_ITEM.get(), Effects.FIRE_RESISTANCE, 0, 3, true));
	}
	
	public static ImbuingRecipe findImbuingRecipe(ItemStack item, FluidStack fluid, int level)
	{
		if(fluid.getFluid() == Registry.POTION_FLUID.get())
		{
			List<EffectInstance> effects = PotionUtils.getEffectsFromTag(fluid.getTag());
			
			for(ImbuingRecipe recipe : RECIPES)
			{
				if( recipe.match(item, effects, level))
					return recipe;
			}
		}
		return null;
	}
	
	public static class ImbuingRecipe
	{
		private final ItemStack ingredient;
		private final ItemStack result;
		private final Effect effect;
		private final int minAmp;
		private final int cost;
		private final boolean exclusive;
		
		public ImbuingRecipe(Item ingredient, Item result, Effect effect, int minAmp, int cost, boolean exclusive)
		{
			this(new ItemStack(ingredient), new ItemStack(result), effect, minAmp, cost, exclusive);
		}
		
		public ImbuingRecipe(Item ingredient, ItemStack result, Effect effect, int minAmp, int cost, boolean exclusive)
		{
			this(new ItemStack(ingredient), result, effect, minAmp, cost, exclusive);
		}
		
		public ImbuingRecipe(ItemStack ingredient, Item result, Effect effect, int minAmp, int cost, boolean exclusive)
		{
			this(ingredient, new ItemStack(result), effect, minAmp, cost, exclusive);
		}
		
		public ImbuingRecipe(ItemStack ingredient, ItemStack result, Effect effect, int minAmp, int cost, boolean exclusive)
		{
			this.ingredient = ingredient;
			this.result = result;
			this.effect = effect;
			this.minAmp = minAmp;
			this.cost = cost;
			this.exclusive = exclusive;
		}
		
		public ItemStack getIngredient()
		{
			return this.ingredient;
		}
		
		public ItemStack getResult()
		{
			return this.result;
		}
		
		public Effect getEffect()
		{
			return this.effect;
		}
		
		public float minAmplifier()
		{
			return this.minAmp;
		}
		
		public int getCost()
		{
			return this.cost;
		}
		
		public boolean isExclusive()
		{
			return this.exclusive;
		}
		
		public boolean match(ItemStack item, List<EffectInstance> effects, int level)
		{
			if(!item.isItemEqual(this.ingredient)) return false;
			
			boolean isValid = false;

			for(EffectInstance eff : effects)
			{
				if( eff.getPotion() == this.effect && eff.getAmplifier() >= this.minAmp && level >= this.cost )
				{
					isValid = true;
				}
				else if( this.exclusive )
					return false;
			}
			
			return isValid;
		}
	}
}

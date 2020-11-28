package com.xevira.concoctions.setup;

import net.minecraft.item.Item;
import net.minecraft.potion.Effect;

public class ImbuingRecipes
{

	public static class ImbuingRecipe
	{
		private final Item ingredient;
		private final Item result;
		private final Effect effect;
		private final float minAmp;
		private final int cost;
		private final boolean exclusive;
		
		public ImbuingRecipe(Item ingredient, Item result, Effect effect, float minAmp, int cost, boolean exclusive)
		{
			this.ingredient = ingredient;
			this.result = result;
			this.effect = effect;
			this.minAmp = minAmp;
			this.cost = cost;
			this.exclusive = exclusive;
		}
		
		public Item getIngredient()
		{
			return this.ingredient;
		}
		
		public Item getResult()
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
	}
}

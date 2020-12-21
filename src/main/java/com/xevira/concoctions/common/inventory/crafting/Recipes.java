package com.xevira.concoctions.common.inventory.crafting;

import java.util.function.Consumer;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.data.CustomRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;

public class Recipes extends RecipeProvider
{
	public Recipes(DataGenerator gen)
	{
		super(gen);
	}
	
	@Override
	protected void registerRecipes(Consumer<IFinishedRecipe> consumer)
	{
		CustomRecipeBuilder.customRecipe(Registry.INCENSE_RECIPE.get()).build(consumer, Concoctions.MOD_ID + ":incense");
	}

}

package com.xevira.concoctions.setup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.xevira.concoctions.Concoctions;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.InstantEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;

public class BrewingRecipes
{
	protected static final int DEFAULT_BREW_TIME = 400;
	protected static final int MIN_BREW_TIME = 20;
	
	private static final ArrayList<BrewingRecipe> RECIPES = new ArrayList<BrewingRecipe>();
	private static final ArrayList<EffectRecipe> EFFECT_RECIPES = new ArrayList<EffectRecipe>();
	private static final ArrayList<DyeRecipe> DYE_RECIPES = new ArrayList<DyeRecipe>();
	
	public static void postInit()
	{
		// EXPLICIT BREWING RECIPES:
		// ================================================================
		RECIPES.add(new BrewingRecipe(Items.GLISTERING_MELON_SLICE, Fluids.WATER, Potions.MUNDANE));
		RECIPES.add(new BrewingRecipe(Items.GHAST_TEAR, Fluids.WATER, Potions.MUNDANE));
		RECIPES.add(new BrewingRecipe(Items.RABBIT_FOOT, Fluids.WATER, Potions.MUNDANE));
		RECIPES.add(new BrewingRecipe(Items.BLAZE_POWDER, Fluids.WATER, Potions.MUNDANE));
		RECIPES.add(new BrewingRecipe(Items.SPIDER_EYE, Fluids.WATER, Potions.MUNDANE));
		RECIPES.add(new BrewingRecipe(Items.SUGAR, Fluids.WATER, Potions.MUNDANE));
		RECIPES.add(new BrewingRecipe(Items.MAGMA_CREAM, Fluids.WATER, Potions.MUNDANE));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Fluids.WATER, Potions.THICK));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Fluids.WATER, Potions.MUNDANE));
		RECIPES.add(new BrewingRecipe(Items.NETHER_WART, Fluids.WATER, Potions.AWKWARD));
		RECIPES.add(new BrewingRecipe(Items.GOLDEN_CARROT, Potions.AWKWARD, Potions.NIGHT_VISION));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.NIGHT_VISION, Potions.LONG_NIGHT_VISION));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Potions.NIGHT_VISION, Potions.INVISIBILITY));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Potions.LONG_NIGHT_VISION, Potions.LONG_INVISIBILITY));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.INVISIBILITY, Potions.LONG_INVISIBILITY));
		RECIPES.add(new BrewingRecipe(Items.MAGMA_CREAM, Potions.AWKWARD, Potions.FIRE_RESISTANCE));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.FIRE_RESISTANCE, Potions.LONG_FIRE_RESISTANCE));
		RECIPES.add(new BrewingRecipe(Items.RABBIT_FOOT, Potions.AWKWARD, Potions.LEAPING));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.LEAPING, Potions.LONG_LEAPING));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Potions.LEAPING, Potions.STRONG_LEAPING));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Potions.LEAPING, Potions.SLOWNESS));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Potions.LONG_LEAPING, Potions.LONG_SLOWNESS));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.SLOWNESS, Potions.LONG_SLOWNESS));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Potions.SLOWNESS, Potions.STRONG_SLOWNESS));
		RECIPES.add(new BrewingRecipe(Items.TURTLE_HELMET, Potions.AWKWARD, Potions.TURTLE_MASTER));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.TURTLE_MASTER, Potions.LONG_TURTLE_MASTER));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Potions.TURTLE_MASTER, Potions.STRONG_TURTLE_MASTER));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Potions.SWIFTNESS, Potions.SLOWNESS));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Potions.LONG_SWIFTNESS, Potions.LONG_SLOWNESS));
		RECIPES.add(new BrewingRecipe(Items.SUGAR, Potions.AWKWARD, Potions.SWIFTNESS));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.SWIFTNESS, Potions.LONG_SWIFTNESS));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Potions.SWIFTNESS, Potions.STRONG_SWIFTNESS));
		RECIPES.add(new BrewingRecipe(Items.PUFFERFISH, Potions.AWKWARD, Potions.WATER_BREATHING));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.WATER_BREATHING, Potions.LONG_WATER_BREATHING));
		RECIPES.add(new BrewingRecipe(Items.GLISTERING_MELON_SLICE, Potions.AWKWARD, Potions.HEALING));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Potions.HEALING, Potions.STRONG_HEALING));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Potions.HEALING, Potions.HARMING));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HEALING, Potions.STRONG_HARMING));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Potions.HARMING, Potions.STRONG_HARMING));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Potions.POISON, Potions.HARMING));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Potions.LONG_POISON, Potions.HARMING));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Potions.STRONG_POISON, Potions.STRONG_HARMING));
		RECIPES.add(new BrewingRecipe(Items.SPIDER_EYE, Potions.AWKWARD, Potions.POISON));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.POISON, Potions.LONG_POISON));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Potions.POISON, Potions.STRONG_POISON));
		RECIPES.add(new BrewingRecipe(Items.GHAST_TEAR, Potions.AWKWARD, Potions.REGENERATION));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.REGENERATION, Potions.LONG_REGENERATION));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Potions.REGENERATION, Potions.STRONG_REGENERATION));
		RECIPES.add(new BrewingRecipe(Items.BLAZE_POWDER, Potions.AWKWARD, Potions.STRENGTH));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.STRENGTH, Potions.LONG_STRENGTH));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Potions.STRENGTH, Potions.STRONG_STRENGTH));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Fluids.WATER, Potions.WEAKNESS));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.WEAKNESS, Potions.LONG_WEAKNESS));
		RECIPES.add(new BrewingRecipe(Items.PHANTOM_MEMBRANE, Potions.AWKWARD, Potions.SLOW_FALLING));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.SLOW_FALLING, Potions.LONG_SLOW_FALLING));
		
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Potions.LONG_LEAPING, generatePotionFluidStack(false, null, new EffectInstance(Effects.JUMP_BOOST, 4800, 1)) ));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.STRONG_LEAPING, generatePotionFluidStack(false, null, new EffectInstance(Effects.JUMP_BOOST, 4800, 1)) ));

		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Potions.LONG_REGENERATION, generatePotionFluidStack(false, null, new EffectInstance(Effects.REGENERATION, 900, 1)) ));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.STRONG_REGENERATION, generatePotionFluidStack(false, null, new EffectInstance(Effects.REGENERATION, 900, 1)) ));

		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Potions.LONG_POISON, generatePotionFluidStack(false, null, new EffectInstance(Effects.POISON, 864, 1)) ));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.STRONG_POISON, generatePotionFluidStack(false, null, new EffectInstance(Effects.POISON, 864, 1)) ));

		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Potions.LONG_SLOWNESS, generatePotionFluidStack(false, null, new EffectInstance(Effects.SLOWNESS, 1000, 3)) ));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.STRONG_SLOWNESS, generatePotionFluidStack(false, null, new EffectInstance(Effects.SLOWNESS, 1000, 3)) ));

		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Potions.LONG_TURTLE_MASTER, generatePotionFluidStack(false, null, new EffectInstance(Effects.SLOWNESS, 800, 5), new EffectInstance(Effects.RESISTANCE, 800, 3)) ));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Potions.STRONG_TURTLE_MASTER, generatePotionFluidStack(false, null, new EffectInstance(Effects.SLOWNESS, 800, 5), new EffectInstance(Effects.RESISTANCE, 800, 3)) ));
		
		// Unobtainable Potions
		//   Haste
		RECIPES.add(new BrewingRecipe(Items.GOLDEN_PICKAXE, Potions.AWKWARD, Registry.HASTE_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.HASTE_POTION.get(), Registry.LONG_HASTE_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.STRONG_HASTE_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.HASTE, 4800, 1))));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.HASTE_POTION.get(), Registry.STRONG_HASTE_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.LONG_HASTE_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.HASTE, 4800, 1))));
		
		//   Dullness - Mining Fatigue
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Registry.HASTE_POTION.get(), Registry.DULLNESS_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Registry.LONG_HASTE_POTION.get(), Registry.LONG_DULLNESS_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Registry.STRONG_HASTE_POTION.get(), Registry.STRONG_DULLNESS_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.DULLNESS_POTION.get(), Registry.LONG_DULLNESS_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.STRONG_DULLNESS_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.MINING_FATIGUE, 4800, 1))));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.DULLNESS_POTION.get(), Registry.STRONG_DULLNESS_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.LONG_DULLNESS_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.MINING_FATIGUE, 4800, 1))));

		//   Confusion - Blindness
		RECIPES.add(new BrewingRecipe(Items.INK_SAC, Potions.NIGHT_VISION, Registry.CONFUSION_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.CONFUSION_POTION.get(), Registry.LONG_CONFUSION_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.INK_SAC, Potions.LONG_NIGHT_VISION, Registry.LONG_CONFUSION_POTION.get()));

		//   Hunger
		RECIPES.add(new BrewingRecipe(Items.ROTTEN_FLESH, Potions.WEAKNESS, Registry.HUNGER_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.HUNGER_POTION.get(), Registry.LONG_HUNGER_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.STRONG_HUNGER_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.HUNGER, 800, 1))));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.HUNGER_POTION.get(), Registry.STRONG_HUNGER_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.LONG_HUNGER_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.HUNGER, 800, 1))));
		RECIPES.add(new BrewingRecipe(Items.ROTTEN_FLESH, Potions.LONG_WEAKNESS, Registry.LONG_HUNGER_POTION.get()));
		
		//   Decay - Wither
		RECIPES.add(new BrewingRecipe(Items.WITHER_ROSE, Potions.AWKWARD, Registry.DECAY_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.DECAY_POTION.get(), Registry.LONG_DECAY_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.STRONG_DECAY_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.WITHER, 800, 1))));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.DECAY_POTION.get(), Registry.STRONG_DECAY_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.LONG_DECAY_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.WITHER, 800, 1))));
		
		//   Resistance
		RECIPES.add(new BrewingRecipe(Items.SHIELD, Potions.AWKWARD, Registry.RESISTANCE_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.RESISTANCE_POTION.get(), Registry.LONG_RESISTANCE_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.STRONG_RESISTANCE_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.RESISTANCE, 2400, 1))));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.RESISTANCE_POTION.get(), Registry.STRONG_RESISTANCE_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.LONG_RESISTANCE_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.RESISTANCE, 2400, 1))));

		//   Notch - Absorption/Saturation
		RECIPES.add(new BrewingRecipe(Items.SHIELD, Potions.AWKWARD, Registry.NOTCH_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.NOTCH_POTION.get(), Registry.LONG_NOTCH_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.STRONG_NOTCH_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.ABSORPTION, 4800, 1), new EffectInstance(Effects.SATURATION, 4800))));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.NOTCH_POTION.get(), Registry.STRONG_NOTCH_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.LONG_NOTCH_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.ABSORPTION, 4800, 1), new EffectInstance(Effects.SATURATION, 4800))));
		
		//   Levitation
		RECIPES.add(new BrewingRecipe(Items.CHORUS_FRUIT, Potions.AWKWARD, Registry.LEVITATION_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.LEVITATION_POTION.get(), Registry.LONG_LEVITATION_POTION.get()));
		
		//   Nausea
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Registry.LEVITATION_POTION.get(), Registry.NAUSEA_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Registry.LONG_LEVITATION_POTION.get(), Registry.LONG_NAUSEA_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.NAUSEA_POTION.get(), Registry.LONG_NAUSEA_POTION.get()));
		
		//   Glowing
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE, Potions.AWKWARD, Registry.GLOWING_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.GLOWING_POTION.get(), Registry.LONG_GLOWING_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Registry.GLOWING_POTION.get(), Potions.INVISIBILITY));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Registry.LONG_GLOWING_POTION.get(), Potions.LONG_INVISIBILITY));
		
		//   Luck
		RECIPES.add(new BrewingRecipe(Items.LAPIS_BLOCK, Potions.AWKWARD, Registry.LUCK_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.LUCK_POTION.get(), Registry.LONG_LUCK_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.STRONG_LUCK_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.LUCK, 4800, 1))));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.LUCK_POTION.get(), Registry.STRONG_LUCK_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.LONG_LUCK_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.LUCK, 4800, 1))));

		//   Bad Luck
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Registry.LUCK_POTION.get(), Registry.BADLUCK_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Registry.LONG_LUCK_POTION.get(), Registry.LONG_BADLUCK_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.FERMENTED_SPIDER_EYE, Registry.STRONG_LUCK_POTION.get(), Registry.STRONG_BADLUCK_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.BADLUCK_POTION.get(), Registry.LONG_BADLUCK_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.STRONG_BADLUCK_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.UNLUCK, 4800, 1))));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.BADLUCK_POTION.get(), Registry.STRONG_BADLUCK_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.LONG_BADLUCK_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Effects.UNLUCK, 4800, 1))));

		//   Neptune - Conduit Power
		RECIPES.add(new BrewingRecipe(Items.NAUTILUS_SHELL, Potions.AWKWARD, Registry.NEPTUNE_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.NEPTUNE_POTION.get(), Registry.LONG_NEPTUNE_POTION.get()));

		//   Grace - Dolphin's Grace
		RECIPES.add(new BrewingRecipe(Registry.DOLPHIN_FIN.get(), Potions.AWKWARD, Registry.GRACE_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.GRACE_POTION.get(), Registry.LONG_GRACE_POTION.get()));

		// Custom Potions
		//   Lead Foot
		RECIPES.add(new BrewingRecipe(Items.IRON_BOOTS, Potions.AWKWARD, Registry.LEADFOOT_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.LEADFOOT_POTION.get(), Registry.LONG_LEADFOOT_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.LEADFOOT_POTION.get(), Registry.STRONG_LEADFOOT_POTION.get()));
		RECIPES.add(new BrewingRecipe(Items.REDSTONE, Registry.STRONG_LEADFOOT_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Registry.LEAD_FOOT.get(),800,1))));
		RECIPES.add(new BrewingRecipe(Items.GLOWSTONE_DUST, Registry.LONG_LEADFOOT_POTION.get(), generatePotionFluidStack(false, null, new EffectInstance(Registry.LEAD_FOOT.get(),800,1))));
		
		// ================================================================
	
		// EFFECT RECIPES:
		// ================================================================
		// Redstone Dust: extends up to 5 minutes
		EFFECT_RECIPES.add(new EffectRecipe(Items.REDSTONE, null, null, 0, 6000, 1.5f, 0.0f, 0, -1, 1.0f, 0.0f, EffectVisibilityEnum.KEEP));
		
		// Block of Redstone: extends from 5 minutes up to 10 minutes
		EFFECT_RECIPES.add(new EffectRecipe(Items.REDSTONE_BLOCK, null, null, 6000, 12000, 1.5f, 0.0f, 0, -1, 1.0f, 0.0f, EffectVisibilityEnum.KEEP).setBrewTime(2*DEFAULT_BREW_TIME));
		
		// Glowstone Dust: amplifies by one level up to level 3
		EFFECT_RECIPES.add(new EffectRecipe(Items.GLOWSTONE_DUST, null, null, 200, -1, 0.5f, 0.0f, 1, 2, 1.0f, 1.0f, EffectVisibilityEnum.KEEP));

		// Glowstone: amplifies by one level from level 3 up to level 5
		EFFECT_RECIPES.add(new EffectRecipe(Items.GLOWSTONE, null, null, 100, -1, 0.45f, 0.0f, 2, 4, 1.0f, 1.0f, EffectVisibilityEnum.KEEP).setBrewTime(2*DEFAULT_BREW_TIME));

		// ================================================================
		
		// DYE RECIPES:
		// ================================================================
		// Normal Colors (Blending)
		DYE_RECIPES.add(new DyeRecipe(Items.WHITE_DYE, DyeColor.WHITE).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.ORANGE_DYE, DyeColor.ORANGE).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.MAGENTA_DYE, DyeColor.MAGENTA).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.LIGHT_BLUE_DYE, DyeColor.LIGHT_BLUE).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.YELLOW_DYE, DyeColor.YELLOW).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.LIME_DYE, DyeColor.LIME).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.PINK_DYE, DyeColor.PINK).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.GRAY_DYE, DyeColor.GRAY).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.LIGHT_GRAY_DYE, DyeColor.LIGHT_GRAY).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.CYAN_DYE, DyeColor.CYAN).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.PURPLE_DYE, DyeColor.PURPLE).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.BLUE_DYE, DyeColor.BLUE).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.BROWN_DYE, DyeColor.BROWN).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.GREEN_DYE, DyeColor.GREEN).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.RED_DYE, DyeColor.RED).setBrewTime(100));
		DYE_RECIPES.add(new DyeRecipe(Items.BLACK_DYE, DyeColor.BLACK).setBrewTime(100));
		// Glimmering Colors (Force Reset)
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_WHITE_DYE.get(), DyeColor.WHITE, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_ORANGE_DYE.get(), DyeColor.ORANGE, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_MAGENTA_DYE.get(), DyeColor.MAGENTA, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_LIGHT_BLUE_DYE.get(), DyeColor.LIGHT_BLUE, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_YELLOW_DYE.get(), DyeColor.YELLOW, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_LIME_DYE.get(), DyeColor.LIME, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_PINK_DYE.get(), DyeColor.PINK, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_GRAY_DYE.get(), DyeColor.GRAY, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_LIGHT_GRAY_DYE.get(), DyeColor.LIGHT_GRAY, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_CYAN_DYE.get(), DyeColor.CYAN, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_PURPLE_DYE.get(), DyeColor.PURPLE, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_BLUE_DYE.get(), DyeColor.BLUE, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_BROWN_DYE.get(), DyeColor.BROWN, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_GREEN_DYE.get(), DyeColor.GREEN, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_RED_DYE.get(), DyeColor.RED, true).setBrewTime(200));
		DYE_RECIPES.add(new DyeRecipe(Registry.GLIMMERING_BLACK_DYE.get(), DyeColor.BLACK, true).setBrewTime(200));
		// ================================================================
	}
	
	public static FluidStack generatePotionFluidStack(boolean isBase, Potion basePotion, EffectInstance... effectsIn) {
		return generatePotionFluidStack(isBase, basePotion, Arrays.asList(effectsIn));
	}
	
	public static FluidStack generatePotionFluidStack(boolean isBase, Potion basePotion, List<EffectInstance> effectsIn){
		FluidStack fluid = new FluidStack(Registry.POTION_FLUID.get(), 1);
		
		CompoundNBT root = fluid.getOrCreateTag();
		
		if( basePotion != null )
		{
			root.putString(isBase ? "BasePotion" : "Potion", basePotion.getRegistryName().toString());
		}
		
		if( effectsIn.size() > 0 )
		{
			ListNBT listEffects = new ListNBT();
			
			for(EffectInstance effect : effectsIn)
			{
				CompoundNBT tag = new CompoundNBT();
				effect.write(tag);
				listEffects.add(tag);
			}

			root.put("CustomPotionEffects", listEffects);
		}
		
		return fluid;
	}
	
	public static FluidStack newBasePotionFluidStack(Potion potion)
	{
		return generatePotionFluidStack(true, potion);
	}
	
	public static FluidStack newPotionFluidStack(Potion potion)
	{
		return generatePotionFluidStack(false, potion);
	}
	
	public static boolean canExtendEffect(EffectInstance effect)
	{
		if( effect.getDuration() <= 1) return false;		// Effects that brief cannot be extended 
		return !(effect.getPotion() instanceof InstantEffect);
	}
	
	public static boolean isPotionFluidEqual(FluidStack a, FluidStack b)
	{
		if( a.isEmpty() || b.isEmpty() ) return false;
		if( a.getFluid() != b.getFluid() ) return false;
		
		if( a.hasTag() != b.hasTag() ) return false;
		
		CompoundNBT tagA = a.getTag();
		CompoundNBT tagB = b.getTag();
		
		if( tagA != null && tagB != null )
		{
			if( a.getFluid() == Registry.POTION_FLUID.get() )
			{
				if( tagA.contains("BasePotion") && tagB.contains("BasePotion") )
				{
					return tagA.getString("BasePotion").compareTo(tagB.getString("BasePotion")) == 0;
				}
			}
		}
		else if( tagA == null && tagB == null )
			return true;
		
		return false;
	}
	
	private static FluidStack getEffectRecipe(ItemStack item, FluidStack baseFluid)
	{
		boolean found = false;

		// Only affect potion fluids (any other base fluid must have an actual recipe involved)
		if( baseFluid.getFluid() != Registry.POTION_FLUID.get() ) return null;

		if( !baseFluid.hasTag() ) return null;
		
		CompoundNBT root = baseFluid.getTag();
		
		List<EffectInstance> baseEffects = new ArrayList<EffectInstance>();;
		if( root.contains("BasePotion") )
		{
			String potionName = root.getString("BasePotion");
			root.putString("Potion", potionName);
			
			baseEffects.addAll(PotionUtils.getPotionTypeFromNBT(root).getEffects());
		}
		else
		{
			PotionUtils.addCustomPotionEffectToList(root, baseEffects);
		}
		
		ArrayList<EffectInstance> newEffects = new ArrayList<EffectInstance>();

		for(EffectInstance baseEffect : baseEffects)
		{
			EffectInstance newEffect = null;
			for(EffectRecipe recipe : EFFECT_RECIPES)
			{
				if( recipe.matches(item, baseEffect) ) {
					newEffect = recipe.generate(baseEffect);
					found = true;
					break;
				}
			}
			
			if( newEffect == null )
				newEffect = new EffectInstance(baseEffect);
			
			newEffects.add(newEffect);
		}
		
		if( root.contains("Potion") )
			root.remove("Potion");

		if( found )
		{
			CompoundNBT newRoot = new CompoundNBT();
			
			if( root.contains("DyedPotion") )
				newRoot.put("DyedPotion", root.get("DyedPotion"));
			
			if( root.contains("CustomPotionColor"))
				newRoot.put("CustomPotionColor", root.get("CustomPotionColor"));
			
			if( newEffects.size() > 0 )
			{
				ListNBT listNewEffects = new ListNBT();
				
				for(EffectInstance newEffect : newEffects)
				{
					CompoundNBT tag = new CompoundNBT();
					newEffect.write(tag);
					listNewEffects.add(tag);
				}
				
				newRoot.put("CustomPotionEffects", listNewEffects);
			}
			
			return new FluidStack(Registry.POTION_FLUID.get(), 1, newRoot);
		}
		
		return null;
	}
	
	private static int canBrewEffect(ItemStack item, FluidStack baseFluid)
	{
		int brewTime = 0;

		// Only affect potion fluids (any other base fluid must have an actual recipe involved)
		if( baseFluid.getFluid() != Registry.POTION_FLUID.get() ) return 0;

		if( !baseFluid.hasTag() ) return 0;
		
		CompoundNBT root = baseFluid.getTag();
		
		List<EffectInstance> baseEffects = new ArrayList<EffectInstance>();;
		if( root.contains("BasePotion") )
		{
			String potionName = root.getString("BasePotion");
			root.putString("Potion", potionName);
			
			baseEffects.addAll(PotionUtils.getPotionTypeFromNBT(root).getEffects());
		}
		else
		{
			PotionUtils.addCustomPotionEffectToList(root, baseEffects);
		}
		
		ArrayList<EffectInstance> newEffects = new ArrayList<EffectInstance>();

		for(EffectInstance baseEffect : baseEffects)
		{
			int bt = 0;
			
			for(EffectRecipe recipe : EFFECT_RECIPES)
			{
				if( recipe.matches(item, baseEffect) ) {
					bt = recipe.getBrewTime();
					break;
				}
			}
			
			if( bt > 0 )
				brewTime = Math.max(brewTime, bt);
		}
		
		if( root.contains("Potion") )
			root.remove("Potion");

		return brewTime;
	}
	
	private static FluidStack getDyeRecipe(ItemStack item, FluidStack baseFluid)
	{
		boolean found = false;
		DyeColor color = null;
		boolean force = false;

		// Only affect potion fluids (any other base fluid must have an actual recipe involved)
		if( baseFluid.getFluid() != Registry.POTION_FLUID.get() ) return null;

		if( !baseFluid.hasTag() ) return null;
		CompoundNBT root = baseFluid.getTag();
	
		for(DyeRecipe recipe : DYE_RECIPES)
		{
			if( recipe.matches(item)) {
				color = recipe.color;
				force = recipe.force;
				found = true;
				break;
			}
		}

		if( found )
		{
			CompoundNBT newRoot = root.copy();
			
			Potion basePotion = Potions.EMPTY;
			
			if( newRoot.contains("BasePotion"))
			{
				String baseName = newRoot.getString("BasePotion");
				newRoot.putString("Potion", baseName);
				newRoot.remove("BasePotion");
				
				basePotion = Potion.getPotionTypeForName(baseName);
				
				if( newRoot.contains("CustomPotionEffects"))
					newRoot.remove("CustomPotionEffects");
			} else {
				if( newRoot.contains("Potion"))
					newRoot.remove("Potion");
			}
			
			int newColor = color.getColorValue();
			
			if( !force ) {
			
				int oldColor;
				if( newRoot.contains("CustomPotionColor"))
				{
					oldColor = newRoot.getInt("CustomPotionColor");
				}
				else
				{
					oldColor = PotionUtils.getPotionColor(basePotion);
				}
				
				float r = ((oldColor      ) & 255) / 255.0f + ((newColor      ) & 255) / 255.0f;
				float g = ((oldColor >>  8) & 255) / 255.0f + ((newColor >>  8) & 255) / 255.0f;
				float b = ((oldColor >> 16) & 255) / 255.0f + ((newColor >> 16) & 255) / 255.0f;
				
				r = ((255.0f * r / 2.0f));
				g = ((255.0f * g / 2.0f));
				b = ((255.0f * b / 2.0f));
				
				newColor = (int)b << 16 | (int)g << 8 | (int)r; 
			}
			
			newRoot.putInt("CustomPotionColor", newColor);
			newRoot.putBoolean("DyedPotion", true);
			
			return new FluidStack(Registry.POTION_FLUID.get(), 1, newRoot);
		}

		return null;
	}

	private static int canBrewDye(ItemStack item, FluidStack baseFluid)
	{
		// Only affect potion fluids (any other base fluid must have an actual recipe involved)
		if( baseFluid.getFluid() != Registry.POTION_FLUID.get() ) return 0;

		if( !baseFluid.hasTag() ) return 0;
		CompoundNBT root = baseFluid.getTag();
	
		for(DyeRecipe recipe : DYE_RECIPES)
		{
			if( recipe.matches(item)) {
				return recipe.getBrewTime();
			}
		}

		return 0;
	}

	public static FluidStack getBrewingRecipe(ItemStack item, FluidStack baseFluid)
	{
		if( item.isEmpty() || baseFluid.isEmpty() )
			return null;
		
		for(BrewingRecipe recipe : RECIPES)
		{
			//Concoctions.GetLogger().info("Brewing: {} {} {}", item.getItem().getRegistryName().toString(), recipe.BaseFluid.getOrCreateTag().getString("BasePotion"), recipe.ResultFluid.getOrCreateTag().getString("BasePotion"));
			
			if( recipe.matches(item, baseFluid) )
				return recipe.generate(baseFluid);
		}
		
		FluidStack resultFluid = getEffectRecipe(item, baseFluid);
		if( resultFluid != null )
			return resultFluid;
		
		resultFluid = getDyeRecipe(item, baseFluid);
		if( resultFluid != null )
			return resultFluid;
		
		return null;
	}
	
	public static int canBrew(ItemStack item, FluidStack baseFluid)
	{
		if( item.isEmpty() || baseFluid.isEmpty() )
			return 0;
		
		for(BrewingRecipe recipe : RECIPES)
		{
			//Concoctions.GetLogger().info("Brewing: {} {} {}", item.getItem().getRegistryName().toString(), recipe.BaseFluid.getOrCreateTag().getString("BasePotion"), recipe.ResultFluid.getOrCreateTag().getString("BasePotion"));
			
			if( recipe.matches(item, baseFluid) )
				return recipe.getBrewTime();
		}
		
		int brewTime = canBrewEffect(item, baseFluid);
		if( brewTime <= 0 )
		{
			brewTime = canBrewDye(item, baseFluid);
		}
		
		return brewTime;
	}
	
	
	public static class BrewingRecipe {
		public final ItemStack Ingredient;
		public final FluidStack BaseFluid;
		public final FluidStack ResultFluid;
		private int brewTime;

		public BrewingRecipe(Item ingredient, Potion basePotion, Potion resultPotion)
		{
			this(new ItemStack(ingredient, 1), basePotion, resultPotion);
		}

		public BrewingRecipe(ItemStack ingredient, Potion basePotion, Potion resultPotion)
		{
			this(ingredient, BrewingRecipes.newBasePotionFluidStack(basePotion), BrewingRecipes.newPotionFluidStack(resultPotion));
		}

		public BrewingRecipe(Item ingredient, Potion basePotion, Fluid resultFluid)
		{
			this(new ItemStack(ingredient, 1), basePotion, new FluidStack(resultFluid, 1));
		}

		public BrewingRecipe(ItemStack ingredient, Potion basePotion, Fluid resultFluid)
		{
			this(ingredient, basePotion, new FluidStack(resultFluid, 1));
		}

		public BrewingRecipe(Item ingredient, Potion basePotion, FluidStack resultFluid)
		{
			this(new ItemStack(ingredient, 1), basePotion, resultFluid);
		}

		public BrewingRecipe(ItemStack ingredient, Potion basePotion, FluidStack resultFluid)
		{
			this(ingredient, BrewingRecipes.newBasePotionFluidStack(basePotion), resultFluid);
		}

		public BrewingRecipe(Item ingredient, FluidStack baseFluid, Potion resultPotion)
		{
			this(new ItemStack(ingredient, 1), baseFluid, resultPotion);
		}

		public BrewingRecipe(Item ingredient, Fluid baseFluid, Potion resultPotion)
		{
			this(new ItemStack(ingredient, 1), new FluidStack(baseFluid, 1), resultPotion);
		}

		public BrewingRecipe(ItemStack ingredient, Fluid baseFluid, Potion resultPotion)
		{
			this(ingredient, new FluidStack(baseFluid, 1), resultPotion);
		}

		public BrewingRecipe(ItemStack ingredient, FluidStack baseFluid, Potion resultPotion)
		{
			this(ingredient, baseFluid.copy(), BrewingRecipes.newPotionFluidStack(resultPotion));
		}

		public BrewingRecipe(Item ingredient, Fluid baseFluid, Fluid resultFluid)
		{
			this(new ItemStack(ingredient, 1), new FluidStack(baseFluid, 1), new FluidStack(resultFluid, 1));
		}

		public BrewingRecipe(Item ingredient, Fluid baseFluid, FluidStack resultFluid)
		{
			this(new ItemStack(ingredient, 1), new FluidStack(baseFluid, 1), resultFluid);
		}

		public BrewingRecipe(Item ingredient, FluidStack baseFluid, Fluid resultFluid)
		{
			this(new ItemStack(ingredient, 1), baseFluid, new FluidStack(resultFluid, 1));
		}

		public BrewingRecipe(Item ingredient, FluidStack baseFluid, FluidStack resultFluid)
		{
			this(new ItemStack(ingredient, 1), baseFluid, resultFluid);
		}

		public BrewingRecipe(ItemStack ingredient, Fluid baseFluid, Fluid resultFluid)
		{
			this(ingredient, new FluidStack(baseFluid, 1), new FluidStack(resultFluid, 1));
		}

		public BrewingRecipe(ItemStack ingredient, Fluid baseFluid, FluidStack resultFluid)
		{
			this(ingredient, new FluidStack(baseFluid, 1), resultFluid);
		}

		public BrewingRecipe(ItemStack ingredient, FluidStack baseFluid, Fluid resultFluid)
		{
			this(ingredient, baseFluid, new FluidStack(resultFluid, 1));
		}

		public BrewingRecipe(ItemStack ingredient, FluidStack baseFluid, FluidStack resultFluid)
		{
			this.Ingredient = ingredient.copy();
			this.BaseFluid = baseFluid.copy();
			this.ResultFluid = resultFluid.copy();
			this.brewTime = DEFAULT_BREW_TIME;
		}
		
		public boolean matches(ItemStack item, FluidStack baseFluid)
		{
			if( !this.Ingredient.isItemEqual(item) ) return false;
			if( !BrewingRecipes.isPotionFluidEqual(this.BaseFluid, baseFluid) ) return false;
			
			return true;
		}
		
		public FluidStack generate(FluidStack baseFluid)
		{
			FluidStack result = this.ResultFluid.copy();
			
			CompoundNBT root = baseFluid.getTag();
			
			if( root != null )
			{
				CompoundNBT newRoot = result.getOrCreateTag();
				
				if( root.contains("DyedPotion"))
					newRoot.put("DyedPotion", root.get("DyedPotion"));
				
				if( root.contains("CustomPotionColor"))
					newRoot.put("CustomPotionColor", root.get("CustomPotionColor"));
			}
			
			return result;
		}
		
		public BrewingRecipe setBrewTime(int brewTime)
		{
			this.brewTime = Math.max(brewTime, MIN_BREW_TIME);
			return this;
		}
		
		public int getBrewTime()
		{
			return this.brewTime;
		}
	}
	
	public enum EffectVisibilityEnum {
		SHOW,		// Show effect
		HIDE,		// Hide effect
		KEEP		// Keep visibility as is 
	}
	
	public static class EffectRecipe {
		public final ItemStack ingredient;
		public final Effect effect;
		
		public final Effect newEffect;
		
		public final int durationMinimum;
		public final int durationMaximum;
		public final float durationMultiplier;
		public final float durationModifier;
		
		public final int amplifierMinimum;
		public final int amplifierMaximum;
		public final float amplifierMultiplier;
		public final float amplifierModifier;
		
		public EffectVisibilityEnum visibility;
		
		private int brewTime;
		
		public EffectRecipe(Item ingredient, Effect effect, Effect newEffect, int durMin, int durMax, float durMult, float durMod, int ampMin, int ampMax, float ampMult, float ampMod, EffectVisibilityEnum vis) {
			this(new ItemStack(ingredient, 1), effect, newEffect, durMin, durMax, durMult, durMod, ampMin, ampMax, ampMult, ampMod, vis);
		}
		
		public EffectRecipe(ItemStack ingredient, Effect effect, Effect newEffect, int durMin, int durMax, float durMult, float durMod, int ampMin, int ampMax, float ampMult, float ampMod, EffectVisibilityEnum vis) {
			this.ingredient = ingredient.copy();
			this.effect = effect;
			this.newEffect = newEffect;
			this.durationMinimum = durMin;
			this.durationMaximum = durMax;
			this.durationMultiplier = durMult;
			this.durationModifier = durMod;
			this.amplifierMinimum = ampMin;
			this.amplifierMaximum = ampMax;
			this.amplifierMultiplier = ampMult;
			this.amplifierModifier = ampMod;
			this.visibility = vis;
			this.brewTime = DEFAULT_BREW_TIME;
		}
		
		public boolean matches(@Nonnull ItemStack item, @Nonnull EffectInstance effectInstIn)
		{
			if( !this.ingredient.isItemEqual(item) ) return false;
			
			if( this.effect != null && this.effect != effectInstIn.getPotion()) return false;
			
			if( effectInstIn.getAmplifier() < this.amplifierMinimum ) return false;
			if( this.amplifierMaximum >= 0 && effectInstIn.getAmplifier() > this.amplifierMaximum ) return false;

			if( effectInstIn.getDuration() < this.durationMinimum ) return false;
			if( this.durationMaximum >= 0 && effectInstIn.getDuration() > this.durationMaximum ) return false;

			return true;
		}
		
		public EffectInstance generate(@Nonnull EffectInstance effectInstIn)
		{
			boolean showParticles = effectInstIn.doesShowParticles();
			
			if( this.visibility == EffectVisibilityEnum.SHOW )
				showParticles = true;
			else if( this.visibility == EffectVisibilityEnum.HIDE)
				showParticles = false;
			
			int oldDuration = effectInstIn.getDuration();
			
			int newDuration = (int)(oldDuration * this.durationMultiplier + this.durationModifier);
			if (oldDuration > 1 && newDuration <= 1 )
				newDuration = Math.min(oldDuration, 10);
			// Prevent "instant" effects from changing
			if( oldDuration <= 1 && newDuration > 1)
				newDuration = oldDuration;
			
			int oldAmplifier = effectInstIn.getAmplifier();
			int newAmplifier = (int)(oldAmplifier * this.amplifierMultiplier + this.amplifierModifier);
			newAmplifier = MathHelper.clamp(newAmplifier, 0, 255);
			Concoctions.GetLogger().info("Amp: Old {} vs New {}, *{} +{}", oldAmplifier, newAmplifier, this.amplifierMultiplier, this.amplifierModifier);
			
			List<ItemStack> curativeItems;
			Effect newEffect;
			
			if( this.newEffect != null )
			{
				newEffect = this.newEffect;
				curativeItems = newEffect.getCurativeItems();
			}
			else
			{
				newEffect = effectInstIn.getPotion();
				curativeItems = effectInstIn.getCurativeItems();
			}
			
			EffectInstance effectInstOut = new EffectInstance(newEffect, newDuration, newAmplifier, false, showParticles);
			effectInstOut.setCurativeItems(List.copyOf(curativeItems));
			
			return effectInstOut;
		}
		
		public EffectRecipe setBrewTime(int brewTime)
		{
			this.brewTime = Math.max(brewTime, MIN_BREW_TIME);
			return this;
		}
		
		public int getBrewTime()
		{
			return this.brewTime;
		}
	}
	
	public static class DyeRecipe {
		public final ItemStack ingredient;
		public final DyeColor color;
		public final boolean force;
		private int brewTime;
		
		public DyeRecipe(Item ingredient, DyeColor color) {
			this(ingredient, color, false);
		}

		public DyeRecipe(Item ingredient, DyeColor color, boolean force) {
			this.ingredient = new ItemStack(ingredient, 1);
			this.color = color;
			this.force = force;
			this.brewTime = DEFAULT_BREW_TIME;
		}
		
		public DyeRecipe(ItemStack ingredient, DyeColor color) {
			this(ingredient, color, false);
		}

		public DyeRecipe(ItemStack ingredient, DyeColor color, boolean force) {
			this.ingredient = ingredient.copy();
			this.color = color;
			this.force = force;
			this.brewTime = DEFAULT_BREW_TIME;
		}
		
		public boolean matches(ItemStack item)
		{
			return this.ingredient.isItemEqual(item);
		}
		
		public DyeRecipe setBrewTime(int brewTime)
		{
			this.brewTime = Math.max(brewTime, MIN_BREW_TIME);
			return this;
		}
		
		public int getBrewTime()
		{
			return this.brewTime;
		}
		
		// This assumes that the base fluid is a POTION FLUID
		public FluidStack generate(FluidStack baseFluid)
		{
			assert (baseFluid.getFluid() == Registry.POTION_FLUID.get());
			
			return null;
		}
	}
	
}

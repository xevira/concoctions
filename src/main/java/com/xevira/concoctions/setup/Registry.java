package com.xevira.concoctions.setup;


import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.block.*;
import com.xevira.concoctions.common.block.tile.*;
import com.xevira.concoctions.common.container.BrewingStationContainer;
import com.xevira.concoctions.common.effects.*;
import com.xevira.concoctions.common.entities.*;
import com.xevira.concoctions.common.fluids.PotionFluid;
import com.xevira.concoctions.common.items.*;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potion;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registry {
	
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Concoctions.MOD_ID);
	public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Concoctions.MOD_ID);
	public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, Concoctions.MOD_ID);
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Concoctions.MOD_ID);
	public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, Concoctions.MOD_ID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Concoctions.MOD_ID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTION_TYPES, Concoctions.MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Concoctions.MOD_ID);

    public static void init(IEventBus event) {
		BLOCKS.register(event);
		CONTAINERS.register(event);
		EFFECTS.register(event);
		ENTITIES.register(event);
		ITEMS.register(event);
		FLUIDS.register(event);
		POTIONS.register(event);
		TILES.register(event);
    }
    
    // Item Properties
    public static final Item.Properties PROPS_BREWING = new Item.Properties().group(ItemGroup.BREWING);
    
    // Blocks
    public static final RegistryObject<BrewingStationBlock> BREWING_STATION = BLOCKS.register("brewing_station", BrewingStationBlock::new);
    public static final RegistryObject<FilledCauldronBlock> FILLED_CAULDRON = BLOCKS.register("filled_cauldron", FilledCauldronBlock::new);
    public static final RegistryObject<LamentingLilyBlock> LAMENTING_LILY = BLOCKS.register("lamenting_lily", LamentingLilyBlock::new);
    
    // Containers
    public static final RegistryObject<ContainerType<BrewingStationContainer>> BREWING_STATION_CONTAINER = CONTAINERS.register("brewing_station_container", () -> IForgeContainerType.create(BrewingStationContainer::new));
    
    // Damage Sources
    public static final DamageSource VOID_DAMAGE = new DamageSource("void").setDamageBypassesArmor().setDamageAllowedInCreativeMode();

    // Entities
    public static final RegistryObject<EntityType<ItemEntityNoImbue>> ITEM_ENTITY_NO_IMBUE = ENTITIES.register("item_no_imbue", () -> EntityType.Builder.<ItemEntityNoImbue>create(ItemEntityNoImbue::new, EntityClassification.MISC).build("item_no_imbue"));
    
    // Effects
    public static final RegistryObject<Effect> AMOROUS_EFFECT = EFFECTS.register("amorous", () -> new AmorousEffect(EffectType.NEUTRAL, 0xFF0000));
    public static final RegistryObject<Effect> BOUNCY_EFFECT = EFFECTS.register("bouncy", () -> new BouncyEffect(EffectType.NEUTRAL, 0x00ff00));
    public static final RegistryObject<Effect> DANGER_SENSE_EFFECT = EFFECTS.register("danger_sense", () -> new DangerSenseEffect(EffectType.BENEFICIAL, 0x39c239));
    public static final RegistryObject<Effect> FLOURISH_EFFECT = EFFECTS.register("flourish", () -> new FlourishingEffect(EffectType.BENEFICIAL,0x00e696));
    public static final RegistryObject<Effect> GRAVITY_EFFECT = EFFECTS.register("gravity", () -> new GravityEffect(EffectType.HARMFUL, 0x7998d7/*16410214*/));
    //public static final RegistryObject<Effect> GROWTH_EFFECT = EFFECTS.register("growth", () -> new ResizeEntityEffect(EffectType.NEUTRAL, 0x777777, true));
    public static final RegistryObject<Effect> INTANGIBLE_EFFECT = EFFECTS.register("intangible", () -> new IntangibleEffectSpectator(EffectType.NEUTRAL, 0x999999));
    public static final RegistryObject<Effect> RECALL_EFFECT = EFFECTS.register("recall", () -> new RecallEffect(EffectType.NEUTRAL, 0x00b5bc));
    public static final RegistryObject<Effect> TAMING_EFFECT = EFFECTS.register("taming", () -> new TamingEffect(EffectType.NEUTRAL, 0xff8484));
    //public static final RegistryObject<Effect> SHRINK_EFFECT = EFFECTS.register("shrink", () -> new ResizeEntityEffect(EffectType.NEUTRAL, 0x777777, false));
    public static final RegistryObject<Effect> VOID_EFFECT = EFFECTS.register("void", () -> new VoidEffect(EffectType.HARMFUL, 0x080808));
    
    // Fluids
    public static final RegistryObject<Fluid> POTION_FLUID = FLUIDS.register("potion_fluid", () -> new PotionFluid(Resources.POTION_FLUID_STILL, Resources.POTION_FLUID_FLOWING));
    
    // Items
    public static final RegistryObject<Item> APOTHECARY_GUIDE = ITEMS.register("apothecary_guide", () -> new ApothecaryGuide());
    public static final RegistryObject<Item> BOTTLE_FIRE = ITEMS.register("bottle_fire", () -> new BottleFireItem((new Item.Properties()).group(ItemGroup.BREWING)));
    public static final RegistryObject<Item> BOTTLE_SOUL_FIRE = ITEMS.register("bottle_soul_fire", () -> new BottleFireItem((new Item.Properties()).group(ItemGroup.BREWING)));
    public static final RegistryObject<Item> BOTTLE_SUNLIGHT = ITEMS.register("bottle_sunlight", () -> new BottleSunlightItem((new Item.Properties()).group(ItemGroup.BREWING)));
    public static final RegistryObject<Item> BOTTLE_VOID_ESSENCE = ITEMS.register("bottle_void_essence", () -> new BottleVoidEssenceItem((new Item.Properties()).group(ItemGroup.BREWING)));
    public static final RegistryObject<Item> BREWING_STATION_ITEM = ITEMS.register("brewing_station", () -> new BrewingStationItem(BREWING_STATION.get(), Registry.PROPS_BREWING));
    public static final RegistryObject<Item> DOLPHIN_FIN = ITEMS.register("dolphin_fin", () -> new Item((new Item.Properties()).group(ItemGroup.BREWING)));
    public static final RegistryObject<Item> FILLED_CAULDRON_ITEM = ITEMS.register("filled_cauldron", () -> new FilledCauldronItem(FILLED_CAULDRON.get(), new Item.Properties()));
    public static final RegistryObject<Item> GLIMMERING_WHITE_DYE = ITEMS.register("glimmering_white_dye", () -> new GlimmeringDyeItem(DyeColor.WHITE, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_ORANGE_DYE = ITEMS.register("glimmering_orange_dye", () -> new GlimmeringDyeItem(DyeColor.ORANGE, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_MAGENTA_DYE = ITEMS.register("glimmering_magenta_dye", () -> new GlimmeringDyeItem(DyeColor.MAGENTA, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_LIGHT_BLUE_DYE = ITEMS.register("glimmering_light_blue_dye", () -> new GlimmeringDyeItem(DyeColor.LIGHT_BLUE, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_YELLOW_DYE = ITEMS.register("glimmering_yellow_dye", () -> new GlimmeringDyeItem(DyeColor.YELLOW, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_LIME_DYE = ITEMS.register("glimmering_lime_dye", () -> new GlimmeringDyeItem(DyeColor.LIME, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_PINK_DYE = ITEMS.register("glimmering_pink_dye", () -> new GlimmeringDyeItem(DyeColor.PINK, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_GRAY_DYE = ITEMS.register("glimmering_gray_dye", () -> new GlimmeringDyeItem(DyeColor.GRAY, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_LIGHT_GRAY_DYE = ITEMS.register("glimmering_light_gray_dye", () -> new GlimmeringDyeItem(DyeColor.LIGHT_GRAY, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_CYAN_DYE = ITEMS.register("glimmering_cyan_dye", () -> new GlimmeringDyeItem(DyeColor.CYAN, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_PURPLE_DYE = ITEMS.register("glimmering_purple_dye", () -> new GlimmeringDyeItem(DyeColor.PURPLE, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_BLUE_DYE = ITEMS.register("glimmering_blue_dye", () -> new GlimmeringDyeItem(DyeColor.BLUE, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_BROWN_DYE = ITEMS.register("glimmering_brown_dye", () -> new GlimmeringDyeItem(DyeColor.BROWN, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_GREEN_DYE = ITEMS.register("glimmering_green_dye", () -> new GlimmeringDyeItem(DyeColor.GREEN, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_RED_DYE = ITEMS.register("glimmering_red_dye", () -> new GlimmeringDyeItem(DyeColor.RED, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> GLIMMERING_BLACK_DYE = ITEMS.register("glimmering_black_dye", () -> new GlimmeringDyeItem(DyeColor.BLACK, (new Item.Properties()).group(ItemGroup.MATERIALS)));
    public static final RegistryObject<Item> LAMENTING_LILY_ITEM = ITEMS.register("lamenting_lily", () -> new LamentingLilyItem(LAMENTING_LILY.get(), new Item.Properties().group(ItemGroup.DECORATIONS)));
    public static final RegistryObject<Item> LINGERING_BOTTLE = ITEMS.register("lingering_bottle", () -> new LingeringBottleItem(new Item.Properties().group(ItemGroup.BREWING)));
    public static final RegistryObject<Item> SPLASH_BOTTLE = ITEMS.register("splash_bottle", () -> new SplashBottleItem(new Item.Properties().group(ItemGroup.BREWING)));

    // Potions
    public static final RegistryObject<Potion> HASTE_POTION = POTIONS.register("haste", () -> new Potion(new EffectInstance(Effects.HASTE, 3600)));
    public static final RegistryObject<Potion> LONG_HASTE_POTION = POTIONS.register("long_haste", () -> new Potion("haste", new EffectInstance(Effects.HASTE, 9600)));
    public static final RegistryObject<Potion> STRONG_HASTE_POTION = POTIONS.register("strong_haste", () -> new Potion("haste", new EffectInstance(Effects.HASTE, 1800, 1)));

    public static final RegistryObject<Potion> DULLNESS_POTION = POTIONS.register("dullness", () -> new Potion(new EffectInstance(Effects.MINING_FATIGUE, 3600)));
    public static final RegistryObject<Potion> LONG_DULLNESS_POTION = POTIONS.register("long_dullness", () -> new Potion("dullness", new EffectInstance(Effects.MINING_FATIGUE, 9600)));
    public static final RegistryObject<Potion> STRONG_DULLNESS_POTION = POTIONS.register("strong_dullness", () -> new Potion("dullness", new EffectInstance(Effects.MINING_FATIGUE, 1800, 1)));

    public static final RegistryObject<Potion> CONFUSION_POTION = POTIONS.register("confusion", () -> new Potion(new EffectInstance(Effects.BLINDNESS, 900)));
    public static final RegistryObject<Potion> LONG_CONFUSION_POTION = POTIONS.register("long_confusion", () -> new Potion("confusion", new EffectInstance(Effects.BLINDNESS, 2400)));
    
    public static final RegistryObject<Potion> HUNGER_POTION = POTIONS.register("hunger", () -> new Potion(new EffectInstance(Effects.HUNGER, 600)));
    public static final RegistryObject<Potion> LONG_HUNGER_POTION = POTIONS.register("long_hunger", () -> new Potion("hunger", new EffectInstance(Effects.HUNGER, 1600)));
    public static final RegistryObject<Potion> STRONG_HUNGER_POTION = POTIONS.register("strong_hunger", () -> new Potion("hunger", new EffectInstance(Effects.HUNGER, 300, 1)));
    
    public static final RegistryObject<Potion> DECAY_POTION = POTIONS.register("decay", () -> new Potion(new EffectInstance(Effects.WITHER, 600)));
    public static final RegistryObject<Potion> LONG_DECAY_POTION = POTIONS.register("long_decay", () -> new Potion("decay", new EffectInstance(Effects.WITHER, 1600)));
    public static final RegistryObject<Potion> STRONG_DECAY_POTION = POTIONS.register("strong_decay", () -> new Potion("decay", new EffectInstance(Effects.WITHER, 300, 1)));
    
    public static final RegistryObject<Potion> RESISTANCE_POTION = POTIONS.register("resistance", () -> new Potion(new EffectInstance(Effects.RESISTANCE, 1800)));
    public static final RegistryObject<Potion> LONG_RESISTANCE_POTION = POTIONS.register("long_resistance", () -> new Potion("resistance", new EffectInstance(Effects.RESISTANCE, 4800)));
    public static final RegistryObject<Potion> STRONG_RESISTANCE_POTION = POTIONS.register("strong_resistance", () -> new Potion("resistance", new EffectInstance(Effects.RESISTANCE, 900, 1)));
    
    public static final RegistryObject<Potion> NOTCH_POTION = POTIONS.register("notch", () -> new Potion(new EffectInstance(Effects.ABSORPTION, 3600), new EffectInstance(Effects.SATURATION, 3600)));
    public static final RegistryObject<Potion> LONG_NOTCH_POTION = POTIONS.register("long_notch", () -> new Potion("notch", new EffectInstance(Effects.ABSORPTION, 9600), new EffectInstance(Effects.SATURATION, 9600)));
    public static final RegistryObject<Potion> STRONG_NOTCH_POTION = POTIONS.register("strong_notch", () -> new Potion("notch", new EffectInstance(Effects.ABSORPTION, 1800, 1), new EffectInstance(Effects.SATURATION, 1800)));
    
    public static final RegistryObject<Potion> LEVITATION_POTION = POTIONS.register("levitation", () -> new Potion(new EffectInstance(Effects.LEVITATION, 600)));
    public static final RegistryObject<Potion> LONG_LEVITATION_POTION = POTIONS.register("long_levitation", () -> new Potion("levitation", new EffectInstance(Effects.LEVITATION, 1600)));
    public static final RegistryObject<Potion> STRONG_LEVITATION_POTION = POTIONS.register("strong_levitation", () -> new Potion("levitation", new EffectInstance(Effects.LEVITATION, 300, 1)));
    
    public static final RegistryObject<Potion> NAUSEA_POTION = POTIONS.register("nausea", () -> new Potion(new EffectInstance(Effects.NAUSEA, 600)));
    public static final RegistryObject<Potion> LONG_NAUSEA_POTION = POTIONS.register("long_nausea", () -> new Potion("nausea", new EffectInstance(Effects.NAUSEA, 1600)));
    
    public static final RegistryObject<Potion> GLOWING_POTION = POTIONS.register("glowing", () -> new Potion(new EffectInstance(Effects.GLOWING, 600)));
    public static final RegistryObject<Potion> LONG_GLOWING_POTION = POTIONS.register("long_glowing", () -> new Potion("glowing", new EffectInstance(Effects.GLOWING, 1600)));
    
    public static final RegistryObject<Potion> LUCK_POTION = POTIONS.register("luck", () -> new Potion(new EffectInstance(Effects.LUCK, 3600)));
    public static final RegistryObject<Potion> LONG_LUCK_POTION = POTIONS.register("long_luck", () -> new Potion("luck", new EffectInstance(Effects.LUCK, 9600)));
    public static final RegistryObject<Potion> STRONG_LUCK_POTION = POTIONS.register("strong_luck", () -> new Potion("luck", new EffectInstance(Effects.LUCK, 1800, 1)));
    
    public static final RegistryObject<Potion> BADLUCK_POTION = POTIONS.register("badluck", () -> new Potion(new EffectInstance(Effects.UNLUCK, 3600)));
    public static final RegistryObject<Potion> LONG_BADLUCK_POTION = POTIONS.register("long_badluck", () -> new Potion("badluck", new EffectInstance(Effects.UNLUCK, 9600)));
    public static final RegistryObject<Potion> STRONG_BADLUCK_POTION = POTIONS.register("strong_badluck", () -> new Potion("badluck", new EffectInstance(Effects.UNLUCK, 1800, 1)));
    
    public static final RegistryObject<Potion> NEPTUNE_POTION = POTIONS.register("neptune", () -> new Potion(new EffectInstance(Effects.CONDUIT_POWER, 600)));
    public static final RegistryObject<Potion> LONG_NEPTUNE_POTION = POTIONS.register("long_neptune", () -> new Potion("neptune", new EffectInstance(Effects.CONDUIT_POWER, 1600)));
    
    public static final RegistryObject<Potion> GRACE_POTION = POTIONS.register("grace", () -> new Potion(new EffectInstance(Effects.DOLPHINS_GRACE, 600)));
    public static final RegistryObject<Potion> LONG_GRACE_POTION = POTIONS.register("long_grace", () -> new Potion("grace", new EffectInstance(Effects.DOLPHINS_GRACE, 1600)));
    
    // Potions - Custom Effects
    public static final RegistryObject<Potion> BOUNCY_POTION = POTIONS.register("bouncy", () -> new Potion(new EffectInstance(Registry.BOUNCY_EFFECT.get(), 1200)));
    public static final RegistryObject<Potion> LONG_BOUNCY_POTION = POTIONS.register("long_bouncy", () -> new Potion("bouncy", new EffectInstance(Registry.BOUNCY_EFFECT.get(), 3200)));

    public static final RegistryObject<Potion> DANGER_SENSE_POTION = POTIONS.register("danger_sense", () -> new Potion(new EffectInstance(Registry.DANGER_SENSE_EFFECT.get(), 3600)));
    public static final RegistryObject<Potion> LONG_DANGER_SENSE_POTION = POTIONS.register("long_danger_sense", () -> new Potion("danger_sense", new EffectInstance(Registry.DANGER_SENSE_EFFECT.get(), 9600)));
    public static final RegistryObject<Potion> STRONG_DANGER_SENSE_POTION = POTIONS.register("strong_danger_sense", () -> new Potion("danger_sense", new EffectInstance(Registry.DANGER_SENSE_EFFECT.get(), 1800, 1)));

    public static final RegistryObject<Potion> GRAVITY_POTION = POTIONS.register("gravity", () -> new Potion(new EffectInstance(Registry.GRAVITY_EFFECT.get(), 600)));
    public static final RegistryObject<Potion> LONG_GRAVITY_POTION = POTIONS.register("long_gravity", () -> new Potion("gravity", new EffectInstance(Registry.GRAVITY_EFFECT.get(), 1600)));
    public static final RegistryObject<Potion> STRONG_GRAVITY_POTION = POTIONS.register("strong_gravity", () -> new Potion("gravity", new EffectInstance(Registry.GRAVITY_EFFECT.get(), 300, 1)));
    
    public static final RegistryObject<Potion> INTANGIBILITY_POTION = POTIONS.register("intangibility", () -> new Potion(new EffectInstance(Registry.INTANGIBLE_EFFECT.get(), 3600)));
    public static final RegistryObject<Potion> LONG_INTANGIBILITY_POTION = POTIONS.register("long_intangibility", () -> new Potion("intangibility", new EffectInstance(Registry.INTANGIBLE_EFFECT.get(), 9600)));
    
    public static final RegistryObject<Potion> LOVE_POTION = POTIONS.register("love", () -> new Potion(new EffectInstance(Registry.AMOROUS_EFFECT.get(), 3600)));
    public static final RegistryObject<Potion> LONG_LOVE_POTION = POTIONS.register("long_love", () -> new Potion("love", new EffectInstance(Registry.AMOROUS_EFFECT.get(), 9600)));
    public static final RegistryObject<Potion> STRONG_LOVE_POTION = POTIONS.register("strong_love", () -> new Potion("love", new EffectInstance(Registry.AMOROUS_EFFECT.get(), 1800, 1)));

    public static final RegistryObject<Potion> RECALL_POTION = POTIONS.register("recall", () -> new Potion(new EffectInstance(Registry.RECALL_EFFECT.get(), 1)));

    public static final RegistryObject<Potion> VOID_POTION = POTIONS.register("void", () -> new Potion(new EffectInstance(Registry.VOID_EFFECT.get(), 600)));
    public static final RegistryObject<Potion> LONG_VOID_POTION = POTIONS.register("long_void", () -> new Potion("void", new EffectInstance(Registry.VOID_EFFECT.get(), 1600)));
    public static final RegistryObject<Potion> STRONG_VOID_POTION = POTIONS.register("strong_void", () -> new Potion("void", new EffectInstance(Registry.VOID_EFFECT.get(), 300, 1)));

    public static final RegistryObject<Potion> FLOURISH_POTION = POTIONS.register("flourish", () -> new Potion(new EffectInstance(Registry.FLOURISH_EFFECT.get(), 3600)));
    public static final RegistryObject<Potion> LONG_FLOURISH_POTION = POTIONS.register("long_flourish", () -> new Potion("flourish", new EffectInstance(Registry.FLOURISH_EFFECT.get(), 9600)));
    public static final RegistryObject<Potion> STRONG_FLOURISH_POTION = POTIONS.register("strong_flourish", () -> new Potion("flourish", new EffectInstance(Registry.FLOURISH_EFFECT.get(), 1800, 1)));

    public static final RegistryObject<Potion> TAMING_POTION = POTIONS.register("taming", () -> new Potion(new EffectInstance(Registry.TAMING_EFFECT.get(), 1)));
    public static final RegistryObject<Potion> STRONG_TAMING_POTION = POTIONS.register("strong_taming", () -> new Potion("taming", new EffectInstance(Registry.TAMING_EFFECT.get(), 1, 1)));

    // Properties
    public static final IntegerProperty STAGE_0_3 = IntegerProperty.create("stage", 0, 3);
    
    // Tile Entities
	public static final RegistryObject<TileEntityType<BrewingStationTile>> BREWING_STATION_TILE = TILES.register("brewing_station", () -> TileEntityType.Builder.create(BrewingStationTile::new, BREWING_STATION.get()).build(null));
	public static final RegistryObject<TileEntityType<FilledCauldronTile>> FILLED_CAULDRON_TILE = TILES.register("filled_cauldron", () -> TileEntityType.Builder.create(FilledCauldronTile::new, FILLED_CAULDRON.get()).build(null));
}

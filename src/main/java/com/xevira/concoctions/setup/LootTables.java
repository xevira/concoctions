package com.xevira.concoctions.setup;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.xevira.concoctions.Concoctions;

import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Concoctions.MOD_ID)
public class LootTables {
	private LootTables() {}
	
	private static final List<String> ENTITY_TABLES = ImmutableList.of("dolphin");
	
	@SuppressWarnings("unused") //used in event
	@SubscribeEvent
	public static void lootLoad(LootTableLoadEvent evt) {
		String chestsPrefix = "minecraft:chests/";
		String entitiesPrefix = "minecraft:entities/";
		String name = evt.getName().toString();

		if( name.startsWith(entitiesPrefix) && ENTITY_TABLES.contains(name.substring(entitiesPrefix.length()))) {
			String file = name.substring("minecraft:".length());
			evt.getTable().addPool(getInjectPool(file));
		}
	}
	

	private static LootPool getInjectPool(String entryName) {
		return LootPool.builder().addEntry(getInjectEntry(entryName)).bonusRolls(0, 1).name("concoctions_inject_pool").build();
	}

	private static LootEntry.Builder<?> getInjectEntry(String name) {
		return TableLootEntry.builder(new ResourceLocation(Concoctions.MOD_ID, "inject/" + name)).weight(1);
	}
}

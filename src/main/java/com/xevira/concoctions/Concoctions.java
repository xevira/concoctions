package com.xevira.concoctions;

import com.xevira.concoctions.client.gui.screen.BrewingStationScreen;
import com.xevira.concoctions.setup.*;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Concoctions.MOD_ID)
public class Concoctions {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "concoctions";
	public static final String MOD_NAME = "Concoctions";
	public static final String MOD_VERSION = "0.0.1";
	
	public Concoctions(){
		IEventBus event = FMLJavaModLoadingContext.get().getModEventBus();

		Registry.init(event);
	
		event.addListener(this::setup);
	    event.addListener(this::clientSetup);
	    event.addListener(this::sendImc);
	    event.addListener(this::loadComplete);
	   
	    MinecraftForge.EVENT_BUS.register(this);
	
	    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SERVER_CONFIG);
	}
	
	public void setup (final FMLCommonSetupEvent event){
	}
	
	public void clientSetup(final FMLClientSetupEvent event){
		ScreenManager.registerFactory(Registry.BREWING_STATION_CONTAINER.get(), BrewingStationScreen::new);
	}
	
	public void sendImc(InterModEnqueueEvent evt) {
	}
	
	public void loadComplete(final FMLLoadCompleteEvent event) {
		BrewingRecipes.init();		// Allow for other mods to add brewing recipes
	}
	
	public static Logger GetLogger() {
		return LOGGER;
	}
}

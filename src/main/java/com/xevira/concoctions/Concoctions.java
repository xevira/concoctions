package com.xevira.concoctions;

import com.xevira.concoctions.client.ClientSetup;
import com.xevira.concoctions.common.EventHandler;
import com.xevira.concoctions.common.inventory.crafting.Recipes;
import com.xevira.concoctions.common.network.PacketHandler;
import com.xevira.concoctions.setup.*;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
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
	    event.addListener(this::gatherData);
	   
	    MinecraftForge.EVENT_BUS.register(this);
	    MinecraftForge.EVENT_BUS.register(new EventHandler());
	
	    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SERVER_CONFIG);
	}
	
	public void setup (final FMLCommonSetupEvent event)
	{
		PacketHandler.register();
	}
	
	public void clientSetup(final FMLClientSetupEvent event)
	{
		ClientSetup.init();
	}
	
	public void sendImc(InterModEnqueueEvent evt) {
	}
	
	public void gatherData(GatherDataEvent event)
	{
		Concoctions.GetLogger().info("Concoctions.handleGatherDataEvent called");
		DataGenerator gen = event.getGenerator();

		if(event.includeServer())
		{
			Concoctions.GetLogger().info("Concoctions.handleGatherDataEvent (SERVER) called");

			gen.addProvider(new Recipes(gen));
		}
	}

	
	public void loadComplete(final FMLLoadCompleteEvent event) {
		BrewingRecipes.postInit();
		ImbuingRecipes.postInit();
	}
	
	public static Logger GetLogger() {
		return LOGGER;
	}
}

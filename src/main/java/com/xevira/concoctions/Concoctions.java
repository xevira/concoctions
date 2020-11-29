package com.xevira.concoctions;

import com.xevira.concoctions.client.gui.screen.*;
import com.xevira.concoctions.client.ter.*;
import com.xevira.concoctions.common.EventHandler;
import com.xevira.concoctions.common.block.FilledCauldronBlock;
import com.xevira.concoctions.common.block.tile.FilledCauldronTile;
import com.xevira.concoctions.common.network.PacketHandler;
import com.xevira.concoctions.common.utils.Utils;
import com.xevira.concoctions.setup.*;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
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
	    MinecraftForge.EVENT_BUS.register(new EventHandler());
	
	    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SERVER_CONFIG);
	}
	
	public void setup (final FMLCommonSetupEvent event){
		PacketHandler.register();
	}
	
	public void clientSetup(final FMLClientSetupEvent event){
		// Screens
		ScreenManager.registerFactory(Registry.BREWING_STATION_CONTAINER.get(), BrewingStationScreen::new);
		
		// Tile Entity Renderers
		ClientRegistry.bindTileEntityRenderer(Registry.FILLED_CAULDRON_TILE.get(), FilledCauldronTileRenderer::new);
		
		// Block Render Types
		RenderTypeLookup.setRenderLayer(Registry.LAMENTING_LILY.get(), RenderType.getCutout());
	}
	
	public void sendImc(InterModEnqueueEvent evt) {
	}
	
	public void loadComplete(final FMLLoadCompleteEvent event) {
		BrewingRecipes.postInit();
		ImbuingRecipes.postInit();
	}
	
	public static Logger GetLogger() {
		return LOGGER;
	}
}

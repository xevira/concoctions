package com.xevira.concoctions.client;

import com.xevira.concoctions.client.gui.screen.*;
import com.xevira.concoctions.client.ter.*;
import com.xevira.concoctions.common.items.*;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientSetup {
	private static Block[] Cutouts = new Block[] {
			Registry.BREWING_STATION.get(),
			Registry.LAMENTING_LILY.get(),
			Registry.MIXER.get()
	};
	
	private static void setRenderLayers(Block[] blocks, RenderType type)
	{
		for(Block block : blocks)
			RenderTypeLookup.setRenderLayer(block, type);
	}
	
	public static void init()
	{
		// Screens
		ScreenManager.registerFactory(Registry.BREWING_STATION_CONTAINER.get(), BrewingStationScreen::new);
		
		// Tile Entity Renderers
		ClientRegistry.bindTileEntityRenderer(Registry.BREWING_STATION_TILE.get(), BrewingStationTileRenderer::new);
		ClientRegistry.bindTileEntityRenderer(Registry.FILLED_CAULDRON_TILE.get(), FilledCauldronTileRenderer::new);
		
		// Block Render Types
		setRenderLayers(Cutouts, RenderType.getCutout());
		//RenderTypeLookup.setRenderLayer(Registry.BREWING_STATION.get(), RenderType.getCutout());
		//RenderTypeLookup.setRenderLayer(Registry.LAMENTING_LILY.get(), RenderType.getCutout());
		//RenderTypeLookup.setRenderLayer(Registry.MIXER.get(), RenderType.getCutout());
		
		// Coloring
		Minecraft.getInstance().getItemColors().register((stack, tintIndex) -> {
			return FilledCauldronItem.getFluidColor(stack, tintIndex);
		}, Registry.FILLED_CAULDRON_ITEM.get());

	}

}

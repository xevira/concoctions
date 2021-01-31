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
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientSetup {
	private static Block[] Cutouts = new Block[] {
			Registry.BREWING_STATION.get(),
			Registry.FIREBLOSSOM.get(),
			Registry.LAMENTING_LILY.get(),
			Registry.MIXER.get(),
			Registry.SYNTHESIZER.get()
	};
	
	private static void setRenderLayers(Block[] blocks, RenderType type)
	{
		for(Block block : blocks)
			RenderTypeLookup.setRenderLayer(block, type);
	}
	
	private static void setupItemColors()
	{
		ItemColors ic = Minecraft.getInstance().getItemColors();

		ic.register((stack, tintIndex) -> {
			return FilledCauldronItem.getFluidColor(stack, tintIndex);
		}, Registry.FILLED_CAULDRON_ITEM.get());
		
		ic.register((stack, tintIndex) -> {
			return (tintIndex == 0) ? PotionUtils.getColor(stack) : -1;
		}, Registry.INCENSE_ITEM.get());
	}
	
	public static void init()
	{
		// Screens
		ScreenManager.registerFactory(Registry.BREWING_STATION_CONTAINER.get(), BrewingStationScreen::new);
		ScreenManager.registerFactory(Registry.MIXER_CONTAINER.get(), MixerScreen::new);
		ScreenManager.registerFactory(Registry.SYNTHESIZER_CONTAINER.get(), SynthesizerScreen::new);
		
		// Tile Entity Renderers
		ClientRegistry.bindTileEntityRenderer(Registry.BREWING_STATION_TILE.get(), BrewingStationTileRenderer::new);
		ClientRegistry.bindTileEntityRenderer(Registry.FILLED_CAULDRON_TILE.get(), FilledCauldronTileRenderer::new);
		ClientRegistry.bindTileEntityRenderer(Registry.INCENSE_BURNER_TILE.get(), IncenseBurnerTileRenderer::new);
		ClientRegistry.bindTileEntityRenderer(Registry.MIXER_TILE.get(), MixerTileRenderer::new);
		//ClientRegistry.bindTileEntityRenderer(Registry.SYNTHESIZER_TILE.get(), SynthesizerTileRenderer::new);
		
		// Block Render Types
		setRenderLayers(Cutouts, RenderType.getCutout());
		//RenderTypeLookup.setRenderLayer(Registry.BREWING_STATION.get(), RenderType.getCutout());
		//RenderTypeLookup.setRenderLayer(Registry.LAMENTING_LILY.get(), RenderType.getCutout());
		//RenderTypeLookup.setRenderLayer(Registry.MIXER.get(), RenderType.getCutout());
		
		// Coloring
		setupItemColors();
	}

}

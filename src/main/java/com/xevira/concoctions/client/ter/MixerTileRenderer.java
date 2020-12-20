package com.xevira.concoctions.client.ter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.xevira.concoctions.client.RenderUtils;
import com.xevira.concoctions.common.block.tile.MixerTile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class MixerTileRenderer extends TileEntityRenderer<MixerTile> {
	private static final AxisAlignedBB TANKS[] = new AxisAlignedBB[] {
		new AxisAlignedBB(6.1D/16.0D, 5.1D/16.0D, -0.9D/16.0D, 9.9D/16.0D, 14.9D/16.0D, 2.9D/16.0D),	// North	
		new AxisAlignedBB(6.1D/16.0D, 5.1D/16.0D, 13.1D/16.0D, 9.9D/16.0D, 14.9D/16.0D, 16.9D/16.0D),	// South
		new AxisAlignedBB(13.1D/16.0D, 5.1D/16.0D, 6.1D/16.0D, 16.9D/16.0D, 14.9D/16.0D, 9.9D/16.0D),	// East
		new AxisAlignedBB(-0.9D/16.0D, 5.1D/16.0D, 6.1D/16.0D, 2.9D/16.0D, 14.9D/16.0D, 9.9D/16.0D),	// West
	};
	private static final AxisAlignedBB CENTER = new AxisAlignedBB(5.1D/16.0D, 1.1D/16.0D, 5.1D/16.0D, 10.9D/16.0D, 15.9D/16.0D, 10.9D/16.0D);


	public MixerTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(MixerTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
			int combinedLightIn, int combinedOverlayIn)
	{
		FluidStack[] fluids = tile.getFluids();
		int[] capacities = tile.getCapacities();
		assert fluids.length == TANKS.length;
		assert capacities.length == TANKS.length;
		
		IVertexBuilder builder = bufferIn.getBuffer(RenderType.getSolid());
		
		for(int i = 0; i < TANKS.length; i++)
			renderTank(tile, builder, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, fluids[i], capacities[i], TANKS[i], true);

		renderTank(tile, builder, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, fluids[TANKS.length], capacities[TANKS.length], CENTER, false);
	}
	
	private void renderTank(MixerTile tile, IVertexBuilder builder, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
			int combinedLightIn, int combinedOverlayIn, FluidStack fluid, int capacity, AxisAlignedBB volume, boolean showBottom)
	{
		if(fluid.isEmpty()) return;
		if(capacity <= 0) return;
		
		double h = fluid.getAmount() * volume.getYSize() / capacity;
		
		AxisAlignedBB box = new AxisAlignedBB(volume.minX, volume.minY, volume.minZ, volume.maxX, volume.minY + h, volume.maxZ);
	
		FluidAttributes attr = fluid.getFluid().getAttributes();
		int color = attr.getColor(fluid);

        TextureAtlasSprite still = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(attr.getStillTexture(fluid));
        //TextureAtlasSprite flowing = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(attr.getFlowingTexture(fluid));
		
		RenderUtils.renderTextureCuboid(builder, matrixStackIn, still, box, color, combinedLightIn, false, false, false, true, true, true, true, true, showBottom);
	}
}

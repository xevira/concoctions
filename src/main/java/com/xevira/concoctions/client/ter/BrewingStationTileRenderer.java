package com.xevira.concoctions.client.ter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.xevira.concoctions.client.RenderUtils;
import com.xevira.concoctions.common.block.BrewingStationBlock;
import com.xevira.concoctions.common.block.tile.BrewingStationTile;
import com.xevira.concoctions.common.block.tile.FilledCauldronTile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class BrewingStationTileRenderer extends TileEntityRenderer<BrewingStationTile> {
	private static final float X1 = 2.0f / 16.0f;
	private static final float X2 = 14.0f / 16.0f;
	private static final float DX = X2 - X1;
	private static final float Z1 = 2.0f / 16.0f;
	private static final float Z2 = 14.0f / 16.0f;
	private static final float DZ = Z2 - Z1;

	public BrewingStationTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(BrewingStationTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
			int combinedLightIn, int combinedOverlayIn)
	{
		FluidStack fluid = tile.getFluidStack();
		int capacity = tile.getFluidCapacity();
		
		if(!fluid.isEmpty() && capacity > 0)
		{
			FluidAttributes attr = fluid.getFluid().getAttributes();
			int color = attr.getColor(fluid);
			
			IVertexBuilder builder = bufferIn.getBuffer(RenderType.getSolid());
			
	        TextureAtlasSprite still = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(attr.getStillTexture(fluid));
	        TextureAtlasSprite flowing = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(attr.getFlowingTexture(fluid));
		
	        AxisAlignedBB box = BrewingStationBlock.SHAPE.getBoundingBox();
			
			float x = (float)box.getXSize();
			float y = (float)box.getYSize() * (float)fluid.getAmount() / (float)capacity;
			float z = (float)box.getZSize();
			
			matrixStackIn.push();
			matrixStackIn.translate(box.minX, box.minY, box.minZ);
			Matrix4f matrix = matrixStackIn.getLast().getMatrix();
			
			RenderUtils.putTexturedQuad(builder, matrix, still, x, y, z, Direction.UP, color, combinedLightIn, false);
			RenderUtils.putTexturedQuad(builder, matrix, still, x, y, z, Direction.NORTH, color, combinedLightIn, false);
			RenderUtils.putTexturedQuad(builder, matrix, still, x, y, z, Direction.SOUTH, color, combinedLightIn, false);
			RenderUtils.putTexturedQuad(builder, matrix, still, x, y, z, Direction.WEST, color, combinedLightIn, false);
			RenderUtils.putTexturedQuad(builder, matrix, still, x, y, z, Direction.EAST, color, combinedLightIn, false);
			
			matrixStackIn.pop();
		}
		
	}

}

package com.xevira.concoctions.client.ter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.xevira.concoctions.client.RenderUtils;
import com.xevira.concoctions.common.block.FilledCauldronBlock;
import com.xevira.concoctions.common.block.tile.FilledCauldronTile;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class FilledCauldronTileRenderer extends TileEntityRenderer<FilledCauldronTile> {
	private static final float X1 = 2.0f / 16.0f;
	private static final float X2 = 14.0f / 16.0f;
	private static final float DX = X2 - X1;
	private static final float Z1 = 2.0f / 16.0f;
	private static final float Z2 = 14.0f / 16.0f;
	private static final float DZ = Z2 - Z1;
	

	public FilledCauldronTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(FilledCauldronTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
			int combinedLightIn, int combinedOverlayIn)
	{
		BlockState state = tileEntityIn.getBlockState();
		int level = state.get(FilledCauldronBlock.LEVEL);
		
		if(level > 0)
		{
			FluidStack fluid = tileEntityIn.getPotionFluid();
			FluidAttributes attr = fluid.getFluid().getAttributes();
			int color = attr.getColor(fluid);
			
			IVertexBuilder builder = bufferIn.getBuffer(RenderType.getSolid());
			
	        TextureAtlasSprite still = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(attr.getStillTexture(fluid));
	        TextureAtlasSprite flowing = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(attr.getFlowingTexture(fluid));
		
			float y = (float)((level * 12.0D / 15) + 4.0D) / 16.0f;	// Height of fluid level
			
			
			matrixStackIn.push();
			matrixStackIn.translate(X1, y, Z1);
			Matrix4f matrix = matrixStackIn.getLast().getMatrix();
			
			RenderUtils.putTexturedQuad(builder, matrix, still, DX, 0.0f, DZ, Direction.UP, color, combinedLightIn, false);
			
			matrixStackIn.pop();
		}
	}
	

}

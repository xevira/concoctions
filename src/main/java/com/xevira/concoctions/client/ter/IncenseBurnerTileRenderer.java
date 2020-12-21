package com.xevira.concoctions.client.ter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.client.RenderUtils;
import com.xevira.concoctions.common.block.tile.IncenseBurnerTile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3f;

public class IncenseBurnerTileRenderer extends TileEntityRenderer<IncenseBurnerTile> {
	private static final float X1 = 7.0f / 16.0f;
	private static final float X2 = 9.0f / 16.0f;
	private static final float Y1 = 10.0f / 16.0f;
	private static final float Y2 = 12.0f / 16.0f;
	private static final float Z1 = 7.0f / 16.0f;
	private static final float Z2 = 9.0f / 16.0f;
	
	private static final Vector3f FROM = new Vector3f(X1,Y1,Z1);
	private static final Vector3f TO = new Vector3f(X2,Y2,Z2);
	
	private static final float U1 = 0.0f / 16.0f;
	private static final float U2 = 2.0f / 16.0f;
	private static final float U3 = 4.0f / 16.0f;
	private static final float V1 = 0.0f / 16.0f;
	private static final float V2 = 2.0f / 16.0f;
	
	private static final ResourceLocation torch = new ResourceLocation("concoctions:block/incense_burner_torch");

	public IncenseBurnerTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(IncenseBurnerTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
			int combinedLightIn, int combinedOverlayIn)
	{
		
		// No need to do anything if it doesn't have any incense
		if(!tileEntityIn.hasIncense()) return;
		
		Concoctions.GetLogger().info("combinedLightsIn = {}", Integer.toHexString(combinedLightIn));
		
        TextureAtlasSprite flame = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(torch);
		
        int light = combinedLightIn;
        Vector2f u, v;
        int color = tileEntityIn.getIncenseColor();
        if(tileEntityIn.isLit())
        {
        	//u = new Vector2f(U2, V1);
        	//v = new Vector2f(U3, V2);
        	light = 0x0000F0;//15728880;	// Full brightness because it is lit
        }
    	u = new Vector2f(U1, V1);
    	v = new Vector2f(U2, V2);
        
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getSolid());
        
		matrixStackIn.push();
//		matrixStackIn.translate(box.minX, box.minY, box.minZ);
		Matrix4f matrix = matrixStackIn.getLast().getMatrix();

        
        RenderUtils.putTexturedQuad(builder, matrix, flame, FROM, TO, u, v, Direction.UP, color, light, 0);
        RenderUtils.putTexturedQuad(builder, matrix, flame, FROM, TO, u, v, Direction.NORTH, color, light, 0);
        RenderUtils.putTexturedQuad(builder, matrix, flame, FROM, TO, u, v, Direction.SOUTH, color, light, 0);
        RenderUtils.putTexturedQuad(builder, matrix, flame, FROM, TO, u, v, Direction.EAST, color, light, 0);
        RenderUtils.putTexturedQuad(builder, matrix, flame, FROM, TO, u, v, Direction.WEST, color, light, 0);
        
        matrixStackIn.pop();
	}

}

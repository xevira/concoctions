package com.xevira.concoctions.client;

import java.util.HashMap;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.xevira.concoctions.common.fluids.PotionFluid;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState.AlphaState;
import net.minecraft.client.renderer.RenderState.TextureState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

public class ClientUtils {
	
	static HashMap<String, ResourceLocation> resourceMap = new HashMap<String, ResourceLocation>();


	public static Tessellator tes()
	{
		return Tessellator.getInstance();
	}
	
	public static Minecraft mc()
	{
		return Minecraft.getInstance();
	}
	
	public static void bindTexture(String path)
	{
		mc().getTextureManager().bindTexture(getResource(path));
	}

	public static void bindAtlas()
	{
		mc().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
	}
	
	public static TextureAtlasSprite getSprite(ResourceLocation rl)
	{
		return mc().getModelManager().getAtlasTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE).getSprite(rl);
	}
	
	public static ResourceLocation getResource(String path)
	{
		ResourceLocation rl = resourceMap.containsKey(path)?resourceMap.get(path): new ResourceLocation(path);
		if(!resourceMap.containsKey(path))
			resourceMap.put(path, rl);
		return rl;
	}
	
	public static RenderType getGui(ResourceLocation texture)
	{
		return RenderType.makeType(
				"gui_"+texture,
				DefaultVertexFormats.POSITION_COLOR_TEX,
				GL11.GL_QUADS,
				256,
				RenderType.State.getBuilder()
						.texture(new TextureState(texture, false, false))
						.alpha(new AlphaState(0.5F))
						.build(false)
		);
	}
	
	public static void drawRepeatedFluidSpriteGui(IRenderTypeBuffer buffer, MatrixStack transform, FluidStack fluid, float x, float y, float w, float h)
	{
		RenderType renderType = getGui(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
		IVertexBuilder builder = buffer.getBuffer(renderType);
		drawRepeatedFluidSprite(builder, transform, fluid, x, y, w, h);
	}

	public static void drawRepeatedFluidSprite(IVertexBuilder builder, MatrixStack transform, FluidStack fluid, float x, float y, float w, float h)
	{
		TextureAtlasSprite sprite = getSprite(fluid.getFluid().getAttributes().getStillTexture(fluid));
		int col = fluid.getFluid().getAttributes().getColor(fluid);
		int iW = sprite.getWidth();
		int iH = sprite.getHeight();
		if(iW > 0&&iH > 0)
			drawRepeatedSprite(builder, transform, x, y, w, h, iW, iH,
					sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(),
					(col >> 16&255)/255.0f, (col >> 8&255)/255.0f, (col&255)/255.0f, 1);
	}

	public static void drawRepeatedSprite(IVertexBuilder builder, MatrixStack transform, float x, float y, float w,
										  float h, int iconWidth, int iconHeight, float uMin, float uMax, float vMin, float vMax,
										  float r, float g, float b, float alpha)
	{
		int iterMaxW = (int)(w/iconWidth);
		int iterMaxH = (int)(h/iconHeight);
		float leftoverW = w%iconWidth;
		float leftoverH = h%iconHeight;
		float leftoverWf = leftoverW/(float)iconWidth;
		float leftoverHf = leftoverH/(float)iconHeight;
		float iconUDif = uMax-uMin;
		float iconVDif = vMax-vMin;
		for(int ww = 0; ww < iterMaxW; ww++)
		{
			for(int hh = 0; hh < iterMaxH; hh++)
				drawTexturedRect(builder, transform, x+ww*iconWidth, y+hh*iconHeight, iconWidth, iconHeight,
						r, g, b, alpha, uMin, uMax, vMin, vMax);
			drawTexturedRect(builder, transform, x+ww*iconWidth, y+iterMaxH*iconHeight, iconWidth, leftoverH,
					r, g, b, alpha, uMin, uMax, vMin, (vMin+iconVDif*leftoverHf));
		}
		if(leftoverW > 0)
		{
			for(int hh = 0; hh < iterMaxH; hh++)
				drawTexturedRect(builder, transform, x+iterMaxW*iconWidth, y+hh*iconHeight, leftoverW, iconHeight,
						r, g, b, alpha, uMin, (uMin+iconUDif*leftoverWf), vMin, vMax);
			drawTexturedRect(builder, transform, x+iterMaxW*iconWidth, y+iterMaxH*iconHeight, leftoverW, leftoverH,
					r, g, b, alpha, uMin, (uMin+iconUDif*leftoverWf), vMin, (vMin+iconVDif*leftoverHf));
		}
	}
	public static void handleGuiTank(MatrixStack transform, FluidStack fluid, int capacity, int x, int y, int w, int h, int oX, int oY, int oW, int oH, int mX, int mY, ResourceLocation originalTexture, List<ITextComponent> tooltip)
	{
		if(tooltip==null)
		{
			transform.push();
			IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
			if(fluid!=null&&fluid.getFluid()!=null)
			{
				int fluidHeight = (int)(h*(fluid.getAmount()/(float)capacity));
				drawRepeatedFluidSpriteGui(buffer, transform, fluid, x, y+h-fluidHeight, w, fluidHeight);
				RenderSystem.color3f(1, 1, 1);
			}
			int xOff = (w-oW)/2;
			int yOff = (h-oH)/2;
			RenderType renderType = getGui(originalTexture);
			drawTexturedRect(buffer.getBuffer(renderType), transform, x+xOff, y+yOff, oW, oH, 256f, oX, oX+oW, oY, oY+oH);
			buffer.finish(renderType);
			transform.pop();
		}
		else
		{
			if(mX >= x&&mX < x+w&&mY >= y&&mY < y+h)
				addFluidTooltip(fluid, tooltip, capacity);
		}
	}
	
	public static void drawTexturedRect(IVertexBuilder builder, MatrixStack transform, float x, float y, float w, float h,
			float r, float g, float b, float alpha, float u0, float u1, float v0, float v1) {
		Matrix4f mat = transform.getLast().getMatrix();
		builder.pos(mat, x, y+h, 0)
			.color(r, g, b, alpha)
			.tex(u0, v1)
			.overlay(OverlayTexture.NO_OVERLAY)
			.lightmap(0xf000f0)
			.normal(1, 1, 1)
			.endVertex();
		builder.pos(mat, x+w, y+h, 0)
			.color(r, g, b, alpha)
			.tex(u1, v1)
			.overlay(OverlayTexture.NO_OVERLAY)
			.lightmap(15728880)
			.normal(1, 1, 1)
			.endVertex();
		builder.pos(mat, x+w, y, 0)
			.color(r, g, b, alpha)
			.tex(u1, v0)
			.overlay(OverlayTexture.NO_OVERLAY)
			.lightmap(15728880)
			.normal(1, 1, 1)
			.endVertex();
		builder.pos(mat, x, y, 0)
			.color(r, g, b, alpha)
			.tex(u0, v0)
			.overlay(OverlayTexture.NO_OVERLAY)
			.lightmap(15728880)
			.normal(1, 1, 1)
			.endVertex();
	}
	
	public static void drawTexturedRect(IVertexBuilder builder, MatrixStack transform, int x, int y, int w, int h, float picSize,
			int u0, int u1, int v0, int v1) {
		drawTexturedRect(builder, transform, x, y, w, h, 1, 1, 1, 1, u0/picSize, u1/picSize, v0/picSize, v1/picSize);
	}
	
	public static IFormattableTextComponent applyFormat(ITextComponent component, TextFormatting... color)
	{
		Style style = component.getStyle();
		for(TextFormatting format : color)
			style = style.applyFormatting(format);
		return component.deepCopy().mergeStyle(style);
	}
	
	public static void addFluidTooltip(FluidStack fluid, List<ITextComponent> tooltip, int tankCapacity)
	{
		if(!fluid.isEmpty()) {
			tooltip.add(applyFormat(
					fluid.getDisplayName(),
					fluid.getFluid().getAttributes().getRarity(fluid).color
			));
			
			if( fluid.getFluid() instanceof PotionFluid ) {
				((PotionFluid)fluid.getFluid()).addInformation(fluid, tooltip);
			}
		} else
			tooltip.add(new TranslationTextComponent("gui.concoctions.empty"));

		if(tankCapacity > 0)
			tooltip.add(applyFormat(new StringTextComponent(fluid.getAmount()+"/"+tankCapacity+"mB"), TextFormatting.GRAY));
		else
			tooltip.add(applyFormat(new StringTextComponent(fluid.getAmount()+"mB"), TextFormatting.GRAY));
	}

}

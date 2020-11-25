package com.xevira.concoctions.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.client.ClientUtils;
import com.xevira.concoctions.common.container.BrewingStationContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BrewingStationScreen extends ContainerScreen<BrewingStationContainer> {
	private static final ResourceLocation background = new ResourceLocation(Concoctions.MOD_ID, "textures/gui/brewing_station.png");
	private static final int[] BUBBLELENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};

	private final BrewingStationContainer container;
	
	public BrewingStationScreen(BrewingStationContainer container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.container = container;
		
		// Override screen size
		this.xSize = 176;
		this.ySize = 180;
	}
	
	@Override
	public void init() {
		super.init();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1, 1, 1, 1);
        getMinecraft().getTextureManager().bindTexture(background);
        this.blit(stack, guiLeft, guiTop, 0, 0, xSize, ySize);
        int left = (this.width - this.xSize) / 2;
        int top = (this.height - this.ySize) / 2;
        
        // How much fuel is left in current charge
        int fuel = this.container.getRemainingFuel();
        int l = MathHelper.clamp((18 * fuel + 20 - 1) / 20, 0, 18);
        if (l > 0) {
           this.blit(stack, left + 60, top + 44, 176, 29, l, 4);
        }
        
        // Progress bar for brewing
        int brew = this.container.getBrewTime();
        int maxbrew = this.container.getMaxBrewTime();
        if( brew > 0 && maxbrew > 0 ) {
        	int w = (int)(28.0F * (1.0F - (float)brew / (float)maxbrew));
        	if( w > 0 ) {
        		this.blit(stack, left + 97, top + 21, 209, 0, w, 9);
        	}
        	
        	int h = BUBBLELENGTHS[(brew / 2 ) % 7];
        	if( h > 0) {
        		this.blit(stack, left + 63, top + 14 + 29 - h, 185, 29 - h, 12, h);
        	}
        }
        
        // Draw fluid bar
        ClientUtils.handleGuiTank(stack, this.container.tile.tankStorage.getFluid(), this.container.tile.tankStorage.getCapacity(), left+130, top+10, 14, 80, 176, 33, 14, 80, mouseX, mouseY, background, null);
	}
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
        int left = (this.width - this.xSize) / 2;
        int top = (this.height - this.ySize) / 2;
        
        // Tooltip for items
		this.renderHoveredTooltip(stack, mouseX, mouseY);

		// Tooltip for fluids		
		List<ITextComponent> tooltip = new ArrayList<>();
		if(mouseX >= (left + 130) && mouseX < (left + 144) &&
			mouseY >= (top + 10) && mouseY < (top + 90))
			ClientUtils.addFluidTooltip(this.container.tile.tankStorage.getFluid(), tooltip, this.container.tile.tankStorage.getCapacity());
		if( !tooltip.isEmpty() )
			GuiUtils.drawHoveringText(stack, tooltip, mouseX, mouseY, width, height, -1, font);
	}
	
    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
        Minecraft.getInstance().fontRenderer.drawString(stack, I18n.format("block.concoctions.brewing_station"), 6, 6, Color.DARK_GRAY.getRGB());
    }

}

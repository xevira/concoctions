package com.xevira.concoctions.client.gui.screen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.client.ClientUtils;
import com.xevira.concoctions.common.block.tile.SynthesizerTile;
import com.xevira.concoctions.common.container.SynthesizerContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

public class SynthesizerScreen extends ContainerScreen<SynthesizerContainer> implements IContainerListener {
	private static final ResourceLocation background = new ResourceLocation(Concoctions.MOD_ID, "textures/gui/synthesizer.png");

	private final SynthesizerContainer container;
	private TextFieldWidget nameField;
	private String nameText;
	
	private ExtendedButton redstoneButton;

	
	public SynthesizerScreen(SynthesizerContainer container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.container = container;
		
		// Override screen size
		this.xSize = 176;
		this.ySize = 211;
	}
	
	private void initFields()
	{
		this.redstoneButton = new ExtendedButton(guiLeft + 82, guiTop + 65, 20, 20, new StringTextComponent(""), b -> {
			int behavior = SynthesizerScreen.this.container.getRedstoneBehavior();
			
			if(behavior == SynthesizerTile.OFF_WHEN_POWERED)
				behavior = SynthesizerTile.ON_WHEN_POWERED;
			else if(behavior == SynthesizerTile.ON_WHEN_POWERED)
				behavior = SynthesizerTile.IGNORE_REDSTONE;
			else
				behavior = SynthesizerTile.OFF_WHEN_POWERED;
			
			SynthesizerScreen.this.container.setRedstoneBehavior(behavior);
		});
		this.children.add(redstoneButton);

		this.minecraft.keyboardListener.enableRepeatEvents(true);
		this.nameField = new TextFieldWidget(this.font, guiLeft + 41, guiTop + 110, 125, 12, new TranslationTextComponent("container.rename"));
		this.nameField.setCanLoseFocus(false);
		this.nameField.setTextColor(-1);
		this.nameField.setDisabledTextColour(-1);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(35);
		this.nameField.setText(this.container.getPotionName());
		this.nameField.setResponder(this::renameItem);
		this.nameText = this.container.getPotionName();
		this.children.add(this.nameField);
		this.setFocusedDefault(this.nameField);
	}

	@Override
	public void init() {
		super.init();
		this.initFields();
		this.container.addListener(this);
	}

	@Override
	public void onClose() {
		super.onClose();
		this.minecraft.keyboardListener.enableRepeatEvents(false);
		this.container.removeListener(this);
	}
	
	@Override
	public void tick() {
		super.tick();
		this.nameField.tick();
	}
	
	private void renameItem(String name) {
		if (!name.equals(this.nameText)) {
			this.nameText = name;
			this.container.updateItemName(name);
		}
	}
	
	private void drawMeter(int value, int maxValue, MatrixStack stack, int x, int y, int u, int v, int w, int h, boolean inverted, boolean orient, boolean flip)
	{
		if(value > 0 && maxValue > 0)
		{
			if(inverted)
				value = maxValue - value;

			int m;
			int mx, my;
			int mu, mv;
			if(orient)
			{
				m = h * value / maxValue;
				if(m <= 0)
					return;
				
				mx = x;
				mu = u;
				if(flip)
				{
					my = y + h - m;
					mv = v + h - m;
				}
				else
				{
					my = y;
					mv = v;
				}
				this.blit(stack, mx, my, mu, mv, w, m);
			}
			else
			{
				m = w * value / maxValue;
				if(m <= 0)
					return;
				
				my = y;
				mv = v;
				if(flip)
				{
					mx = x + w - m;
					mu = u + w - m;
				}
				else
				{
					mx = x;
					mu = u;
				}
				this.blit(stack, mx, my, mu, mv, m, h);
			}
			
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.color4f(1, 1, 1, 1);
		getMinecraft().getTextureManager().bindTexture(background);
		this.blit(stack, guiLeft, guiTop, 0, 0, xSize, ySize);
		this.blit(stack, guiLeft + 38, guiTop + 107, 0, ySize, 131, 16);
		
		// Synth Time: 59, 55 -> 0, ySize + 32 -> 66, 9
		drawMeter(this.container.getSynthTime(), this.container.getSynthMaxTime(), stack, guiLeft+59, guiTop+55, 0, ySize+32, 66, 9, true, false, false);

		// Burn Time: 28, 106 -> xSize+28, 0 -> 4, 18
		drawMeter(this.container.getEnergyBurn(), this.container.getEnergyBurnMax(), stack, guiLeft+28, guiTop+106, xSize+28, 0, 4, 18, false, true, true);
		
		// Energy: 10, 20 -> xSize+14, 0 -> 14, 80
		drawMeter(this.container.getEnergy(), this.container.getEnergyMax(), stack, guiLeft+10, guiTop+20, xSize+14, 0, 14, 80, false, true, true);

		// Input Tank: 40, 53 -> 14, 14
		ClientUtils.handleGuiFluid(stack, this.container.getInputFluid(), guiLeft + 40, guiTop + 53, 14, 14, mouseX, mouseY, background, null);

		// Output Tank: 130, 20 -> xSize, 0 -> 14, 80
		ClientUtils.handleGuiTank(stack, this.container.getOutputFluid(), this.container.getOutputCapacity(), guiLeft + 130, guiTop + 20, 14, 80, xSize, 0, 14, 80, mouseX, mouseY, background, null);
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		
        RenderSystem.disableBlend();
        this.nameField.render(stack, mouseX, mouseY, partialTicks);

        this.redstoneButton.render(stack, mouseX, mouseY, partialTicks);

        RenderSystem.enableBlend();
		getMinecraft().getTextureManager().bindTexture(background);
        int behavior = this.container.getRedstoneBehavior();
        this.blit(stack, guiLeft + 84, guiTop + 67, (16 * behavior) + 131, ySize, 16, 16);

        // Tooltip for items
		this.renderHoveredTooltip(stack, mouseX, mouseY);
		
		// Tooltip for fluids		
		List<ITextComponent> tooltip = new ArrayList<>();

		if(isPointInRegion(40, 53, 14, 14, mouseX, mouseY))
		{
			ClientUtils.addFluidTooltip(this.container.getInputFluid(), tooltip);
		}
		else if(isPointInRegion(130, 20, 14, 80, mouseX, mouseY))
		{
			ClientUtils.addFluidTooltip(this.container.getOutputFluid(), tooltip, this.container.getOutputCapacity());
		}
		else if(isPointInRegion(10, 20, 14, 80, mouseX, mouseY))
		{
			tooltip.add(new TranslationTextComponent("gui.concoctions.energy_stored_label"));
			tooltip.add(new TranslationTextComponent("gui.concoctions.energy_stored", this.container.getEnergy(), this.container.getEnergyMax()).mergeStyle(TextFormatting.GREEN));
			
			int burnTime = this.container.getEnergyBurn();
			if(burnTime > 0)
			{
				tooltip.add(new StringTextComponent(""));
				if( burnTime > 20)
					tooltip.add(new TranslationTextComponent("gui.concoctions.burn_seconds_remaining", (burnTime + 19) / 20).mergeStyle(TextFormatting.YELLOW));
				else
					tooltip.add(new TranslationTextComponent("gui.concoctions.burn_second_remaining").mergeStyle(TextFormatting.YELLOW));
			}
		}
		else if(isPointInRegion(82, 65, 20, 20, mouseX, mouseY))
		{
			if(behavior == SynthesizerTile.OFF_WHEN_POWERED)
				tooltip.add(new TranslationTextComponent("gui.concoctions.off_when_powered"));
			else if(behavior == SynthesizerTile.ON_WHEN_POWERED)
				tooltip.add(new TranslationTextComponent("gui.concoctions.on_when_powered"));
			else
				tooltip.add(new TranslationTextComponent("gui.concoctions.ignores_redstone"));
		}
		else if(isPointInRegion(59, 55, 66, 9, mouseX, mouseY))
		{
			tooltip.add(new TranslationTextComponent("gui.concoctions.synthesizing.get_recipes").mergeStyle(TextFormatting.WHITE));
			this.container.getSynthesizingInformation(tooltip);
		}
		
		if( !tooltip.isEmpty() )
			GuiUtils.drawHoveringText(stack, tooltip, mouseX, mouseY, width, height, -1, font);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		ClientUtils.drawCenterStringNoShadow(stack, fontRenderer, new TranslationTextComponent("block.concoctions.synthesizer"), xSize / 2, 6, Color.DARK_GRAY.getRGB());
	}


	@Override
	public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
		// TODO Auto-generated method stub
		
	}
}

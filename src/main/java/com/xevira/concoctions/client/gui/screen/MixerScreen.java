package com.xevira.concoctions.client.gui.screen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.client.ClientUtils;
import com.xevira.concoctions.client.RenderUtils;
import com.xevira.concoctions.client.gui.widgets.SliderVertical;
import com.xevira.concoctions.common.block.tile.MixerTile;
import com.xevira.concoctions.common.container.MixerContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.client.gui.widget.Slider;

public class MixerScreen extends ContainerScreen<MixerContainer> implements IContainerListener, Slider.ISlider {
	private static final ResourceLocation background = new ResourceLocation(Concoctions.MOD_ID,
			"textures/gui/mixer.png");

	private static final int COLUMNS[] = new int[] {
			10,
			50,
			90,
			130,
			178
		};
	
	private static final String LABELS[] = new String[] {
			"gui.concoctions.tank_front",
			"gui.concoctions.tank_back",
			"gui.concoctions.tank_left",
			"gui.concoctions.tank_right",
			"gui.concoctions.tank_center"
	};

	private final MixerContainer container;
	private SliderVertical sliderTanks[];
	private boolean wasValveOpen;

	public MixerScreen(MixerContainer container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.container = container;

		// Override screen size
		this.xSize = 216;
		this.ySize = 196;

		this.sliderTanks = new SliderVertical[MixerTile.TOTAL_INPUTS];
	}

	private void initFields() {
		int left = (this.width - this.xSize) / 2;
		int top = (this.height - this.ySize) / 2;
		
		this.wasValveOpen = this.container.isCenterValveOpen();

		for (int i = 0; i < this.sliderTanks.length; i++) {
			this.sliderTanks[i] = new SliderVertical(left + COLUMNS[i] + 14, top + 43, 18, 46, StringTextComponent.EMPTY,
					StringTextComponent.EMPTY, 0, 100, 5, this.container.getValve(i), false, false, s -> {
					}, this);
			this.sliderTanks[i].active = !this.wasValveOpen;
			addButton(this.sliderTanks[i]);
		}
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
		this.container.removeListener(this);
	}

	@Override
	public void tick() {
		super.tick();
		
		boolean isValveOpen = this.container.isCenterValveOpen();
		if(this.wasValveOpen != isValveOpen)
		{
			this.wasValveOpen = isValveOpen;

			for(int i = 0; i < this.sliderTanks.length; i++)
				this.sliderTanks[i].active = !this.wasValveOpen;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1, 1, 1, 1);
		getMinecraft().getTextureManager().bindTexture(background);
		this.blit(stack, guiLeft, guiTop, 0, 0, xSize, ySize);
		int left = (this.width - this.xSize) / 2;
		int top = (this.height - this.ySize) / 2;

		int mixingTime = this.container.getMixingTime();
		if (mixingTime > 0) {
			int h = 80 * mixingTime / 400;

			if (h > 0)
				fill(stack, left + 171, top + 106 - h, left + 172, top + 105, 0x3F7FFF);
		}
		
		for(int i = 0; i < MixerTile.TOTAL_TANKS; i++)
		{
			ClientUtils.handleGuiTank(stack, this.container.getFluid(i), this.container.getCapacity(i), left + COLUMNS[i], top + 48, 10, 80, xSize, 36, 10, 80, mouseX, mouseY, background, null);
		}
		
		this.blit(stack, left + 192, top + 43, xSize + (this.container.isCenterValveOpen() ? 18 : 0), 0, 18, 18);
		this.blit(stack, left + 192, top + 71, xSize + ((this.container.getMixerStatus() == MixerTile.ERROR_NONE) ? 18 : 0), 18, 18, 18);
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		
        // Tooltip for items
		this.renderHoveredTooltip(stack, mouseX, mouseY);

		// Tooltip for fluids		
		List<ITextComponent> tooltip = new ArrayList<>();

		if( isPointInRegion(192, 43, 18, 18, mouseX, mouseY) )
		{
			tooltip.add(new TranslationTextComponent(this.container.isCenterValveOpen() ? "gui.concoctions.turn_off" : "gui.concoctions.turn_on"));
		}
		else if(isPointInRegion(192, 71, 18, 18, mouseX, mouseY))
		{
			int status = this.container.getMixerStatus();
			
			if(status != MixerTile.ERROR_NONE)
				tooltip.add(new TranslationTextComponent("gui.concoctions.mixer.error" + Integer.toString(status)));				
		}
		else
		{
			for(int i = 0; i < this.sliderTanks.length; i++)
			{
				Slider slider = this.sliderTanks[i];
				
				if(isPointInRegion(COLUMNS[i] + 14, 43, 18, 46, mouseX, mouseY))
				{
					int value = slider.getValueInt();
					
					if(value <= 0)
						tooltip.add(new TranslationTextComponent("gui.concoctions.valve_closed"));
					else if(value >= 100)
						tooltip.add(new TranslationTextComponent("gui.concoctions.valve_fully"));
					else
						tooltip.add(new TranslationTextComponent("gui.concoctions.valve_opened", value));
				}
			}
			
			for(int i = 0; i < MixerTile.TOTAL_TANKS; i++)
			{
				if(isPointInRegion(COLUMNS[i], 26, 10, 80, mouseX, mouseY))
				{
					ClientUtils.addFluidTooltip(this.container.reservoirs[i].getFluid(), tooltip, this.container.reservoirs[i].getCapacity());
				}
				
			}
		}
		if( !tooltip.isEmpty() )
			GuiUtils.drawHoveringText(stack, tooltip, mouseX, mouseY, width, height, -1, font);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
		int left = (this.width - this.xSize) / 2;
		int top = (this.height - this.ySize) / 2;

		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		fontRenderer.drawString(stack, I18n.format("block.concoctions.mixer"), 17, 6, Color.DARK_GRAY.getRGB());

		for (int i = 0; i < LABELS.length; i++) {
			ClientUtils.drawCenterStringNoShadow(stack, fontRenderer,
					new TranslationTextComponent(LABELS[i]), COLUMNS[i] + 15, 14, Color.GRAY.getRGB());
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(isPointInRegion(192, 43, 18, 18, mouseX, mouseY))
		{
			if(this.container.getMixerStatus() == MixerTile.ERROR_NONE)
			{
				this.container.setValve(MixerTile.CENTER_TANK, this.container.isCenterValveOpen() ? 0 : 1);
				Concoctions.GetLogger().info("Center On set to {}", !this.container.isCenterValveOpen());
			}
			return true;
		}
			
		
		return super.mouseClicked(mouseX, mouseY, button);
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

	private void handleChangedSlider(int slot, Slider slider) {
		if (slider.dragging) {
			slider.dragging = false;

			this.container.setValve(slot,  slider.getValueInt());
			Concoctions.GetLogger().info("Slider[{}]: {}", slot, slider.getValueInt());
		}
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		for (int i = 0; i < this.sliderTanks.length; i++)
			handleChangedSlider(i, this.sliderTanks[i]);
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void onChangeSliderValue(Slider slider) {
	}
}

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
import net.minecraft.client.gui.widget.TextFieldWidget;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.client.gui.widget.Slider;

public class MixerScreen extends ContainerScreen<MixerContainer> implements IContainerListener, Slider.ISlider {
	private static final ResourceLocation background = new ResourceLocation(Concoctions.MOD_ID, "textures/gui/mixer.png");

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
	private TextFieldWidget nameField;
	private String nameText;
	private boolean wasValveOpen;

	public MixerScreen(MixerContainer container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.container = container;

		// Override screen size
		this.xSize = 217;
		this.ySize = 219;

		this.sliderTanks = new SliderVertical[MixerTile.TOTAL_INPUTS];
	}

	private void initFields() {
		this.wasValveOpen = this.container.isCenterValveOpen();

		for (int i = 0; i < this.sliderTanks.length; i++) {
			this.sliderTanks[i] = new SliderVertical(guiLeft + COLUMNS[i] + 14, guiTop + 46, 18, 46, StringTextComponent.EMPTY,
					StringTextComponent.EMPTY, 0, 100, 5, this.container.getValve(i), false, false, s -> {
					}, this);
			this.sliderTanks[i].active = !this.wasValveOpen;
			addButton(this.sliderTanks[i]);
		}
		
		this.minecraft.keyboardListener.enableRepeatEvents(true);
		this.nameField = new TextFieldWidget(this.font, guiLeft + xSize - 120, guiTop + 119, 109, 12, new TranslationTextComponent("container.rename"));
		this.nameField.setCanLoseFocus(false);
		this.nameField.setTextColor(-1);
		this.nameField.setDisabledTextColour(-1);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(35);
		this.nameField.setText(this.container.getPotionName());
		this.nameField.setEnabled(!this.wasValveOpen);
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

	private void renameItem(String name) {
		if (!name.equals(this.nameText)) {
			this.nameText = name;
			this.container.updateItemName(name);
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.nameField.tick();
		
		boolean isValveOpen = this.container.isCenterValveOpen();
		if(this.wasValveOpen != isValveOpen)
		{
			this.nameField.setEnabled(wasValveOpen);
			this.setListener(this.nameField);
			
			for(int i = 0; i < this.sliderTanks.length; i++)
				this.sliderTanks[i].active = this.wasValveOpen;
			
			this.wasValveOpen = isValveOpen;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1, 1, 1, 1);
		getMinecraft().getTextureManager().bindTexture(background);
		this.blit(stack, guiLeft, guiTop, 0, 0, xSize, ySize);

		int mixingTime = this.container.getMixingTime();
		int mixingTimeTotal = this.container.getMixingTimeTotal();
		if (mixingTime > 0 && mixingTimeTotal > 0) {
			int h = 80 * mixingTime / mixingTimeTotal;

			if (h > 0)
				fill(stack, guiLeft + 171, guiTop + 109 - h, guiLeft + 173, guiTop + 108, 0xFF3F7FFF);
		}
		
        // Draw Name Field background
        this.blit(stack, guiLeft + xSize - 123, guiTop + 116, 0, this.ySize + (this.container.isCenterValveOpen() ? 16 : 0), 116, 16);
		
		for(int i = 0; i < MixerTile.TOTAL_TANKS; i++)
		{
			ClientUtils.handleGuiTank(stack, this.container.getFluid(i), this.container.getCapacity(i), guiLeft + COLUMNS[i], guiTop + 29, 10, 80, xSize, 36, 10, 80, mouseX, mouseY, background, null);
		}
		
		this.blit(stack, guiLeft + 192, guiTop + 46, xSize + (this.container.isCenterValveOpen() ? 18 : 0), 0, 18, 18);
		this.blit(stack, guiLeft + 192, guiTop + 74, xSize + ((this.container.getMixerStatus() == MixerTile.ERROR_NONE) ? 18 : 0), 18, 18, 18);
	}
	
	public void renderNameField(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.nameField.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		
        RenderSystem.disableBlend();
        this.renderNameField(stack, mouseX, mouseY, partialTicks);
		
        // Tooltip for items
		this.renderHoveredTooltip(stack, mouseX, mouseY);

		// Tooltip for fluids		
		List<ITextComponent> tooltip = new ArrayList<>();

		if( isPointInRegion(192, 46, 18, 18, mouseX, mouseY) )
		{
			tooltip.add(new TranslationTextComponent(this.container.isCenterValveOpen() ? "gui.concoctions.turn_off" : "gui.concoctions.turn_on"));
		}
		else if(isPointInRegion(192, 74, 18, 18, mouseX, mouseY))
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
				
				if(isPointInRegion(COLUMNS[i] + 14, 46, 18, 46, mouseX, mouseY))
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
			
			for(int i = 0; i < MixerTile.TOTAL_INPUTS; i++)
			{
				if(isPointInRegion(COLUMNS[i], 29, 10, 80, mouseX, mouseY))
				{
					ClientUtils.addFluidTooltip(this.container.reservoirs[i].getFluid(), tooltip, this.container.reservoirs[i].getCapacity());
				}
			}
			
			if(isPointInRegion(COLUMNS[MixerTile.CENTER_TANK], 29, 10, 80, mouseX, mouseY))
			{
				ClientUtils.addFluidTooltip(this.container.reservoirs[MixerTile.CENTER_TANK].getFluid(), tooltip, this.container.reservoirs[MixerTile.CENTER_TANK].getCapacity());
				
				// Show target fluid
				FluidStack targetFluid = this.container.getTargetFluid();
				if(!targetFluid.isEmpty())
				{
					tooltip.add(new StringTextComponent(""));
					tooltip.add(new TranslationTextComponent("gui.concoctions.mixing_result"));
					ClientUtils.addFluidTooltip(targetFluid, tooltip, 0);
				}
			}
			
		}
		if( !tooltip.isEmpty() )
			GuiUtils.drawHoveringText(stack, tooltip, mouseX, mouseY, width, height, -1, font);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		ClientUtils.drawCenterStringNoShadow(stack, fontRenderer, new TranslationTextComponent("block.concoctions.mixer"), xSize / 2, 6, Color.DARK_GRAY.getRGB());
		
		//fontRenderer.drawString(stack, I18n.format("block.concoctions.mixer"), 17, 6, Color.DARK_GRAY.getRGB());

		for (int i = 0; i < LABELS.length; i++) {
			ClientUtils.drawCenterStringNoShadow(stack, fontRenderer,
					new TranslationTextComponent(LABELS[i]), COLUMNS[i] + 15, 17, Color.GRAY.getRGB());
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(isPointInRegion(192, 46, 18, 18, mouseX, mouseY))
		{
			if(this.container.getMixerStatus() == MixerTile.ERROR_NONE)
			{
				this.container.setValve(MixerTile.CENTER_TANK, this.container.isCenterValveOpen() ? 0 : 1);
				//Concoctions.GetLogger().info("Center On set to {}", !this.container.isCenterValveOpen());
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
			//Concoctions.GetLogger().info("Slider[{}]: {}", slot, slider.getValueInt());
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

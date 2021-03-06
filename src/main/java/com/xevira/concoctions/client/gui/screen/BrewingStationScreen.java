package com.xevira.concoctions.client.gui.screen;

import com.google.gson.JsonParseException;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.client.ClientUtils;
import com.xevira.concoctions.common.block.tile.BrewingStationTile;
import com.xevira.concoctions.common.container.BrewingStationContainer;
import com.xevira.concoctions.common.network.PacketHandler;
import com.xevira.concoctions.common.network.packets.PacketPotionRename;
import com.xevira.concoctions.common.utils.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.client.CRenameItemPacket;
import net.minecraft.util.NonNullList;
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

public class BrewingStationScreen extends ContainerScreen<BrewingStationContainer> implements IContainerListener {
	private static final ResourceLocation background = new ResourceLocation(Concoctions.MOD_ID, "textures/gui/brewing_station.png");
	private static final int[] BUBBLELENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};
	private static final int OUTPUT_SLOT = BrewingStationTile.Slots.BOTTLE_OUT.getId(); 

	private final BrewingStationContainer container;
	private TextFieldWidget nameField;
	private String nameText;
	
	private boolean hadPotion = false;
	
	
	public BrewingStationScreen(BrewingStationContainer container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.container = container;
		
		// Override screen size
		this.xSize = 176;
		this.ySize = 199;
	}
	
	private void initFields() {
		this.minecraft.keyboardListener.enableRepeatEvents(true);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.nameField = new TextFieldWidget(this.font, i + 10, j + 100, 109, 12, new TranslationTextComponent("container.rename"));
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
	public void tick() {
		super.tick();
		this.nameField.tick();
		
		boolean hasPotion = this.container.hasPotionFluid();
		
		if( hasPotion != this.hadPotion )
		{
			this.hadPotion = hasPotion;
			this.nameField.setEnabled(hasPotion);
			this.setListener(this.nameField);
		}
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
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 256) {
			this.minecraft.player.closeScreen();
		}

		return !this.nameField.keyPressed(keyCode, scanCode, modifiers) && !this.nameField.canWrite() ? super.keyPressed(keyCode, scanCode, modifiers) : true;
	}

	public void renderNameField(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.nameField.render(matrixStack, mouseX, mouseY, partialTicks);
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
           this.blit(stack, left + 60, top + 87, 176, 29, l, 4);
        }
        
        // Draw Name Field background
        this.blit(stack, left + 7, top + 96, 0, this.ySize + (this.isOutputPotionItemStack() ? 0 : 16), 116, 16);
        
        // Progress bar for brewing
        int brew = this.container.getBrewTime();
        int maxbrew = this.container.getMaxBrewTime();
        if( brew > 0 && maxbrew > 0 ) {
        	int w = (int)(28.0F * (1.0F - (float)brew / (float)maxbrew));
        	if( w > 0 ) {
        		this.blit(stack, left + 97, top + 68, 209, 0, w, 9);
        	}
        	
        	int h = BUBBLELENGTHS[(brew / 2 ) % 7];
        	if( h > 0) {
        		this.blit(stack, left + 63, top + 57 + 29 - h, 185, 29 - h, 12, h);
        	}
        }
        
        // Draw fluid bar
        ClientUtils.handleGuiTank(stack, this.container.tile.tankStorage.getFluid(), this.container.tile.tankStorage.getCapacity(), left+130, top+29, 14, 80, 176, 33, 14, 80, mouseX, mouseY, background, null);
	}
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
        int left = (this.width - this.xSize) / 2;
        int top = (this.height - this.ySize) / 2;
        
        RenderSystem.disableBlend();
        this.renderNameField(stack, mouseX, mouseY, partialTicks);
        
        // Tooltip for items
		this.renderHoveredTooltip(stack, mouseX, mouseY);

		// Tooltip for fluids		
		List<ITextComponent> tooltip = new ArrayList<>();
		if(mouseX >= (left + 130) && mouseX < (left + 144) &&
			mouseY >= (top + 29) && mouseY < (top + 109))
			ClientUtils.addFluidTooltip(this.container.tile.tankStorage.getFluid(), tooltip, this.container.tile.tankStorage.getCapacity());
		if( !tooltip.isEmpty() )
			GuiUtils.drawHoveringText(stack, tooltip, mouseX, mouseY, width, height, -1, font);
	}
	
    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
        Minecraft.getInstance().fontRenderer.drawString(stack, I18n.format("block.concoctions.brewing_station"), 17, 6, Color.DARK_GRAY.getRGB());
    }

	@Override
	public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
		this.sendSlotContents(containerToSend, OUTPUT_SLOT, containerToSend.getSlot(OUTPUT_SLOT).getStack());
	}

	@Override
	/**
	 * Sends the contents of an inventory slot to the client-side Container. This doesn't have to match the actual
	 * contents of that slot.
	 */
	public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
		if (slotInd == OUTPUT_SLOT) {
			this.nameField.setEnabled(Utils.isPotionItemStack(stack));
			this.setListener(this.nameField);
		}
	}
	
	@Override
	public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
		// Do Nothing
	}

	private boolean isOutputPotionItemStack()
	{
		if( this.container.hasPotionFluid()) return true;
		
		Slot slot = this.container.getSlot(OUTPUT_SLOT); 
		
		if(!slot.getHasStack()) return false;
		
		return Utils.isPotionItemStack(slot.getStack());
	}

}

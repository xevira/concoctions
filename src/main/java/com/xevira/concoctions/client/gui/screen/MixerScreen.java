package com.xevira.concoctions.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.container.MixerContainer;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class MixerScreen extends ContainerScreen<MixerContainer> implements IContainerListener
{
	private static final ResourceLocation background = new ResourceLocation(Concoctions.MOD_ID, "textures/gui/mixer.png");

	private final MixerContainer container;
	
	public MixerScreen(MixerContainer container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.container = container;
		
		// Override screen size
		this.xSize = 176;
		this.ySize = 180;
	}
	
	private void initFields()
	{
		this.minecraft.keyboardListener.enableRepeatEvents(true);
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
	}

	
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
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

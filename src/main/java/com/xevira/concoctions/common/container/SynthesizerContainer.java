package com.xevira.concoctions.common.container;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.block.tile.SynthesizerTile;
import com.xevira.concoctions.common.handlers.ItemStackHandlerEx;
import com.xevira.concoctions.common.network.PacketHandler;
import com.xevira.concoctions.common.network.packets.PacketPotionRename;
import com.xevira.concoctions.common.network.packets.PacketRedstoneBehavior;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;

public class SynthesizerContainer extends Container implements IContainerPotionRenamer, IContainerRedstoneBehavior {

	private String newPotionName = "";
	public final IIntArray data;
	public final SynthesizerTile tile;

	public SynthesizerContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
		this((SynthesizerTile) playerInventory.player.world.getTileEntity(extraData.readBlockPos()), null, windowId, playerInventory);
	}
	
	public SynthesizerContainer(@Nullable SynthesizerTile tile, IIntArray synthData, int windowId, PlayerInventory playerInventory) {
		super(Registry.SYNTHESIZER_CONTAINER.get(), windowId);
		
		this.tile = tile;
		if(synthData != null)
		{
			this.data = synthData;
		}
		else
		{
			this.data = new IntArray(tile.synthesizerData.size());
			for(int i = tile.synthesizerData.size() - 1; i >= 0; i--)
				this.data.set(i, tile.synthesizerData.get(i));
		}
		this.newPotionName = tile.getPotionName();
		this.setup(playerInventory);
		
		trackIntArray(this.data);
	}
	
	public void setup(PlayerInventory inventory)
	{
		addSlot(new SynthesizerSlot(this.tile.getEnergyInput(), 0, 9, 107));
		addSlot(new SynthesizerSlot(this.tile.getInputTankItem(), 0, 39, 18));
		addSlot(new SynthesizerSlot(this.tile.getOutputs(), 0, 39, 86));
		addSlot(new SynthesizerSlot(this.tile.getCatalyst(), 0, 84, 37));
		addSlot(new SynthesizerSlot(this.tile.getOutputTankItem(), 0, 152, 18));
		addSlot(new SynthesizerSlot(this.tile.getOutputs(), 1, 152, 86));
		
		// Player Inventory
        //   Hotbar
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 187));
        }
        //   Main inventory
        for (int row = 1; row < 4; ++ row) {
            for (int col = 0; col < 9; ++ col) {
                addSlot(new Slot(inventory, col + row * 9, 8 + col * 18, row * 18 + 111));
            }
        }
	}


	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
        BlockPos pos = this.tile.getPos();
        return this.tile != null && !this.tile.isRemoved() && playerIn.getDistanceSq(new Vector3d(pos.getX(), pos.getY(), pos.getZ()).add(0.5D, 0.5D, 0.5D)) <= 64D;
	}
	
	public String getPotionName()
	{
		return this.tile.getPotionName();
	}

	@Override
	public void renameItemName()
	{
		this.tile.updatePotionName(this.newPotionName);		
	}

	@Override
	public void updateItemName(String newName)
	{
		if(this.newPotionName == null || !newName.equals(this.newPotionName))
		{
			this.newPotionName = newName;
			this.renameItemName();

			if( this.tile.hasWorld() && this.tile.getWorld().isRemote )
			{
				PacketHandler.sendToServer(new PacketPotionRename(this.newPotionName));
			}
		}
	}
	
	public int getSynthTime()
	{
		return this.data.get(0);
	}
	
	public int getSynthMaxTime()
	{
		return this.data.get(1);
	}
	
	public int getEnergy()
	{
		return this.data.get(2);
	}
	
	public int getEnergyMax()
	{
		return this.data.get(3);
	}
	
	public int getEnergyBurn()
	{
		return this.data.get(4);
	}
	
	public int getEnergyBurnMax()
	{
		return this.data.get(5);
	}

	public FluidStack getInputFluid()
	{
		return this.tile.getInputFluid();
	}
	
	public int getInputCapacity()
	{
		return this.tile.getInputCapacity();
	}

	public FluidStack getOutputFluid()
	{
		return this.tile.getOutputFluid();
	}
	
	public int getOutputCapacity()
	{
		return this.tile.getOutputCapacity();
	}
	
	public int getRedstoneBehavior()
	{
		return this.tile.getRedstoneBehavior();
	}
	
	public void setRedstoneBehavior(int behavior)
	{
		Concoctions.GetLogger().info("setRedstoneBehavior({})", behavior);
		this.tile.setRedstoneBehavior(behavior);
		
		if(this.tile.hasWorld() && this.tile.getWorld().isRemote)
		{
			PacketHandler.sendToServer(new PacketRedstoneBehavior(behavior));
		}
	}
	
	public void getSynthesizingInformation(List<ITextComponent> tooltip)
	{
		this.tile.getSynthesizingInformation(tooltip);
	}

	public static class SynthesizerSlot extends SlotItemHandler {
		protected final ItemStackHandlerEx itemHandlerEx;
		
		public SynthesizerSlot(ItemStackHandlerEx itemHandler, int slot, int xPos, int yPos) {
			super(itemHandler, slot, xPos, yPos);
			this.itemHandlerEx = itemHandler;
		}

	    @Override
	    public boolean canTakeStack(PlayerEntity playerIn)
	    {
	        return true;
	    }
	    
	    @Override
	    @Nonnull
	    public ItemStack decrStackSize(int amount)
	    {
	        return this.itemHandlerEx.forceExtractItem(this.getSlotIndex(), amount, false);
	    }
	}
	
}

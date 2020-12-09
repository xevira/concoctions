package com.xevira.concoctions.common.container;

import javax.annotation.Nonnull;

import com.xevira.concoctions.common.block.tile.MixerTile;
import com.xevira.concoctions.common.handlers.ItemStackHandlerEx;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.items.SlotItemHandler;

public class MixerContainer extends Container {

	public MixerTile tile;
	
	protected MixerContainer(ContainerType<?> type, int id) {
		super(type, id);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
        BlockPos pos = this.tile.getPos();
        return this.tile != null && !this.tile.isRemoved() && playerIn.getDistanceSq(new Vector3d(pos.getX(), pos.getY(), pos.getZ()).add(0.5D, 0.5D, 0.5D)) <= 64D;
	}
	
	static class MixerSlot extends SlotItemHandler {
		protected final ItemStackHandlerEx itemHandlerEx;
		
		public MixerSlot(ItemStackHandlerEx itemHandler, int slot, int xPos, int yPos) {
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

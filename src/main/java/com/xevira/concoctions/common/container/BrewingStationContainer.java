package com.xevira.concoctions.common.container;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.xevira.concoctions.common.block.tile.BrewingStationTile;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class BrewingStationContainer extends Container {
	private static final int SLOTS = 4;
	private static final int DATA_SIZE = 5;
	
    public final IIntArray data;
    public ItemStackHandler handler;
    
    public BrewingStationTile tile;
    
	public BrewingStationContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
		this((BrewingStationTile) playerInventory.player.world.getTileEntity(extraData.readBlockPos()), new IntArray(DATA_SIZE), windowId, playerInventory, new ItemStackHandler(SLOTS));
	}
	
	public BrewingStationContainer(@Nullable BrewingStationTile tile, IIntArray brewingStationData, int windowId, PlayerInventory playerInventory, ItemStackHandler handler) {
		super(Registry.BREWING_STATION_CONTAINER.get(), windowId);
		assert(brewingStationData.size() == DATA_SIZE);
		
        this.handler = handler;
        this.tile = tile;

        this.data = brewingStationData;
        this.setup(playerInventory);

        trackIntArray(brewingStationData);
	}

	public void setup(PlayerInventory inventory) {
		// Brewing Station
		addSlot(new BrewingStationSlot(handler, BrewingStationTile.Slots.FUEL, 17, 17));
		addSlot(new BrewingStationSlot(handler, BrewingStationTile.Slots.ITEM, 79, 17));
		addSlot(new BrewingStationSlot(handler, BrewingStationTile.Slots.BOTTLE_IN, 152, 8));
		addSlot(new BrewingStationSlot(handler, BrewingStationTile.Slots.BOTTLE_OUT, 152, 76));

		// Player Inventory
        //   Hotbar
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 156));
        }
        //   Main inventory
        for (int row = 1; row < 4; ++ row) {
            for (int col = 0; col < 9; ++ col) {
                addSlot(new Slot(inventory, col + row * 9, 8 + col * 18, row * 18 + 80));
            }
        }
	}

	@Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack currentStack = slot.getStack();
            itemstack = currentStack.copy();

            if (index < SLOTS) {
                if (! this.mergeItemStack(currentStack, SLOTS, this.inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (! this.mergeItemStack(currentStack, 0, SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (currentStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }
	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
        BlockPos pos = this.tile.getPos();
        return this.tile != null && !this.tile.isRemoved() && playerIn.getDistanceSq(new Vector3d(pos.getX(), pos.getY(), pos.getZ()).add(0.5D, 0.5D, 0.5D)) <= 64D;
	}

	static class BrewingStationSlot extends SlotItemHandler {
		public BrewingStationSlot(IItemHandler itemHandler, BrewingStationTile.Slots slot, int xPos, int yPos) {
			super(itemHandler, slot.getId(), xPos, yPos);
		}
		
		@Override
		public boolean isItemValid(@Nonnull ItemStack stack) {
			if( getSlotIndex() == BrewingStationTile.Slots.FUEL.getId() )
				return super.isItemValid(stack) && stack.getItem() == Items.BLAZE_POWDER;
			
			if( getSlotIndex() == BrewingStationTile.Slots.BOTTLE_IN.getId() )
				return super.isItemValid(stack) && (
		    		stack.getItem() == Items.GLASS_BOTTLE ||
					stack.getItem() == Registry.SPLASH_BOTTLE.get() ||
					stack.getItem() == Registry.LINGERING_BOTTLE.get() ||
		    		stack.getItem() == Items.POTION ||
		    		stack.getItem() == Items.BUCKET ||
	        		stack.getItem() == Items.LAVA_BUCKET ||
	        		stack.getItem() == Items.WATER_BUCKET);
		
		    // Output only
		    if( getSlotIndex() == BrewingStationTile.Slots.BOTTLE_OUT.getId() )
		    	return false;
		
		    return super.isItemValid(stack);
		}
	}
	
	public int getBrewTime() {
		return this.data.get(0);
	}
	
	public int getRemainingFuel() {
		return this.data.get(1);
	}
	
	public int getFluidColor() {
		return this.data.get(2);
	}
	
	public int getFluidAmount() {
		return this.data.get(3);
	}
	
	public int getMaxBrewTime() {
		return this.data.get(4);
	}
	
}

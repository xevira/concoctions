package com.xevira.concoctions.common.container;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.block.tile.BrewingStationTile;
import com.xevira.concoctions.common.block.tile.BrewingStationTile.Slots;
import com.xevira.concoctions.common.handlers.*;
import com.xevira.concoctions.common.network.PacketHandler;
import com.xevira.concoctions.common.network.packets.PacketPotionRename;
import com.xevira.concoctions.common.utils.Utils;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class BrewingStationContainer extends Container {
	private static final int SLOTS = BrewingStationTile.INV_SLOTS;
	private static final int DATA_SIZE = 6;
	private static final int OUTPUT_SLOT = BrewingStationTile.Slots.BOTTLE_OUT.getId();
	
	// Annoying hack to allow this container to see the list of listeners as it is private in the parent class and there is no getListeners() function
    private final List<IContainerListener> copy_listeners = Lists.newArrayList();

	private String newPotionName = "";
    public final IIntArray data;
    public BrewingFuelItemStackHandler invFuel;
    public BrewingQueueItemStackHandler invItems;
    public BrewingBottleInItemStackHandler invBottleIn;
    public BrewingBottleOutItemStackHandler invBottleOut;
	private boolean hasPotion = false;
    
    public BrewingStationTile tile;
    
	public BrewingStationContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
		this((BrewingStationTile) playerInventory.player.world.getTileEntity(extraData.readBlockPos()), new IntArray(DATA_SIZE), windowId, playerInventory, new BrewingFuelItemStackHandler(), new BrewingQueueItemStackHandler(), new BrewingBottleInItemStackHandler(), new BrewingBottleOutItemStackHandler());
	}
	
	public BrewingStationContainer(@Nullable BrewingStationTile tile, IIntArray brewingStationData, int windowId, PlayerInventory playerInventory, BrewingFuelItemStackHandler invFuel, BrewingQueueItemStackHandler invItems, BrewingBottleInItemStackHandler invBottleIn, BrewingBottleOutItemStackHandler invBottleOut) {
		super(Registry.BREWING_STATION_CONTAINER.get(), windowId);
		assert(brewingStationData.size() == DATA_SIZE);
		
        this.invFuel = invFuel;
        this.invItems = invItems;
        this.invBottleIn = invBottleIn;
        this.invBottleOut = invBottleOut;
        this.tile = tile;
        this.newPotionName = tile.getPotionName();

        this.data = brewingStationData;
        this.setup(playerInventory);

        trackIntArray(brewingStationData);
	}

	public void setup(PlayerInventory inventory) {
		// Brewing Station
		addSlot(new BrewingStationFuelSlot(invFuel, 0, 17, 41));
		addSlot(new BrewingStationQueueSlot(invItems, 0, 79, 41));
		addSlot(new BrewingStationQueueSlot(invItems, 1, 89, 17));
		addSlot(new BrewingStationQueueSlot(invItems, 2, 71, 17));
		addSlot(new BrewingStationQueueSlot(invItems, 3, 53, 17));
		addSlot(new BrewingStationQueueSlot(invItems, 4, 35, 17));
		addSlot(new BrewingStationQueueSlot(invItems, 5, 17, 17));
		addSlot(new BrewingStationBottleInSlot(invBottleIn, 0, 152, 8));
		addSlot(new BrewingStationBottleOutSlot(invBottleOut, 0, 152, 76, this));

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
		protected final ItemStackHandlerEx itemHandlerEx;
		
		public BrewingStationSlot(ItemStackHandlerEx itemHandler, int slot, int xPos, int yPos) {
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
	
	static class BrewingStationFuelSlot extends BrewingStationSlot {
		public BrewingStationFuelSlot(ItemStackHandlerEx itemHandler, int slot, int xPos, int yPos) {
			super(itemHandler, slot, xPos, yPos);
		}
	}
	
	static class BrewingStationQueueSlot extends BrewingStationSlot {
		public BrewingStationQueueSlot(ItemStackHandlerEx itemHandler, int slot, int xPos, int yPos) {
			super(itemHandler, slot, xPos, yPos);
		}
	}
	
	static class BrewingStationBottleInSlot extends BrewingStationSlot {
		public BrewingStationBottleInSlot(ItemStackHandlerEx itemHandler, int slot, int xPos, int yPos) {
			super(itemHandler, slot, xPos, yPos);
		}
	}
	
	static class BrewingStationBottleOutSlot extends BrewingStationSlot {
		private final BrewingStationContainer container;

		public BrewingStationBottleOutSlot(ItemStackHandlerEx itemHandler, int slot, int xPos, int yPos, BrewingStationContainer container) {
			super(itemHandler, slot, xPos, yPos);
			this.container = container;
		}
		
	    @Override
	    public void onSlotChanged()
	    {
    		this.container.renameItemName();
    		this.container.detectValidPotionChanges();
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
	
	public boolean hasPotionFluid()
	{
		return this.data.get(5) > 0;
	}
	
	public String getPotionName()
	{
		return this.newPotionName;
	}
	
	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		if (!this.copy_listeners.contains(listener)) {
			this.copy_listeners.add(listener);
		}
	}

	/**
	 * Remove the given Listener. Method name is for legacy.
	 */
	@OnlyIn(Dist.CLIENT)
	@Override
	public void removeListener(IContainerListener listener) {
		super.removeListener(listener);
		this.copy_listeners.remove(listener);
	}

	
	public void detectValidPotionChanges()
	{
		ItemStack stack = this.invBottleOut.getStackInSlot(0);
		boolean isPotion = Utils.isPotionItemStack(stack);
		
		if( isPotion != hasPotion )
		{
			hasPotion = isPotion;
			
			for(IContainerListener icontainerlistener : this.copy_listeners)
			{
				icontainerlistener.sendSlotContents(this, OUTPUT_SLOT, this.invBottleOut.getStackInSlot(0));
			}
		}
	}
	
	public void renameItemName()
	{
		this.tile.updatePotionName(this.newPotionName);
	}
	
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
}

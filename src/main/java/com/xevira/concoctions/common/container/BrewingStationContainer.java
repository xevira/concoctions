package com.xevira.concoctions.common.container;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.block.tile.BrewingStationTile;
import com.xevira.concoctions.common.block.tile.BrewingStationTile.Slots;
import com.xevira.concoctions.common.network.PacketHandler;
import com.xevira.concoctions.common.network.packets.PacketPotionRename;
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
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class BrewingStationContainer extends Container {
	private static final int SLOTS = 4;
	private static final int DATA_SIZE = 5;
	private static final int OUTPUT_SLOT = 3;
	
	// Annoying hack to allow this container to see the list of listeners as it is private in the parent class and there is no getListeners() function
    private final List<IContainerListener> copy_listeners = Lists.newArrayList();

	private String newItemName = "";
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
		addSlot(new BrewingStationSlot(this, handler, BrewingStationTile.Slots.FUEL, 17, 17));
		addSlot(new BrewingStationSlot(this, handler, BrewingStationTile.Slots.ITEM, 79, 17));
		addSlot(new BrewingStationSlot(this, handler, BrewingStationTile.Slots.BOTTLE_IN, 152, 8));
		addSlot(new BrewingStationSlot(this, handler, BrewingStationTile.Slots.BOTTLE_OUT, 152, 76));

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
		private final BrewingStationContainer container;
		public BrewingStationSlot(BrewingStationContainer container, IItemHandler itemHandler, BrewingStationTile.Slots slot, int xPos, int yPos) {
			super(itemHandler, slot.getId(), xPos, yPos);
			this.container = container;
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
					stack.getItem() == Items.ARROW ||
		    		stack.getItem() == Items.POTION ||
		    		stack.getItem() == Items.BUCKET ||
	        		stack.getItem() == Items.LAVA_BUCKET ||
	        		stack.getItem() == Items.WATER_BUCKET ||
	        		stack.getItem() == Items.SPONGE);
		
		    // Output only
		    if( getSlotIndex() == BrewingStationTile.Slots.BOTTLE_OUT.getId() )
		    	return false;
		
		    return super.isItemValid(stack);
		}
		
	    @Override
	    public void onSlotChange(@Nonnull ItemStack oldStackIn, @Nonnull ItemStack newStackIn)
	    {
	    }
	    
	    @Override
	    public void onSlotChanged()
	    {
	    	if( this.slotNumber == OUTPUT_SLOT)
	    	{
//		    	Concoctions.GetLogger().info("BrewingStationSlot.onSlotChanged called: slot = {} ({})", this.slotNumber, this.container.getWorldSide());
	    		this.container.renameItemName();
	    		this.container.detectValidPotionChanges();
	    	}
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
	
	private void setCustomPotionName(ItemStack stack, String prefix)
	{
		if( StringUtils.isBlank(this.newItemName))
		{
			if(stack.hasTag())
			{
				CompoundNBT root = stack.getTag();
				if( root.contains("CustomPotionName") )
					root.remove("CustomPotionName");
			}
			
			if( stack.getItem() == Items.TIPPED_ARROW )
				stack.setDisplayName(new TranslationTextComponent("item.concoctions.tipped_arrow.solution"));
			else
				stack.setDisplayName(new TranslationTextComponent("text.concoctions.solution"));
		}
		else
		{
			CompoundNBT root = stack.getOrCreateTag();
			root.putString("CustomPotionName", this.newItemName);
			
			stack.setDisplayName(new TranslationTextComponent(prefix, this.newItemName));
		}
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

	
	private boolean hasPotion = false;
	private boolean isPotionItemStack(ItemStack stack)
	{
		if( stack.isEmpty())
			return false;
		else
			return (stack.getItem() == Items.POTION || stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.LINGERING_POTION || stack.getItem() == Items.TIPPED_ARROW);
	}
	
	public void detectValidPotionChanges()
	{
		ItemStack stack = this.handler.getStackInSlot(OUTPUT_SLOT);
		boolean isPotion = isPotionItemStack(stack);
		
		if( isPotion != hasPotion )
		{
			hasPotion = isPotion;
			
			for(IContainerListener icontainerlistener : this.copy_listeners)
			{
				icontainerlistener.sendSlotContents(this, OUTPUT_SLOT, this.handler.getStackInSlot(OUTPUT_SLOT));
			}
		}
	}
	
	public void renameItemName()
	{
		this.tile.updatePotionName(this.newItemName);
	}
	
	public void updateItemName(String newName)
	{
		if(this.newItemName == null || !newName.equals(this.newItemName))
		{
			this.newItemName = newName;
			this.renameItemName();

			if( this.tile.hasWorld() && this.tile.getWorld().isRemote )
			{
				PacketHandler.sendToServer(new PacketPotionRename(this.newItemName));
			}
		}
	}
}

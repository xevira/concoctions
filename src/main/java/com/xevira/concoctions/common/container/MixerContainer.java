package com.xevira.concoctions.common.container;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.xevira.concoctions.common.block.tile.MixerTile;
import com.xevira.concoctions.common.block.tile.MixerTile.MixerReservoir;
import com.xevira.concoctions.common.handlers.ItemStackHandlerEx;
import com.xevira.concoctions.common.handlers.MixerOutputItemStackHandler;
import com.xevira.concoctions.common.network.PacketHandler;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MixerContainer extends Container
{
	private static final int COLUMNS[] = new int[] {
			25,
			65,
			105,
			145,
			193
		};

	public final IIntArray data;
	public final MixerTile tile;
	public final MixerReservoir reservoirs[];
	public final MixerOutputItemStackHandler outputs;

	public MixerContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
		this((MixerTile) playerInventory.player.world.getTileEntity(extraData.readBlockPos()), windowId, playerInventory);
	}

	public MixerContainer(@Nullable MixerTile tile, int windowId, PlayerInventory playerInventory) {
		super(Registry.MIXER_CONTAINER.get(), windowId);
		
		this.tile = tile;
		this.data = tile.mixerData;
		this.reservoirs = tile.getReservoirs();
		this.outputs = tile.getOutputItemHandler();
		this.setup(playerInventory);
		
		trackIntArray(this.data);
	}

	public void setup(PlayerInventory inventory)
	{
		for(int i = 0; i < MixerTile.TOTAL_TANKS; i++)
		{
			int x = COLUMNS[i];
			MixerReservoir tank = this.reservoirs[i];
			addSlot(new MixerSlot(tank.input.orElse(new ItemStackHandlerEx(1)), 0, x, 24));
			addSlot(new MixerSlot(this.outputs, i, x, 92));
		}
		
		// Player Inventory
        //   Hotbar
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(inventory, col, 28 + col * 18, 172));
        }
        //   Main inventory
        for (int row = 1; row < 4; ++ row) {
            for (int col = 0; col < 9; ++ col) {
                addSlot(new Slot(inventory, col + row * 9, 28 + col * 18, row * 18 + 114));
            }
        }
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
        BlockPos pos = this.tile.getPos();
        return this.tile != null && !this.tile.isRemoved() && playerIn.getDistanceSq(new Vector3d(pos.getX(), pos.getY(), pos.getZ()).add(0.5D, 0.5D, 0.5D)) <= 64D;
	}
	
	public int getValve(int valve)
	{
		assert valve >= 0 && valve < MixerTile.TOTAL_INPUTS;
		
		return this.data.get(valve);
	}
	
	public int getMixingTime()
	{
		return this.data.get(MixerTile.TOTAL_INPUTS);
	}
	
	public boolean isCenterValveOpen()
	{
		return this.data.get(MixerTile.TOTAL_INPUTS + 1) != 0;
	}
	
	public int getMixerStatus()
	{
		return this.data.get(MixerTile.TOTAL_INPUTS + 2);
	}
	
	public FluidStack getFluid(int tank)
	{
		assert tank >= 0 && tank < MixerTile.TOTAL_TANKS;
		
		return this.reservoirs[tank].getFluid();
	}
	
	public int getCapacity(int tank)
	{
		assert tank >= 0 && tank < MixerTile.TOTAL_TANKS;
		
		return this.reservoirs[tank].getCapacity();
	}
	
	public void setValve(int valve, int value)
	{
		this.tile.setValve(valve, value);
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

package com.xevira.concoctions.common.handlers;

import javax.annotation.Nonnull;
import com.xevira.concoctions.common.block.tile.BrewingStationTile;
import com.xevira.concoctions.common.block.tile.BrewingStationTile.Slots;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.items.ItemStackHandler;

public class BrewingQueueItemStackHandler extends ItemStackHandlerEx
{
	public BrewingQueueItemStackHandler()
	{
		super(BrewingStationTile.ITEM_SLOTS);
	}
	
	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack)
	{
		return !stack.isEmpty();
	}
	
    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
    	return ItemStack.EMPTY;
    }
    
	private boolean areItemStacksEqual(ItemStack a, ItemStack b) {
		// same functionality as ItemStack.areItemStackEqual(a,b) but ignoring item count
		if( a.isEmpty() && b.isEmpty() ) {
			return true;
		} else if (a.getItem() != b.getItem()) {
			return false;
		} else if (a.getTag() == null && b.getTag() != null) {
			return false;
		} else {
			return (a.getTag() == null || a.getTag().equals(b.getTag())) && a.areCapsCompatible(b);
		}
	}
    
    public void collapseQueue()
    {
		for(int slot = 0; slot < (this.getSlots() - 1); slot++)
		{
			ItemStack stack = this.getStackInSlot(slot);
			
			for( int moveSlot = slot + 1; moveSlot < this.getSlots(); moveSlot++)
			{
				ItemStack moveStack = this.getStackInSlot(moveSlot);
				if(!moveStack.isEmpty())
				{
					boolean moved = false;  

					if(stack.isEmpty())
					{
						this.setStackInSlot(moveSlot, ItemStack.EMPTY);
						this.setStackInSlot(slot, moveStack);
						stack = moveStack;
						moved = true;
					}
					else if(areItemStacksEqual(stack, moveStack))
					{
						if( stack.isStackable() && moveStack.isStackable())
						{
							int count = stack.getCount() + moveStack.getCount();
							
							if( count > stack.getMaxStackSize() )
							{
								stack.setCount(stack.getMaxStackSize());
								moveStack.setCount(count - stack.getMaxStackSize());
							}
							else
							{
								stack.setCount(count);
								moveStack.setCount(0);
							}
							
							if( moveStack.isEmpty() )
								moveStack = ItemStack.EMPTY;
							
							this.setStackInSlot(slot, stack);
							this.setStackInSlot(moveSlot, moveStack);
							
							if( stack.getCount() < stack.getMaxStackSize() )
								moved = true;
						}
					}

					// if we haven't moved anything (or we've filled up the current slot)
					//   break out the inner loot to slide the bucket to fill to the next slot
					if(!moved) break;
				}
			}
			
		}

    }

}

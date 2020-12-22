package com.xevira.concoctions.common;

import com.xevira.concoctions.common.block.IncenseBurnerBlock;
import com.xevira.concoctions.common.block.tile.IncenseBurnerTile;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CustomIncenseDispenser implements IDispenseItemBehavior {

	@Override
	public ItemStack dispense(IBlockSource source, ItemStack stack)
	{
		World world = source.getWorld();
		Direction direction = source.getBlockState().get(DispenserBlock.FACING);
		BlockPos blockpos = source.getBlockPos().offset(direction);
		BlockState blockstate = world.getBlockState(blockpos);
		if(blockstate.getBlock() == Registry.INCENSE_BURNER.get())
		{
			boolean successful = false;
			IncenseBurnerTile tile = (IncenseBurnerTile)world.getTileEntity(blockpos);
			if(tile != null)
			{
				if(!blockstate.get(IncenseBurnerBlock.HAS_INCENSE))
				{
					if(tile.setIncense(stack))
					{
						world.setBlockState(blockpos, blockstate.with(IncenseBurnerBlock.HAS_INCENSE, Boolean.valueOf(true)), 11);
						stack.shrink(1);
						successful = true;
					}
				}
			}

			source.getWorld().playEvent(successful ? 1000 : 1001, blockpos, 0);
		}

		return stack;
	}

	
	public static void register()
	{
		DispenserBlock.registerDispenseBehavior(Registry.INCENSE_ITEM.get(), new CustomIncenseDispenser());
	}
}

package com.xevira.concoctions.common;

import java.util.Random;

import com.xevira.concoctions.common.block.IncenseBurnerBlock;
import com.xevira.concoctions.common.block.tile.IncenseBurnerTile;
import com.xevira.concoctions.common.utils.Utils;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CustomFlintAndSteelDispenser implements IDispenseItemBehavior {
	private static final Random random = new Random();
	private final IDispenseItemBehavior original;
	
	
	
	public CustomFlintAndSteelDispenser(IDispenseItemBehavior original) {
		this.original = original;
	}

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
				if(blockstate.get(IncenseBurnerBlock.HAS_INCENSE) && !blockstate.get(IncenseBurnerBlock.LIT))
				{
					if(tile.setLit())
					{
						world.setBlockState(blockpos, blockstate.with(IncenseBurnerBlock.LIT, Boolean.valueOf(true)), 11);
						
						if(stack.attemptDamageItem(1, world.rand, (ServerPlayerEntity)null))
						{
							stack.setCount(0);
						}
						successful = true;
					}
				}
			}
			
			source.getWorld().playEvent(successful ? 1000 : 1001, blockpos, 0);
			return stack;
		}

		return this.original != null ? this.original.dispense(source, stack) : stack;
	}

	
	public static void register()
	{
		IDispenseItemBehavior behavior = Utils.getDispenserBehavior(Items.FLINT_AND_STEEL);
		
		DispenserBlock.registerDispenseBehavior(Items.FLINT_AND_STEEL, new CustomFlintAndSteelDispenser(behavior));
	}
}

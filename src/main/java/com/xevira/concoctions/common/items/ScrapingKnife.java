package com.xevira.concoctions.common.items;

import java.util.HashMap;
import java.util.Random;

import com.xevira.concoctions.setup.Registry;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ScrapingKnife extends Item {
	private final static HashMap<Block, Block> MOSSY_BLOCKS = new HashMap<Block,Block>();
	
	static {
		MOSSY_BLOCKS.put(Blocks.MOSSY_COBBLESTONE, Blocks.COBBLESTONE);
		MOSSY_BLOCKS.put(Blocks.MOSSY_COBBLESTONE_SLAB, Blocks.COBBLESTONE_SLAB);
		MOSSY_BLOCKS.put(Blocks.MOSSY_COBBLESTONE_STAIRS, Blocks.COBBLESTONE_STAIRS);
		MOSSY_BLOCKS.put(Blocks.MOSSY_COBBLESTONE_WALL, Blocks.COBBLESTONE_WALL);
		MOSSY_BLOCKS.put(Blocks.MOSSY_STONE_BRICKS, Blocks.STONE_BRICKS);
		MOSSY_BLOCKS.put(Blocks.MOSSY_STONE_BRICK_SLAB, Blocks.STONE_BRICK_SLAB);
		MOSSY_BLOCKS.put(Blocks.MOSSY_STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS);
		MOSSY_BLOCKS.put(Blocks.MOSSY_STONE_BRICK_WALL, Blocks.STONE_BRICK_WALL);
		MOSSY_BLOCKS.put(Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
	}

	public ScrapingKnife(Properties properties) {
		super(properties);
	}

	public ActionResultType onItemUse(ItemUseContext context)
	{
		PlayerEntity playerentity = context.getPlayer();
		World world = context.getWorld();
		if(world.isRemote)
			return ActionResultType.PASS;
		
		BlockPos pos = context.getPos();
		BlockState state = world.getBlockState(pos);
		
		Block targetBlock = MOSSY_BLOCKS.getOrDefault(state.getBlock(), null);
		if(targetBlock != null)
		{
			BlockState newState;
			
			if(targetBlock instanceof StairsBlock)
			{
				newState = targetBlock.getDefaultState()
						.with(BlockStateProperties.HORIZONTAL_FACING, state.get(BlockStateProperties.HORIZONTAL_FACING))
						.with(BlockStateProperties.HALF, state.get(BlockStateProperties.HALF))
						.with(BlockStateProperties.STAIRS_SHAPE, state.get(BlockStateProperties.STAIRS_SHAPE))
						.with(BlockStateProperties.WATERLOGGED, state.get(BlockStateProperties.WATERLOGGED));
			}
			else if(targetBlock instanceof SlabBlock)
			{
				newState = targetBlock.getDefaultState()
						.with(BlockStateProperties.SLAB_TYPE, state.get(BlockStateProperties.SLAB_TYPE))
						.with(BlockStateProperties.WATERLOGGED, state.get(BlockStateProperties.WATERLOGGED));
			}
			else if(targetBlock instanceof WallBlock)
			{
				newState = targetBlock.getDefaultState()
						.with(BlockStateProperties.UP, state.get(BlockStateProperties.UP))
						.with(BlockStateProperties.WALL_HEIGHT_NORTH, state.get(BlockStateProperties.WALL_HEIGHT_NORTH))
						.with(BlockStateProperties.WALL_HEIGHT_SOUTH, state.get(BlockStateProperties.WALL_HEIGHT_SOUTH))
						.with(BlockStateProperties.WALL_HEIGHT_EAST, state.get(BlockStateProperties.WALL_HEIGHT_EAST))
						.with(BlockStateProperties.WALL_HEIGHT_WEST, state.get(BlockStateProperties.WALL_HEIGHT_WEST))
						.with(BlockStateProperties.WATERLOGGED, state.get(BlockStateProperties.WATERLOGGED));
			}
			else
			{
				newState = targetBlock.getDefaultState();
			}

			// TODO: make new sound
			world.playSound(playerentity, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
			world.setBlockState(pos, newState, 11);
			
			ItemStack moss = new ItemStack(Registry.MOSS.get(), 1);
			InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), moss);

			if (playerentity != null) {
				context.getItem().damageItem(1, playerentity, (player) -> {
					player.sendBreakAnimation(context.getHand());
					});
			}
			
			return ActionResultType.func_233537_a_(world.isRemote());
		}
		
		/*
		if (CampfireBlock.canBeLit(blockstate))
		{
			world.playSound(playerentity, blockpos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
			world.setBlockState(blockpos, blockstate.with(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
			if (playerentity != null) {
				context.getItem().damageItem(1, playerentity, (player) -> {
					player.sendBreakAnimation(context.getHand());
					});
			}
			
			return ActionResultType.func_233537_a_(world.isRemote());
		}
		else
		{
			BlockPos blockpos1 = blockpos.offset(context.getFace());
			if (AbstractFireBlock.canLightBlock(world, blockpos1, context.getPlacementHorizontalFacing()))
			{
				world.playSound(playerentity, blockpos1, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
				BlockState blockstate1 = AbstractFireBlock.getFireForPlacement(world, blockpos1);
				world.setBlockState(blockpos1, blockstate1, 11);
				ItemStack itemstack = context.getItem();
				if (playerentity instanceof ServerPlayerEntity)
				{
					CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity)playerentity, blockpos1, itemstack);
					itemstack.damageItem(1, playerentity, (player) -> {
						player.sendBreakAnimation(context.getHand());
					});
				}
				
				return ActionResultType.func_233537_a_(world.isRemote());
			}
			else
			{
				return ActionResultType.FAIL;
			}
		}
		*/
		return ActionResultType.FAIL;
	}
}

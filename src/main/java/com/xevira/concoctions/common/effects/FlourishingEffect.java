package com.xevira.concoctions.common.effects;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.block.NetherrackBlock;
import net.minecraft.block.NyliumBlock;
import net.minecraft.block.SeaGrassBlock;
import net.minecraft.block.SpreadableSnowyDirtBlock;
import net.minecraft.block.TallFlowerBlock;
import net.minecraft.block.TallGrassBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;

public class FlourishingEffect extends EffectBase {
	private static final int ANIMAL_GROWTH = 20;
	private static final int HORIZONTAL_RANGE = 5;
	private static final int VERTICAL_RANGE = 2;
	private static final AxisAlignedBB BOUNDING = new AxisAlignedBB(-HORIZONTAL_RANGE, -VERTICAL_RANGE, -HORIZONTAL_RANGE, HORIZONTAL_RANGE, VERTICAL_RANGE, HORIZONTAL_RANGE);
	
	public FlourishingEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn, 200, true);	// Rank I is SLOW (one pulse every ten seconds)
	}
	
	@Override
	public void performEffect(LivingEntity living, int amplifier)
	{
		if(living instanceof PlayerEntity && !(living instanceof FakePlayer))
		{
			World world = living.getEntityWorld();
			if(!world.isRemote && world instanceof ServerWorld)
			{
				ServerWorld serverWorld = (ServerWorld)world;
				
				// Check for da growables/plantables
				// ------------------------------------------------------
				BlockPos pos = living.getPosition();
				Vector3i minPos = new Vector3i(pos.getX() - HORIZONTAL_RANGE, pos.getY() - VERTICAL_RANGE, pos.getZ() - HORIZONTAL_RANGE);
				Vector3i maxPos = new Vector3i(pos.getX() + HORIZONTAL_RANGE, pos.getY() + VERTICAL_RANGE, pos.getZ() + HORIZONTAL_RANGE);

				Random rand = new Random();
				for(int y = minPos.getY(); y <= maxPos.getY(); y++)
				{
					for(int z = minPos.getZ(); z <= maxPos.getZ(); z++)
					{
						for(int x = minPos.getX(); x <= maxPos.getX(); x++)
						{
							BlockPos blockPos = new BlockPos(x, y, z);
							BlockState state = serverWorld.getBlockState(blockPos);
							Block block = state.getBlock();
							
							if(isAllowedBonemealableBlock(block))
							{
								IGrowable growable = ((IGrowable)state.getBlock());
								
								if(growable.canGrow(serverWorld, blockPos, state, false) &&
										growable.canUseBonemeal(serverWorld, rand, blockPos, state))
								{
									growable.grow(serverWorld, rand, blockPos, state);
								}
							}
							else if(block instanceof IPlantable || block instanceof IGrowable)
								state.randomTick(serverWorld, blockPos, rand);
						}
					}
				}
				// ------------------------------------------------------
				
				// Check for da babbies
				// ------------------------------------------------------
				AxisAlignedBB box = BOUNDING.offset(pos);
			
				List<AnimalEntity> animals = serverWorld.getEntitiesWithinAABB(AnimalEntity.class, box, (AnimalEntity e) -> {
					// Only get adults that are ready to breed and aren't enamored already 
					return e.isChild();
				});

				for(AnimalEntity animal : animals)
				{
					animal.addGrowth(ANIMAL_GROWTH);
				}
				// ------------------------------------------------------
			}
		}
		else if(living instanceof AnimalEntity)
		{
			AnimalEntity animal = (AnimalEntity)living;
			
			if(animal.isChild())
			{
				animal.addGrowth(ANIMAL_GROWTH);
			}
		}
	}
	
	private boolean isAllowedBonemealableBlock(Block block)
	{
		if(!(block instanceof IGrowable)) return false;
		
		// Blacklisted block types
		if(block instanceof SpreadableSnowyDirtBlock) return false;	// things like grass
		if(block instanceof NetherrackBlock) return false;
		if(block instanceof NyliumBlock) return false;
		if(block instanceof SeaGrassBlock) return false;
		if(block instanceof TallFlowerBlock) return false;
		if(block instanceof TallGrassBlock) return false;
		
		return true;
	}
}

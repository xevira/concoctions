package com.xevira.concoctions.common.effects;

import java.util.List;
import java.util.Random;

import com.xevira.concoctions.Concoctions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class AmorousEffect extends EffectBase {
	private static final int BASE_IN_LOVE = 600;
	private static final int LOVE_PER_LEVEL = 200;
	private static final int HORIZONTAL_RANGE = 10;
	private static final int VERTICAL_RANGE = 5;
	
	private static final Random RNG = new Random();

	public AmorousEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn, 40, false);
	}
	
	@Override
	public void performEffect(LivingEntity livingEntity, int amplifier) {
		super.performEffect(livingEntity, amplifier);
		
		World world = livingEntity.getEntityWorld();
		
		if(!world.isRemote)
		{
			if( livingEntity instanceof PlayerEntity )
			{
				PlayerEntity player = (PlayerEntity)livingEntity;
				// When on a player, it will cause the player to pick a random animal nearby that can breed
				
				BlockPos pos = player.getPosition();
				Vector3d corner1 = new Vector3d(pos.getX() - HORIZONTAL_RANGE, pos.getY() - VERTICAL_RANGE, pos.getZ() - HORIZONTAL_RANGE);
				Vector3d corner2 = new Vector3d(pos.getX() + HORIZONTAL_RANGE, pos.getY() + VERTICAL_RANGE, pos.getZ() + HORIZONTAL_RANGE);
				
				List<AnimalEntity> list = world.getEntitiesWithinAABB(AnimalEntity.class, new AxisAlignedBB(corner1, corner2), (AnimalEntity e) -> {
					// Only get adults that are ready to breed and aren't enamored already 
					return (e.getGrowingAge() == 0) && e.canFallInLove();
				});
				Concoctions.GetLogger().info("Animals Nearby: {}", list.size());
				if( list.size() > 0)
				{
					int targetIdx = RNG.nextInt(list.size());
					AnimalEntity animal = list.get(targetIdx);
					
					animal.setInLove(player);
					
					// Change the duration of the inLove status based upon the level of the effect
					if( animal.isInLove() )
						animal.setInLove(this.getInLove(amplifier));
					
				}
			}
			else if( livingEntity instanceof AnimalEntity)
			{
				AnimalEntity animal = (AnimalEntity)livingEntity;
				
				if( (animal.getGrowingAge() == 0) && animal.canFallInLove() )
				{
					animal.setInLove(null);
					if( animal.isInLove())
						animal.setInLove(this.getInLove(amplifier));

				}
			}
		}
	}
	
	/*
	@Override
	public void affectEntity(Entity source, Entity indirectSource, LivingEntity livingEntity, int amplifier, double health)
	{
		if( livingEntity instanceof AnimalEntity )
		{
			AnimalEntity animal = (AnimalEntity)livingEntity;
			if(!animal.canFallInLove()) return;
			
			PlayerEntity player = null;
			if(indirectSource instanceof PlayerEntity)
				player = (PlayerEntity)indirectSource;

			animal.setInLove(player);
			if( animal.isInLove())
				animal.setInLove(this.getInLove(amplifier));
		}
	}
	*/
	
	private int getInLove(int amplifier)
	{
		return BASE_IN_LOVE + amplifier * LOVE_PER_LEVEL;
	}
}

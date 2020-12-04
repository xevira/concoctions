package com.xevira.concoctions.common.effects;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ChannelingEffect extends EffectBase
{
	public ChannelingEffect(EffectType typeIn, int liquidColorIn)
	{
		super(typeIn, liquidColorIn, 100, false);
	}
	
	@Override
	public void performEffect(LivingEntity living, int amplifier)
	{
		World world = living.getEntityWorld();
		
		if(world instanceof ServerWorld && world.isThundering())
		{
			BlockPos blockpos = living.getPosition();
			
			double chance = 0.0025D + 0.0025D * amplifier;
			
			if (world.canSeeSky(blockpos) && world.rand.nextDouble() < chance) {
	            LightningBoltEntity lightningboltentity = EntityType.LIGHTNING_BOLT.create(world);
	            lightningboltentity.moveForced(Vector3d.copyCenteredHorizontally(blockpos));
	            lightningboltentity.setCaster(null);
	            world.addEntity(lightningboltentity);
				world.playSound((PlayerEntity)null, living.getPosX(), living.getPosY(), living.getPosZ(), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.WEATHER, 5.0f, 1.0f);
			}
		}
	}
}

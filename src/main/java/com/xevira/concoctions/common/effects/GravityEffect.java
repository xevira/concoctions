package com.xevira.concoctions.common.effects;

import javax.annotation.Nullable;

import com.xevira.concoctions.Concoctions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.vector.Vector3d;


public class GravityEffect extends EffectBase
{
	public GravityEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn, 10);
	}
	
	@Override
	public void performEffect(LivingEntity entity, int amplifier)
	{
		Vector3d motion = entity.getMotion();
		boolean inWater = entity.areEyesInFluid(FluidTags.WATER);
		boolean inLava = entity.areEyesInFluid(FluidTags.LAVA);
		boolean onGround = entity.isOnGround();
		
		if(entity instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity)entity;
			
			if(player.abilities.isCreativeMode && player.abilities.isFlying)
			{
				return;
			}
		}
		
		if( !onGround /*&& (inWater || inLava)*/ )
		{
			motion = motion.subtract(0.0D, 0.12D * (amplifier+1), 0.0D);
			
			entity.setMotion(motion);
		}
	}

}

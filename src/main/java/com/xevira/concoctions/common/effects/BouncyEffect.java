package com.xevira.concoctions.common.effects;

import com.xevira.concoctions.Concoctions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.vector.Vector3d;

public class BouncyEffect extends EffectBase
{
	public BouncyEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn, 10);
	}

	/*
	@Override
	public void performEffect(LivingEntity entityLivingBaseIn, int amplifier)
	{
		Vector3d motion = entityLivingBaseIn.getMotion();
		
		if( motion.y < -0.2D || motion.y > 0.2D)
			Concoctions.GetLogger().info("Bouncy: motion = {}", motion);
	}
	*/
}

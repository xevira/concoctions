package com.xevira.concoctions.common.effects;

import com.xevira.concoctions.setup.Registry;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;

public class VoidEffect extends EffectBase {

	public VoidEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn, 10, false);
	}
	
	@Override
	public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
		super.performEffect(entityLivingBaseIn, amplifier);
		
		// Block bosses
		
		int scale = (amplifier + 1);
		
		entityLivingBaseIn.attackEntityFrom(Registry.VOID_DAMAGE, 0.1f * (scale * scale * scale));
	}
}

package com.xevira.concoctions.common.effects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;

public class RevealEffect extends InstantEffectBase {

	public RevealEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public void affectEntity(Entity source, Entity indirectSource, LivingEntity living, int amplifier, double health)
	{
		living.removePotionEffect(Effects.INVISIBILITY);
	}

}

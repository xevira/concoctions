package com.xevira.concoctions.common.effects;

import javax.annotation.Nullable;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;


public class LeadFootEffect extends Effect
{
	public LeadFootEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public boolean isReady(int duration, int amplifier)
	{
		return true;
	}
}

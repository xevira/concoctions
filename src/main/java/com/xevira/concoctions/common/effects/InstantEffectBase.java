package com.xevira.concoctions.common.effects;

import net.minecraft.potion.EffectType;

public class InstantEffectBase extends EffectBase {

	public InstantEffectBase(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn, 1, false);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}

}

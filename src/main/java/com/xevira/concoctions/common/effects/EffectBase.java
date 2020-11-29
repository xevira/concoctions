package com.xevira.concoctions.common.effects;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class EffectBase extends Effect {
	private final int tickRate;

	protected EffectBase(EffectType typeIn, int liquidColorIn, int tickRate) {
		super(typeIn, liquidColorIn);
		this.tickRate = tickRate;
	}
	
	@Override
	public boolean isReady(int duration, int amplifier)
	{
		if( this.tickRate < 0)
			return false;
		
		int rate = this.tickRate >> amplifier;
        if (rate > 0)
           return (duration % rate) == 0;

        return true;
	}


}

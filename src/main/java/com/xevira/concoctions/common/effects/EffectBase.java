package com.xevira.concoctions.common.effects;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class EffectBase extends Effect {
	private final int tickRate;
	private final boolean fasterWithAmplify;
	

	protected EffectBase(EffectType typeIn, int liquidColorIn, int tickRate) {
		this(typeIn, liquidColorIn, tickRate, false);
	}

	protected EffectBase(EffectType typeIn, int liquidColorIn, int tickRate, boolean faster) {
		super(typeIn, liquidColorIn);
		this.tickRate = tickRate;
		this.fasterWithAmplify = faster;
	}
	
	@Override
	public boolean isReady(int duration, int amplifier)
	{
		if( this.tickRate < 0)
			return false;
		
		int rate = this.fasterWithAmplify ? (this.tickRate >> amplifier) : this.tickRate;
        if (rate > 0)
           return (duration % rate) == 0;

        return true;
	}


}

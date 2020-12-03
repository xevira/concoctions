package com.xevira.concoctions.common.effects;

import com.xevira.concoctions.common.events.ResizeEntityHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.MathHelper;

public class ResizeEntityEffect extends EffectBase
{
	private static final float[] GROWTH = new float[] {
			1.5f,	// Growth I
			2.0f,	// Growth II
			2.5f,	// Growth III
			3.0f,	// Growth IV
			3.5f,	// Growth V
			4.0f	// Growth VI
	};
	private static final float[] SHRINK = new float[] {
			0.8f,	// Shrink I	
			0.6f,	// Shrink II
			0.4f,	// Shrink III
			0.2f,	// Shrink IV
			0.15f,	// Shrink V
			0.1f	// Shrink VI
	};
	private static final int MAX_AMPLIFIER = 5;	// Amplifier = 0 is rank I, so level above VI is pointless
	
	private final boolean isGrowth;

	public ResizeEntityEffect(EffectType typeIn, int liquidColorIn, boolean isGrowth) {
		super(typeIn, liquidColorIn, 1, false);
		this.isGrowth = isGrowth;
	}

	@Override
	public void performEffect(LivingEntity living, int amplifier)
	{
		int amp = MathHelper.clamp(amplifier, 0, MAX_AMPLIFIER);
		if(this.isGrowth)
			ResizeEntityHandler.addLivingEntityGrowth(living, GROWTH[amp]);
		else
			ResizeEntityHandler.addLivingEntityShrink(living, SHRINK[amp]);
	}
}

package com.xevira.concoctions.common.effects;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.events.IntangibleHandler;
import com.xevira.concoctions.common.network.PacketHandler;
import com.xevira.concoctions.common.network.packets.PacketPlayerCaps;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

// TODO: Rework this so as not to use Spectator Mode if possible

public class IntangibleEffect extends EffectBase {

	public IntangibleEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn, 10);
	}

	@Override
	public void performEffect(LivingEntity entityLivingBaseIn, int amplifier)
	{
//		IntangibleHandler.addIntangibleHandler(entityLivingBaseIn);		// Make sure it's in there
//		Concoctions.GetLogger().info("IntangibleEffect: noClip = {}, isRemote = {}", entityLivingBaseIn.noClip, entityLivingBaseIn.getEntityWorld().isRemote);
	}
	
	@Override
	public void applyAttributesModifiersToEntity(LivingEntity entityLivingBaseIn,
			AttributeModifierManager attributeMapIn, int amplifier) {
		super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);

//		IntangibleHandler.addIntangibleHandler(entityLivingBaseIn);
	}
}

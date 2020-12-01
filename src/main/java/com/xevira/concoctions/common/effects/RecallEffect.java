package com.xevira.concoctions.common.effects;

import com.xevira.concoctions.common.events.RecallHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.world.World;

public class RecallEffect extends EffectBase {

	public RecallEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn, 0, false);
	}
	
	@Override
	public void performEffect(LivingEntity livingEntity, int amplifier) {
		
		World world = livingEntity.getEntityWorld();
		if(!world.isRemote)
		{
			if(livingEntity instanceof ServerPlayerEntity)
			{
				ServerPlayerEntity player = (ServerPlayerEntity)livingEntity;
				RecallHandler.addPlayer(player);
			}
		}
	}
	
	@Override
	public boolean isInstant() {
		return true;
	}
	
	@Override
	public void affectEntity(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn, int amplifier,
			double health) {
		performEffect(entityLivingBaseIn, amplifier);
	}
	
	
}

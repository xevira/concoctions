package com.xevira.concoctions.common.effects;

import com.xevira.concoctions.setup.Registry;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.world.GameType;


// TODO: Temporary measure to get an intangibility effect until I can find out how to make a player properly noclip without being in spectator mode.
public class IntangibleEffectSpectator extends EffectBase {

	public IntangibleEffectSpectator(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn, 0);
	}
	
	@Override
	public void performEffect(LivingEntity entityLivingBaseIn, int amplifier)
	{
		if( entityLivingBaseIn instanceof ServerPlayerEntity)
		{
			ServerPlayerEntity player = ((ServerPlayerEntity)entityLivingBaseIn);
			
			if(player.interactionManager.getGameType() != GameType.SPECTATOR)
			{
				player.removeActivePotionEffect(Registry.INTANGIBLE_EFFECT.get());
				player.sendPlayerAbilities();
			}
		}
	}
	
	@Override
	public void applyAttributesModifiersToEntity(LivingEntity entityLivingBaseIn, 
			AttributeModifierManager attributeMapIn, int amplifier)
	{
		if( entityLivingBaseIn instanceof ServerPlayerEntity)
		{
			ServerPlayerEntity player = ((ServerPlayerEntity)entityLivingBaseIn);
			
			if(player.interactionManager.getGameType() == GameType.SURVIVAL)
			{
				player.interactionManager.setGameType(GameType.SPECTATOR);
			}
		}
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn,
			AttributeModifierManager attributeMapIn, int amplifier)
	{
		if( entityLivingBaseIn instanceof ServerPlayerEntity)
		{
			ServerPlayerEntity player = ((ServerPlayerEntity)entityLivingBaseIn);
			
			if(player.interactionManager.getGameType() == GameType.SPECTATOR)
			{
				player.interactionManager.setGameType(GameType.SURVIVAL);
			}
		}
	}
}	

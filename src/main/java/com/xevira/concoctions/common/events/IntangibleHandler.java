package com.xevira.concoctions.common.events;

import java.util.IdentityHashMap;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class IntangibleHandler
{
	private static final IdentityHashMap<PlayerEntity, IntangibleHandler> intangibleEntities = new IdentityHashMap<>();
	
	public final PlayerEntity player;
	
	public IntangibleHandler(PlayerEntity player)
	{
		this.player = player;
	}

	@SubscribeEvent
	public void playerTickPost(TickEvent.PlayerTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END && event.player == this.player )
		{
			if( this.player instanceof ServerPlayerEntity )
			{
				if(((ServerPlayerEntity)this.player).interactionManager.getGameType() != GameType.SURVIVAL)
				{
					// Return to normal
					this.player.noClip = false;
					this.player.abilities.allowFlying = false;
					this.player.abilities.isFlying = false;
					this.player.abilities.disableDamage = false;

					MinecraftForge.EVENT_BUS.unregister(this);
					intangibleEntities.remove(this.player);
					event.player.removeActivePotionEffect(Registry.INTANGIBLE_EFFECT.get());
					this.player.sendPlayerAbilities();
				}
				return;
			}
			
			if(event.player.getActivePotionEffect(Registry.INTANGIBLE_EFFECT.get()) != null)
			{
				this.player.noClip = true;
				this.player.abilities.allowFlying = true;
				this.player.abilities.isFlying = true;
				this.player.abilities.disableDamage = true;
				
				Concoctions.GetLogger().info("IntangibleHandler: this.player.noClip = {}", this.player.noClip);
			}
			else
			{
				// Return to normal
				this.player.noClip = false;
				this.player.abilities.allowFlying = false;
				this.player.abilities.isFlying = false;
				this.player.abilities.disableDamage = false;
				
				MinecraftForge.EVENT_BUS.unregister(this);
				intangibleEntities.remove(this.player);
			}
		}
	}

	public static void addIntangibleHandler(LivingEntity entity)
	{
		Concoctions.GetLogger().info("IntangibleHandler.addIntangibleHandler called");
		// only supports actual players as it uses the PlayerTick event
		if (!(entity instanceof PlayerEntity) || entity instanceof FakePlayer)
	      return;
		
		PlayerEntity player = (PlayerEntity)entity;

		IntangibleHandler handler = intangibleEntities.get(player);
		if (handler == null)
		{
			// wasn't bouncing yet, register it
			MinecraftForge.EVENT_BUS.register(new IntangibleHandler(player));
		}
	}

}

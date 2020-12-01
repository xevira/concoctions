package com.xevira.concoctions.common.events;

import java.util.IdentityHashMap;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.effects.DangerSenseEffect;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.EvokerFangsEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class DangerSenseMobHandler
{
	private static final IdentityHashMap<Entity, DangerSenseMobHandler> monsterEntities = new IdentityHashMap<>();

	private final MonsterEntity monster;
	private final PlayerEntity player;
	private int timer;
	private int lastMonsterTick;
	private int lastPlayerTick;
	
	public DangerSenseMobHandler(MonsterEntity monster, PlayerEntity player)
	{
		this.monster = monster;
		this.player = player;
		this.timer = 0;
		this.lastMonsterTick = 0;
		this.lastPlayerTick = 0;
		
		monsterEntities.put(monster, this);
	}
	
	private void remove()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
		monsterEntities.remove(this.monster);
	}

	@SubscribeEvent
	public void playerWorldTickPre(TickEvent.ClientTickEvent event)
	{
		if(event.side != LogicalSide.CLIENT || !this.monster.isAlive())
		{
			// Only allow client side
			Concoctions.GetLogger().info("Terminating DangerSenseMobHandler due to !CLIENT or mob dead");
			this.remove();
			return;
		}
		
		// Timeouts
		if(this.lastMonsterTick == 0 || (this.monster.ticksExisted - this.lastMonsterTick) < 10)
		{
			this.lastMonsterTick = this.monster.ticksExisted;
		}
		else
		{
			this.remove();
			return;
		}
		
		if(this.lastPlayerTick == 0 || (this.player.ticksExisted - this.lastPlayerTick) < 10)
		{
			this.lastPlayerTick = this.player.ticksExisted;
		}
		else
		{
			this.remove();
			return;
		}

		
		if(event.phase == TickEvent.Phase.START)
		{
			EffectInstance effect = this.player.getActivePotionEffect(Registry.DANGER_SENSE_EFFECT.get());
			
			if( effect != null )
			{
				AxisAlignedBB bounding = DangerSenseEffect.getBoundingBox(this.player, effect.getAmplifier());
				Concoctions.GetLogger().info("Player BB: {}", bounding);
				
				if( bounding.intersects(this.monster.getBoundingBox()) )
				{
					this.monster.setGlowing(true);
					
					Concoctions.GetLogger().info("Monster: {}, Glowing: {}", this.monster.getClass().getName(), this.monster.isGlowing());
					return;
				}
			}
			
			// Fallthrough due to loss of effect or being too far away
			if( this.monster.getActivePotionEffect(Effects.GLOWING) == null )
			{
				this.monster.setGlowing(false);
			}
			
			this.remove();
			return;
		}
	}

	@SubscribeEvent
	public void handleWorldUnload(WorldEvent.Unload event)
	{
		this.remove();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void addDangerSenseMobHandler(MonsterEntity entity, PlayerEntity player)
	{
		DangerSenseMobHandler handler = monsterEntities.get(entity);
		if( handler == null )
			MinecraftForge.EVENT_BUS.register(new DangerSenseMobHandler(entity, player));
		
	}

	
}

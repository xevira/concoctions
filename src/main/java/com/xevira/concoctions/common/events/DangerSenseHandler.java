package com.xevira.concoctions.common.events;

import java.util.IdentityHashMap;
import java.util.List;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.effects.DangerSenseEffect;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class DangerSenseHandler
{
	private static final IdentityHashMap<Entity, DangerSenseHandler> sensingEntities = new IdentityHashMap<>();
	
	private final PlayerEntity player;
	private int timer;
	private int lastTick;
	
	public DangerSenseHandler(PlayerEntity player)
	{
		this.player = player;
		this.timer = 0;
		this.lastTick = 0;
		
		sensingEntities.put(player, this);
	}
	
	private void remove()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
		sensingEntities.remove(this.player);
	}
	
	@SubscribeEvent
	public void playerTickPost(TickEvent.PlayerTickEvent event)
	{
		if(event.side != LogicalSide.CLIENT)
		{
			// Only allow client side
			this.remove();
			return;
		}
		
		if (this.lastTick == 0 || (this.player.ticksExisted - this.timer) < 10)
		{
			this.lastTick = this.player.ticksExisted;
		}
		else
		{
			this.remove();
			return;
		}


		if (event.phase == TickEvent.Phase.END && event.player == this.player)
		{
			if( this.timer <= 0 )
			{
				EffectInstance effect = this.player.getActivePotionEffect(Registry.DANGER_SENSE_EFFECT.get());
				
				if( effect != null )
				{
					AxisAlignedBB bounding = DangerSenseEffect.getBoundingBox(this.player, effect.getAmplifier());
				
					List<MonsterEntity> mobs = this.player.getEntityWorld().getEntitiesWithinAABB(MonsterEntity.class, bounding, (MonsterEntity e) -> {
						return !e.isGlowing();
					});
					
					Concoctions.GetLogger().info("Bounding: {}, mobs.size: {}", bounding, mobs.size());
					
					for(MonsterEntity mob : mobs)
					{
						Concoctions.GetLogger().info("Mob Class: {}", mob.getClass().getName());
						DangerSenseMobHandler.addDangerSenseMobHandler(mob, player);
					}
					
					this.timer = 5;
				}
				else
				{
					this.remove();
				}
			}
			else
				this.timer--;
			
		}
	}

	@SubscribeEvent
	public void handleWorldUnload(WorldEvent.Unload event)
	{
		this.remove();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void addDangerSenseHandler(LivingEntity entity)
	{
		if (!(entity instanceof PlayerEntity) || (entity instanceof FakePlayer))
	      return;

		DangerSenseHandler handler = sensingEntities.get(entity);
		if (handler == null)
			MinecraftForge.EVENT_BUS.register(new DangerSenseHandler((PlayerEntity)entity));
	}
	
}

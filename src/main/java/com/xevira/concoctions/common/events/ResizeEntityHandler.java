package com.xevira.concoctions.common.events;

import java.util.HashMap;
import java.util.IdentityHashMap;

import com.mojang.datafixers.util.Pair;
import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ResizeEntityHandler
{
	private static final float GROWTH = 1.1f;
	private static final float SHRINK = 0.9f;
	
	private static final IdentityHashMap<Entity, ResizeEntityHandler> resizedEntities = new IdentityHashMap<Entity, ResizeEntityHandler>();
	
	
	private final HashMap<Pose, Pair<EntitySize, Float>> entitySizes;
	private final LivingEntity entity;
	private Pose currentPose;
	private float currentSize;
	private float targetSize;
	private float shrinkSize;
	private float growthSize;
	
	private int timer;
	
	public ResizeEntityHandler(LivingEntity entity, float growth, float shrink)
	{
		this.entity = entity;
		this.entitySizes = new HashMap<Pose, Pair<EntitySize, Float>>();
		for(Pose pose : Pose.values())
		{
			Pair<EntitySize, Float> value = new Pair<EntitySize, Float>(this.entity.getSize(pose), this.entity.getEyeHeight(pose));
			
			this.entitySizes.put(pose, value);
		}
		
		this.currentSize = 1.0f;	// When they are first put into the system, they will be normal size
		this.growthSize = growth;
		this.shrinkSize = shrink;
		this.targetSize = growth * shrink;
		this.timer = 0;
	}
	
	private void setGrowth(float size)
	{
		this.growthSize = size;
		this.targetSize = this.growthSize * this.shrinkSize;
	}
	
	private void setShrink(float size)
	{
		this.shrinkSize = size;
		this.targetSize = this.growthSize * this.shrinkSize;
	}
	
	private void remove()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
		resizedEntities.remove(this.entity);
	}
	
	private boolean hasEffects(LivingEntity entity)
	{
		/*
		return entity.getActivePotionEffect(Registry.GROWTH_EFFECT.get()) != null ||
				entity.getActivePotionEffect(Registry.SHRINK_EFFECT.get()) != null;
				*/
		return false;
	}
	
	@SubscribeEvent
	public void playerTickPre(TickEvent.PlayerTickEvent event)
	{
		// Timeout the handler
		if(this.timer == 0 || (this.entity.ticksExisted - this.timer) < 10)
			this.timer = this.entity.ticksExisted;
		else
		{
			remove();
			return;
		}
		
		if(event.phase == Phase.START && event.player == this.entity)
		{
			boolean maintain = hasEffects(entity);
			
			if(!maintain)
			{
				this.growthSize = 1.0f;
				this.shrinkSize = 1.0f;
				this.targetSize = 1.0f;
			}
			
			if( this.currentSize < this.targetSize)
			{
				this.currentSize = Math.min(this.currentSize * GROWTH, this.targetSize);
			}
			else if(this.currentSize > this.targetSize)
			{
				this.currentSize = Math.max(this.currentSize * SHRINK, this.targetSize);
			}
			else if(!maintain)
			{
				remove();
				return;
			}

			if(this.currentSize != this.targetSize || (this.currentSize != 1.0f && this.currentPose != this.entity.getPose()))
			{
				this.currentPose = this.entity.getPose();
				this.entity.recalculateSize();
			}
		}
	}
	
	
	@SubscribeEvent
	public void handleEntitySize(EntityEvent.Size event)
	{
		if(event.getEntity() == this.entity)
		{
			//Pair<EntitySize, Float> base = this.entitySizes.get(this.currentPose);
			EntitySize entitySize = event.getOldSize();//base.getFirst();
			float entityEyeHeight = event.getOldEyeHeight();//base.getSecond();
			
			event.setNewSize(entitySize.scale(this.currentSize));
			event.setNewEyeHeight(entityEyeHeight * this.currentSize);
		}
	}
	
	public void onWorldUnload(WorldEvent.Unload event)
	{
		remove();
	}

	public static void addLivingEntityGrowth(Entity living, float size)
	{
		if(!(living instanceof LivingEntity) || living instanceof FakePlayer)
			return;
		
		if(size < 1.0f || size > 10.0f)
			return;
		
		ResizeEntityHandler handler = resizedEntities.get(living);
		if(handler == null)
			MinecraftForge.EVENT_BUS.register(new ResizeEntityHandler((LivingEntity)living, size, 1.0f));
		else
			handler.setGrowth(size);
	}

	public static void addLivingEntityShrink(Entity living, float size)
	{
		if(!(living instanceof PlayerEntity) || living instanceof FakePlayer)
			return;
		
		if(size < 0.1f || size >= 1.0f)
			return;
		
		ResizeEntityHandler handler = resizedEntities.get(living);
		if(handler == null)
			MinecraftForge.EVENT_BUS.register(new ResizeEntityHandler((LivingEntity)living, 1.0f, size));
		else
			handler.setShrink(size);
	}
}

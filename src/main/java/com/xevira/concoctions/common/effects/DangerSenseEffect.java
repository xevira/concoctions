package com.xevira.concoctions.common.effects;

import com.xevira.concoctions.common.events.DangerSenseHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class DangerSenseEffect extends EffectBase {
	public static final int HORIZONTAL_RANGE = 6;
	public static final int VERTICAL_RANGE = 3;
	public static final int HORIZONTAL_RANGE_GROWTH = 3;
	public static final int VERTICAL_RANGE_GROWTH = 1;
	public static final AxisAlignedBB BOUNDING = new AxisAlignedBB(-HORIZONTAL_RANGE, -VERTICAL_RANGE, -HORIZONTAL_RANGE, HORIZONTAL_RANGE, VERTICAL_RANGE, HORIZONTAL_RANGE);


	public DangerSenseEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn, 5, false);
	}
	
	@Override
	public void performEffect(LivingEntity livingEntity, int amplifier)
	{
		World world = livingEntity.getEntityWorld();
		
		// Client Side only rendering
		if(world.isRemote)
		{
			if( livingEntity instanceof PlayerEntity )
			{
				DangerSenseHandler.addDangerSenseHandler(livingEntity);

				/*
				
				PlayerEntity player = (PlayerEntity)livingEntity;
				
				AxisAlignedBB bbox = BOUNDING.grow(HORIZONTAL_RANGE * amplifier, VERTICAL_RANGE * amplifier, HORIZONTAL_RANGE * amplifier).offset(player.getPosition());
			

				List<MonsterEntity> list = world.getEntitiesWithinAABB(MonsterEntity.class, bbox, (MonsterEntity e) -> {
					return !e.isGlowing();
				});
				Concoctions.GetLogger().info("DangerSense: list.size() = {}", list.size());
				if( list.size() > 0 ) 
				{
					for(MonsterEntity monster : list)
					{
					}
				}
				 */
			}

		}
	}
	
	@Override
	public void applyAttributesModifiersToEntity(LivingEntity livingEntity,
			AttributeModifierManager attributeMapIn, int amplifier) {
		super.applyAttributesModifiersToEntity(livingEntity, attributeMapIn, amplifier);
		
		// Add to the system
		World world = livingEntity.getEntityWorld();
		
		// Client Side only rendering
		if(world.isRemote)
		{
			if( livingEntity instanceof PlayerEntity )
			{

				DangerSenseHandler.addDangerSenseHandler(livingEntity);
			}
		}
	}
	
	public static AxisAlignedBB getBoundingBox(LivingEntity entity, int amplifier)
	{
		if( amplifier > 0)
			return BOUNDING.grow(HORIZONTAL_RANGE_GROWTH * amplifier, VERTICAL_RANGE_GROWTH * amplifier, HORIZONTAL_RANGE_GROWTH * amplifier).offset(entity.getPosition());
		
		return BOUNDING.offset(entity.getPosition());
	}

}

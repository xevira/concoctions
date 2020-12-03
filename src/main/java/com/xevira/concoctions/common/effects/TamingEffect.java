package com.xevira.concoctions.common.effects;

import java.util.List;
import java.util.Random;

import com.xevira.concoctions.common.network.PacketHandler;
import com.xevira.concoctions.common.network.packets.PacketTamedAnimal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

public class TamingEffect extends InstantEffectBase {
	private Random random = new Random();

	public TamingEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	// Assumes server side
	private void TameAnimal(TameableEntity animal, ServerPlayerEntity player)
	{
		if(animal instanceof IAngerable)
		{
			// Check if the animal is angry
			if(((IAngerable)animal).func_233678_J__())
				return;
		}
		
		if(!animal.isTamed())
		{
			animal.setTamedBy(player);
			animal.func_233687_w_(true);	// Make them sit
			animal.getEntityWorld().setEntityState(animal, (byte)7);	// Called whenever tameable animal is tamed
			
			PacketHandler.sendTo(new PacketTamedAnimal(animal.getEntityId()), player);
		}
	}
	
	
	// Assumes server side
	private void TameAnimals(ServerPlayerEntity player, int amplifier)
	{
		int w = 5 + 3 * amplifier;
		int h = 2 + amplifier;
		BlockPos pos = player.getPosition();
		
		AxisAlignedBB box = new AxisAlignedBB(pos.getX() - w, pos.getY() - h, pos.getZ() - w, pos.getX() + w, pos.getY() + h, pos.getZ() + w);
		
		List<TameableEntity> tameables = player.getEntityWorld().getEntitiesWithinAABB(TameableEntity.class, box, (TameableEntity e) ->{
			return !e.isTamed();
		});
		
		for(TameableEntity animal : tameables)
		{
			if(canTame(amplifier))
				TameAnimal(animal, player);
		}
	}
	
	@Override
	public void performEffect(LivingEntity living, int amplifier)
	{
		if(living instanceof ServerPlayerEntity && !(living instanceof FakePlayer))
		{
			World world = living.getEntityWorld();
			if(!world.isRemote)
			{
				TameAnimals((ServerPlayerEntity)living, amplifier);
			}
		}
	}
	
	@Override
	public void affectEntity(Entity source, Entity indirectSource, LivingEntity living, int amplifier,
			double health)
	{
		World world = living.getEntityWorld();
		if(!world.isRemote)
		{
			if(living instanceof TameableEntity)
			{
				if(indirectSource instanceof ServerPlayerEntity && !(indirectSource instanceof FakePlayer))
				{
					if(canTame(amplifier))
						TameAnimal((TameableEntity)living,(ServerPlayerEntity)indirectSource);
				}
			}
		}
	}
	
	private boolean canTame(int amplifier)
	{
		double chance = 0.50D + 0.1D * amplifier;	// Max usable potency is VI
		return this.random.nextDouble() < chance;
	}

}

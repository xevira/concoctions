package com.xevira.concoctions.common.effects;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectType;

public class RestorationEffect extends InstantEffectBase {

	public RestorationEffect(EffectType typeIn, int liquidColorIn)
	{
		super(typeIn, liquidColorIn);
	}

	@Override
	public void performEffect(LivingEntity living, int amplifier)
	{
		if(living instanceof ServerPlayerEntity)
		{
			ServerPlayerEntity player = (ServerPlayerEntity)living;
			
			int xp = (amplifier + 1)*(amplifier + 1);
			
			for(ItemStack stack : player.inventory.mainInventory) {
				applyMending(stack, xp);
			}
			
			for(ItemStack stack : player.inventory.armorInventory) {
				applyMending(stack, xp);
			}

			for(ItemStack stack : player.inventory.offHandInventory) {
				applyMending(stack, xp);
			}
		}
	}
	
	@Override
	public void affectEntity(Entity source, Entity indirectSource, LivingEntity living, int amplifier, double health)
	{
		this.performEffect(living, amplifier);
	}
	
	private void applyMending(ItemStack stack, int xp)
	{
		if(stack.isEmpty()) return;
		if(!stack.isDamageable() || !stack.isDamaged()) return;
		if(EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack) < 1) return;
		
		int repair = Math.min((int)(xp * stack.getXpRepairRatio()), stack.getDamage());
		
		stack.setDamage(stack.getDamage() - repair);
	}
}

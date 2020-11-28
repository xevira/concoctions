package com.xevira.concoctions.common.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemEntityNoImbue extends ItemEntity {

	public ItemEntityNoImbue(EntityType<? extends ItemEntityNoImbue> p_i50217_1_, World world)
	{
		super(p_i50217_1_, world);
	}

	public ItemEntityNoImbue(World worldIn, double x, double y, double z, ItemStack stack)
	{
		super(worldIn, x, y, z, stack);
	}
}

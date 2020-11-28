package com.xevira.concoctions.common.items;

import java.util.Objects;

import com.xevira.concoctions.setup.Registry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import vazkii.patchouli.api.PatchouliAPI;

public class ApothecaryGuide extends Item
{
	public ApothecaryGuide()
	{
		super(new Item.Properties()
				.maxStackSize(1).
				group(ItemGroup.BREWING));
	}
	
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        if (worldIn.isRemote()) {
            return ActionResult.resultPass(playerIn.getHeldItem(handIn));
        }
        ServerPlayerEntity player = (ServerPlayerEntity)playerIn;
        PatchouliAPI.instance.openBookGUI(player, Objects.requireNonNull(Registry.APOTHECARY_GUIDE.get().getRegistryName()));
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}

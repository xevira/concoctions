package com.xevira.concoctions.common.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Item.Properties;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DrinkHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class LingeringBottleItem extends Item {

	public LingeringBottleItem(Properties properties) {
		super(properties);
	}

	/**
	 * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
	 * {@link #onItemUse}.
	 */
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		RayTraceResult raytraceresult = rayTrace(worldIn, playerIn, RayTraceContext.FluidMode.SOURCE_ONLY);
		if (raytraceresult.getType() == RayTraceResult.Type.MISS) {
				return ActionResult.resultPass(itemstack);
		} else {
				if (raytraceresult.getType() == RayTraceResult.Type.BLOCK) {
					BlockPos blockpos = ((BlockRayTraceResult)raytraceresult).getPos();
					if (!worldIn.isBlockModifiable(playerIn, blockpos)) {
							return ActionResult.resultPass(itemstack);
					}

					if (worldIn.getFluidState(blockpos).isTagged(FluidTags.WATER)) {
						worldIn.playSound(playerIn, playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
						return ActionResult.func_233538_a_(this.turnBottleIntoItem(itemstack, playerIn, PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), Potions.WATER)), worldIn.isRemote());
					}
				}
				return ActionResult.resultPass(itemstack);
		}
	}

	protected ItemStack turnBottleIntoItem(ItemStack bottleStack, PlayerEntity player, ItemStack stack) {
		player.addStat(Stats.ITEM_USED.get(this));
		return DrinkHelper.fill(bottleStack, player, stack);
	}

}

package com.xevira.concoctions.common;

import java.util.Random;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.block.FilledCauldronBlock;
import com.xevira.concoctions.common.block.tile.FilledCauldronTile;
import com.xevira.concoctions.common.utils.Utils;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.stats.Stats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;

public class EventHandler
{
	private void handleLivingJump_LeadFoot(LivingEntity entity)
	{
		Vector3d motion = entity.getMotion();
		EffectInstance effect = entity.getActivePotionEffect(Registry.LEAD_FOOT.get()); 
		if( effect != null )
		{
			if( entity instanceof PlayerEntity)
			{
				PlayerEntity player = (PlayerEntity)entity;
				
				// Creative flight negates the effect of Lead Foot entirely
				if( player.abilities.isCreativeMode && player.abilities.isFlying )
					return;
			}

			// Only affect it if you are trying to go UP
			if( motion.y > 0)
			{
				motion = motion.subtract(0, (effect.getAmplifier()+1)*0.1F, 0);

				// Prevent lead foot from causing you to drop fast than you should
				if( motion.y < 0)
					motion = new Vector3d(motion.x, 0, motion.z);
				entity.setMotion(motion);
			}
		}
	}
	
	@SubscribeEvent
	public void handleLivingJump(LivingJumpEvent event)
	{
		handleLivingJump_LeadFoot(event.getEntityLiving());
	}

	/*
	@SubscribeEvent
	public void onLivingJump(LivingJumpEvent event)
	{
		Vector3d motion = event.getEntity().getMotion();
		if(event.getEntityLiving().getActivePotionEffect(IEPotions.sticky)!=null)
			motion = motion.subtract(0, (event.getEntityLiving().getActivePotionEffect(IEPotions.sticky).getAmplifier()+1)*0.3F, 0);
		else if(event.getEntityLiving().getActivePotionEffect(IEPotions.concreteFeet)!=null)
			motion = Vector3d.ZERO;
		event.getEntity().setMotion(motion);
	}
	*/
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void handleRightClick(PlayerInteractEvent.RightClickBlock event)
	{
		BlockPos pos = event.getPos();
		
		PlayerEntity player = event.getPlayer();
		if(player == null)
			return;
		
		World world = event.getWorld();
		if(world == null)
			return;
		
		BlockState state = world.getBlockState(pos);
		if(state != null)
		{
			//Concoctions.GetLogger().info("Right Clicked on {}", state.getBlock().getRegistryName().toString());
			
			if( state.getBlock() == Blocks.CAULDRON && state.get(CauldronBlock.LEVEL) == 0 )
			{
				ItemStack stack = event.getItemStack();
				if( stack.isEmpty() )
					return;
				
				if( stack.getItem() == Items.POTION )
				{
					FluidStack fluid = Utils.getPotionFluidFromNBT(stack.getTag());
					if( fluid.getFluid() == Registry.POTION_FLUID.get() )
					{
						BlockState newState = Registry.FILLED_CAULDRON.get().getDefaultState().with(FilledCauldronBlock.LEVEL, Integer.valueOf(FilledCauldronBlock.LEVELS_PER_POTION));
						world.setBlockState(pos, newState);
						FilledCauldronTile tile = (FilledCauldronTile)world.getTileEntity(pos);
						tile.setPotionFluid(fluid);
						
						if(!player.abilities.isCreativeMode)
						{
							ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
							player.addStat(Stats.USE_CAULDRON);
							player.setHeldItem(event.getHand(), bottle);
							if (player instanceof ServerPlayerEntity)
								((ServerPlayerEntity)player).sendContainerToPlayer(player.container);
							
						}
						
						world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
						event.setUseBlock(Result.ALLOW);
						event.setUseItem(Result.ALLOW);
						return;
					}
				}
			}
			else if(state.getBlock() == Blocks.FIRE && event.getItemStack().getItem() == Items.GLASS_BOTTLE)
			{
				ItemStack stack = event.getItemStack();
				
				ItemStack bottleFire = new ItemStack(Registry.BOTTLE_FIRE.get(), 1);
				
				Random rand = new Random();
				if(!player.abilities.isCreativeMode)
				{
					stack.shrink(1);
					
					if(stack.isEmpty())
						player.setHeldItem(event.getHand(), bottleFire);
					else if (!player.inventory.addItemStackToInventory(bottleFire))
						player.dropItem(bottleFire, false);
					else if (player instanceof ServerPlayerEntity)
						((ServerPlayerEntity)player).sendContainerToPlayer(player.container);
					player.addStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));
					world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
					
					// Chance to extinguish the fire
					if( rand.nextDouble() < 0.1D)
					{
						world.playEvent((PlayerEntity)null, 1009, pos, 0);
						world.setBlockState(pos, Blocks.AIR.getDefaultState());
					}
				}
			}
			else if(state.getBlock() == Blocks.SOUL_FIRE && event.getItemStack().getItem() == Items.GLASS_BOTTLE)
			{
				ItemStack stack = event.getItemStack();
				
				ItemStack bottleFire = new ItemStack(Registry.BOTTLE_SOUL_FIRE.get(), 1);
				
				Random rand = new Random();
				if(!player.abilities.isCreativeMode)
				{
					stack.shrink(1);
					
					if(stack.isEmpty())
						player.setHeldItem(event.getHand(), bottleFire);
					else if (!player.inventory.addItemStackToInventory(bottleFire))
						player.dropItem(bottleFire, false);
					else if (player instanceof ServerPlayerEntity)
						((ServerPlayerEntity)player).sendContainerToPlayer(player.container);
					player.addStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));
					world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
					
					// Chance to extinguish the fire
					if( rand.nextDouble() < 0.1D)
					{
						world.playEvent((PlayerEntity)null, 1009, pos, 0);
						world.setBlockState(pos, Blocks.AIR.getDefaultState());
					}
				}
			}
		}
	}
}

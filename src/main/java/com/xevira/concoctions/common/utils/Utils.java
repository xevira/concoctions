package com.xevira.concoctions.common.utils;

import java.util.List;

import com.xevira.concoctions.common.fluids.PotionFluid;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class Utils
{
	public static boolean isPotionItemStack(ItemStack stack)
	{
		if( stack.isEmpty())
			return false;
		else
			return (stack.getItem() == Items.POTION ||
					stack.getItem() == Items.SPLASH_POTION ||
					stack.getItem() == Items.LINGERING_POTION ||
					stack.getItem() == Items.TIPPED_ARROW);
	}
	
	public static FluidStack getPotionFluidFromNBT(CompoundNBT root)
	{
		FluidStack fluidStack = new FluidStack(Registry.POTION_FLUID.get(), FluidAttributes.BUCKET_VOLUME);
		
		if(root == null) return fluidStack;
		
		// Add NBT data
		List<EffectInstance> effects = PotionUtils.getEffectsFromTag(root);
		CompoundNBT tag = new CompoundNBT();

		if( root.contains("Potion")) {
			String basePotion = root.getString("Potion");
			
			Potion potion = Potion.getPotionTypeForName(basePotion);
			if( potion == Potions.WATER)
			{
				// This is water... have it as water, instead of the "potion" water
				return new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
			}
			
			tag.putString("Potion", basePotion);
		}
		else
		{
			ListNBT listNBT = new ListNBT();
			for(EffectInstance effect : effects) {
				CompoundNBT tagEffect = new CompoundNBT();
				
//				Concoctions.GetLogger().info("Potion: {} {} {}", effect.getEffectName(), effect.getAmplifier(), effect.getDuration());
			
				effect.write(tagEffect);
				listNBT.add(tagEffect);
			}
			tag.put("CustomPotionEffects", listNBT);
		}
		
		if( root.contains("DyedPotion") && root.contains("CustomPotionColor"))
		{
			if( root.getBoolean("DyedPotion"))
			{
				tag.putInt("CustomPotionColor", root.getInt("CustomPotionColor"));
				tag.putBoolean("DyedPotion", true);
			}
		}
		
		if( root.contains("CustomPotionName") )
			tag.putString("CustomPotionName", root.getString("CustomPotionName"));

		fluidStack.setTag(tag);
		return fluidStack;

	}

	public static void addPotionEffectsToItemStack(FluidStack inFluid, ItemStack outStack)
	{
		if( !Utils.isPotionItemStack(outStack) )
			return;
		
		if( inFluid.getFluid() == Fluids.WATER )
		{
			// Shortcut if the fluid is water and the output is to be a potion (ie. water bottle)
			PotionUtils.addPotionToItemStack(outStack, Potions.WATER);
			return;
		}
		
		CompoundNBT root = outStack.getOrCreateTag();
		
		if(!inFluid.getTag().contains("Potion"))
		{
			if( inFluid.getTag().contains("CustomPotionEffects", 9))
			{
				ListNBT effects = inFluid.getTag().getList("CustomPotionEffects", 10);
				root.put("CustomPotionEffects", effects.copy());
				
				Fluid fluid = inFluid.getFluid();
				if(fluid instanceof PotionFluid)
				{
					int col = fluid.getAttributes().getColor(inFluid);
					
					root.putInt("CustomPotionColor", col);
					root.putBoolean("DyedPotion", false);	// Used to differentiate between raw custom potions and dyed potions 
				}
			}
		}

		if( inFluid.getTag().contains("CustomPotionColor") )
		{
			root.putInt("CustomPotionColor", inFluid.getTag().getInt("CustomPotionColor"));
			root.putBoolean("DyedPotion", inFluid.getTag().getBoolean("DyedPotion"));
		}
		
		if( inFluid.getTag().contains("CustomPotionName"))
		{
			root.putString("CustomPotionName", inFluid.getTag().getString("CustomPotionName"));
		}
	}

}

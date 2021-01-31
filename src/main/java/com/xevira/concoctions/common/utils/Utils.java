package com.xevira.concoctions.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.fluids.PotionFluid;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.text.TranslationTextComponent;
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
	
	public static FluidStack getPotionFluidFromEffects(List<EffectInstance> effects)
	{
		FluidStack fluidStack = new FluidStack(Registry.POTION_FLUID.get(), FluidAttributes.BUCKET_VOLUME);
		
		if(effects.size() > 0)
		{
			CompoundNBT root = new CompoundNBT();
			
			ListNBT listNBT = new ListNBT();
			
			for(EffectInstance effect : effects)
			{
				listNBT.add(effect.write(new CompoundNBT()));
			}
			
			root.put("CustomPotionEffects", listNBT);

			fluidStack.setTag(root);
		}
		
		return fluidStack;
	}
	
	public static FluidStack getPotionFluidFromNBT(CompoundNBT root)
	{
		FluidStack fluidStack = new FluidStack(Registry.POTION_FLUID.get(), FluidAttributes.BUCKET_VOLUME);
		
		if(root == null) return fluidStack;
		
		// Add NBT data
		List<EffectInstance> effects = PotionUtils.getEffectsFromTag(root);
		CompoundNBT tag = new CompoundNBT();

		if(root.contains("Potion"))
		{
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
			for(EffectInstance effect : effects)
			{
				CompoundNBT tagEffect = new CompoundNBT();
				effect.write(tagEffect);
				listNBT.add(tagEffect);
			}
			tag.put("CustomPotionEffects", listNBT);
		}
		
		if(root.contains("DyedPotion") && root.contains("CustomPotionColor"))
		{
			if(root.getBoolean("DyedPotion"))
			{
				tag.putInt("CustomPotionColor", root.getInt("CustomPotionColor"));
				tag.putBoolean("DyedPotion", true);
			}
		}
		
		if(root.contains("CustomPotionName"))
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
		
		CompoundNBT srcRoot = inFluid.getTag();
		CompoundNBT root = outStack.getOrCreateTag();
		
		if(srcRoot.contains("Potion"))
		{
			root.putString("Potion", srcRoot.getString("Potion"));
		}
		else
		{
			if(srcRoot.contains("CustomPotionEffects", 9))
			{
				ListNBT effects = srcRoot.getList("CustomPotionEffects", 10);
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

		if(srcRoot.contains("CustomPotionColor"))
		{
			root.putInt("CustomPotionColor", srcRoot.getInt("CustomPotionColor"));
			root.putBoolean("DyedPotion", srcRoot.getBoolean("DyedPotion"));
		}
		
		if(srcRoot.contains("CustomPotionName"))
		{
			root.putString("CustomPotionName", srcRoot.getString("CustomPotionName"));
		}
	}
	
	public static boolean isPotionItem(ItemStack stack)
	{
		return (stack.getItem() == Items.POTION ||
				stack.getItem() == Items.SPLASH_POTION ||
				stack.getItem() == Items.LINGERING_POTION ||
				stack.getItem() == Items.TIPPED_ARROW ||
				stack.getItem() == Registry.INCENSE_ITEM.get());
		
	}
	
	public static String getCustomPotionName(ItemStack stack)
	{
		if(!isPotionItem(stack))
			return null;
		
		if(!stack.hasTag())
			return "";

		return stack.getTag().getString("CustomPotionName");
	}
	
	public static void setCustomPotionName(ItemStack stack, String prefix, String name)
	{
		if( StringUtils.isBlank(name))
		{
			boolean isBasePotion = false;
			if(stack.hasTag())
			{
				CompoundNBT root = stack.getTag();
				if( root.contains("CustomPotionName") )
					root.remove("CustomPotionName");
				
				isBasePotion = root.contains("Potion");
			}
			
			if( isBasePotion )
			{
				stack.clearCustomName();
			}
			else if (stack.getItem() == Items.TIPPED_ARROW )
			{
				stack.setDisplayName(new TranslationTextComponent("item.concoctions.tipped_arrow.solution"));
			}
			else if (stack.getItem() == Registry.INCENSE_ITEM.get() )
			{
				stack.setDisplayName(new TranslationTextComponent("item.concoctions.incense.solution"));
			}
			else
			{
				stack.setDisplayName(new TranslationTextComponent("item.concoctions.solution"));
			}
		}
		else
		{
			CompoundNBT root = stack.getOrCreateTag();
			root.putString("CustomPotionName", name);
			
			stack.setDisplayName(new TranslationTextComponent(prefix, name));
		}
	}

	
	public static void renamePotionStack(ItemStack stack)
	{
		if( stack.isEmpty() )
			return;

		String name = getCustomPotionName(stack);
		
		if(name != null)
		{
			
			if( stack.getItem() == Items.POTION )
			{
				setCustomPotionName(stack, "item.concoctions.potion.prefix", name);
			}
			else if( stack.getItem() == Items.SPLASH_POTION )
			{
				setCustomPotionName(stack, "item.concoctions.splash_potion.prefix", name);
			}
			else if( stack.getItem() == Items.LINGERING_POTION )
			{
				setCustomPotionName(stack, "item.concoctions.lingering_potion.prefix", name);
			}
			else if( stack.getItem() == Items.TIPPED_ARROW )
			{
				setCustomPotionName(stack, "item.concoctions.tipped_arrow.prefix", name);
			}
			else if( stack.getItem() == Registry.INCENSE_ITEM.get() )
			{
				setCustomPotionName(stack, "item.concoctions.incense.prefix", name);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static IDispenseItemBehavior getDispenserBehavior(Item item)
	{
		try {
			Field field = DispenserBlock.class.getDeclaredField("DISPENSE_BEHAVIOR_REGISTRY");
			field.setAccessible(true);
			
			Map<Item,IDispenseItemBehavior> map = (Map<Item,IDispenseItemBehavior>)field.get(null);

			IDispenseItemBehavior behavior = null;
			
			if(map.containsKey(item))
				behavior = map.get(item);

			return behavior;
		} catch (NoSuchFieldException e) {
			Concoctions.GetLogger().info("getDispenserBehavior = NoSuchFieldException");
			e.printStackTrace();
			return null;
		} catch (SecurityException e) {
			Concoctions.GetLogger().info("getDispenserBehavior = SecurityException");
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			Concoctions.GetLogger().info("getDispenserBehavior = IllegalArgumentException");
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			Concoctions.GetLogger().info("getDispenserBehavior = IllegalAccessException");
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean areItemStacksEqual(ItemStack a, ItemStack b) {
		// same functionality as ItemStack.areItemStackEqual(a,b) but ignoring item count
		if( a.isEmpty() && b.isEmpty() ) {
			return true;
		} else if (a.getItem() != b.getItem()) {
			return false;
		} else if (a.getTag() == null && b.getTag() != null) {
			return false;
		} else {
			return (a.getTag() == null || a.getTag().equals(b.getTag())) && a.areCapsCompatible(b);
		}
	}

}

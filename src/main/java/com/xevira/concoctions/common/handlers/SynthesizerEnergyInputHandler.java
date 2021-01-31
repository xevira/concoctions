package com.xevira.concoctions.common.handlers;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.energy.CapabilityEnergy;

public class SynthesizerEnergyInputHandler extends InputItemStackHandler {

	public SynthesizerEnergyInputHandler(int slots) {
		super(slots);
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack)
	{
		if(stack.getCapability(CapabilityEnergy.ENERGY).isPresent())
			return true;
		
		if(ForgeHooks.getBurnTime(stack) > 0)
			return true;
		
		return false;
	}
}

package com.xevira.concoctions.common.items;

import java.util.List;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.block.FilledCauldronBlock;
import com.xevira.concoctions.common.block.tile.FilledCauldronTile;
import com.xevira.concoctions.common.fluids.PotionFluid;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

public class FilledCauldronItem extends BlockItem {
	private static final int WHITE = 0xFFFFFFFF;

	public FilledCauldronItem(Block blockIn, Properties builder) {
		super(blockIn, builder);
	}
	
	private static FluidStack getFluidStackFromItemStack(ItemStack stack)
	{
		if(stack.hasTag())
		{
			CompoundNBT tag = stack.getTag();
			
			if(tag.contains("BlockEntityTag"))
			{
				CompoundNBT root = tag.getCompound("BlockEntityTag");
				
				if(root.contains("fluid"))
				{
					return new FluidStack(Registry.POTION_FLUID.get(), 1, root.getCompound("fluid"));
				}
			}
		}
		
		return FluidStack.EMPTY;
	}
	
	private static String getLevelFromItemStack(ItemStack stack)
	{
		if(stack.hasTag())
		{
			CompoundNBT tag = stack.getTag();
			
			if(tag.contains("BlockStateTag"))
			{
				CompoundNBT root = tag.getCompound("BlockStateTag");
				
				if(root.contains("level"))
				{
					return root.getString("level");
				}
			}
		}
		
		return "0";
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		FluidStack fluidStack = getFluidStackFromItemStack(stack);
		if(!fluidStack.isEmpty() && fluidStack.getFluid() == Registry.POTION_FLUID.get())
		{
			PotionFluid potionFluid = (PotionFluid)fluidStack.getFluid();

			tooltip.add(new TranslationTextComponent("item.concoctions.filled_with").mergeStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
			tooltip.add(potionFluid.getAttributes().getDisplayName(fluidStack));
			potionFluid.addInformation(fluidStack, tooltip);

			String level = getLevelFromItemStack(stack);
			
			tooltip.add(StringTextComponent.EMPTY);
			tooltip.add(new TranslationTextComponent("item.concoctions.fill_level", level));
		}
		
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static int getFluidColor(ItemStack stack, int tintIndex) {
		//Concoctions.GetLogger().info("FilledCauldronItem.getFluidColor() called");
		
		if(tintIndex <= 0) return WHITE;
		
		if(!stack.hasTag()) return WHITE;
		
		CompoundNBT root = stack.getTag();
		if(!root.contains("BlockEntityTag")) return WHITE;
		
		CompoundNBT nbt = root.getCompound("BlockEntityTag");
		if(!nbt.contains("fluid")) return WHITE;

		FluidStack fluidStack = new FluidStack(Registry.POTION_FLUID.get(), 1, nbt.getCompound("fluid"));

		int color = fluidStack.getFluid().getAttributes().getColor(fluidStack);
		
		//Concoctions.GetLogger().info("FilledCauldronItem.getFluidColor(): color = {}", color);
		return color;
	}
	
}

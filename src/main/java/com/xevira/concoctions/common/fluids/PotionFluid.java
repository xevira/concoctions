package com.xevira.concoctions.common.fluids;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class PotionFluid extends Fluid {

	private static final int UNCOLORED_WITH_PARTIAL_TRANSPARENCY = 0xC0FFFFFF;

    private final FluidAttributes.Builder builder;
	public PotionFluid(ResourceLocation still, ResourceLocation flowing) {
		super();
		this.builder = FluidAttributes
			.builder(still, flowing)
			.temperature(293)
			.color(UNCOLORED_WITH_PARTIAL_TRANSPARENCY);
	}
   
	@Override
	protected FluidAttributes createAttributes() {
		return new PotionFluidAttributes(builder, this);
	}
	
	@Override
	public Item getFilledBucket() {
		return Items.AIR;
	}

	@Override
	protected boolean canDisplace(FluidState fluidState, IBlockReader blockReader, BlockPos pos, Fluid fluid, Direction direction) {
		return false;
	}

	@Override
	protected Vector3d getFlow(IBlockReader blockReader, BlockPos pos, FluidState fluidState) {
		return Vector3d.ZERO;
	}

	@Override
	public int getTickRate(IWorldReader p_205569_1_) {
		return 5;
	}

	@Override
	protected float getExplosionResistance() {
		return 100;
	}

	@Override
	public float getActualHeight(FluidState p_215662_1_, IBlockReader p_215662_2_, BlockPos p_215662_3_) {
		return 1;
	}

	@Override
	public float getHeight(FluidState p_223407_1_) {
		return 0;
	}

	@Override
	protected BlockState getBlockState(FluidState state) {
		return Blocks.AIR.getDefaultState();
	}

	@Override
	public boolean isSource(FluidState state) {
		return true;
	}

	@Override
	public int getLevel(FluidState state) {
		return 0;
	}

	@Override
	public VoxelShape func_215664_b(FluidState p_215664_1_, IBlockReader p_215664_2_, BlockPos p_215664_3_) {
		return VoxelShapes.fullCube();
	}
	
	public void addInformation(FluidStack fluidStack, List<ITextComponent> tooltip)
	{
		if(fluidStack!=null&&fluidStack.hasTag())
		{
			List<EffectInstance> effects = PotionUtils.getEffectsFromTag(fluidStack.getTag());
			List<Pair<Attribute, AttributeModifier>> attrPairs = Lists.newArrayList();
			if(effects.isEmpty())
				tooltip.add(new TranslationTextComponent("effect.none").mergeStyle(TextFormatting.GRAY));
			else
			{
				for(EffectInstance instance : effects)
				{
					IFormattableTextComponent itextcomponent = new TranslationTextComponent(instance.getEffectName());
					Effect effect = instance.getPotion();
					
		            Map<Attribute, AttributeModifier> map = effect.getAttributeModifierMap();
		            if (!map.isEmpty()) {
		               for(Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
		                  AttributeModifier attributemodifier = entry.getValue();
		                  AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), effect.getAttributeModifierAmount(instance.getAmplifier(), attributemodifier), attributemodifier.getOperation());
		                  attrPairs.add(new Pair<>(entry.getKey(), attributemodifier1));
		               }
		            }

					if(instance.getAmplifier() > 0)
						itextcomponent.appendString(" ").append(new TranslationTextComponent("potion.potency."+instance.getAmplifier()));
					if(instance.getDuration() > 20)
						itextcomponent.appendString(" (").appendString(EffectUtils.getPotionDurationString(instance, 1)).appendString(")");

					tooltip.add(itextcomponent.mergeStyle(effect.getEffectType().getColor()));
				}
			}
			
			if( fluidStack.getTag().getBoolean("DyedPotion"))
			{
				tooltip.add(new TranslationTextComponent("item.dyed").mergeStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
			}
			
			// Add attributes
			
			if (!attrPairs.isEmpty())
			{
				tooltip.add(StringTextComponent.EMPTY);
				tooltip.add((new TranslationTextComponent("potion.whenDrank")).mergeStyle(TextFormatting.DARK_PURPLE));

				for(Pair<Attribute, AttributeModifier> pair : attrPairs)
				{
					AttributeModifier attributemodifier2 = pair.getSecond();
					double d0 = attributemodifier2.getAmount();
					double d1;
					if (attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL)
						d1 = attributemodifier2.getAmount();
					else
						d1 = attributemodifier2.getAmount() * 100.0D;

					if (d0 > 0.0D)
						tooltip.add((new TranslationTextComponent("attribute.modifier.plus." + attributemodifier2.getOperation().getId(), ItemStack.DECIMALFORMAT.format(d1), new TranslationTextComponent(pair.getFirst().getAttributeName()))).mergeStyle(TextFormatting.BLUE));
					else if (d0 < 0.0D)
						tooltip.add((new TranslationTextComponent("attribute.modifier.take." + attributemodifier2.getOperation().getId(), ItemStack.DECIMALFORMAT.format(-d1), new TranslationTextComponent(pair.getFirst().getAttributeName()))).mergeStyle(TextFormatting.RED));
				}
			}
		}
	}

	public static class PotionFluidAttributes extends FluidAttributes
	{
		protected PotionFluidAttributes(Builder builder, Fluid fluid)
		{
			super(builder, fluid);
		}

		@Override
		public ITextComponent getDisplayName(FluidStack stack)
		{
			if(stack==null||!stack.hasTag())
				return super.getDisplayName(stack);
			
			CompoundNBT tag = stack.getTag();
			if( tag.contains("BasePotion"))
			{
				if( tag.contains("CustomPotionName"))
					return new TranslationTextComponent("item.concoctions.potion.prefix", tag.getString("CustomPotionName"));
				else
					return new TranslationTextComponent(Potion.getPotionTypeForName(tag.getString("BasePotion")).getNamePrefixed("item.minecraft.potion.effect."));
			}
			else if( tag.contains("CustomPotionName"))
				return new TranslationTextComponent("item.concoctions.solution.prefix", tag.getString("CustomPotionName"));
			else
				return new TranslationTextComponent("item.concoctions.solution");
		}

		@Override
		public int getColor(FluidStack stack)
		{
			if(stack==null||!stack.hasTag())
				return 0xff0000ff;
			
			CompoundNBT tag = stack.getTag();
			if( tag.contains("CustomPotionColor"))
				return 0xff000000|tag.getInt("CustomPotionColor");
			
			return 0xff000000|PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromTag(stack.getTag()));
		}
	}
}

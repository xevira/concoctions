package com.xevira.concoctions.common.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.block.tile.FilledCauldronTile;
import com.xevira.concoctions.common.entities.ItemEntityNoImbue;
import com.xevira.concoctions.common.utils.Utils;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DrinkHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;

public class FilledCauldronBlock extends Block
{
	public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_0_15;
	public static final int LEVELS_PER_POTION = 5;
	private static final VoxelShape INSIDE = makeCuboidShape(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
	protected static final VoxelShape SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), VoxelShapes.or(makeCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D), makeCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D), makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), INSIDE), IBooleanFunction.ONLY_FIRST);
	
	public FilledCauldronBlock()
	{
		super(AbstractBlock.Properties.create(Material.IRON, MaterialColor.STONE).setRequiresTool().hardnessAndResistance(2.0F).notSolid());
		this.setDefaultState(this.stateContainer.getBaseState().with(LEVEL, Integer.valueOf(0)));
	}
	
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
	
    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return Registry.FILLED_CAULDRON_TILE.get().create();
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}

	public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return INSIDE;
	}
	
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) 
	{
		int level = state.get(LEVEL);
		float f = (float)pos.getY() + (4.0F + (float)(0.8F * level)) / 16.0F;
		if (!worldIn.isRemote && level > 0 && entityIn.getPosY() <= (double)f)
		{
			FilledCauldronTile tile = (FilledCauldronTile)worldIn.getTileEntity(pos);
			if(tile == null) return;

			FluidStack fluid = tile.getPotionFluid();
			
			if( entityIn instanceof LivingEntity)
			{
				if( entityIn.isBurning() )
				{
					entityIn.extinguish();
					this.setCauldronLevel(worldIn, pos, state, level - 1);
				}
				else
				{
					// TODO: Apply the potion effects
				}
			}
			else if( (entityIn instanceof ItemEntity) && !(entityIn instanceof ItemEntityNoImbue) )
			{
				
				ItemEntity itemEntity = (ItemEntity)entityIn;
				ItemStack itemStack = itemEntity.getItem();

				// TODO: Add Imbuing full recipe system 

				if(itemStack.getItem() == Items.LILY_OF_THE_VALLEY) 
				{
					if(fluid.getFluid() == Registry.POTION_FLUID.get())
					{
						List<EffectInstance> effects = PotionUtils.getEffectsFromTag(fluid.getTag());
						boolean isValid = false;
						int cost = 3;
						
						// Make sure that it only has Regeneration that is stronger than the base
						for(EffectInstance effect : effects)
						{
							if( effect.getPotion() == Effects.REGENERATION && effect.getAmplifier() > 0.0f && level >= cost)
							{
								isValid = true;
							}
							else
							{
								isValid = false;
								break;
							}
						}
						
						
						if(isValid)
						{
							int created = Math.min(level / cost, itemStack.getCount());
							Concoctions.GetLogger().info("Created {} roses", created);

							ItemStack result = new ItemStack(Items.ROSE_BUSH, created);

							ItemEntityNoImbue resultEntity = new ItemEntityNoImbue(itemEntity.getEntityWorld(),
									itemEntity.getPosX(), itemEntity.getPosY(), itemEntity.getPosZ(), result);
							resultEntity.setPickupDelay(20);
							itemEntity.getEntityWorld().addEntity(resultEntity);

							itemStack.shrink(created);
							if(itemStack.isEmpty())
								itemEntity.remove();
							
							this.setCauldronLevel(worldIn, pos, state, level - created * cost);
						}
					}
				}
			}
		}
	}
	
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		ItemStack itemstack = player.getHeldItem(handIn);
		if( itemstack.isEmpty() )
			return ActionResultType.PASS;
		else
		{
			int level = state.get(LEVEL);
			Item item = itemstack.getItem();
	         
			if(item == Items.POTION)
			{
				FilledCauldronTile tile = (FilledCauldronTile)worldIn.getTileEntity(pos);
				if( tile == null )
					return ActionResultType.FAIL;
				 
				FluidStack fluid = Utils.getPotionFluidFromNBT(itemstack.getTag());
				FluidStack fluidCauldron = tile.getPotionFluid();
				
				if( fluidCauldron.isFluidEqual(fluid) )
				{
					if (level <= 2 * LEVELS_PER_POTION && !worldIn.isRemote)
		            {
		            	if (!player.abilities.isCreativeMode)
		            	{
		            		ItemStack itemstack3 = new ItemStack(Items.GLASS_BOTTLE);
		            		player.setHeldItem(handIn, itemstack3);
		            		if (player instanceof ServerPlayerEntity)
		            		{
		            			((ServerPlayerEntity)player).sendContainerToPlayer(player.container);
		            		}
			            	player.addStat(Stats.USE_CAULDRON);
		            	}

		            	worldIn.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
		            	this.setCauldronLevel(worldIn, pos, state, level + LEVELS_PER_POTION);
		            }

				}
				return ActionResultType.func_233537_a_(worldIn.isRemote);	// Returns SUCCESS for client, CONSUME for server
			}
			else if(item == Items.GLASS_BOTTLE)
			{
				FilledCauldronTile tile = (FilledCauldronTile)worldIn.getTileEntity(pos);
				if( tile == null )
					return ActionResultType.FAIL;
				
				if( level >= LEVELS_PER_POTION && !worldIn.isRemote )
				{
					if (!player.abilities.isCreativeMode)
					{
						FluidStack fluidCauldron = tile.getPotionFluid();
						ItemStack itemstack4 = new ItemStack(Items.POTION);
						Utils.addPotionEffectsToItemStack(fluidCauldron, itemstack4);
						itemstack.shrink(1);
						if(itemstack.isEmpty())
							player.setHeldItem(handIn, itemstack4);
						else if (!player.inventory.addItemStackToInventory(itemstack4))
							player.dropItem(itemstack4, false);
						else if (player instanceof ServerPlayerEntity)
							((ServerPlayerEntity)player).sendContainerToPlayer(player.container);
						player.addStat(Stats.USE_CAULDRON);
						player.addStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));
						worldIn.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
					}
					
					this.setCauldronLevel(worldIn, pos, state, level - LEVELS_PER_POTION);
				}
				return ActionResultType.func_233537_a_(worldIn.isRemote);	// Returns SUCCESS for client, CONSUME for server
			}
			/*
			else if(item == Items.SPONGE)
			{
				// Used to empty the cauldron out (up to) one potion volume at a time
				if (level > 0 && !worldIn.isRemote)
	            {
	            	if (!player.abilities.isCreativeMode)
	            	{
	            		ItemStack itemstack3 = new ItemStack(Items.WET_SPONGE);
	            		player.setHeldItem(handIn, itemstack3);
	            		if (player instanceof ServerPlayerEntity)
	            		{
	            			((ServerPlayerEntity)player).sendContainerToPlayer(player.container);
	            		}
		            	player.addStat(Stats.USE_CAULDRON);
	            	}

	            	this.setCauldronLevel(worldIn, pos, state, level - LEVELS_PER_POTION);
	            }
            	return ActionResultType.func_233537_a_(worldIn.isRemote);	// Returns SUCCESS for client, CONSUME for server
			}
			*/

		}
		
		return ActionResultType.PASS;
	}
	
	public void setCauldronLevel(World worldIn, BlockPos pos, BlockState state, int level)
	{
		level = MathHelper.clamp(level, 0, 15);
		
		if( level > 0)
		{
			worldIn.setBlockState(pos, state.with(LEVEL, Integer.valueOf(level)), 2);
			worldIn.updateComparatorOutputLevel(pos, this);
		}
		else
		{
			BlockState newState = Blocks.CAULDRON.getDefaultState();
			worldIn.setBlockState(pos, newState);
		}
	}
	
	/**
	 * @deprecated call via {@link IBlockState#hasComparatorInputOverride()} whenever possible. Implementing/overriding
	 * is fine.
	 */
	public boolean hasComparatorInputOverride(BlockState state)
	{
		return true;
	}

	/**
	 * @deprecated call via {@link IBlockState#getComparatorInputOverride(World,BlockPos)} whenever possible.
	 * Implementing/overriding is fine.
	 */
	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos)
	{
		return blockState.get(LEVEL);
	}

	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(LEVEL);
	}

	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type)
	{
		return false;
	}
}

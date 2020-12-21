package com.xevira.concoctions.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.xevira.concoctions.common.block.tile.IncenseBurnerTile;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

public class IncenseBurnerBlock extends Block {
	public static final VoxelShape SHAPE_EMPTY = makeCuboidShape(2.0, 0.0, 2.0, 15.0, 2.0, 15.0);
	public static final VoxelShape SHAPE_INCENSE = makeCuboidShape(7.0, 2.0, 7.0, 9.0, 12.0, 9.0);
	public static final VoxelShape SHAPE_WITH_INCENSE = VoxelShapes.or(SHAPE_EMPTY, SHAPE_INCENSE);
	public static final VoxelShape SHAPE_FLAME = makeCuboidShape(7.0, 10.0, 7.0, 9.0, 12.0, 9.0);
	public static final BooleanProperty HAS_INCENSE = BooleanProperty.create("has_incense");
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	private static final Random random = new Random();

	public IncenseBurnerBlock() {
		super(Block.Properties
				.create(new Material(MaterialColor.IRON, false, true, true, false, false, false, PushReaction.NORMAL))
				.sound(SoundType.METAL).hardnessAndResistance(2.0f, 6.0f).notSolid().harvestTool(ToolType.PICKAXE)
				.harvestLevel(1));
		setDefaultState(getStateContainer().getBaseState().with(HAS_INCENSE, false).with(LIT, false));
	}
	

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(HAS_INCENSE, LIT);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState()
				.with(HAS_INCENSE, false)
				.with(LIT, false);
	}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return Registry.INCENSE_BURNER_TILE.get().create();
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return state.get(HAS_INCENSE) ? SHAPE_WITH_INCENSE : SHAPE_EMPTY;
	}
	
	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		return state.get(LIT) ? 7 : 0;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(newState.getBlock() != this)
		{
			IncenseBurnerTile tile = (IncenseBurnerTile)worldIn.getTileEntity(pos);
			if(tile != null)
				tile.dropItems(worldIn, pos);
			super.onReplaced(state, worldIn, pos, newState, isMoving);
		}
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		IncenseBurnerTile tile = (IncenseBurnerTile)worldIn.getTileEntity(pos);
		if(tile == null)
			return ActionResultType.FAIL; 
		
		ItemStack itemstack = player.getHeldItem(handIn);
		if( itemstack.isEmpty() )
		{
			if(state.get(HAS_INCENSE) && !state.get(LIT))
			{
				if(!worldIn.isRemote)
					tile.removeIncense(worldIn, pos);
			}
		}
		else
		{
			Item item = itemstack.getItem();
			// TileEntity
			
			if(item == Registry.INCENSE_ITEM.get())
			{
				if(!state.get(HAS_INCENSE))
				{
					if(!worldIn.isRemote && tile.setIncense(itemstack))
					{
				        worldIn.playSound(player, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
						worldIn.setBlockState(pos, state.with(HAS_INCENSE, Boolean.valueOf(true)), 11);
						if (!player.isCreative())
						{
							itemstack.shrink(1);
						}
					}
					return ActionResultType.func_233537_a_(worldIn.isRemote);
				}
			}
			else if(item == Items.FLINT_AND_STEEL)
			{
				if(state.get(HAS_INCENSE) && !state.get(LIT))
				{
					if(!worldIn.isRemote && tile.setLit())
					{
				        worldIn.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
						worldIn.setBlockState(pos, state.with(LIT, Boolean.valueOf(true)), 11);
						if (!player.isCreative())
						{
							itemstack.damageItem(1, player, (player1) -> { player1.sendBreakAnimation(handIn); });
						}
					}
					return ActionResultType.func_233537_a_(worldIn.isRemote);
				}
			}
			
		}
		return ActionResultType.PASS;
	}
	
	private double getPosRandom(Random rand, double p, double w)
	{
		return p + (rand.nextDouble() - 0.5D) * w;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand)
	{
		if( stateIn.get(HAS_INCENSE) && stateIn.get(LIT) )
		{
			IncenseBurnerTile tile = (IncenseBurnerTile)worldIn.getTileEntity(pos);
			if(tile == null)
				return;
			VoxelShape voxelshape = SHAPE_FLAME;
			AxisAlignedBB bbox = voxelshape.getBoundingBox();
			Vector3d vector3d = bbox.getCenter();
			double d0 = (double)pos.getX() + vector3d.x;
			double d1 = (double)pos.getY() + bbox.maxY;
			double d2 = (double)pos.getZ() + vector3d.z;
	
			// Do torch particles
			worldIn.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
			worldIn.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
			
			// Do potion particles
			if(rand.nextBoolean())
			{
				int color = tile.getIncenseColor();
	            double r = (double)(color >> 16 & 255) / 255.0D;
	            double g = (double)(color >> 8 & 255) / 255.0D;
	            double b = (double)(color >> 0 & 255) / 255.0D;
	            worldIn.addParticle(ParticleTypes.ENTITY_EFFECT, getPosRandom(rand, d0, 0.3D), getPosRandom(rand, d1 + 0.1D, 0.3D), getPosRandom(rand, d2, 0.3D), r, g, b);
			}
		}
	}

}

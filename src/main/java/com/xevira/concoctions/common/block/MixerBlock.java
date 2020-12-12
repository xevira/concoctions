package com.xevira.concoctions.common.block;

import javax.annotation.Nullable;

import com.xevira.concoctions.common.block.tile.MixerTile;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;

public class MixerBlock extends ModBlock {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

	public MixerBlock() {
		super(Block.Properties
				.create(new Material(MaterialColor.IRON, false, true, true, false, false, false, PushReaction.NORMAL))
				.sound(SoundType.METAL).hardnessAndResistance(2.0f, 6.0f).notSolid().harvestTool(ToolType.PICKAXE)
				.harvestLevel(1));
		setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH));
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(FACING);
	}
	
	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return Registry.MIXER_TILE.get().create();
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (newState.getBlock() != this) {
			MixerTile tileEntity = (MixerTile)worldIn.getTileEntity(pos);
			if (tileEntity != null) {
				tileEntity.dropItems(worldIn, pos);
			}
			super.onReplaced(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand hand, BlockRayTraceResult blockRayTraceResult) {
		// Only execute on the server
		if (worldIn.isRemote)
			return ActionResultType.SUCCESS;

		TileEntity te = worldIn.getTileEntity(pos);
		if (!(te instanceof MixerTile)) {

			return ActionResultType.FAIL;
		}

		NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) te, pos);
		return ActionResultType.SUCCESS;
	}

}

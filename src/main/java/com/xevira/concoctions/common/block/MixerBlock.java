package com.xevira.concoctions.common.block;

import javax.annotation.Nullable;

import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

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
}

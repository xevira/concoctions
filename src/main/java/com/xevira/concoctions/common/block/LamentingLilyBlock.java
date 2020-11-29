package com.xevira.concoctions.common.block;

import java.util.Random;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BushBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LamentingLilyBlock extends BushBlock
{
	public static final IntegerProperty STAGE = Registry.STAGE_0_3;
	protected static final VoxelShape[] SHAPE = new VoxelShape[] {
			Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 12.0D, 12.0D, 12.0D),	// Stage 0
			Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D),	// Stage 1
			Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 15.0D, 14.0D, 15.0D),	// Stage 2
			Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 15.0D, 16.0D, 15.0D)	// Stage 3
	};
	protected static final VoxelShape FLOWERS = Block.makeCuboidShape(3.0D, 5.0D, 3.0D, 13.0D, 16.0D, 13.0D);
	private static int BLOOMING_STAGE = 3;
	
	public LamentingLilyBlock()
	{
		super(AbstractBlock.Properties.create(Material.PLANTS).doesNotBlockMovement().zeroHardnessAndResistance().sound(SoundType.PLANT).tickRandomly());
		this.setDefaultState(this.stateContainer.getBaseState().with(STAGE, Integer.valueOf(0)));
	}
	
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
	
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(STAGE);
    }

	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		Vector3d vector3d = state.getOffset(worldIn, pos);
		return SHAPE[state.get(STAGE)].withOffset(vector3d.x, vector3d.y, vector3d.z);
	}
	
	private VoxelShape getFlowerShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		Vector3d vector3d = state.getOffset(worldIn, pos);
		return FLOWERS.withOffset(vector3d.x, vector3d.y, vector3d.z);
	}
	
	public AbstractBlock.OffsetType getOffsetType()
	{
		return AbstractBlock.OffsetType.XZ;
	}
	
	private int getLightLevel(World world, BlockPos pos)
	{
		if (world.getDimensionType().hasSkyLight()) {
			int lighti = world.getLightFor(LightType.SKY, pos) - world.getSkylightSubtracted();
			float f = world.getCelestialAngleRadians(1.0F);
			if(lighti > 0)
			{
				float f1 = f < (float)Math.PI ? 0.0F : ((float)Math.PI * 2F);
				f = f + (f1 - f) * 0.2F;
				lighti = Math.round((float)lighti * MathHelper.cos(f));
			}
			return MathHelper.clamp(lighti, 0, 15);
		}

		return 0;
	}
	
	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random)
	{
//		Concoctions.GetLogger().info("Lamenting Lily random tick called");
		
		if (!worldIn.isAreaLoaded(pos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
		
		int light = getLightLevel(worldIn, pos);
		int lightBlock = worldIn.getLightFor(LightType.BLOCK, pos);
		boolean seeSky = worldIn.canSeeSky(pos); 
		
		if( !seeSky || lightBlock > light )
			light = lightBlock;
		
		//Concoctions.GetLogger().info("Lamenting Lily: stage {}, light {}, seeSky {}", state.get(STAGE), light, seeSky);
		
		switch(state.get(STAGE))
		{
		case 0:
			if(light < 8)
				worldIn.setBlockState(pos, state.with(STAGE, Integer.valueOf(1)));
			break;
		case 1:
			if(light < 4)
				worldIn.setBlockState(pos, state.with(STAGE, Integer.valueOf(2)));
			else if(light >= 8)
				worldIn.setBlockState(pos, state.with(STAGE, Integer.valueOf(0)));
			
			break;
		case 2:
			if(light <= 0 && seeSky)
				worldIn.setBlockState(pos, state.with(STAGE, Integer.valueOf(3)));
			else if(light >= 4)
				worldIn.setBlockState(pos, state.with(STAGE, Integer.valueOf(1)));
			break;
		case 3:
			if( light > 0 || !seeSky)
				worldIn.setBlockState(pos, state.with(STAGE, Integer.valueOf(2)));
			else
			{
				// Adjust chance so that it is highest probability at New Moon and the least at Full Moon 
				float moon = DimensionType.MOON_PHASE_FACTORS[worldIn.getMoonPhase()];
				float chance = 0.25f - 0.2f * moon;
				if( random.nextDouble() < chance )
				{
					// Spawn a ghast tear
					ItemStack tear = new ItemStack(Items.GHAST_TEAR);
					
					double x = pos.getX() + 0.2D * random.nextDouble() + 0.4D;
					double y = pos.getY() + 0.2D * random.nextDouble() + 0.65D;
					double z = pos.getZ() + 0.2D * random.nextDouble() + 0.4D;
					
					ItemEntity itemEntity = new ItemEntity(worldIn, x, y, z, tear);
					itemEntity.setMotion(0, -0.1D, 0);

					worldIn.addEntity(itemEntity);
				}
				
				if( !worldIn.isRemote )
					worldIn.getPendingBlockTicks().scheduleTick(pos, this, random.nextInt(300) + 900);
			}
			break;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand)
	{
		int level = stateIn.get(STAGE);
		int light = getLightLevel(worldIn, pos);
		
		if( level == BLOOMING_STAGE && light <= 0)
		{
			VoxelShape voxelshape = this.getFlowerShape(stateIn, worldIn, pos, ISelectionContext.dummy());
			Vector3d vector3d = voxelshape.getBoundingBox().getCenter();
			double d0 = (double)pos.getX() + vector3d.x;
			double d1 = (double)pos.getZ() + vector3d.z;
	
			for(int i = 0; i < 3; ++i) {
				if (rand.nextInt(10) == 0) {
					worldIn.addParticle(ParticleTypes.DRIPPING_WATER, d0 + rand.nextDouble() / 5.0D, (double)pos.getY() + (0.5D - rand.nextDouble()), d1 + rand.nextDouble() / 5.0D, 0.0D, 0.0D, 0.0D);
				}
			}
		}
	}

}

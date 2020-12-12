package com.xevira.concoctions.common.block;

import java.util.Random;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FireBlossomBlock extends BushBlock {
	
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 15.0D, 13.0D);
	protected static final VoxelShape FLOWER = Block.makeCuboidShape(3.0D, 8.0D, 3.0D, 13.0D, 15.0D, 13.0D);
	private static final double CHANCE = 0.1D;

	public FireBlossomBlock()
	{
		super(AbstractBlock.Properties.create(Material.PLANTS).doesNotBlockMovement().zeroHardnessAndResistance().
				sound(SoundType.PLANT).setLightLevel((state)-> { return 8; }).tickRandomly());
	}

	@Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		Vector3d vector3d = state.getOffset(worldIn, pos);
		return SHAPE.withOffset(vector3d.x, vector3d.y, vector3d.z);
	}
	
	public AbstractBlock.OffsetType getOffsetType()
	{
		return AbstractBlock.OffsetType.XZ;
	}

	@Override
	protected boolean isValidGround(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return state.isIn(Blocks.NETHERRACK) || state.isIn(Blocks.CRIMSON_NYLIUM) || state.isIn(Blocks.WARPED_NYLIUM);
	}

	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand)
	{
		VoxelShape voxelshape = this.getShape(stateIn, worldIn, pos, ISelectionContext.dummy());
		Vector3d vector3d = voxelshape.getBoundingBox().getCenter();
		double d0 = (double)pos.getX() + vector3d.x;
		double d1 = (double)pos.getZ() + vector3d.z;
		
		for(int i = 0; i < 3; ++i)
		{
			if (rand.nextBoolean())
	            worldIn.addParticle(ParticleTypes.FLAME, d0 + rand.nextDouble() / 5.0D, (double)pos.getY() + (0.5D - rand.nextDouble()), d1 + rand.nextDouble() / 5.0D, 0.0D, 0.0D, 0.0D);
		}
	}
	
	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random)
	{
		if (!worldIn.isAreaLoaded(pos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
		
		double chance = (worldIn.getDimensionKey() == World.THE_NETHER) ? (3 * CHANCE) : CHANCE;
		
		if( random.nextDouble() < CHANCE )
		{
			ItemStack cream = new ItemStack(Items.MAGMA_CREAM);
			
			double x = pos.getX() + 0.2D * random.nextDouble() + 0.4D;
			double y = pos.getY() + 0.2D * random.nextDouble() + 0.65D;
			double z = pos.getZ() + 0.2D * random.nextDouble() + 0.4D;
			
			ItemEntity itemEntity = new ItemEntity(worldIn, x, y, z, cream);
			itemEntity.setMotion(0, -0.1D, 0);

			worldIn.addEntity(itemEntity);
		}
		
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, random.nextInt(300) + 900);
	}
	
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
	{
		if(entityIn instanceof LivingEntity && !entityIn.isImmuneToFire())
		{
			entityIn.forceFireTicks(entityIn.getFireTimer() + 1);
			if (entityIn.getFireTimer() == 0)
			{
				entityIn.setFire(8);
			}
			entityIn.attackEntityFrom(DamageSource.IN_FIRE, (worldIn.getDimensionKey() == World.THE_NETHER) ? 1.5f : 0.5f);
		}
	}
}

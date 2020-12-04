package com.xevira.concoctions.common.block;

import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemTier;
import net.minecraftforge.common.ToolType;

public class BrokenBedrockBlock extends Block {
	public BrokenBedrockBlock(float hardness, float resistance) {
		super(AbstractBlock.Properties.create(Material.ROCK).setRequiresTool().hardnessAndResistance(hardness, resistance).harvestTool(ToolType.PICKAXE).harvestLevel(ItemTier.NETHERITE.getHarvestLevel()).setAllowsSpawn(ModBlock::neverAllowSpawn));
		this.setDefaultState(this.stateContainer.getBaseState());
	}
	
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
    
    public static boolean isValidBlock(Block block)
    {
    	if(block == Blocks.BEDROCK) return true;
    	if(block == Registry.CRACKED_BEDROCK.get()) return true;
    	if(block == Registry.BROKEN_BEDROCK.get()) return true;
    	if(block == Registry.CRUMBLING_BEDROCK.get()) return true;
    	//if(block == Registry.SHATTERED_BEDROCK.get()) return true;		// This no longer can supply void essence
    	
    	return false;
    }
    
    public static BlockState nextBlockState(Block block)
    {
    	if(block == Blocks.BEDROCK)
    		return Registry.CRACKED_BEDROCK.get().getDefaultState();

    	if(block == Registry.CRACKED_BEDROCK.get())
    		return Registry.BROKEN_BEDROCK.get().getDefaultState();

    	if(block == Registry.BROKEN_BEDROCK.get())
    		return Registry.CRUMBLING_BEDROCK.get().getDefaultState();

    	if(block == Registry.CRUMBLING_BEDROCK.get())
    		return Registry.SHATTERED_BEDROCK.get().getDefaultState();

    	return null;
    }
}

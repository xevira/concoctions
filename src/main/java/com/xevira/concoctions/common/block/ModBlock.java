package com.xevira.concoctions.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class ModBlock extends Block {
    public ModBlock(){
        this(defaultProperties());
    }

    public ModBlock(Properties properties) {
        super(properties);
    }

    public static Block.Properties defaultProperties(){
        return Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(2.0f, 6.0f);
    }
    
    public static Boolean neverAllowSpawn(BlockState state, IBlockReader reader, BlockPos pos, EntityType<?> entity) {
        return (boolean)false;
     }

}

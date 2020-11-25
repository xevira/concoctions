package com.xevira.concoctions.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

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
}

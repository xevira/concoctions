package com.xevira.concoctions.setup;

import java.nio.file.Path;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

public class Config {

    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_EFFECTS = "effects";
    public static final String CATEGORY_INCENSE_BURNER = "incense_burner";


    public static ForgeConfigSpec SERVER_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;
    
    public static ForgeConfigSpec.IntValue INCENSE_BURNER_DURATION;

    static {

        ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

        SERVER_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        SERVER_BUILDER.pop();
        
        SERVER_BUILDER.comment("Incense Burner settings").push(CATEGORY_INCENSE_BURNER);
        
        INCENSE_BURNER_DURATION = SERVER_BUILDER.comment("How many minutes an incense lasts when burned (minimum 5, maximum 60, default 10)").defineInRange("duration", 10, 5, 60);
        
        SERVER_BUILDER.pop();
        
        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path)
    {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent)
    {
    	
    }

    @SubscribeEvent
    public static void onReload(final ModConfig.Reloading configEvent) {
    }
}

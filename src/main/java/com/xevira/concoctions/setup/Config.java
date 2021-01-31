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
    public static final String CATEGORY_SYNTHESIZER = "synthesizer";
    public static final String CATEGORY_POWER = "power";


    public static ForgeConfigSpec SERVER_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;
    
    public static ForgeConfigSpec.IntValue INCENSE_BURNER_DURATION;
    
    public static ForgeConfigSpec.IntValue SYNTHESIZER_POWER_RATIO;
    public static ForgeConfigSpec.IntValue SYNTHESIZER_STRENGTH_RATIO;
    
    public static ForgeConfigSpec.IntValue FUEL_TO_POWER;
    public static ForgeConfigSpec.IntValue MAX_POWER_TRANSFER;

    static {

        ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

        SERVER_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        SERVER_BUILDER.pop();
       
        SERVER_BUILDER.comment("Incense Burner settings").push(CATEGORY_INCENSE_BURNER);
        INCENSE_BURNER_DURATION = SERVER_BUILDER.comment("How many minutes an incense lasts when burned?").defineInRange("duration", 10, 5, 60);
        SERVER_BUILDER.pop();
        
        SERVER_BUILDER.comment("Synthesizer settings").push(CATEGORY_SYNTHESIZER);
        SYNTHESIZER_POWER_RATIO = SERVER_BUILDER.comment("How much power used when no catalyst is present?").defineInRange("ratioPower", 1000, 1, Integer.MAX_VALUE);
        SYNTHESIZER_STRENGTH_RATIO = SERVER_BUILDER.comment("How much catalyst does power provide used when no catalyst is present?").defineInRange("ratioPower", 1, 1, Integer.MAX_VALUE);
        SERVER_BUILDER.pop();
        
        SERVER_BUILDER.comment("Power Settings").push(CATEGORY_POWER);
        FUEL_TO_POWER = SERVER_BUILDER.comment("Power generated per tick for fuel items.").defineInRange("fuelToPower", 5, 0, Integer.MAX_VALUE);
        MAX_POWER_TRANSFER = SERVER_BUILDER.comment("Maximum power transfer rate per tick.").defineInRange("maxPowerTransfer", 1000000, 0, Integer.MAX_VALUE);
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

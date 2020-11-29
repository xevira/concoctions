package com.xevira.concoctions.common.network;

import com.xevira.concoctions.Concoctions;

import com.xevira.concoctions.common.network.packets.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = Integer.toString(1);
    private static short index = 0;
    
    public static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Concoctions.MOD_ID, "main_network_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();
    
    public static void register()
    {
    	int id = 0;
    	
    	HANDLER.registerMessage(id++, PacketPotionRename.class,	PacketPotionRename::encode,		PacketPotionRename::decode,		PacketPotionRename.Handler::handle);
    	HANDLER.registerMessage(id++, PacketBounce.class,		PacketBounce::encode,			PacketBounce::decode,			PacketBounce.Handler::handle);
    	HANDLER.registerMessage(id++, PacketPlayerCaps.class,	PacketPlayerCaps::encode,		PacketPlayerCaps::decode,		PacketPlayerCaps.Handler::handle);
    }
    
    public static void sendTo(Object msg, ServerPlayerEntity player) {
        if (!(player instanceof FakePlayer))
            HANDLER.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToAll(Object msg, World world) {
        //Todo Maybe only send to nearby players?
        for (PlayerEntity player : world.getPlayers()) {
            if (!(player instanceof FakePlayer))
                HANDLER.sendTo(msg, ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public static void sendToServer(Object msg) {
        HANDLER.sendToServer(msg);
    }
}

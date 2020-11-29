package com.xevira.concoctions.common.network.packets;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketBounce {
	
	public static void encode(PacketBounce pkt, PacketBuffer buffer) { }
	
	public static PacketBounce decode(PacketBuffer buffer) { return new PacketBounce(); }
	
	public static class Handler {
		public static void handle(PacketBounce pkt, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() -> {
				ServerPlayerEntity player = ctx.get().getSender();
				
				if(player == null)
					return;

				player.fallDistance = 0.0f;
			});
			
			ctx.get().setPacketHandled(true);
		}
	}

}

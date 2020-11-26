package com.xevira.concoctions.common.network.packets;

import java.util.function.Supplier;

import com.xevira.concoctions.common.container.BrewingStationContainer;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketPotionRename {
	private static final int MAX_LENGTH = 35;
	private final String name;
	
	public PacketPotionRename(String name)
	{
		this.name = name;
	}
	
	public static void encode(PacketPotionRename pkt, PacketBuffer buffer)
	{
		buffer.writeString(pkt.name, MAX_LENGTH);
	}
	
	public static PacketPotionRename decode(PacketBuffer buffer)
	{
		return new PacketPotionRename(buffer.readString());
	}
	
	public static class Handler {
		public static void handle(PacketPotionRename pkt, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() -> {
				ServerPlayerEntity player = ctx.get().getSender();
				
				if(player == null)
					return;
				
				if(player.openContainer instanceof BrewingStationContainer )
				{
					((BrewingStationContainer)player.openContainer).updateItemName(pkt.name);
				}
			});
			
			ctx.get().setPacketHandled(true);
		}
	}
}

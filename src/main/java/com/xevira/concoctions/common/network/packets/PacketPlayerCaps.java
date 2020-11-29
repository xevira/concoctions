package com.xevira.concoctions.common.network.packets;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketPlayerCaps
{
	public final boolean state;
	
	public PacketPlayerCaps(boolean state)
	{
		this.state = state;
	}
	
	public static void encode(PacketPlayerCaps pkt, PacketBuffer buffer)
	{
		buffer.writeBoolean(pkt.state);
	}
	
	public static PacketPlayerCaps decode(PacketBuffer buffer)
	{
		return new PacketPlayerCaps(buffer.readBoolean());
	}
	
	public static class Handler {
		public static void handle(PacketPlayerCaps pkt, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() -> {
				ServerPlayerEntity player = ctx.get().getSender();
				
				if(player == null)
					return;

				if(player.interactionManager.getGameType() == GameType.SPECTATOR && !pkt.state)
					player.interactionManager.setGameType(GameType.SURVIVAL);
				else if(player.interactionManager.getGameType() == GameType.SURVIVAL && pkt.state)
					player.interactionManager.setGameType(GameType.SPECTATOR);

			});
			
			ctx.get().setPacketHandled(true);
		}
	}

}

package com.xevira.concoctions.common.network.packets;

import java.util.function.Supplier;

import com.xevira.concoctions.common.container.MixerContainer;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketMixerValveChanges
{
	public final byte valve;
	public final byte state;
	
	public PacketMixerValveChanges(byte valve, byte state)
	{
		this.valve = valve;
		this.state = state;
	}
	
	public static void encode(PacketMixerValveChanges pkt, PacketBuffer buffer)
	{
		buffer.writeByte(pkt.valve);
		buffer.writeByte(pkt.state);
	}
	
	public static PacketMixerValveChanges decode(PacketBuffer buffer)
	{
		byte valve = buffer.readByte();
		byte state = buffer.readByte();
		
		return new PacketMixerValveChanges(valve, state);
	}
	
	public static class Handler {
		public static void handle(PacketMixerValveChanges pkt, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() -> {
				ServerPlayerEntity player = ctx.get().getSender();
				
				if(player == null)
					return;

				if(!(player.openContainer instanceof MixerContainer))
					return;
				
				MixerContainer container = (MixerContainer)player.openContainer;
				container.tile.setValve(pkt.valve, pkt.state);
			});
			
			ctx.get().setPacketHandled(true);
		}
	}


}

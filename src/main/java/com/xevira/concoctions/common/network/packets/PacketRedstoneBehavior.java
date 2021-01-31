package com.xevira.concoctions.common.network.packets;

import java.util.function.Supplier;

import com.xevira.concoctions.common.container.IContainerRedstoneBehavior;
import com.xevira.concoctions.common.container.MixerContainer;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketRedstoneBehavior {
	
	public final byte behavior;
	
	public PacketRedstoneBehavior(int behavior)
	{
		this((byte)(behavior & 0x7F));
	}
	
	public PacketRedstoneBehavior(byte behavior)
	{
		this.behavior = behavior;
	}
	
	public static void encode(PacketRedstoneBehavior pkt, PacketBuffer buffer)
	{
		buffer.writeByte(pkt.behavior);
	}
	
	public static PacketRedstoneBehavior decode(PacketBuffer buffer)
	{
		return new PacketRedstoneBehavior(buffer.readByte());
	}
	
	public static class Handler
	{
		public static void handle(PacketRedstoneBehavior pkt, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() -> {
				ServerPlayerEntity player = ctx.get().getSender();
				
				if(player == null)
					return;

				if(!(player.openContainer instanceof IContainerRedstoneBehavior))
					return;
				
				IContainerRedstoneBehavior container = (IContainerRedstoneBehavior)player.openContainer;
				container.setRedstoneBehavior(pkt.behavior);
			});
			
			ctx.get().setPacketHandled(true);

		}
	}

}

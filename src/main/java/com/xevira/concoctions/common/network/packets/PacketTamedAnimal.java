package com.xevira.concoctions.common.network.packets;

import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketTamedAnimal
{
	public final int Id;
	
	public PacketTamedAnimal(int id)
	{
		this.Id = id;
	}
	
	public static void encode(PacketTamedAnimal pkt, PacketBuffer buffer) {
		buffer.writeInt(pkt.Id);
	}
	
	public static PacketTamedAnimal decode(PacketBuffer buffer) { 
		return new PacketTamedAnimal(buffer.readInt());
	}
	
	public static class Handler {
		public static void handle(PacketTamedAnimal pkt, Supplier<NetworkEvent.Context> ctx)
		{
			if(ctx.get().getDirection().getReceptionSide().isServer())
			{
				ctx.get().setPacketHandled(true);
				return;
			}
			
			ctx.get().enqueueWork(new Runnable() {
				
				@Override
				public void run() {
					ClientWorld world = Minecraft.getInstance().world;
					Entity entity = world.getEntityByID(pkt.Id);
					Random random = new Random();
					
					for(int i = 0; i < 7; ++i) {
						double d0 = random.nextGaussian() * 0.02D;
						double d1 = random.nextGaussian() * 0.02D;
						double d2 = random.nextGaussian() * 0.02D;
						world.addParticle(ParticleTypes.HEART, entity.getPosXRandom(1.0D), entity.getPosYRandom() + 0.5D, entity.getPosZRandom(1.0D), d0, d1, d2);
					}
				}
			});
			
			ctx.get().setPacketHandled(true);
		}
	}


}

package com.xevira.concoctions.common.events;

import java.util.IdentityHashMap;
import java.util.Optional;

import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RecallHandler {
	private static final IdentityHashMap<ServerPlayerEntity, RecallHandler> recallingPlayers = new IdentityHashMap<>();

	private final ServerPlayerEntity player;
	
	public RecallHandler(ServerPlayerEntity player)
	{
		this.player = player;
		
		recallingPlayers.put(this.player, this);
	}
	
	private void remove()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
		recallingPlayers.remove(this.player);
	}
	
	@SubscribeEvent
	public void serverTickPre(TickEvent.ServerTickEvent event)
	{
		if(event.phase == Phase.START)
		{
			this.remove();

			// Send player to recall
			//this.player.server.getPlayerList().func_232644_a_(this.player, true);
			
			BlockPos oldpos = this.player.getPosition();
			BlockPos pos = this.player.func_241140_K_();
			float f = this.player.func_242109_L();
			boolean flag = this.player.func_241142_M_();
			ServerWorld serverworld = this.player.server.getWorld(this.player.func_241141_L_());
			ServerWorld oldWorld = this.player.getServerWorld();
			
			Optional<Vector3d> optional;
			if (serverworld != null && pos != null)
				optional = PlayerEntity.func_242374_a(serverworld, pos, f, flag, false);
			else
				optional = Optional.empty();
			
			if(optional.isPresent())
			{
				Vector3d v = optional.get();
				this.player.teleport(serverworld, v.getX(), v.getY(), v.getZ(), this.player.getYaw(0), this.player.getPitch(0));
				
				SoundEvent soundevent = SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
	            oldWorld.playSound((PlayerEntity)null, oldpos.getX(), oldpos.getY(), oldpos.getZ(), soundevent, SoundCategory.PLAYERS, 1.0F, 1.0F);
	            this.player.playSound(soundevent, 1.0F, 1.0F);

			}
			else if(pos != null)
			{
				this.player.connection.sendPacket(new SChangeGameStatePacket(SChangeGameStatePacket.field_241764_a_, 0.0F));
			}

			//this.player.teleport(newWorld, pos.getX(), pos.getY(), pos.getZ(), this.player.getYaw(0), this.player.getPitch(0));
		}
	}

	
	@SubscribeEvent
	public void handleWorldUnload(WorldEvent.Unload event)
	{
		this.remove();
	}

	
	public static void addPlayer(ServerPlayerEntity player)
	{
		if(player instanceof FakePlayer)
			return;
		
		RecallHandler handler = recallingPlayers.get(player);
		if(handler == null)
		{
			MinecraftForge.EVENT_BUS.register(new RecallHandler(player));
		}
	}
}

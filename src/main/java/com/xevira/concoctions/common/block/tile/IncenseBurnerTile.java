package com.xevira.concoctions.common.block.tile;

import java.util.List;

import com.google.common.collect.Lists;
import com.xevira.concoctions.common.block.IncenseBurnerBlock;
import com.xevira.concoctions.setup.Config;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.InstantEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

public class IncenseBurnerTile extends TileEntity implements ITickableTileEntity
{
	private static final int BURNING_TIME = 20*60;			// 10 minute duration
	private static final int EFFECT_TIME = 20*10;			// 10 seconds
	private static final int EFFECT_RANGE = 16;
	private static final int MAX_TAXI_DIST = 3 * EFFECT_RANGE;
	
	private List<EffectInstance> effects = Lists.newArrayList();
	private int color = -1;
	private int burningTime = 0;
	private ItemStack incenseItem = ItemStack.EMPTY;

	public IncenseBurnerTile() {
		super(Registry.INCENSE_BURNER_TILE.get());
	}

	@Override
	public void tick()
	{
		if(this.world == null || this.world.isRemote)
			return;

		if(this.burningTime > 0)
		{
			this.burningTime--;
			
			if((this.effects.size() > 0) && (this.world.getGameTime() % 80L == 0L))
			{
				AxisAlignedBB axisalignedbb = (new AxisAlignedBB(this.pos)).grow(EFFECT_RANGE);
				List<PlayerEntity> players = this.world.getEntitiesWithinAABB(PlayerEntity.class, axisalignedbb);
				
				for(PlayerEntity player : players)
				{
					for(EffectInstance effect : this.effects)
					{
						if( effect.getPotion().isInstant())
							effect.getPotion().affectEntity(player, player, player, effect.getAmplifier(), 1.0D);
						else
							player.addPotionEffect(new EffectInstance(effect));
					}
				}
			}
			
			if(this.burningTime == 0)
				clearIncense();
		}
	}
	
	@Override
	public void read(BlockState stateIn, CompoundNBT compound)
	{
		super.read(stateIn, compound);
		
		this.burningTime = compound.getInt("b");
		this.color = compound.getInt("c");
		
		this.effects.clear();
		if(compound.contains("e"))
		{
			ListNBT listEffects = compound.getList("e", 10);
			
			for(INBT inbt : listEffects)
			{
				if(inbt instanceof CompoundNBT)
				{
					this.effects.add(EffectInstance.read((CompoundNBT)inbt));
				}
			}
		}
		
		this.incenseItem = ItemStack.read(compound.getCompound("i"));
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		compound.putInt("b", this.burningTime);
		compound.putInt("c", this.color);
		
		if(this.effects.size() > 0)
		{
			ListNBT listEffects = new ListNBT();
			for(EffectInstance effect : effects)
			{
				listEffects.add(effect.write(new CompoundNBT()));
			}
			
			compound.put("e", listEffects);
		}
		
		compound.put("i", this.incenseItem.write(new CompoundNBT()));
		
		return super.write(compound);
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		// Vanilla uses the type parameter to indicate which type of tile entity (command block, skull, or beacon?) is receiving the packet, but it seems like Forge has overridden this behavior
		return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return write(new CompoundNBT());
	}

	@Override
	public void handleUpdateTag(BlockState stateIn, CompoundNBT tag) {
		read(stateIn, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		read(this.getBlockState(), pkt.getNbtCompound());
	}
	
	private void clearIncense()
	{
		effects.clear();
		color = -1;

		BlockState state = this.world.getBlockState(this.pos);
		this.world.setBlockState(this.pos, state.with(IncenseBurnerBlock.LIT, false).with(IncenseBurnerBlock.HAS_INCENSE, false), 11);
	}
	
	public boolean setLit()
	{
		if(effects.size() > 0)
		{
			this.burningTime = BURNING_TIME * Config.INCENSE_BURNER_DURATION.get();
			this.incenseItem = ItemStack.EMPTY;
			return true;
		}
		
		return false;
	}
	
	public boolean isLit()
	{
		return this.burningTime > 0;
	}
	
	public boolean setIncense(ItemStack incense)
	{
		if(!incenseItem.isEmpty())
			return false;
		
		if(incense.isEmpty())
			return false;
		
		if(incense.getItem() != Registry.INCENSE_ITEM.get())
			return false;
		
		List<EffectInstance> itemEffects = PotionUtils.getEffectsFromStack(incense);
		if(itemEffects.size() > 0)
		{
			effects.clear();
			
			for(EffectInstance itemEffect : itemEffects)
			{
				EffectInstance newEffect = new EffectInstance(
						itemEffect.getPotion(),
						EFFECT_TIME,
						itemEffect.getAmplifier(),
						true,
						itemEffect.doesShowParticles(),
						itemEffect.isShowIcon());
				
				effects.add(newEffect);
			}

			if(effects.size() > 0)
			{
				color = PotionUtils.getColor(incense);
				incenseItem = incense.copy();
				incenseItem.setCount(1);
				return true;
			}
		}
		
		return false;
	}
	
	public int getIncenseColor()
	{
		return this.color;
	}
	
	public boolean hasIncense()
	{
		return this.effects.size() > 0;
	}
	
	public void removeIncense(World worldIn, BlockPos pos)
	{
		if(!this.incenseItem.isEmpty())
		{
			InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), this.incenseItem);
			this.incenseItem = ItemStack.EMPTY;

			clearIncense();
		}
	}
	
	public void dropItems(World worldIn, BlockPos pos)
	{
		if(!this.incenseItem.isEmpty())
		{
			InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), this.incenseItem);
			this.incenseItem = ItemStack.EMPTY;
		}
	}
}

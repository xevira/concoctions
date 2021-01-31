package com.xevira.concoctions.common.block.tile;

import java.awt.image.ComponentSampleModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.block.MixerBlock;
import com.xevira.concoctions.common.container.MixerContainer;
import com.xevira.concoctions.common.handlers.*;
import com.xevira.concoctions.common.network.PacketHandler;
import com.xevira.concoctions.common.network.packets.PacketMixerValveChanges;
import com.xevira.concoctions.common.utils.Utils;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class MixerTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider, ITilePotionRenamer, IFluidTankCallbacks
{
	public static final int NORTH_TANK = 0;
	public static final int SOUTH_TANK = 1;
	public static final int EAST_TANK = 2;
	public static final int WEST_TANK = 3;
	public static final int TOTAL_INPUTS = 4;
	public static final int CENTER_TANK = 4;
	public static final int TOTAL_TANKS = 5;
	
	public static final int TOTAL_DATA = TOTAL_INPUTS + 4;
	
	public static final int MAX_EFFECT_COUNT = 4;
	
	public static final int ERROR_NONE = 0;
	public static final int ERROR_NO_RECIPE = 1;		// Nothing to mix
	public static final int ERROR_TOO_MANY_EFFECTS = 2;	// Total number of effects is too great
	public static final int ERROR_NOT_ENOUGH = 3;		// Number of potions mixed isn't enough (requires 2+)
	public static final int ERROR_NO_FLUID = 4;			// Not enough input fluids to mix
	public static final int ERROR_NO_SPACE = 5;			// Not enough space in output tank
	
	private static final String NBT_FIELDS[] = new String[] {
		"north",
		"south",
		"east",
		"west",
		"center"
	};
	
	private final MixerReservoir tanks[];
	private final LazyOptional<OutputItemStackHandler> output;		// Outputs are shared across all tank I/O
	private final int valves[];
	private int mixingTimeTotal = 0;
	private int mixingTime = 0;
	private FluidStack mixingTarget;
	private boolean centerValveOpen = false;
	private int mixingStatus = 0;
	private MixerResult mixingResult = null;
	private String newPotionName = "";
	
    public final IIntArray mixerData = new IIntArray() {
        @Override
        public int get(int index) {
            switch (index) {
                case NORTH_TANK:
                case SOUTH_TANK:
                case EAST_TANK:
                case WEST_TANK:
                	return valves[index];
                case TOTAL_INPUTS:
                	return MixerTile.this.mixingTime;
                case TOTAL_INPUTS+1:
                	return MixerTile.this.centerValveOpen ? 1 : 0;
                case TOTAL_INPUTS+2:
                	return MixerTile.this.mixingStatus;
                case TOTAL_INPUTS+3:
                	return MixerTile.this.mixingTimeTotal;
                default:
                    throw new IllegalArgumentException("Invalid index: " + index);
            }
        }

        @Override
        public void set(int index, int value) {
            throw new IllegalStateException("Cannot set values through IIntArray");
        }

        @Override
        public int size() {
            return TOTAL_DATA;
        }
    };

	
	public MixerTile()
	{
		super(Registry.MIXER_TILE.get());
		
		tanks = new MixerReservoir[TOTAL_TANKS];
		tanks[CENTER_TANK] = new MixerReservoir(10, true, this);
		tanks[NORTH_TANK] = new MixerReservoir(10, false);
		tanks[SOUTH_TANK] = new MixerReservoir(10, false);
		tanks[EAST_TANK] = new MixerReservoir(10, false);
		tanks[WEST_TANK] = new MixerReservoir(10, false);
		
		output = LazyOptional.of(() -> new OutputItemStackHandler(TOTAL_TANKS));
		
		valves = new int[TOTAL_INPUTS];
		valves[NORTH_TANK] = 0;
		valves[SOUTH_TANK] = 0;
		valves[EAST_TANK] = 0;
		valves[WEST_TANK] = 0;
		
		mixingTime = 0;
		mixingTarget = FluidStack.EMPTY;
		
		centerValveOpen = false;
	}

	@Override
	public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player)
	{
		assert world != null;
		return new MixerContainer(this, this.mixerData, i, inventory);
	}
	
	public MixerReservoir[] getReservoirs()
	{
		return this.tanks;
	}
	
	public OutputItemStackHandler getOutputItemHandler()
	{
		return this.output.orElse(new OutputItemStackHandler(TOTAL_TANKS));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("tile.concoctions.mixer");
	}
	
	private boolean areItemStacksEqual(ItemStack a, ItemStack b) {
		// same functionality as ItemStack.areItemStackEqual(a,b) but ignoring item count
		if( a.isEmpty() && b.isEmpty() ) {
			return true;
		} else if (a.getItem() != b.getItem()) {
			return false;
		} else if (a.getTag() == null && b.getTag() != null) {
			return false;
		} else {
			return (a.getTag() == null || a.getTag().equals(b.getTag())) && a.areCapsCompatible(b);
		}
	}
	
	private ItemStack fillInputTank(FluidTank tank, ItemStack inStack, ItemStack outStack, FluidStack inFluid, ItemStack resultStack)
	{
		if( resultStack.isEmpty() )
		{
			//Concoctions.GetLogger().info("resultStack.isEmpty() {}", resultStack.isEmpty());
			return null;
		}
		
		// Check if result and current output stack are compatible
		if( !outStack.isEmpty() && !areItemStacksEqual(outStack, resultStack))
		{
			//Concoctions.GetLogger().info("!outStack.isEmpty() {} && !areItemStacksEqual(outStack, resultStack) {}", !outStack.isEmpty(), !areItemStacksEqual(outStack, resultStack));
			return null;
		}
		
		// Check if the output pile can handle more items
		int newSize = outStack.getCount() + 1; 
		if( newSize > resultStack.getMaxStackSize() )
		{
			//Concoctions.GetLogger().info("newSize {} > resultStack.getMaxStackSize() {}", newSize, resultStack.getMaxStackSize());
			return null;
		}
		
		// Can they even stack properly?
		if( !outStack.isEmpty() && (!outStack.isStackable() || !resultStack.isStackable()))
		{
			//Concoctions.GetLogger().info("!outStack.isEmpty() {} && (!outStack.isStackable() {} || !resultStack.isStackable() {})", !outStack.isEmpty(), !outStack.isStackable(), !resultStack.isStackable());
			return null;
		}
		
		FluidStack tankFluid = tank.getFluid();
		
		if( !tankFluid.isEmpty() && !tankFluid.isFluidEqual(inFluid) )
		{
			//Concoctions.GetLogger().info("!tankFluid.isEmpty() {} && !tankFluid.isFluidEqual(inFluid) {}", !tankFluid.isEmpty(), !tankFluid.isFluidEqual(inFluid));
			return null;
		}

		
		int res = tank.fill(inFluid, FluidAction.SIMULATE);
		if( res < inFluid.getAmount() )
		{
			//Concoctions.GetLogger().info("res {} < inFluid.getAmount() {}", res, inFluid.getAmount());
			return null;
		}
		
		tank.fill(inFluid, FluidAction.EXECUTE);
		
		inStack.shrink(1);
		if( outStack.isEmpty() )
			outStack = resultStack.copy();
		else
			outStack.grow(1);
		
		return outStack;
	}

	private boolean tryFillInputTank(int slot, FluidTank tank, ItemStack inStack, OutputItemStackHandler output)
	{
		ItemStack outStack = output.getStackInSlot(slot);
		
		Item item = inStack.getItem();
		FluidStack fluidStack = FluidStack.EMPTY;
		ItemStack outputStack = ItemStack.EMPTY;
		
		
		if( item == Items.POTION ) {
			fluidStack = Utils.getPotionFluidFromNBT(inStack.getTag());
			if(fluidStack.getFluid() != Registry.POTION_FLUID.get())
				return false;
			
			outputStack = new ItemStack(Items.GLASS_BOTTLE, 1);
		}

		// Filling
		if( !fluidStack.isEmpty() && !outputStack.isEmpty()) {
			outStack = fillInputTank(tank, inStack, outStack, fluidStack, outputStack);
			
			if( outStack != null )
			{
				output.setStackInSlot(slot, outStack);
				this.markDirty();
				this.markContainingBlockForUpdate(null);
				
				return true;
			}
		}
		
		return false;
	}

	private ItemStack emptyInputTank(FluidTank tank, ItemStack inStack, ItemStack outStack, FluidStack inFluid, ItemStack resultStack)
	{
		if( resultStack.isEmpty())
			return null;

		FluidStack tankFluid = tank.getFluid();
		
		if( !tankFluid.isEmpty() && !inFluid.isEmpty() && tankFluid.getFluid() != inFluid.getFluid())
			return null;	// Not the same fluid
		
		int volume = FluidAttributes.BUCKET_VOLUME;
		if( !inFluid.isEmpty() )
			volume = inFluid.getAmount();
		
		FluidStack result = tank.drain(volume, FluidAction.SIMULATE);
		if( result.isEmpty() || result.getAmount() < volume )
			return null;	// Not enough room
		
		Utils.addPotionEffectsToItemStack(result, resultStack);

		// Check if result and current output stack are compatible
		if( !outStack.isEmpty() && !areItemStacksEqual(outStack, resultStack))
			return null;	// Not the same result
		
		// Check if the output pile can handle more items
		int newSize = outStack.getCount() + 1; 
		if( newSize > resultStack.getMaxStackSize() )
				return null;
		
		// Can they even stack properly?
		if( !outStack.isEmpty() && (!outStack.isStackable() || !resultStack.isStackable()))
			return null;
		
		tank.drain(volume, FluidAction.EXECUTE);

		inStack.shrink(1);
		if( outStack.isEmpty())
		{
			outStack = resultStack.copy();
			outStack.setCount(1);
		}
		else
			outStack.grow(1);
		
		return outStack;
	}

	private boolean tryEmptyInputTank(int slot, FluidTank tank, ItemStack inStack, OutputItemStackHandler output)
	{
		ItemStack outStack = output.getStackInSlot(slot);
		
		Item item = inStack.getItem();
		FluidStack fluidStack = FluidStack.EMPTY;
		ItemStack outputStack = ItemStack.EMPTY;

		FluidStack tankFluid = tank.getFluid();
		if( tankFluid.isEmpty() )
			return false;
		
		if( item == Items.SPONGE)
		{
			fluidStack = new FluidStack(Registry.POTION_FLUID.get(), Math.min(tankFluid.getAmount(),FluidAttributes.BUCKET_VOLUME));
			outputStack = new ItemStack(Items.WET_SPONGE, 1);
		}
		else if( item == Items.GLASS_BOTTLE ) {
			if( tankFluid.getFluid() != Registry.POTION_FLUID.get())
				return false;

			outputStack = new ItemStack(Items.POTION, 1);
		}
		
		if( !outputStack.isEmpty() ) {
			outStack = emptyInputTank(tank, inStack, outStack, fluidStack, outputStack);
			
			if( outStack != null ) {
				output.setStackInSlot(slot, outStack);
				this.markDirty();
				this.markContainingBlockForUpdate(null);

				return true;
			}
		}
		
		return false;
	}

	private boolean processInputTank(int tank)
	{
		if(tank < 0 || tank >= TOTAL_INPUTS)
			return false;
		
		MixerReservoir reservoir = this.tanks[tank];
		
		if(!reservoir.tank.isPresent())
			return false;
		
		if(!reservoir.input.isPresent())
			return false;
		
		if(!this.output.isPresent())
			return false;
		
		ItemStack inStack = reservoir.input.resolve().get().getStackInSlot(0);
		FluidTank inTank = reservoir.tank.resolve().get();
		OutputItemStackHandler outHandler = this.output.resolve().get();
		
		if(tryFillInputTank(tank, inTank, inStack, outHandler))
			return true;
		if(tryEmptyInputTank(tank, inTank, inStack, outHandler))
			return true;
		
		return false;
	}
	

	private ItemStack emptyOutputTank(FluidTank tank, ItemStack inStack, ItemStack outStack, FluidStack inFluid, ItemStack resultStack)
	{
		if( resultStack.isEmpty())
			return null;

		FluidStack tankFluid = tank.getFluid();
		
		if( !tankFluid.isEmpty() && !inFluid.isEmpty() && tankFluid.getFluid() != inFluid.getFluid())
			return null;	// Not the same fluid
		
		int volume = FluidAttributes.BUCKET_VOLUME;
		if( !inFluid.isEmpty() )
			volume = inFluid.getAmount();
		
		FluidStack result = tank.drain(volume, FluidAction.SIMULATE);
		if( result.isEmpty() || result.getAmount() < volume )
			return null;	// Not enough room
		
		Utils.addPotionEffectsToItemStack(result, resultStack);
		renameItemName(resultStack);

		// Check if result and current output stack are compatible
		if( !outStack.isEmpty() && !areItemStacksEqual(outStack, resultStack))
			return null;	// Not the same result
		
		// Check if the output pile can handle more items
		int newSize = outStack.getCount() + 1; 
		if( newSize > resultStack.getMaxStackSize() )
				return null;
		
		// Can they even stack properly?
		if( !outStack.isEmpty() && (!outStack.isStackable() || !resultStack.isStackable()))
			return null;
		
		tank.drain(volume, FluidAction.EXECUTE);

		inStack.shrink(1);
		if( outStack.isEmpty())
		{
			outStack = resultStack.copy();
			outStack.setCount(1);
		}
		else
			outStack.grow(1);
		
		return outStack;
	}

	private void tryEmptyOutputTank(FluidTank tank, ItemStack inStack, OutputItemStackHandler output)
	{
		ItemStack outStack = output.getStackInSlot(CENTER_TANK);
		
		Item item = inStack.getItem();
		FluidStack fluidStack = FluidStack.EMPTY;
		ItemStack outputStack = ItemStack.EMPTY;

		FluidStack tankFluid = tank.getFluid();
		if( tankFluid.isEmpty() )
			return;
		
		if( item == Items.SPONGE)
		{
			fluidStack = new FluidStack(Registry.POTION_FLUID.get(), Math.min(tankFluid.getAmount(),FluidAttributes.BUCKET_VOLUME));
			outputStack = new ItemStack(Items.WET_SPONGE, 1);
		}
		else if( item == Items.GLASS_BOTTLE ) {
			if( tankFluid.getFluid() == Fluids.WATER )
			{
				fluidStack = new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
				outputStack = new ItemStack(Items.POTION, 1);
			}
			else if( tankFluid.getFluid() == Registry.POTION_FLUID.get())
			{
				outputStack = new ItemStack(Items.POTION, 1);
			}
			
		} else if( item == Registry.SPLASH_BOTTLE.get() ) {
			if( tankFluid.getFluid() == Fluids.WATER )
			{
				fluidStack = new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
				outputStack = new ItemStack(Items.SPLASH_POTION, 1);
			}
			else if( tankFluid.getFluid() == Registry.POTION_FLUID.get())
			{
				outputStack = new ItemStack(Items.SPLASH_POTION, 1);
			}
			
		} else if( item == Registry.LINGERING_BOTTLE.get() ) {
			if( tankFluid.getFluid() == Fluids.WATER )
			{
				fluidStack = new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
				outputStack = new ItemStack(Items.LINGERING_POTION, 1);
			}
			else if( tankFluid.getFluid() == Registry.POTION_FLUID.get())
			{
				outputStack = new ItemStack(Items.LINGERING_POTION, 1);
			}
			
		} else if( item == Items.ARROW ) {
			if( tankFluid.getFluid() == Fluids.WATER )
			{
				fluidStack = new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
				outputStack = new ItemStack(Items.LINGERING_POTION, 1);
			}
			else if( tankFluid.getFluid() == Registry.POTION_FLUID.get())
			{
				fluidStack = new FluidStack(Registry.POTION_FLUID.get(), 125);	// BUCKET_VOLUME / 8
				outputStack = new ItemStack(Items.TIPPED_ARROW, 1);
			}
		}
		
		if( !outputStack.isEmpty() ) {
			outStack = emptyOutputTank(tank, inStack, outStack, fluidStack, outputStack);
			
			if( outStack != null ) {
				output.setStackInSlot(CENTER_TANK, outStack);
				
				this.markDirty();
				this.markContainingBlockForUpdate(null);

				return;
			}
		}
	}

	private void processOutputTank()
	{
		MixerReservoir reservoir = this.tanks[CENTER_TANK];
		
		if(!reservoir.tank.isPresent())
			return;
		
		if(!reservoir.input.isPresent())
			return;
		
		if(!this.output.isPresent())
			return;
		
		ItemStack inStack = reservoir.input.resolve().get().getStackInSlot(0);
		FluidTank inTank = reservoir.tank.resolve().get();
		OutputItemStackHandler outHandler = this.output.resolve().get();
		
		boolean wasEmpty = inTank.isEmpty();
		
		tryEmptyOutputTank(inTank, inStack, outHandler);
		
		if(!wasEmpty && inTank.isEmpty())
		{
			if(this.mixingResult == null || this.mixingResult.code != ERROR_NONE)
			{
				handleOnEmpty();
			}
		}
	}
	
	private List<EffectInstance> getEffectCount(FluidStack fluidStack, int valve)
	{
		List<EffectInstance> effects = new ArrayList<EffectInstance>();
		
		if(fluidStack.getFluid() != Registry.POTION_FLUID.get())
			return effects;
		
		List<EffectInstance> rawEffects = PotionUtils.getEffectsFromTag(fluidStack.getTag());
		
		for(EffectInstance rawEffect : rawEffects)
		{
			int duration = rawEffect.getDuration();
			if(duration >= 20)
				duration = Math.max(duration * valve / 100, 20);	// Reduce duration by valve percentage
			
			EffectInstance effect = new EffectInstance(rawEffect.getPotion(), duration, rawEffect.getAmplifier(), rawEffect.isAmbient(), rawEffect.doesShowParticles(), rawEffect.isShowIcon());
			effects.add(effect);
		}
		
		return effects;
	}
	
	// TODO: MERGE effects, combine durations
	private List<EffectInstance> mergeEffects(List<EffectInstance> effects)
	{
		HashMap<Effect, MixerEffect> effectMap = new HashMap<Effect, MixerEffect>();
		ArrayList<Effect> priority = new ArrayList<Effect>();
		
		// Tally up all effects
		for(EffectInstance effect : effects)
		{
			if(effectMap.containsKey(effect.getPotion()))
			{
				MixerEffect me = effectMap.get(effect.getPotion());
				
				if(me.addEffect(effect))
				{
					// Force it to the end of the list
					priority.remove(effect.getPotion());
					priority.add(effect.getPotion());
				}
			}
			else
			{
				effectMap.put(effect.getPotion(), new MixerEffect(effect));
				priority.add(effect.getPotion());
			}
		}
		
		List<EffectInstance> merged = Lists.newArrayList();
		for(Effect e : priority)
		{
			MixerEffect me = effectMap.getOrDefault(e, null);
			if(me == null)
				continue;
			
			// Average durations
			int duration = me.duration;
			if(me.count > 1)
				duration = me.duration / me.count;
			
			EffectInstance effect = new EffectInstance(e, duration, me.amplifier, me.ambient, me.showParticles, me.showIcon);
			merged.add(effect);
		}
		
		// Prune all instant effects until there are atmost MAX_EFFECT_COUNT effects or there are no more instant effects
		
		while(merged.size() > MAX_EFFECT_COUNT)
		{
			boolean removed = false;
			for(int i = merged.size() - 1; i >= 0; i--)
			{
				EffectInstance effect = merged.get(i);
				
				if(effect.getDuration() >= 20)
					continue;
				
				merged.remove(i);
				removed = true;
				break;
			}
			
			if(!removed)
				break;
		}

		/*
		merged.sort(new Comparator<EffectInstance>() {
			@Override
			public int compare(EffectInstance a, EffectInstance b)
			{
				return Integer.compare(b.getDuration(), a.getDuration());
			}
		});
		*/
		
		return merged;
	}
	
	// TODO
	private MixerResult _canMixFluids()
	{
		for(int i = 0; i < TOTAL_INPUTS; i++)
		{
			if(!this.tanks[i].tank.isPresent())
				return new MixerResult(ERROR_NO_RECIPE);
			
			if(this.valves[i] > 0)
			{
				int volume = this.valves[i] * 10;
				if(this.tanks[i].getFluidAmount() < volume)
					return new MixerResult(ERROR_NO_FLUID);
			}
		}
		
		int total = 0;
		List<EffectInstance> totalEffects = new ArrayList<EffectInstance>();
		ArrayList<MixerComponent> components = new ArrayList<MixerComponent>();
		
		for(int i = 0; i < TOTAL_INPUTS; i++)
		{
			if(this.valves[i] > 0)
			{
				MixerReservoir reservoir = this.tanks[i];
				int volume = this.valves[i] * 10;
				
				if(reservoir.getFluidAmount() >= volume)
				{
					List<EffectInstance> effects = getEffectCount(reservoir.getFluid(), this.valves[i]);
					
					if(effects.size() > 0)
					{
						components.add(new MixerComponent(i, reservoir.getFluid(), this.valves[i]));
						total += this.valves[i];
						totalEffects.addAll(effects);
					}
				}
			}
		}
		
		if(components.size() < 2)
			return new MixerResult(ERROR_NOT_ENOUGH);
		
		totalEffects = mergeEffects(totalEffects);
		if(totalEffects.size() > MAX_EFFECT_COUNT)
			return new MixerResult(ERROR_TOO_MANY_EFFECTS);

		FluidStack resultFluid = Utils.getPotionFluidFromEffects(totalEffects);
		if(!this.newPotionName.isEmpty())
		{
			CompoundNBT root = resultFluid.getOrCreateTag();
			root.putString("CustomPotionName", this.newPotionName);
		}
		resultFluid.setAmount(total * 10);
		
		FluidTank center = this.tanks[CENTER_TANK].tank.resolve().get();
		FluidStack centerFluid = center.getFluid();
		if(!centerFluid.isEmpty() && !centerFluid.isFluidEqual(resultFluid))
			return new MixerResult(-5);
		
		if(center.fill(resultFluid, FluidAction.SIMULATE) != resultFluid.getAmount())
			return new MixerResult(ERROR_NO_SPACE);
		
		return new MixerResult(totalEffects.size(), components, resultFluid);
	}
	
	private MixerResult canMixFluids()
	{
		MixerResult result = _canMixFluids();
		
		this.mixingStatus = Math.max(result.code, ERROR_NONE);
		
		return result; 
	}
	
	private boolean canStillMixFluids()
	{
		for(int i = 0; i < TOTAL_INPUTS; i++)
		{
			if(!this.tanks[i].tank.isPresent())
			{
				this.mixingResult = new MixerResult(ERROR_NO_RECIPE);
				return false;
			}
			
			if(this.valves[i] > 0)
			{
				int volume = this.valves[i] * 10;
				if(this.tanks[i].getFluidAmount() < volume)
				{
					this.mixingResult = new MixerResult(ERROR_NO_FLUID);
					return false;
				}
			}
		}
		return true;
	}

	private void processMixing(MixerResult result)
	{
		for(MixerComponent component : result.components)
		{
			MixerReservoir reservoir = this.tanks[component.tank];
			
			if(!reservoir.tank.isPresent())
				return;
		}
		
		for(MixerComponent component : result.components)
		{
			MixerReservoir reservoir = this.tanks[component.tank];
			
			reservoir.tank.resolve().get().drain(component.valve * 10, FluidAction.EXECUTE);
		}
		
		FluidTank center = this.tanks[CENTER_TANK].tank.resolve().get();
		center.fill(result.fluid, FluidAction.EXECUTE);
	}
	
	public void handleOnEmpty()
	{
		this.mixingResult = canMixFluids();
		
	}
	
	@Override
	public void tick()
	{
		// TODO
		processOutputTank();
		
		if(this.centerValveOpen)
		{
			/*
			if(this.mixingResult != null)
			{
				Concoctions.GetLogger().info("this.mixingResult.code = {}", this.mixingResult.code);
			}
			else
			{
				Concoctions.GetLogger().info("this.mixingResult is null");
			}
			*/
			
			if(this.mixingResult != null && this.mixingResult.code == ERROR_NONE)
			{
				//Concoctions.GetLogger().info("this.mixingTime = {}", mixingTime);
				if(this.mixingTime > 0)
				{
					--this.mixingTime;
					
					if(this.mixingTime <= 0)
					{
						processMixing(mixingResult);
						this.mixingTime = 0;
						this.markDirty();
						this.markContainingBlockForUpdate(null);
					}
				}
				else if(canStillMixFluids())
				{
					this.mixingTimeTotal = 100 * mixingResult.totalEffects;
					this.mixingTime = this.mixingTimeTotal;
					this.markDirty();
					this.markContainingBlockForUpdate(null);
				}
				else
				{
					this.mixingResult = null;
					this.mixingTime = 0;
				}

			}
			
			/*
			if(this.mixingTime > 0 && mixingResult != null)
			{
				if(mixingResult.code == ERROR_NONE)
				{
					--this.mixingTime;
					
					if(this.mixingTime <= 0)
					{
						processMixing(mixingResult);
						this.mixingResult = null;
						this.mixingTime = 0;
						this.markDirty();
						this.markContainingBlockForUpdate(null);
					}
				}
				else
				{
					this.mixingTime = 0;
				}
			}
			else
			{
				this.mixingResult = canMixFluids();
				this.mixingStatus = Math.max(mixingResult.code, ERROR_NONE);
				
				if(mixingResult.code == ERROR_NONE)
				{
					this.mixingTimeTotal = 100 * mixingResult.totalEffects;
					this.mixingTime = this.mixingTimeTotal;
					this.markDirty();
					this.markContainingBlockForUpdate(null);
				}
				else
				{
					this.mixingTime = 0;
				}
			}
			*/
		}
		else
		{
			boolean updated = false;
			
			for(int i = 0; i < TOTAL_INPUTS; i++)
			{
				if(processInputTank(i))
					updated = true;
			}
			
			if(updated)
			{
				this.mixingResult = canMixFluids();

//				if(this.mixingResult != null)
//				{
//					Concoctions.GetLogger().info("this.mixingResult.code = {}", this.mixingResult.code);
//				}
			}
		}
	}
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			if(side == Direction.DOWN)
				return output.cast();
			
			if(side == Direction.UP)
				return tanks[CENTER_TANK].input.cast();
			
			if(side == Direction.NORTH)
				return tanks[NORTH_TANK].input.cast();

			if(side == Direction.SOUTH)
				return tanks[SOUTH_TANK].input.cast();

			if(side == Direction.EAST)
				return tanks[EAST_TANK].input.cast();

			if(side == Direction.WEST)
				return tanks[WEST_TANK].input.cast();
		}
		else if( cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
		{
			if(side == Direction.DOWN)
				return tanks[CENTER_TANK].lazy.cast();
			
			if(side == Direction.NORTH)
				return tanks[NORTH_TANK].lazy.cast();

			if(side == Direction.SOUTH)
				return tanks[SOUTH_TANK].lazy.cast();

			if(side == Direction.EAST)
				return tanks[EAST_TANK].lazy.cast();

			if(side == Direction.WEST)
				return tanks[WEST_TANK].lazy.cast();

		}
		
		return super.getCapability(cap, side);
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
	
	public void markContainingBlockForUpdate(@Nullable BlockState newState)
	{
		if(this.world!=null)
			markBlockForUpdate(getPos(), newState);
	}

	public void markBlockForUpdate(BlockPos pos, @Nullable BlockState newState)
	{
		BlockState state = world.getBlockState(pos);
		if(newState==null)
			newState = state;
		world.notifyBlockUpdate(pos, state, newState, 3);
		world.notifyNeighborsOfStateChange(pos, newState.getBlock());
	}

    @Override
    public void read(BlockState stateIn, CompoundNBT compound)
    {
        super.read(stateIn, compound);
        
        this.mixingTime = compound.getInt("time");
        this.mixingTimeTotal = compound.getInt("total");
        this.mixingTarget = FluidStack.loadFluidStackFromNBT(compound.getCompound("target"));
        this.centerValveOpen = compound.getBoolean("open");
        
        this.output.ifPresent(o -> { o.deserializeNBT(compound.getCompound("output")); });

        for(int i = 0; i < TOTAL_TANKS; i++)
        	this.tanks[i].deserializeNBT(compound.getCompound(NBT_FIELDS[i]));

        for(int i = 0; i < TOTAL_INPUTS; i++)
        	valves[i] = compound.getInt("valve_" + NBT_FIELDS[i]);
        
        if(compound.contains("mr"))
        	this.mixingResult = MixerResult.read(compound.getCompound("mr"));
        else
        	this.mixingResult = new MixerResult(ERROR_NO_RECIPE);

        this.mixingStatus = Math.max(this.mixingResult.code, ERROR_NONE);
        
        this.newPotionName = compound.getString("name");
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
    	compound.putInt("time", this.mixingTime);
    	compound.putInt("total", this.mixingTimeTotal);
    	compound.put("target", this.mixingTarget.writeToNBT(new CompoundNBT()));
    	compound.putBoolean("open", this.centerValveOpen);
    	
    	this.output.ifPresent(o -> { compound.put("output", o.serializeNBT()); });
    	
    	for(int i = 0; i < TOTAL_TANKS; i++)
    		compound.put(NBT_FIELDS[i], this.tanks[i].serializeNBT());

        for(int i = 0; i < TOTAL_INPUTS; i++)
        	compound.putInt("valve_" + NBT_FIELDS[i], valves[i]);
        
        if(mixingResult != null)
        {
        	compound.put("mr", mixingResult.write());
        }
        
        compound.putString("name", this.newPotionName);

        return super.write(compound);
    }

	@Override
	public void remove() {
		for(MixerReservoir tank : this.tanks)
		{
			tank.tank.invalidate();
			tank.input.invalidate();
		}
		this.output.invalidate();
		super.remove();
	}
	
	public int[] getCapacities()
	{
		assert this.world != null;
		
		int[] caps = new int[TOTAL_TANKS];
		
		for(int i = 0; i < TOTAL_TANKS; i++)
			caps[i] = this.tanks[i].getCapacity();
		
		return caps;
	}
	
	public FluidStack[] getFluids()
	{
		assert this.world != null;
		
		FluidStack[] stacks = new FluidStack[TOTAL_TANKS];
		
		for(int i = 0; i < TOTAL_TANKS; i++)
			stacks[i] = this.tanks[i].getFluid();
		
		return stacks;
	}
	
	public FluidStack getTargetFluid()
	{
		if( this.mixingResult != null && this.mixingResult.code == ERROR_NONE )
			return this.mixingResult.fluid;
		
		return FluidStack.EMPTY;
	}
	
	private void handlerDropItems(IItemHandler handler, World worldIn, BlockPos pos)
	{
		for (int i = 0; i < handler.getSlots(); i++)
			InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(),
					handler.getStackInSlot(i));
	}
	
	public void dropItems(World worldIn, BlockPos pos)
	{
		for(MixerReservoir tank : this.tanks)
		{
			tank.input.ifPresent(handler -> handlerDropItems(handler, worldIn, pos));
		}
	}
	
	public void setValve(int valve, int value)
	{
		//Concoctions.GetLogger().info("[{}] setValve({},{})", this.world.isRemote?"CLIENT":"SERVER", valve, value);
		
		if(valve >= 0 && valve < TOTAL_INPUTS)
		{
			if(!this.centerValveOpen)
			{
				int v = MathHelper.clamp(value, 0, 100);
//				if( this.valves[valve] != v)
//				{
					this.valves[valve] = v;
					this.markContainingBlockForUpdate(null);
					
					if(this.world.isRemote)
						PacketHandler.sendToServer(new PacketMixerValveChanges((byte)valve, (byte)this.valves[valve]));
					else
						this.mixingResult = canMixFluids();
//				}
			}
		}
		else if(valve == CENTER_TANK)
		{
			this.centerValveOpen = value != 0;
			this.mixingTime = 0;
			this.mixingTimeTotal = 0;
			this.markContainingBlockForUpdate(null);

			if(this.world.isRemote)
				PacketHandler.sendToServer(new PacketMixerValveChanges((byte)valve, (byte)(this.centerValveOpen ? 1 : 0)));
		}
	}
	
	private void setCustomPotionName(ItemStack stack, String prefix)
	{
		if( StringUtils.isBlank(this.newPotionName))
		{
			boolean isBasePotion = false;
			if(stack.hasTag())
			{
				CompoundNBT root = stack.getTag();
				if( root.contains("CustomPotionName") )
					root.remove("CustomPotionName");
				
				isBasePotion = root.contains("Potion");
			}
			
			if( isBasePotion )
			{
				stack.clearCustomName();
			}
			else if (stack.getItem() == Items.TIPPED_ARROW )
			{
				stack.setDisplayName(new TranslationTextComponent("item.concoctions.tipped_arrow.solution"));
			}
			else
			{
				stack.setDisplayName(new TranslationTextComponent("item.concoctions.solution"));
			}
		}
		else
		{
			CompoundNBT root = stack.getOrCreateTag();
			root.putString("CustomPotionName", this.newPotionName);
			
			stack.setDisplayName(new TranslationTextComponent(prefix, this.newPotionName));
		}
	}
	
	private void renameItemName(ItemStack stack)
	{
		if( stack.isEmpty() )
			return;
		
		if( stack.getItem() == Items.POTION )
		{
			setCustomPotionName(stack, "item.concoctions.potion.prefix");
		}
		else if( stack.getItem() == Items.SPLASH_POTION )
		{
			setCustomPotionName(stack, "item.concoctions.splash_potion.prefix");
		}
		else if( stack.getItem() == Items.LINGERING_POTION )
		{
			setCustomPotionName(stack, "item.concoctions.lingering_potion.prefix");
		}
		else if( stack.getItem() == Items.TIPPED_ARROW )
		{
			setCustomPotionName(stack, "item.concoctions.tipped_arrow.prefix");
		}
	}

	private void renameItemName()
	{
		ItemStackHandler inv = this.output.orElse(null);
		
		if( inv != null )
		{
			ItemStack stack = inv.getStackInSlot(CENTER_TANK);
			renameItemName(stack);
		}
	}
	
	private void updatePotionName(FluidStack fluid, String name)
	{
		if(fluid.getFluid() == Registry.POTION_FLUID.get())
		{
			CompoundNBT root = fluid.getTag();
			if(StringUtils.isBlank(name))
			{
				if(root.contains("CustomPotionName"))
					root.remove("CustomPotionName");
			}
			else
			{
				root.putString("CustomPotionName", name);
			}
		}
	}
	
	public void updatePotionName(String name)
	{
		if( this.hasWorld() )
		{
			this.newPotionName = name;
			this.renameItemName();
			
			this.tanks[CENTER_TANK].tank.ifPresent(t -> {
				updatePotionName(t.getFluid(), name);
			});
			
			if(this.mixingResult != null && !this.mixingResult.fluid.isEmpty())
			{
				updatePotionName(this.mixingResult.fluid, name);
			}
			
		}
	}
	
	public String getPotionName()
	{
		return this.newPotionName;
	}


	private static class MixerComponent
	{
		public final int tank;
		public final FluidStack fluid;
		public final int valve;
		
		public MixerComponent(int tank, FluidStack fluid, int valve)
		{
			this.tank = tank;
			this.fluid = fluid;
			this.valve = valve;
		}
		
		public static MixerComponent read(CompoundNBT nbt)
		{
			int t = nbt.getInt("t");
			FluidStack f = FluidStack.loadFluidStackFromNBT(nbt.getCompound("f"));
			int v = nbt.getInt("v");
			
			return new MixerComponent(t, f, v);
		}
		
		public CompoundNBT write()
		{
			CompoundNBT nbt = new CompoundNBT();
			
			nbt.putInt("t", this.tank);
			nbt.put("f", fluid.writeToNBT(new CompoundNBT()));
			nbt.putInt("v", this.valve);
			
			return nbt;
		}
	}
	
	private static class MixerEffect
	{
		public int amplifier;
		public int duration;
		public boolean ambient;
		public boolean showParticles;
		public boolean showIcon;
		
		public int count;
		
		public MixerEffect(EffectInstance effect)
		{
			setEffect(effect);
		}
		
		private void setEffect(EffectInstance effect)
		{
			this.amplifier = effect.getAmplifier();
			this.duration = effect.getDuration();
			this.ambient = effect.isAmbient();
			this.showParticles = effect.doesShowParticles();
			this.showIcon = effect.isShowIcon();
			this.count = 1;
		}
		
		public boolean addEffect(EffectInstance effect)
		{
			if(effect.getAmplifier() > this.amplifier)
			{
				setEffect(effect);
				return true;
			}
			else if(effect.getAmplifier() == this.amplifier)
			{
				if(this.duration >= 20 && effect.getDuration() >= 20)
				{
					this.duration += effect.getDuration();
					this.count++;
				}
				
				this.ambient = this.ambient || effect.isAmbient();
				this.showParticles = this.showParticles || effect.doesShowParticles();
				this.showIcon = this.showIcon || effect.isShowIcon();
			}
			return false;
		}
		
	}
	
	private static class MixerResult
	{
		public final int code;
		public final int totalEffects;
		public final List<MixerComponent> components;
		public final FluidStack fluid;
		
		public MixerResult(int code)
		{
			this.code = code;
			this.totalEffects = 0;
			this.components = Lists.newArrayList();
			this.fluid = FluidStack.EMPTY;
		}
		
		public MixerResult(int totalEffects, List<MixerComponent> components, FluidStack fluid)
		{
			this.code = ERROR_NONE;
			this.totalEffects = totalEffects;
			this.components = components;
			this.fluid = fluid;
		}
		
		public static MixerResult read(CompoundNBT nbt)
		{
			int c = nbt.getInt("c");
			if(c != ERROR_NONE)
				return new MixerResult(c);
			
			int te = nbt.getInt("te");
			
			List<MixerComponent> lc = Lists.newArrayList();
			ListNBT lstNBT = nbt.getList("lc", 10);	// list of compounds
			for(INBT inbt : lstNBT)
			{
				if(!(inbt instanceof CompoundNBT))
					return null;
				
				lc.add(MixerComponent.read((CompoundNBT)inbt));
			}
			
			FluidStack f = FluidStack.loadFluidStackFromNBT(nbt.getCompound("f"));
			
			return new MixerResult(te, lc, f);
		}
		
		public CompoundNBT write()
		{
			CompoundNBT nbt = new CompoundNBT();
			
			nbt.putInt("c", this.code);
			
			if(this.code == ERROR_NONE)
			{
				nbt.putInt("te", this.totalEffects);
				
				if(this.components.size() > 0)
				{
					ListNBT lstNBT = new ListNBT();
					for(MixerComponent mc : components)
						lstNBT.add(mc.write());
					nbt.put("lc", lstNBT);
				}
				nbt.put("f", this.fluid.writeToNBT(new CompoundNBT()));
			}
			
			return nbt;
		}
	}
	
	public static class MixerReservoir implements IFluidHandler, IFluidTank
	{
	
		public final LazyOptional<FluidTank> tank;
		public final LazyOptional<ItemStackHandlerEx> input;
		private final boolean isOutput;
		public final LazyOptional<MixerReservoir> lazy;
		
	    protected IFluidTankCallbacks callbacks;
	    
	    public MixerReservoir(int capacity, boolean isOutput)
	    {
	    	this(capacity, isOutput, null);
	    }
		
		public MixerReservoir(int capacity, boolean isOutput, IFluidTankCallbacks callbacks)
		{
			this.tank = LazyOptional.of(() -> new FluidTank(capacity * FluidAttributes.BUCKET_VOLUME));
			if(isOutput)
			{
				this.input = LazyOptional.of(() -> new MixerEmptyingItemStackHandler());
			}
			else
			{
				this.input = LazyOptional.of(() -> new MixerFillingItemStackHandler());
			}
			this.isOutput = isOutput;
			this.lazy = LazyOptional.of(() -> this);
			this.callbacks = callbacks;
		}
		
		public void deserializeNBT(CompoundNBT nbt)
		{
			this.tank.ifPresent(t -> { t.readFromNBT(nbt.getCompound("tank")); });
			this.input.ifPresent(i -> { i.deserializeNBT(nbt.getCompound("inv"));});
		}
		
		public CompoundNBT serializeNBT()
		{
			CompoundNBT nbt = new CompoundNBT();
			
			this.tank.ifPresent(t -> { nbt.put("tank", t.writeToNBT(new CompoundNBT())); });
			this.input.ifPresent(i -> { nbt.put("inv", i.serializeNBT()); });
			
			return nbt;
		}
		
	    protected void onEmpty()
	    {
	    	if(this.callbacks != null)
	    		this.callbacks.handleOnEmpty();
	    }

		@Override
		public FluidStack getFluid() {
			if(this.tank.isPresent())
				return this.tank.resolve().get().getFluid();
			
			return FluidStack.EMPTY;
		}

		@Override
		public int getFluidAmount() {
			if(this.tank.isPresent())
				return this.tank.resolve().get().getFluidAmount();

			return 0;
		}

		@Override
		public int getCapacity() {
			if(this.tank.isPresent())
				return this.tank.resolve().get().getCapacity();

			return 0;
		}

		@Override
		public boolean isFluidValid(FluidStack stack) {
			if(this.tank.isPresent())
				return this.tank.resolve().get().isFluidValid(stack);

			return false;
		}

		@Override
		public int getTanks() {
			if(this.tank.isPresent())
				return this.tank.resolve().get().getTanks();

			return 0;
		}

		@Override
		public FluidStack getFluidInTank(int tank) {
			if(this.tank.isPresent())
				return this.tank.resolve().get().getFluidInTank(tank);

			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank) {
			if(this.tank.isPresent())
				return this.tank.resolve().get().getTankCapacity(tank);

			return 0;
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			if(this.tank.isPresent())
				return this.tank.resolve().get().isFluidValid(tank, stack);

			return false;
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			if(!this.isOutput && this.tank.isPresent())
				return this.tank.resolve().get().fill(resource, action);

			return 0;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			if(this.isOutput && this.tank.isPresent())
			{
				FluidTank t = this.tank.resolve().get(); 
				FluidStack fs = t.drain(resource, action);
				
				if(t.isEmpty())
					onEmpty();
				
				return fs;
			}

			return FluidStack.EMPTY;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			if(this.isOutput && this.tank.isPresent())
			{
				FluidTank t = this.tank.resolve().get(); 
				FluidStack fs = t.drain(maxDrain, action);
				
				if(t.isEmpty())
					onEmpty();
				
				return fs;
			}

			return FluidStack.EMPTY;
		}
	}
}

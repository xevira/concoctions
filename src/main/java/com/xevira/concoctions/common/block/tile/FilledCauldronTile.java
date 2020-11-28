package com.xevira.concoctions.common.block.tile;

import javax.annotation.Nullable;

import com.xevira.concoctions.Concoctions;
import com.xevira.concoctions.common.block.FilledCauldronBlock;
import com.xevira.concoctions.setup.Registry;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

public class FilledCauldronTile extends TileEntity {
	private FluidStack potionFluid;

	public FilledCauldronTile()
	{
		super(Registry.FILLED_CAULDRON_TILE.get());
		
		this.potionFluid = FluidStack.EMPTY;
	}
	
	public boolean setPotionFluid(FluidStack fluid)
	{
		if(fluid.getFluid() == Registry.POTION_FLUID.get())
		{
			if( this.potionFluid.isEmpty() )
			{
				this.potionFluid = fluid.copy();
				this.markContainingBlockForUpdate(null);
				this.markDirty();
				return true;
			}
		}
		return false;
	}

	public FluidStack getPotionFluid()
	{
		return this.potionFluid;
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
        
        if(compound.contains("fluid"))
        	this.potionFluid = new FluidStack(Registry.POTION_FLUID.get(),1,compound.getCompound("fluid"));
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
    	if(this.potionFluid.hasTag())
    		compound.put("fluid", this.potionFluid.getTag());
       
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

}

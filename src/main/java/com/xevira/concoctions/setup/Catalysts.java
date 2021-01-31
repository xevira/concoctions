package com.xevira.concoctions.setup;

import java.util.ArrayList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class Catalysts {
	
	private static final ArrayList<Catalyst> CATALYSTS = new ArrayList<Catalyst>();
	
	static
	{
																				// PER TICK
		CATALYSTS.add(new Catalyst(Registry.BEDROCK_SHARD.get(), 100, 300));	// 1/3
		CATALYSTS.add(new Catalyst(Items.NETHER_STAR, 1000, 600));				// 5/3
	}
	
	public static Catalyst getCatalyst(ItemStack stack)
	{
		for(Catalyst catalyst : CATALYSTS)
		{
			if(catalyst.item == stack.getItem())
				return catalyst;
		}
		
		return null;
	}

	public static class Catalyst
	{
		public final Item item;
		public final int strength;
		public final int time;
		
		public Catalyst(Item item, int strength, int time)
		{
			this.item = item;
			this.strength = strength;
			this.time = Math.max(time, 1);
		}
	}
}

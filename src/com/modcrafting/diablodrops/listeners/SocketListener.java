package com.modcrafting.diablodrops.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

import com.modcrafting.diablodrops.DiabloDrops;
import com.modcrafting.diablodrops.events.PreSocketEnhancementEvent;
import com.modcrafting.diablodrops.events.SocketEnhancementEvent;
import com.modcrafting.diablodrops.tier.Tome;
import com.modcrafting.skullapi.lib.Skull;
import com.modcrafting.toolapi.lib.Tool;
import com.stirante.PrettyScaryLib.Namer;

public class SocketListener implements Listener
{
	DiabloDrops plugin;

	public SocketListener(DiabloDrops instance)
	{
		plugin = instance;
	}

	@EventHandler
	public void onSmeltSocket(FurnaceSmeltEvent event)
	{
		if (!plugin.furnanceMap.containsKey(event.getBlock())
				&& !plugin.drop.isTool(event.getResult().getType()))
			return;
		ItemStack is = plugin.furnanceMap.remove(event.getBlock());
		Material fuel = is.getType();
		Tool tool = new Tool(event.getResult().getType());
		Tool oldtool = new Tool(event.getSource());
		boolean namTest = false;
		for (String n : oldtool.getLoreList())
		{
			if (n.equalsIgnoreCase("(Socket)"))
				namTest = true;
		}
		if (!namTest)
		{
			event.setResult(event.getSource());
			return;
		}

		int eni = plugin.config.getInt("SocketItem.EnhanceBy", 1);
		int ene = plugin.config.getInt("SocketItem.EnhanceMax", 10);
		for (Enchantment ench : oldtool.getEnchantments().keySet())
		{
			int il = oldtool.getEnchantments().get(ench);
			if (il < ene)
				il = il + eni;
			tool.addUnsafeEnchantment(ench, il);
		}

		if (fuel.equals(Material.SKULL))
		{
			ChatColor color = this.findColor(oldtool.getName());
			String skullName = new Skull(((CraftItemStack) is).getHandle())
					.getOwner();
			tool.setName(color + skullName + "'s "
					+ ChatColor.stripColor(oldtool.getName()));
		}
		else
		{
			tool.setName(oldtool.getName());
		}
		if (plugin.config.getBoolean("Lore.Enabled", true))
		{
			for (int i = 0; i < plugin.config.getInt("Lore.EnhanceAmount", 2); i++)
			{
				tool.setLore(plugin.lore.get(plugin.gen.nextInt(plugin.lore
						.size())));
			}
		}
		SocketEnhancementEvent see = new SocketEnhancementEvent(
				event.getSource(), is, tool, ((Furnace) event.getBlock()
						.getState()));
		plugin.getServer().getPluginManager().callEvent(see);
		if (see.isCancelled())
			return;
		event.setResult(tool);
		return;

	}

	@EventHandler
	public void burnGem(FurnaceBurnEvent event)
	{
		Furnace furn = (Furnace) event.getBlock().getState();
		ItemStack tis = furn.getInventory().getSmelting();
		if (plugin.drop.isArmor(tis.getType())
				|| plugin.drop.isTool(tis.getType()))
		{
			for (String name : plugin.config.getStringList("SocketItem.Items"))
			{
				if (event.getFuel().getType()
						.equals(Material.matchMaterial(name)))
				{
					boolean test = false;
					for (String t : new Tool(tis).getLoreList())
					{
						if (t.contains("Socket"))
							test = true;
					}
					if (Namer.getName(event.getFuel()) != null
							&& Namer.getName(event.getFuel())
									.contains("Socket") && test)
					{
						PreSocketEnhancementEvent psee = new PreSocketEnhancementEvent(
								tis, event.getFuel(), furn);
						plugin.getServer().getPluginManager().callEvent(psee);
						if (psee.isCancelled())
							continue;
						plugin.furnanceMap.put(event.getBlock(),
								event.getFuel());
						event.setBurnTime(240);
						event.setBurning(true);
						return;
					}
				}
			}
			event.setCancelled(true);
			event.setBurning(false);
			event.setBurnTime(120000);
			return;
		}
	}
	//Close enough.
	@EventHandler
	public void onCraftItem(CraftItemEvent e) {
	    ItemStack item = e.getCurrentItem();
	    if(item.getType().equals(Material.WRITTEN_BOOK)){
	        e.setCurrentItem(new Tome());
	    }
	}
	public ChatColor findColor(String s)
	{
		char[] c = s.toCharArray();
		for (int i = 0; i < c.length; i++)
		{
			if (c[i] == new Character((char) 167))
			{
				return ChatColor.getByChar(c[i + 1]);
			}
		}
		return null;
	}
}

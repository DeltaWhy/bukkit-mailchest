package com.github.deltawhy.mailchest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;

public class InventoryListener implements Listener {
	private MailChest plugin;
	private List<InventoryHolder> boxes;
	private List<Chest> chests;
	
	public InventoryListener(MailChest plugin) {
		this.plugin = plugin;
		this.boxes = new ArrayList<InventoryHolder>();
		this.chests = new ArrayList<Chest>();
	}
	
	public void add(InventoryHolder box, Chest chest) {
		this.boxes.add(box);
		this.chests.add(chest);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Inventory inv = event.getInventory();
		InventoryHolder box = inv.getHolder();
		if (box == null) return;
		
		int i = boxes.indexOf(box);
		if (i == -1) return;
		Chest chest = chests.get(i);
		
		Inventory chestInv = chest.getInventory();
				
		HumanEntity player = event.getPlayer();
		Inventory playerInv = player.getInventory();
		
		boolean overflowed = false;
		for (ItemStack stack : inv) {
			if (stack == null) continue;
			HashMap<Integer, ItemStack> overflow = chestInv.addItem(stack);
			if (overflow != null && overflow.size() > 0) {
				overflowed = true;
				for (ItemStack overStack : overflow.values()) {
					playerInv.addItem(overStack);
				}
			}
		}
		
		inv.clear();
		
		if (overflowed && player instanceof Player) {
			((Player)player).sendMessage(ChatColor.RED + "[MailChest] Couldn't fit everything in the mailbox."
					+ " Your items have been returned.");
		}
	}
}

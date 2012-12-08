package com.github.deltawhy.mailchest;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class Mailbox implements Serializable, InventoryHolder {
	private static final long serialVersionUID = 1L;

	private String ownerName;

	private transient Inventory inventory;
	
	public Mailbox(String ownerName) {
		this.setOwnerName(ownerName);
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	
	public String toString() {
		return String.format("<Mailbox owner=%s>", ownerName);
	}

	@Override
	public Inventory getInventory() {
		if (this.inventory == null) {
			this.inventory = Bukkit.createInventory(this, 9, ownerName + "'s Mailbox");
		}
		return this.inventory;
	}
}

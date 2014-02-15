package com.github.deltawhy.mailchest;

import java.util.HashMap;

import java.io.*;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.*;
import org.bukkit.entity.Player;

public class MailChest extends JavaPlugin {
	private HashMap<MailboxLocation, Mailbox> mailboxes;
	private InventoryListener inventoryListener;
	public ConfigAccessor userConfig;
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		getServer().getPluginManager().registerEvents(new BlockListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		inventoryListener = new InventoryListener(this);
		getServer().getPluginManager().registerEvents(inventoryListener, this);
		userConfig = new ConfigAccessor(this, "users.yml");
		userConfig.reloadConfig();
		readMailboxData();
	}

	@SuppressWarnings("unchecked")
	private void readMailboxData() {
		try {
			getLogger().info("Loading mailbox data");
			File mailboxFile = new File(getDataFolder(), "mailboxes.dat");
			if (mailboxFile.exists()) {
				FileInputStream fileIn = new FileInputStream(mailboxFile);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				Object read = in.readObject();
				if (read instanceof HashMap<?,?>) {
					mailboxes = (HashMap<MailboxLocation, Mailbox>) read;
				} else {
					getLogger().warning("[MailChest] Could not read data file!");
				}
				in.close();
				fileIn.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			getLogger().warning("Could not read data file!");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			getLogger().warning("Could not read data file!");
		}
		
		if (mailboxes == null) {
			mailboxes = new HashMap<MailboxLocation, Mailbox>();
		}
		
		//getLogger().info(mailboxes.toString());
	}
	
	private void writeMailboxData() {
		try {
			getLogger().info("Saving mailbox data");
			File mailboxFile = new File(getDataFolder(), "mailboxes.dat");
			FileOutputStream fileOut = new FileOutputStream(mailboxFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(mailboxes);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
			getLogger().warning("Could not write data file!");
		}
		
		//getLogger().info(mailboxes.toString());
	}
	
	public boolean autoCreateMailbox(Block chest, Player player) {
		if (isMailbox(chest)) {
			player.sendMessage(ChatColor.RED + "[MailChest] That's already a mailbox!");
			return false;
		} else if (!player.hasPermission("mailchest.autocreate")) {
			return false;
		} else {
			Mailbox box = new Mailbox(player.getName());
			mailboxes.put(new MailboxLocation(chest.getLocation()), box);
			writeMailboxData();
			player.sendMessage(ChatColor.GOLD + "[MailChest] Created a mailbox!");
			getLogger().info(player.getName() + " created a mailbox.");
			return true;
		}
	}
	
 	public boolean createMailbox(Block chest, Player creator, String ownerName) {
 		if (ownerName.equals("")) ownerName = creator.getName();
 		if (isMailbox(chest)) {
			creator.sendMessage(ChatColor.RED + "[MailChest] That's already a mailbox!");
			return false;
 		} else if (!creator.getName().equals(ownerName) && !creator.hasPermission("mailchest.create.others")) {
 			creator.sendMessage(ChatColor.RED + "[MailChest] You don't have permission to create mailboxes for other players.");
			return false;
		} else if (!creator.hasPermission("mailchest.create")) {
			creator.sendMessage(ChatColor.RED + "[MailChest] You don't have permission to create mailboxes.");
			return false;
		} else if (!getServer().getOfflinePlayer(ownerName).hasPlayedBefore()) {
			creator.sendMessage(ChatColor.RED + "[MailChest] Couldn't find player " + ownerName + ".");
			return false;
		} else {
			Mailbox box = new Mailbox(creator.getName(), ownerName);
			mailboxes.put(new MailboxLocation(chest.getLocation()), box);
			writeMailboxData();
			creator.sendMessage(ChatColor.GOLD + "[MailChest] Created a mailbox!");
			getLogger().info(creator.getName() + " created a mailbox.");
			return true;
		}
 	}
 	
 	public boolean isMailbox(Block block) {
 		return mailboxes.containsKey(new MailboxLocation(block.getLocation()));
 	}
 	
 	public Mailbox getMailbox(Block block) {
 		return mailboxes.get(new MailboxLocation(block.getLocation()));
 	}
 	
 	public Player getMailboxOwner(Block block) {
 		Mailbox mailbox = getMailbox(block);
 		if (mailbox == null) return null;
 		return getServer().getPlayerExact(mailbox.getOwnerName());
 	}
 	
 	public Player getMailboxOwner(Mailbox mailbox) {
 		return getServer().getPlayerExact(mailbox.getOwnerName());
 	}

	public boolean destroyMailbox(Player player, Block block) {
		Mailbox box = getMailbox(block);
		if (box == null) return true;
		if (player == null) return !getConfig().getBoolean("protect-mailboxes");
		if (player.getName().equals(box.getOwnerName()) || player.getName().equals(box.getCreatorName()) 
				|| player.hasPermission("mailchest.destroy")) {
			mailboxes.remove(new MailboxLocation(block.getLocation()));
			writeMailboxData();
			player.sendMessage(ChatColor.GOLD + "[MailChest] Destroyed a mailbox.");
			if (player.getName().equals(box.getOwnerName())) {
				getLogger().info(player.getName() + " destroyed their mailbox.");
			} else {
				getLogger().info(player.getName() + " destroyed " + box.getOwnerName() + "'s mailbox.");
			}
			return true;
		} else {
			player.sendMessage(ChatColor.RED + "[MailChest] You don't have permission to destroy this mailbox.");
			return false;
		}
	}

	public void openMailbox(Player player, Block block) {
		Mailbox box = getMailbox(block);
		Chest chest = (Chest)block.getState();
		
		if (!player.hasPermission("mailchest.send")) {
			player.sendMessage(ChatColor.RED + "[MailChest] You don't have permission to send mail.");
		} else {
			inventoryListener.add(box, chest);
			player.openInventory(box.getInventory());
		}
	}

	public void gotMail(String ownerName) {
		userConfig.getConfig().set(ownerName + ".got-mail", new Boolean(true));
		userConfig.saveConfig();
	}
}

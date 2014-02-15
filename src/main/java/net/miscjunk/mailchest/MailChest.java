package net.miscjunk.mailchest;

import java.util.HashMap;

import java.io.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.mcstats.Metrics;

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
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            Bukkit.getLogger().info("Failed to send metrics");
        }
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
	
	public boolean autoCreateMailbox(Inventory inv, Player player) {
		if (isMailbox(inv)) {
			player.sendMessage(ChatColor.RED + "[MailChest] That's already a mailbox!");
			return false;
		} else if (!player.hasPermission("mailchest.autocreate")) {
			return false;
		} else {
			Mailbox box = new Mailbox(player.getName());
            Location l;
            if (inv.getHolder() instanceof BlockState) {
                l = ((BlockState)inv.getHolder()).getLocation();
            } else if (inv.getHolder() instanceof DoubleChest) {
                l = ((DoubleChest)inv.getHolder()).getLocation();
            } else {
                return false;
            }
			mailboxes.put(new MailboxLocation(l), box);
			writeMailboxData();
			player.sendMessage(ChatColor.GOLD + "[MailChest] Created a mailbox!");
			getLogger().info(player.getName() + " created a mailbox.");
			return true;
		}
	}
	
 	public boolean createMailbox(Inventory inv, Player creator, String ownerName) {
 		if (ownerName.equals("")) ownerName = creator.getName();
 		if (isMailbox(inv)) {
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
            Location l;
            if (inv.getHolder() instanceof BlockState) {
                l = ((BlockState)inv.getHolder()).getLocation();
            } else if (inv.getHolder() instanceof DoubleChest) {
                l = ((DoubleChest)inv.getHolder()).getLocation();
            } else {
                return false;
            }
			mailboxes.put(new MailboxLocation(l), box);
			writeMailboxData();
			creator.sendMessage(ChatColor.GOLD + "[MailChest] Created a mailbox!");
			getLogger().info(creator.getName() + " created a mailbox.");
			return true;
		}
 	}
 	
 	public boolean isMailbox(Block block) {
        if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST) return false;
        return isMailbox(((InventoryHolder) block.getState()).getInventory());
 	}

    public boolean isMailbox(Inventory inv) {
        InventoryHolder h = inv.getHolder();
        Location l;
        if (h instanceof BlockState) {
            l = ((BlockState)h).getLocation();
        } else if (h instanceof DoubleChest) {
            l = ((DoubleChest)h).getLocation();
        } else {
            return false;
        }
        return mailboxes.containsKey(new MailboxLocation(l));
    }

    public void addMailbox(MailboxLocation l, Mailbox m) {
        mailboxes.put(l, m);
    }

    public void removeMailbox(Inventory inv) {
        InventoryHolder h = inv.getHolder();
        Location l;
        if (h instanceof BlockState) {
            l = ((BlockState)h).getLocation();
        } else if (h instanceof DoubleChest) {
            l = ((DoubleChest)h).getLocation();
        } else {
            return;
        }
        mailboxes.remove(new MailboxLocation(l));
    }
 	
 	public Mailbox getMailbox(Block block) {
        if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST) return null;
 		return getMailbox(((InventoryHolder)block.getState()).getInventory());
 	}
    public Mailbox getMailbox(Inventory inv) {
        InventoryHolder h = inv.getHolder();
        Location l;
        if (h instanceof BlockState) {
            l = ((BlockState)h).getLocation();
        } else if (h instanceof DoubleChest) {
            l = ((DoubleChest)h).getLocation();
        } else {
            return null;
        }
        return mailboxes.get(new MailboxLocation(l));
    }

 	public Player getMailboxOwner(Inventory inv) {
 		Mailbox mailbox = getMailbox(inv);
 		if (mailbox == null) return null;
 		return getServer().getPlayerExact(mailbox.getOwnerName());
 	}
 	
 	public Player getMailboxOwner(Mailbox mailbox) {
 		return getServer().getPlayerExact(mailbox.getOwnerName());
 	}

    public boolean destroyMailbox(Player player, Block block) {
        if (block.getState() instanceof InventoryHolder) {
            return destroyMailbox(player, ((InventoryHolder)block.getState()).getInventory());
        } else {
            return true;
        }
    }
	public boolean destroyMailbox(Player player, Inventory inv) {
		Mailbox box = getMailbox(inv);
		if (box == null) return true;
		if (player == null) return !getConfig().getBoolean("protect-mailboxes");
		if (player.getName().equals(box.getOwnerName()) || player.getName().equals(box.getCreatorName()) 
				|| player.hasPermission("mailchest.destroy")) {
            Location l;
            if (inv.getHolder() instanceof BlockState) {
                l = ((BlockState)inv.getHolder()).getLocation();
            } else if (inv.getHolder() instanceof DoubleChest) {
                l = ((DoubleChest)inv.getHolder()).getLocation();
            } else {
                return false;
            }
			mailboxes.remove(new MailboxLocation(l));
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

	public void openMailbox(Player player, Inventory inv) {
		Mailbox box = getMailbox(inv);

		if (!player.hasPermission("mailchest.send")) {
			player.sendMessage(ChatColor.RED + "[MailChest] You don't have permission to send mail.");
		} else {
			inventoryListener.add(box, inv);
			player.openInventory(box.getInventory());
		}
	}

	public void gotMail(String ownerName) {
		userConfig.getConfig().set(ownerName + ".got-mail", new Boolean(true));
		userConfig.saveConfig();
	}
}

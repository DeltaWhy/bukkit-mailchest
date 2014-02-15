package net.miscjunk.mailchest;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.*;

public class BlockListener implements Listener {
	private MailChest plugin;
	
	public BlockListener(MailChest plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (player == null)
			return;
		
		Block block = event.getBlock();
		if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            Inventory inv = ((InventoryHolder)event.getBlock().getState()).getInventory();
            if (inv instanceof DoubleChestInventory) {
                Mailbox box;
                DoubleChestInventory dc = (DoubleChestInventory)inv;
                if (plugin.isMailbox(dc.getLeftSide())) {
                    box = plugin.getMailbox(dc.getLeftSide());
                } else if (plugin.isMailbox(dc.getRightSide())) {
                    box = plugin.getMailbox(dc.getRightSide());
                } else {
                    return;
                }
                player.sendMessage(ChatColor.GOLD + "[MailChest] Extended mailbox.");
                plugin.removeMailbox(dc.getLeftSide());
                plugin.removeMailbox(dc.getRightSide());
                plugin.addMailbox(new MailboxLocation(dc.getHolder().getLocation()), box);
            } else {
                Block beneathBlock = block.getRelative(BlockFace.DOWN);
                if (beneathBlock != null && beneathBlock.getType() == Material.FENCE
                        && plugin.getConfig().getBoolean("auto-create.fence")) {
                    plugin.autoCreateMailbox(inv, player);
                } else if (beneathBlock != null	&& beneathBlock.getType() == Material.COBBLE_WALL
                        && plugin.getConfig().getBoolean("auto-create.wall")) {
                    plugin.autoCreateMailbox(inv, player);
                }
            }
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (plugin.isMailbox(block)) {
			Player player = event.getPlayer();
            Inventory inv = ((InventoryHolder)block.getState()).getInventory();
			if (!plugin.destroyMailbox(player, inv)) {
				event.setCancelled(true);
			}
		} else if (block.getType() == Material.WALL_SIGN) {
			Sign sign = (Sign)block.getState();
            org.bukkit.material.Sign mat = (org.bukkit.material.Sign)block.getState().getData();
			if (mat.isWallSign() && sign.getLine(0).equals("[" + plugin.getConfig().getString("sign-text") + "]")) {
				Player player = event.getPlayer();

				Block chest = block.getRelative(mat.getAttachedFace());
				
				if (chest == null) {
					return;
				}
                Inventory inv = ((InventoryHolder)chest.getState()).getInventory();

				if (!plugin.destroyMailbox(player, inv)) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (event.getLine(0).equals("[" + plugin.getConfig().getString("sign-text") + "]")) {
			Player creator = event.getPlayer();
			Block signBlock = event.getBlock();
            org.bukkit.material.Sign mat = (org.bukkit.material.Sign)signBlock.getState().getData();
            if (!mat.isWallSign()) return;

			Block chest = signBlock.getRelative(mat.getAttachedFace());
			
			if (chest == null || chest.getType() != Material.CHEST && chest.getType() != Material.TRAPPED_CHEST) {
				creator.sendMessage(ChatColor.RED + "[MailChest] No chest found.");
				event.setCancelled(true);
				return;
			}

            if (chest.getState() instanceof InventoryHolder) {
                Inventory inv = ((InventoryHolder)chest.getState()).getInventory();

                if (!plugin.createMailbox(inv, creator, event.getLine(1))) {
                    event.setCancelled(true);
                }
            }
		}
	}

	private Block findAdjacentChest(Block block) {
		BlockFace[] directions = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
		Block chest = null;
		
		for (BlockFace direction : directions) {
			if (block.getRelative(direction).getType() == Material.CHEST || block.getRelative(direction).getType() == Material.TRAPPED_CHEST) {
				chest = block.getRelative(direction);
				break;
			}
		}
		return chest;
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) return;
		for (int i=0; i < event.blockList().size(); i++) {
			Block block = event.blockList().get(i);
			if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
				if (!plugin.destroyMailbox(null, block)) {
					event.blockList().remove(i);
					i--;
				}
			} else if (block.getType() == Material.WALL_SIGN) {
                org.bukkit.material.Sign mat = (org.bukkit.material.Sign)block.getState().getData();
				Block chest = block.getRelative(mat.getAttachedFace());
				if (chest != null && !plugin.destroyMailbox(null, chest)) {
					event.blockList().remove(i);
					i--;
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event) {
		if (event.isCancelled()) return;
		Block block = event.getBlock();
		if (plugin.isMailbox(block)) {
			if (!plugin.destroyMailbox(null, block)) {
				event.setCancelled(true);
			}
		} else if (block.getType() == Material.WALL_SIGN) {
			Sign sign = (Sign)block.getState();
			if (sign.getLine(0).equals("[" + plugin.getConfig().getString("sign-text") + "]")) {
                org.bukkit.material.Sign mat = (org.bukkit.material.Sign)block.getState().getData();
                Block chest = block.getRelative(mat.getAttachedFace());

				if (chest == null) {
					return;
				}
				
				if (!plugin.destroyMailbox(null, chest)) {
					event.setCancelled(true);
				}
			}
		}
	}
}

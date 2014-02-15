package com.github.deltawhy.mailchest;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;

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
		if (block.getType() == Material.CHEST) {
			Block otherChest = findAdjacentChest(block);
			if (otherChest != null) {
				if (plugin.isMailbox(otherChest)) {
					player.sendMessage(ChatColor.RED + "[MailChest] You can't have a double chest mailbox.");
					event.setCancelled(true);
					return;
				} else {
					return;
				}
			}
			Block beneathBlock = block.getRelative(BlockFace.DOWN);
			if (beneathBlock != null && beneathBlock.getType() == Material.FENCE 
					&& plugin.getConfig().getBoolean("auto-create.fence")) {
				plugin.autoCreateMailbox(block, player);
			} else if (beneathBlock != null	&& beneathBlock.getType() == Material.COBBLE_WALL
					&& plugin.getConfig().getBoolean("auto-create.wall")) {
				plugin.autoCreateMailbox(block, player);
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block.getType() == Material.CHEST && plugin.isMailbox(block)) {
			Player player = event.getPlayer();
			if (!plugin.destroyMailbox(player, block)) {
				event.setCancelled(true);
			}
		} else if (block.getType() == Material.WALL_SIGN) {
			Sign sign = (Sign)block.getState();
			if (sign.getLine(0).equals("[" + plugin.getConfig().getString("sign-text") + "]")) {
				Player player = event.getPlayer();
				
				Block chest = findAdjacentChest(block);
				
				if (chest == null) {
					return;
				}
				
				if (!plugin.destroyMailbox(player, chest)) {
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
			
			Block chest = findAdjacentChest(signBlock);
			
			if (chest == null) {
				creator.sendMessage(ChatColor.RED + "[MailChest] No chest found.");
				event.setCancelled(true);
				return;
			}
			
			Block otherChest = findAdjacentChest(chest);
			
			if (otherChest != null) {
				creator.sendMessage(ChatColor.RED + "[MailChest] You can't have a double chest mailbox.");
				event.setCancelled(true);
				return;
			}
			
			if (plugin.createMailbox(chest, creator, event.getLine(1))) {
				//attach sign to chest
				if (signBlock.getType() == Material.SIGN_POST) {
					signBlock.setType(Material.WALL_SIGN);
					switch (signBlock.getFace(chest)) {
					case NORTH:
						signBlock.setData((byte) 5);
						break;
					case SOUTH:
						signBlock.setData((byte) 4);
						break;
					case EAST:
						signBlock.setData((byte) 3);
						break;
					case WEST:
						signBlock.setData((byte) 2);
						break;
					default:
						break;
					}
					Sign sign = (Sign)signBlock.getState();
					sign.setLine(0, event.getLine(0));
					sign.setLine(1, creator.getName());
					sign.setLine(2, "");
					sign.setLine(3, "");
					sign.update(true);
				}
			} else {
				event.setCancelled(true);
			}
		}
	}

	private Block findAdjacentChest(Block block) {
		BlockFace[] directions = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
		Block chest = null;
		
		for (BlockFace direction : directions) {
			if (block.getRelative(direction).getType() == Material.CHEST) {
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
			if (block.getType() == Material.CHEST) {
				if (!plugin.destroyMailbox(null, block)) {
					event.blockList().remove(i);
					i--;
				}
			} else if (block.getType() == Material.WALL_SIGN) {
				Block chest = findAdjacentChest(block);
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
		if (block.getType() == Material.CHEST && plugin.isMailbox(block)) {
			if (!plugin.destroyMailbox(null, block)) {
				event.setCancelled(true);
			}
		} else if (block.getType() == Material.WALL_SIGN) {
			Sign sign = (Sign)block.getState();
			if (sign.getLine(0).equals("[" + plugin.getConfig().getString("sign-text") + "]")) {
				Block chest = findAdjacentChest(block);
				
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

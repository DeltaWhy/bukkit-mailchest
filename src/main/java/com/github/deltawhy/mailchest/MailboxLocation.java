package com.github.deltawhy.mailchest;
import java.io.Serializable;

import org.bukkit.Location;

public class MailboxLocation implements Serializable {

	private static final long serialVersionUID = 1L;

	private String worldName;
	private int x;
	private int y;
	private int z;
	
	public MailboxLocation(Location loc) {
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();
		this.worldName = loc.getWorld().getName();
	}
	
	@Override
	public String toString() {
		return String.format("<world=%s x=%d y=%d z=%d>", worldName, x, y, z);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof MailboxLocation) {
			MailboxLocation l = (MailboxLocation) o;
			return (l.worldName.equals(worldName) && l.x == x && l.y == y && l.z == z);
		} else {
			return false;
		}
	}
}

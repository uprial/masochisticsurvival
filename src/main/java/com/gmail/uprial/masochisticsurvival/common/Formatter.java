package com.gmail.uprial.masochisticsurvival.common;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Formatter {
   public static String format(final Entity entity) {
        return String.format("%s[%s:%.0f:%.0f:%.0f]",
                entity.getType(),
                entity.getWorld().getName(),
                entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ());
    }

    public static String format(final Player player) {
        return String.format("%s[%s:%.0f:%.0f:%.0f]",
                player.getName(),
                player.getWorld().getName(),
                player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
    }

    public static String format(final Location location) {
        return String.format("%s:%.0f:%.0f:%.0f",
                (location.getWorld() != null) ? location.getWorld().getName() : "empty",
                location.getX(), location.getY(), location.getZ());
    }
}

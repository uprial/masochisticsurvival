package com.gmail.uprial.masochisticsurvival.common;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;

public class Formatter {
    public static String format(final Entity entity) {
        if (entity instanceof Player) {
            return format((Player) entity);
        }
        return String.format("%s[%s:%.0f:%.0f:%.0f]",
                entity.getType(),
                entity.getWorld().getName(),
                entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ())
                +
                ((entity instanceof Projectile)
                        ? String.format(" launched by %s", ps2string(((Projectile)entity).getShooter()))
                        : "");
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

    private static String ps2string(final ProjectileSource ps) {
        if(ps instanceof Entity) {
            return format((Entity)ps);
        } else {
            return ps.getClass().getName();
        }
    }
}

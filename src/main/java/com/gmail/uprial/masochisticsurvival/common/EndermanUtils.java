package com.gmail.uprial.masochisticsurvival.common;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;

public class EndermanUtils {
    public static boolean isAppropriatePlayer(final Player player, final boolean baby) {
        final double yHeadroom = baby ? 1.1D : 2.1D;
        return (player.isValid())
                && (player.getWorld()
                .getBlockAt(player.getLocation().clone().add(0.0D, yHeadroom, 0.0D)).isPassable());
    }

    public static void setBaby(final Enderman enderman) {
        final AttributeInstance a = enderman.getAttribute(Attribute.SCALE);
        a.setBaseValue(a.getBaseValue() * 0.65);
    }
}

package com.gmail.uprial.masochisticsurvival.common;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;

public class EndermanUtils {
    /*
        According to https://minecraft.wiki/w/Enderman,
        Enderman height is 2.9 blocks.
     */
    private static final double HEIGHT = 2.9D;
    /*
        Must make Enderman height less than 2.0,
        which means at most 2.0/2.9=0.689655.
     */
    private static final double BABY_SCALE = 0.6D;

    public static boolean isAppropriatePlayer(final Player player, final boolean baby) {
        final double yHeadroom = baby ? (HEIGHT * BABY_SCALE) : HEIGHT;

        return (player.isValid())
                && (player.getWorld()
                .getBlockAt(player.getLocation().clone().add(0.0D, yHeadroom, 0.0D)).isPassable());
    }

    public static void setBaby(final Enderman enderman) {
        final AttributeInstance a = enderman.getAttribute(Attribute.SCALE);
        a.setBaseValue(a.getBaseValue() * BABY_SCALE);
    }
}

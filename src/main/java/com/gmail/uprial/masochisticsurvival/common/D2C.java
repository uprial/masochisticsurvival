package com.gmail.uprial.masochisticsurvival.common;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class D2C {
    public static double get(final LivingEntity entity) {
        final Location location = entity.getLocation();
        return Math.sqrt(
                Math.pow(Math.abs(location.getX()), 2.0D)
                        +
                Math.pow(Math.abs(location.getZ()), 2.0D)
        );
    }
}

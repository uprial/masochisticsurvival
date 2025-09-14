package com.gmail.uprial.masochisticsurvival.helpers;

import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;

public class TestDamageSourceBuilder implements DamageSource.Builder {
    @Override
    public DamageSource.Builder withCausingEntity(Entity entity) {
        return null;
    }

    @Override
    public DamageSource.Builder withDirectEntity(Entity entity) {
        return null;
    }

    @Override
    public DamageSource.Builder withDamageLocation(Location location) {
        return null;
    }

    @Override
    public DamageSource build() {
        return new DamageSource() {
            @Override
            public DamageType getDamageType() {
                return null;
            }

            @Override
            public Entity getCausingEntity() {
                return null;
            }

            @Override
            public Entity getDirectEntity() {
                return null;
            }

            @Override
            public Location getDamageLocation() {
                return null;
            }

            @Override
            public Location getSourceLocation() {
                return null;
            }

            @Override
            public boolean isIndirect() {
                return false;
            }

            @Override
            public float getFoodExhaustion() {
                return 0;
            }

            @Override
            public boolean scalesWithDifficulty() {
                return false;
            }
        };
    }
}

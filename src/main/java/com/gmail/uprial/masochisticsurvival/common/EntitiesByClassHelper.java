package com.gmail.uprial.masochisticsurvival.common;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

/*
    More performant than getEntitiesByClass()
 */
public class EntitiesByClassHelper {
    public static void fetch(final World world,
                             final Set<EntityType> types,
                             final Consumer<Entity> consumer) {
        for(final Entity entity : world.getEntities()) {
            if(types.contains(entity.getType()) && entity.isValid()) {
                consumer.accept(entity);
            }
        }
    }

    public static <T extends Entity> Collection<T> get(final World world,
                                                       final EntityType type) {
        final Collection<T> entities = new ArrayList<>();
        for(final Entity entity : world.getEntities()) {
            if(entity.getType().equals(type) && entity.isValid()) {
                entities.add((T)entity);
            }
        }

        return entities;
    }
}

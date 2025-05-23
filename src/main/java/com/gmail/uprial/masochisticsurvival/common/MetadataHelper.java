package com.gmail.uprial.masochisticsurvival.common;

import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class MetadataHelper {
    public static void setMetadata(JavaPlugin plugin, LivingEntity entity, String key, Object value) {
        MetadataValue metadataValue = new FixedMetadataValue(plugin, value);
        entity.setMetadata(key, metadataValue);
    }

    public static <T> T getMetadata(LivingEntity entity, String key) {
        MetadataValue metadataValue = getMetadataValue(entity, key);
        //noinspection unchecked
        return (metadataValue != null) ? (T) metadataValue.value() : null;
    }

    public interface MetadataGetter<T> {
        T get();
    }
    public static <T> T getMetadataOrDefault(JavaPlugin plugin, LivingEntity entity, String key, MetadataGetter<T> getter) {
        T result = getMetadata(entity, key);
        if(result == null) {
            result = getter.get();
            setMetadata(plugin, entity, key, result);
        }

        return result;
    }

    private static MetadataValue getMetadataValue(LivingEntity entity, String key) {
        if (entity.hasMetadata(key)) {
            List<MetadataValue> metadataValues = entity.getMetadata(key);
            if (!metadataValues.isEmpty()) {
                return metadataValues.get(0);
            }
        }

        return null;
    }
}

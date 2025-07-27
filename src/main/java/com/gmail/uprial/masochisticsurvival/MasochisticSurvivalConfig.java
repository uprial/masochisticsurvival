package com.gmail.uprial.masochisticsurvival;

import com.gmail.uprial.masochisticsurvival.common.CustomLogger;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderSimple;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.listeners.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

import java.util.*;

public final class MasochisticSurvivalConfig {
    private final Map<String,Listener> listeners;

    private MasochisticSurvivalConfig(final Map<String,Listener> listeners) {
        this.listeners = listeners;
    }

    public List<Listener> getListeners() {
        final List<Listener> listeners = new ArrayList<>();

        for(final Listener listener : this.listeners.values()) {
            if(listener != null) {
                listeners.add(listener);
            }
        }

        return listeners;
    }

    static boolean isDebugMode(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        return ConfigReaderSimple.getBoolean(config, customLogger, "debug", "'debug' flag", false);
    }

    public static MasochisticSurvivalConfig getFromConfig(MasochisticSurvival plugin, FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        final Map<String,Listener> listeners = new LinkedHashMap<>();

        listeners.put("nasty-enderman", NastyEndermanListener.getFromConfig(config, customLogger, "nasty-enderman", "'nasty-enderman'"));
        listeners.put("nasty-archer", NastyArcherListener.getFromConfig(plugin, config, customLogger, "nasty-archer", "'nasty-archer'"));
        listeners.put("angry-shooter", AngryShooterListener.getFromConfig(plugin, config, customLogger, "angry-shooter", "'angry-shooter'"));
        listeners.put("nasty-ender-dragon", NastyEnderDragonListener.getFromConfig(plugin, config, customLogger, "nasty-ender-dragon", "'nasty-ender-dragon'"));
        listeners.put("explosive-shooter", ExplosiveShooterListener.getFromConfig(plugin, config, customLogger, "explosive-shooter", "'explosive-shooter'"));
        listeners.put("greedy-villager", GreedyVillagerListener.getFromConfig(config, customLogger, "greedy-villager", "'greedy-villager'"));
        listeners.put("limit-elytras", LimitElytrasListener.getFromConfig(config, customLogger, "limit-elytras", "'limit-elytras'"));
        listeners.put("hydra-spiders", HydraSpiderListener.getFromConfig(config, customLogger, "hydra-spiders", "'hydra-spiders'"));
        listeners.put("radical-phantoms", RadicalPhantomListener.getFromConfig(plugin, config, customLogger, "radical-phantoms", "'radical-phantoms'"));

        return new MasochisticSurvivalConfig(listeners);
    }

    @Override
    public String toString() {
        return listeners.toString();
    }
}
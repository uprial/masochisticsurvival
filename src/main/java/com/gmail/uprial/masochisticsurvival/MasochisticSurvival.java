package com.gmail.uprial.masochisticsurvival;

import com.gmail.uprial.masochisticsurvival.common.CustomLogger;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.listeners.TimeListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.gmail.uprial.masochisticsurvival.MasochisticSurvivalCommandExecutor.COMMAND_NS;

public final class MasochisticSurvival extends JavaPlugin {
    private final String CONFIG_FILE_NAME = "config.yml";
    private final File configFile = new File(getDataFolder(), CONFIG_FILE_NAME);

    private CustomLogger consoleLogger = null;

    private List<TimeListener> timeListenerList = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        consoleLogger = new CustomLogger(getLogger());

        register(loadConfig(this, getConfig(), consoleLogger));

        getCommand(COMMAND_NS).setExecutor(new MasochisticSurvivalCommandExecutor(this));
        consoleLogger.info("Plugin enabled");
    }

    private void register(final MasochisticSurvivalConfig masochisticSurvivalConfig) {
        for(final Listener listener : masochisticSurvivalConfig.getListeners()) {
            if(listener != null) {
                getServer().getPluginManager().registerEvents(listener, this);
                if(listener instanceof TimeListener) {
                    final TimeListener timeListener = (TimeListener) listener;
                    timeListener.register();
                    timeListenerList.add(timeListener);
                }
            }
        }
    }

    private void unregister() {
        HandlerList.unregisterAll(this);
        timeListenerList.forEach(TimeListener::unregister);
        timeListenerList.clear();
    }

    public boolean reloadMasochisticSurvivalConfig(CustomLogger userLogger) {
        reloadConfig();

        final MasochisticSurvivalConfig masochisticSurvivalConfig = loadConfig(this, getConfig(), consoleLogger, userLogger);
        if(masochisticSurvivalConfig != null) {
            unregister();
            register(masochisticSurvivalConfig);

            return true;
        } else {
            return false;
        }
    }

    public void scheduleDelayed(final Runnable runnable, final long delay) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, runnable, delay);
    }

    @Override
    public void onDisable() {
        unregister();
        consoleLogger.info("Plugin disabled");
    }

    @Override
    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            saveResource(CONFIG_FILE_NAME, false);
        }
    }

    @Override
    public FileConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(configFile);
    }

    static MasochisticSurvivalConfig loadConfig(MasochisticSurvival plugin, FileConfiguration config, CustomLogger customLogger) {
        return loadConfig(plugin, config, customLogger, null);
    }

    private static MasochisticSurvivalConfig loadConfig(MasochisticSurvival plugin, FileConfiguration config, CustomLogger mainLogger, CustomLogger secondLogger) {
        MasochisticSurvivalConfig masochisticSurvivalConfig = null;

        try {
            final boolean isDebugMode = MasochisticSurvivalConfig.isDebugMode(config, mainLogger);
            mainLogger.setDebugMode(isDebugMode);
            if(secondLogger != null) {
                secondLogger.setDebugMode(isDebugMode);
            }

            masochisticSurvivalConfig = MasochisticSurvivalConfig.getFromConfig(plugin, config, mainLogger);
        } catch (InvalidConfigException e) {
            mainLogger.error(e.getMessage());
            if(secondLogger != null) {
                secondLogger.error(e.getMessage());
            }
        }

        return masochisticSurvivalConfig;
    }
}

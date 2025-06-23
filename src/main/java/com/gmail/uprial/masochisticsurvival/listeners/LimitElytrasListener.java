package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.common.CustomLogger;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderNumbers;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderSimple;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import static com.gmail.uprial.masochisticsurvival.common.Formatter.format;
import static com.gmail.uprial.masochisticsurvival.common.Utils.joinPaths;
import static com.gmail.uprial.masochisticsurvival.common.Utils.seconds2ticks;

public class LimitElytrasListener implements Listener {
    private final int worldMaxHeightExcess;
    private final int freezeTicksInS;

    private final CustomLogger customLogger;

    public LimitElytrasListener(final CustomLogger customLogger,
                                final int worldMaxHeightExcess,
                                final int freezeTicksInS) {
        this.customLogger = customLogger;
        this.worldMaxHeightExcess = worldMaxHeightExcess;
        this.freezeTicksInS = freezeTicksInS;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.isCancelled()) {
            final Player player = event.getPlayer();
            if(player.getLocation().getY() > player.getWorld().getMaxHeight() + worldMaxHeightExcess) {
                /*
                    According to https://minecraft.wiki/w/Powder_Snow#Freezing,
                    After seven seconds, the player begins taking damage.
                 */
                player.setFreezeTicks(seconds2ticks(freezeTicksInS + 7));
                if (customLogger.isDebugMode()) {
                    customLogger.debug(String.format("%s frozen", format(player)));
                }
            }
        }
    }

    public static LimitElytrasListener getFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        if(!ConfigReaderSimple.getBoolean(config, customLogger,
                joinPaths(key, "enabled"), String.format("'enabled' flag of %s", title))) {

            return null;
        }

        int worldMaxHeightExcess = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "world-max-height-excess"), String.format("world max height excess of %s", title), -10_000, 10_000);
        int freezeTicksInS = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "freeze-ticks-in-s"), String.format("freeze ticks in s of %s", title), 1, 300);

        return new LimitElytrasListener(customLogger, worldMaxHeightExcess, freezeTicksInS);
    }

    @Override
    public String toString() {
        return String.format("{world-max-height-excess: %d, freeze-ticks-in-s: %d}", worldMaxHeightExcess, freezeTicksInS);
    }
}

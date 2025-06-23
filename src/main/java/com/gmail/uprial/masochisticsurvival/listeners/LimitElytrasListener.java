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
    private final int initialHeightExcess;
    private final int heightPerFreezeSecond;

    private final CustomLogger customLogger;

    public LimitElytrasListener(final CustomLogger customLogger,
                                final int initialHeightExcess,
                                final int heightPerFreezeSecond) {
        this.customLogger = customLogger;
        this.initialHeightExcess = initialHeightExcess;
        this.heightPerFreezeSecond = heightPerFreezeSecond;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.isCancelled()) {
            final Player player = event.getPlayer();
            final int freezeSeconds
                    = (int)Math.round((player.getLocation().getY()
                    - player.getWorld().getMaxHeight()
                    - initialHeightExcess)
                    / heightPerFreezeSecond);
            if((freezeSeconds > 0) && (seconds2ticks(freezeSeconds) > player.getFreezeTicks())) {
                player.setFreezeTicks(seconds2ticks(freezeSeconds));
                if (customLogger.isDebugMode()) {
                    customLogger.debug(String.format("%s frozen for %d seconds", format(player), freezeSeconds));
                }
            }
        }
    }

    public static LimitElytrasListener getFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        if(!ConfigReaderSimple.getBoolean(config, customLogger,
                joinPaths(key, "enabled"), String.format("'enabled' flag of %s", title))) {

            return null;
        }

        int initialHeightExcess = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "initial-height-excess"), String.format("initial height excess of %s", title), -10_000, 10_000);
        int heightPerFreezeSecond = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "height-per-freeze-second"), String.format("height per freeze second of %s", title), 1, 300);

        return new LimitElytrasListener(customLogger, initialHeightExcess, heightPerFreezeSecond);
    }

    @Override
    public String toString() {
        return String.format("{initial-height-excess: %d, height-per-freeze-second: %d}", initialHeightExcess, heightPerFreezeSecond);
    }
}

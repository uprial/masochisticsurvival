package com.gmail.uprial.masochisticsurvival;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.gmail.uprial.masochisticsurvival.common.CustomLogger;

class MasochisticSurvivalCommandExecutor implements CommandExecutor {
    public static final String COMMAND_NS = "masochisticsurvival";

    private final MasochisticSurvival plugin;

    MasochisticSurvivalCommandExecutor(MasochisticSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase(COMMAND_NS)) {
            final CustomLogger customLogger = new CustomLogger(plugin.getLogger(), sender);
            if ((args.length >= 1) && (args[0].equalsIgnoreCase("reload"))) {
                if (sender.hasPermission(COMMAND_NS + ".reload")) {
                    if(plugin.reloadMasochisticSurvivalConfig(customLogger)) {
                        customLogger.info("MasochisticSurvival config reloaded.");
                    }
                    return true;
                }
            } else if ((args.length == 0) || (args[0].equalsIgnoreCase("help"))) {
                String helpString = "==== MasochisticSurvival help ====\n";

                if (sender.hasPermission(COMMAND_NS + ".reload")) {
                    helpString += '/' + COMMAND_NS + " reload - reload config from disk\n";
                }

                customLogger.info(helpString);
                return true;
            }
        }
        return false;
    }
}

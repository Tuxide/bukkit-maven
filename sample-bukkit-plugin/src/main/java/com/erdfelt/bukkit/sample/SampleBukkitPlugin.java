package com.erdfelt.bukkit.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SampleBukkitPlugin extends JavaPlugin {
    private static final Logger                 log           = Logger.getLogger(SampleBukkitPlugin.class.getName());

    private String                              pluginId      = SampleBukkitPlugin.class.getSimpleName();
    private final MagicStixBlockListener        blockListener = new MagicStixBlockListener(this);
    private final Map<Player, ArrayList<Block>> enabledUsers  = new HashMap<Player, ArrayList<Block>>();

    public void debug(String format, Object... args) {
        log.info(String.format("[" + pluginId + "] " + format, args));
    }

    public boolean isUserActive(Player player) {
        return this.enabledUsers.containsKey(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("magicstix")) {
            toggleActivePlayer((Player) sender);
            return true;
        }

        return false;
    }

    @Override
    public void onDisable() {
        debug("Plugin Disabled");
    }

    public void onEnable() {
        PluginDescriptionFile yml = this.getDescription();
        pluginId = yml.getName() + " version " + yml.getVersion();
        debug("Plugin Enabled");

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener, Event.Priority.Normal, this);
    }

    public void toggleActivePlayer(Player player) {
        if (isUserActive(player)) {
            this.enabledUsers.remove(player);
            player.sendMessage("MagicStix Disabled");
        } else {
            enabledUsers.put(player, null);
            player.sendMessage("MagicStix Enabled");
        }
    }
}
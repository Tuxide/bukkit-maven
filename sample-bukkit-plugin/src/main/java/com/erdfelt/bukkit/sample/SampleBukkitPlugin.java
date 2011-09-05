package com.erdfelt.bukkit.sample;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SampleBukkitPlugin extends JavaPlugin {
    private static final Logger    log           = Logger.getLogger(SampleBukkitPlugin.class.getName());

    private String                 pluginId      = SampleBukkitPlugin.class.getSimpleName();
    private MagicStixBlockListener blockListener = new MagicStixBlockListener(this);
    private MagicStixUserListener  userListener  = new MagicStixUserListener(this);
    private Set<Player>            enabledUsers  = new HashSet<Player>();

    public void debug(String format, Object... args) {
        log.info(String.format("[" + pluginId + "] " + format, args));
    }

    public boolean isUserActive(Player player) {
        return this.enabledUsers.contains(player);
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
        pm.registerEvent(Event.Type.BLOCK_DAMAGE, this.blockListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, this.userListener, Event.Priority.Normal, this);
    }

    public void removeUser(Player player) {
        enabledUsers.remove(player);
    }

    public void toggleActivePlayer(Player player) {
        if (isUserActive(player)) {
            this.enabledUsers.remove(player);
            player.sendMessage("MagicStix Disabled");
        } else {
            enabledUsers.add(player);
            player.sendMessage("MagicStix Enabled");
        }
    }
}
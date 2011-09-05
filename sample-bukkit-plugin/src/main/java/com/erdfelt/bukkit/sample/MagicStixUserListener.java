package com.erdfelt.bukkit.sample;

import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class MagicStixUserListener extends PlayerListener {
    private SampleBukkitPlugin plugin;

    public MagicStixUserListener(SampleBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerKick(PlayerKickEvent event) {
        plugin.removeUser(event.getPlayer());
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.removeUser(event.getPlayer());
    }
}

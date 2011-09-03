package com.erdfelt.bukkit.sample;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;

public class MagicStixBlockListener extends BlockListener {
    private SampleBasicPlugin plugin;

    public MagicStixBlockListener(SampleBasicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.isUserActive(event.getPlayer())) {
            return;
        }
        plugin.debug("onBlockBreak() - %s", event.getBlock().getType());
    }

    @Override
    public void onBlockDamage(BlockDamageEvent event) {
        if (!plugin.isUserActive(event.getPlayer())) {
            return;
        }
        plugin.debug("onBlockDamage() - %s", event.getBlock().getType());
    }
}

package com.erdfelt.bukkit.sample;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;

public class MagicStixBlockListener extends BlockListener {
    private SampleBukkitPlugin plugin;
    private Random             rand;
    private Material           randomBlocks[];

    public MagicStixBlockListener(SampleBukkitPlugin plugin) {
        this.plugin = plugin;
        this.randomBlocks = new Material[] { Material.DIRT, Material.AIR, Material.BOOKSHELF, Material.STONE,
                Material.WOOD, Material.WOOL, Material.COAL_ORE, Material.SAND, Material.IRON_ORE, Material.LAPIS_ORE,
                Material.REDSTONE_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE };
        this.rand = new Random();
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        plugin.debug("onBlockBreak() - %s", event.getBlock().getType());
        if (!plugin.isUserActive(event.getPlayer())) {
            return;
        }
    }

    @Override
    public void onBlockDamage(BlockDamageEvent event) {
        if (!plugin.isUserActive(event.getPlayer())) {
            plugin.debug("onBlockDamage() - %s (with %s)", event.getBlock().getType(), event.getItemInHand().getType());
            if (event.getItemInHand().getType() == Material.STICK) {
                event.getBlock().setType(randomMaterial());
            }
            return;
        }
    }

    private Material randomMaterial() {
        return randomBlocks[rand.nextInt(randomBlocks.length)];
    }
}

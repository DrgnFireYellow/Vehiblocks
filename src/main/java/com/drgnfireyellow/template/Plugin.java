package com.drgnfireyellow.template;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {
    @Override
    public void onEnable() {
        Bukkit.getLogger().info("Loaded!");
    }
}

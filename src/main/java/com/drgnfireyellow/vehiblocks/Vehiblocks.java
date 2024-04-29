package com.drgnfireyellow.vehiblocks;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

import io.papermc.paper.event.entity.EntityMoveEvent;

public class Vehiblocks extends JavaPlugin implements Listener {
    HashMap<Player,ArrayList<Location>> newVehiclePositions = new HashMap<Player,ArrayList<Location>>();
    ArrayList<Player> vehicleCreators = new ArrayList<Player>();
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getLogger().info("[Vehiblocks] Loaded!");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (command.getName().equals("createvehicle")) {
                vehicleCreators.add((Player) sender);
                newVehiclePositions.put((Player) sender, new ArrayList<Location>());
                sender.sendMessage("Please left click the first position on the vehicle.");
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getScoreboardTags().contains("vehiblocks_controller")) {
            event.getRightClicked().addPassenger(event.getPlayer());
        }
    }

    // @EventHandler
    // public void onEntityMove(EntityMoveEvent event) {
    //     if (event.getEntity().getScoreboardTags().contains("vehiblocks_controller")) {
    //         for (String tag : event.getEntity().getScoreboardTags()) {
    //             if (tag.startsWith("vehiblocks.height.")) {
    //                 int height = Integer.parseInt(tag.split("\\.")[3]) - 1;
    //                 if (event.getEntity().isOnGround()) {
    //                     event.getEntity().getLocation().setY(event.getEntity().getY() + height);
    //                 }
    //             }
    //         }
    //     }
    // }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (vehicleCreators.contains(event.getPlayer())) {
            event.setCancelled(true);
            ArrayList<Location> vehiclePositions = newVehiclePositions.get(event.getPlayer());
            vehiclePositions.add(event.getBlock().getLocation());
            if (vehiclePositions.size() == 1) {
                event.getPlayer().sendMessage("First position selected, please select the second position.");
            }
            else if (vehiclePositions.size() == 2) {
                event.getPlayer().sendMessage("Second position selected, please select the seat position.");
            }
            else if (vehiclePositions.size() == 3) {
                event.getPlayer().sendMessage("All positions selected. Creating vehicle...");

                Vector max = Vector.getMaximum(vehiclePositions.get(0).toVector(), vehiclePositions.get(1).toVector());
                Vector min = Vector.getMinimum(vehiclePositions.get(0).toVector(), vehiclePositions.get(1).toVector());

                int startX = min.getBlockX();
                int startY = min.getBlockY();
                int startZ = min.getBlockZ();
                int endX = max.getBlockX();
                int endY = max.getBlockY();
                int endZ = max.getBlockZ();

                int seatX = (int) vehiclePositions.get(2).getX();
                int seatY = (int) vehiclePositions.get(2).getY();
                int seatZ = (int) vehiclePositions.get(2).getZ();

                World world = event.getPlayer().getWorld();
                BlockDisplay base = (BlockDisplay) world.spawnEntity(vehiclePositions.get(2), EntityType.BLOCK_DISPLAY);
                Pig controller = (Pig) world.spawnEntity(vehiclePositions.get(2).add(-0.5, -0.5, -0.5), EntityType.PIG);
                controller.setInvisible(true);
                controller.setPersistent(true);
                controller.setSilent(true);
                controller.setAI(false);
                controller.addPassenger(base);
                controller.addScoreboardTag("vehiblocks_controller");
                controller.addScoreboardTag("vehiblocks.height." + Integer.toString(startY < endY ? startY - seatY : endY - seatY));
                controller.setSaddle(true);
                controller.setInvulnerable(true);
                controller.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false));

                for (int x = startX; x <= endX; x++) {
                    for (int y = startY; y <= endY; y++) {
                        for (int z = startZ; z <= endZ; z++) {
                            BlockDisplay newBlock = (BlockDisplay) world.spawnEntity(new Location(world, x, y, z), EntityType.BLOCK_DISPLAY);
                            newBlock.setBlock(world.getBlockData(new Location(world, x, y, z)));
                            Transformation transform = newBlock.getTransformation();
                            transform.getTranslation().set(new Vector3f((float) (x - seatX), (float) (y - seatY + (startY < endY ? startY - seatY : endY - seatY)), (float) (z - seatZ)));
                            newBlock.setTransformation(transform);
                            base.addPassenger(newBlock);
                            event.getPlayer().getWorld().setType((int) x, (int) y, (int) z, Material.AIR);
                        }
                    }
                }
                vehicleCreators.remove(event.getPlayer());
                newVehiclePositions.remove(event.getPlayer());
            }
        }
    }
}

package me.aleksilassila.islands.listeners;

import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.utils.ChatUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class IslandsListener extends ChatUtils implements Listener {
    private Main plugin;

    public IslandsListener(Main plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        boolean canFlow = plugin.islands.grid.isBlockInIslandSphere(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());

        if(!canFlow) {
            event.setCancelled(true);
        }
    }

    @EventHandler // Player teleportation in void, damage restrictions
    public void onDamageEvent(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();

            if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID) && player.getWorld().equals(plugin.islandsWorld)) {
                World targetWorld = plugin.wildernessWorld;

                Location location = player.getLocation();
                location.setWorld(targetWorld);

                location.setX(location.getBlockX() * 4);
                location.setZ(location.getBlockZ() * 4);
                location.setY(targetWorld.getHighestBlockYAt(location) + 40);

                player.teleport(location);

                player.sendTitle("", ChatColor.GOLD + "Type /home to get back to your island.", (int)(20 * 0.5), 20 * 5, (int)(20 * 0.5));

                plugin.islands.playersWithNoFall.add(player);

                e.setCancelled(true);
            } else if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL) && plugin.islands.playersWithNoFall.contains(player)) {
                plugin.islands.playersWithNoFall.remove(player);
                e.setCancelled(true);
            } else if (player.getWorld().equals(plugin.islandsWorld)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent e) {
         if (e.getEntity().getWorld().equals(plugin.islandsWorld)) {
             int x = e.getEntity().getLocation().getBlockX();
             int z = e.getEntity().getLocation().getBlockZ();

             String ownerUUID = plugin.islands.grid.getBlockOwnerUUID(x, z);
                 if (ownerUUID != null && e.getDamager() instanceof Player && !ownerUUID.equals(e.getDamager().getUniqueId().toString())) {
                     if (plugin.islands.grid.getTrusted(plugin.islands.grid.getIslandId(x, z)).contains(e.getDamager().getUniqueId().toString())) {
                         return;
                     }

                     e.setCancelled(true);

                 e.getDamager().sendMessage(error("You cannot interact here."));
            }
        }
    }

    @EventHandler // Player interact restriction
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        if (e.getPlayer().getWorld().equals(plugin.islandsWorld)) {
            int x = e.getClickedBlock().getX();
            int z = e.getClickedBlock().getZ();

            String ownerUUID = plugin.islands.grid.getBlockOwnerUUID(x, z);

            if (ownerUUID == null || !ownerUUID.equals(e.getPlayer().getUniqueId().toString())) {
                if (plugin.islands.grid.getTrusted(plugin.islands.grid.getIslandId(x, z)).contains(e.getPlayer().getUniqueId().toString())) {
                    return;
                }

                e.setCancelled(true);

                e.getPlayer().sendMessage(error("You cannot interact here."));
            }
        }
    }

    // Above only checks if  the block clicked is in build radius, allowing block placement in restricted areas.
    @EventHandler
    private void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        if (e.getBlock().getWorld().equals(plugin.islandsWorld)) {
            int x = e.getBlock().getX();
            int z = e.getBlock().getZ();

            String ownerUUID = plugin.islands.grid.getBlockOwnerUUID(x, z);

            if (ownerUUID == null || !ownerUUID.equals(e.getPlayer().getUniqueId().toString())) {
                if (plugin.islands.grid.getTrusted(plugin.islands.grid.getIslandId(x, z)).contains(e.getPlayer().getUniqueId().toString())) {
                    return;
                }

                e.setCancelled(true);

                e.getPlayer().sendMessage(error("You cannot interact here."));
            }
        }
    }
}

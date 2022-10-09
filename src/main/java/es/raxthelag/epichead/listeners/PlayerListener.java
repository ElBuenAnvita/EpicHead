package es.raxthelag.epichead.listeners;

import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.controllers.DataConnection;
import es.raxthelag.epichead.controllers.EpicPlayer;
import es.raxthelag.epichead.objects.Task;
import es.raxthelag.epichead.objects.tasks.*;
import es.raxthelag.epichead.util.MessageUtil;
import es.raxthelag.epichead.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.sql.SQLException;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                Main.getInstance().getDataConnection().loadPlayer(EpicPlayer.get(e.getName()));
                Main.getInstance().getDataConnection().loadPlayerHomes(EpicPlayer.get(e.getName()));
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onPlayerEnter(PlayerLoginEvent e) {
        EpicPlayer epicPlayer = EpicPlayer.get(e.getPlayer());
        epicPlayer.setPlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogout(PlayerQuitEvent e) {
        EpicPlayer epicPlayer = EpicPlayer.get(e.getPlayer());
        epicPlayer.setLastSeenLocation(e.getPlayer().getLocation());

        Bukkit.getServer().getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                Main.getInstance().getDataConnection().savePlayer(epicPlayer);
                Main.getInstance().getDataConnection().savePlayerHomes(epicPlayer);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Location movedFrom = e.getFrom();
        Location movedTo = e.getTo();

        // If, by any reason, movedTo is null, we won't count it as a real movement.
        if (movedTo == null) return;

        if (!(movedFrom.getBlockX() != movedTo.getBlockX()
                || movedFrom.getBlockY() != movedTo.getBlockY()
                || movedFrom.getBlockZ() != movedTo.getBlockZ()
        )) return;

        // if (!(e.getFrom().getZ() != e.getTo().getZ() && e.getFrom().getX() != e.getTo().getX())) return;
        /* if (e.getFrom().getBlockZ() != e.getTo().getBlockZ() &&
                e.getFrom().getBlockX() != e.getTo().getBlockX()) return; */

        Player p = e.getPlayer();
        if (!Util.isPlayerTasked(p)) return;

        Task entry = Util.getTask(p);
        if (entry == null) return;

        // MessageUtil.debug(entry.toString(), false);
        // MessageUtil.debug(entry.getRecipient().toString() + "|" + entry.getSender().toString() + "|" + entry.getTaskId(), false);

        Bukkit.getScheduler().cancelTask(entry.getTaskId());

        if (entry instanceof TpaTask) MessageUtil.sendMessage(p, "general.tpa.canceled-by-movement", "Cancelado por movimiento");
        if (entry instanceof SpawnTask) MessageUtil.sendMessage(p, "general.spawn.canceled-by-movement", "Cancelado por movimiento");
        if (entry instanceof WarpTask) MessageUtil.sendMessage(p, "general.warp.canceled-by-movement", "Cancelado por movimiento");
        if (entry instanceof HomeTask) MessageUtil.sendMessage(p, "general.home.canceled-by-movement", "Cancelado por movimiento");
        if (entry instanceof BackTask) MessageUtil.sendMessage(p, "general.back.canceled-by-movement", "Cancelado por movimiento");

        Main.pendingTasks.remove(p.getName());
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!Util.isPlayerTasked(p)) return;

        // Bukkit.getScheduler().cancelTask(Main.pendingTasks.get(p.getName()).entrySet().iterator().next().getValue());
        Bukkit.getScheduler().cancelTask(Main.pendingTasks.get(p.getName()).getTaskId());
        Main.pendingTasks.remove(p.getName());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (!Main.getInstance().getConfig().getBoolean("spawn.teleport-on-respawn", false)) return;

        Location spawn = Main.getInstance().getLocations().getLocation("spawn");
        if (spawn == null) { MessageUtil.sendMessage(e.getPlayer(), "general.spawn.tp-failed", "<spawn_prefix> <red>Ocurrió un error al llevarte al spawn"); return; }
        e.setRespawnLocation(spawn);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        EpicPlayer epicPlayer = EpicPlayer.get(e.getEntity());
        epicPlayer.setDeathLocation(e.getEntity().getLocation());
    }
}

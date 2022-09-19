package es.raxthelag.epichead.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.controllers.EpicPlayer;
import es.raxthelag.epichead.objects.tasks.BackTask;
import es.raxthelag.epichead.util.MessageUtil;
import es.raxthelag.epichead.util.Util;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@CommandAlias("back")
public class BackCommand extends BaseCommand {
    @Default
    @CatchUnknown
    @CommandPermission("epiclol.back")
    public void onBack(Player player) {
        if (Main.getIfPlayerHasPendingTasks(player)) {
            MessageUtil.sendMessage(player, "error.back-pending-task", "<red>Ya tienes una tarea pendiente. Espera a acabarla para ejecutar otro comando.");
            return;
        }

        EpicPlayer epicPlayer = EpicPlayer.get(player);

        if (epicPlayer.getDeathLocation() == null) {
            MessageUtil.sendMessage(
                    player,
                    "general.back.no-location-saved",
                    "No pudimos ubicar su lecho de muerte"
            );
            return;
        }

        int delay = Main.getInstance().getConfig().getInt("times.back.teleport-delay", 0);

        if (delay == 0) {
            MessageUtil.sendMessage(player, "general.back.tp-success", "<back_prefix> <green>Has sido teleportado hacia tu último lecho de muerte.");
            player.teleport(epicPlayer.getDeathLocation());
            return;
        }

        MessageUtil.sendMessage(
                player,
                "general.back.delay-start",
                "No te muevas, serás teletransportado en <time> segundos",
                Placeholder.unparsed("time", delay + "")
        );

        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (player.isOnline()) {
                MessageUtil.sendMessage(player, "general.back.tp-success", "<back_prefix> <green>Has sido teleportado hacia tu último lecho de muerte.");
                player.teleport(epicPlayer.getDeathLocation());

                Util.removeTask(player);
            }
        }, delay * 20L);

        Main.pendingTasks.put(player.getName(), new BackTask(player, epicPlayer.getDeathLocation(), bukkitTask.getTaskId()));
    }
}

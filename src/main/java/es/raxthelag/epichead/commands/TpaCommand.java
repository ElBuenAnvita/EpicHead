package es.raxthelag.epichead.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.objects.tasks.TpaTask;
import es.raxthelag.epichead.objects.TpaType;
import es.raxthelag.epichead.util.MessageUtil;
import es.raxthelag.epichead.util.Util;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

@CommandAlias("tpa")
public class TpaCommand extends BaseCommand {

    @Default
    @Subcommand("send")
    @CommandCompletion("@players")
    @CatchUnknown
    public void onSend(Player player, String[] args) {
        // Si no especificó un usuario al cual enviar.
        if (args.length == 0) {
            MessageUtil.sendMessage(player, "general.tpa.tpa-no-player-specified","Debes indicar un usuario");
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);

        // Si el target no existe, o no está conectado, no enviar tpa.
        if (target == null || !target.isOnline()) {
            MessageUtil.sendMessage(player, "general.tpa.tpa-player-not-found","Usuario no encontrado", Placeholder.unparsed("target", args[0]));
            return;
        }

        // Si el jugador se hace el chistoso enviándose un tpa a sí mismo, se cancela
        if (target.getName().equals(player.getName())) {
            MessageUtil.sendMessage(player, "general.tpa.tpa-self","Eres tú mismo");
            return;
        }

        MessageUtil.sendMessage(target, "general.tpa.tpa-received", "Has recibido un tpa de <player>", Placeholder.unparsed("player", player.getName()));

        if (Main.pendingTpas == null) return;
        Main.pendingTpas.put(target.getName(), Map.of(player.getName(), TpaType.NORMAL));
    }

    @CommandAlias("tpaccept|tpaaccept")
    @Subcommand("accept")
    public void onAccept(Player player) {
        int delay = Main.getInstance().getConfig().getInt("times.tpa.teleport-delay", 0);
        Map<String, TpaType> tpaAccepted = Main.pendingTpas.getIfPresent(player.getName());

        // No pending TPAs.
        if (tpaAccepted == null) {
            MessageUtil.sendMessage(player, "general.tpa.no-tpa-to-accept","No tienes tpas pendientes");
            return;
        }

        Map.Entry<String, TpaType> entry = tpaAccepted.entrySet().iterator().next();
        Player caller = Bukkit.getPlayer(entry.getKey());

        Main.pendingTpas.invalidate(player.getName());

        if (caller == null || !caller.isOnline()) {
            MessageUtil.sendMessage(player, "general.tpa.player-got-offline","<player> se ha desconectado", Placeholder.unparsed("player", entry.getKey()));
            return;
        }

        if (entry.getValue() == TpaType.NORMAL) {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                if (!caller.isOnline()) { MessageUtil.sendMessage(player, "general.tpa.player-got-offline","<player> se ha desconectado", Placeholder.unparsed("player", caller.getName())); return; }
                if (!player.isOnline()) { MessageUtil.sendMessage(caller, "general.tpa.player-got-offline","<player> se ha desconectado", Placeholder.unparsed("player", player.getName())); return; }

                // CALLER ===> PLAYER
                caller.teleport(player);
                MessageUtil.sendMessage(player, "general.tpa.tp-success-target", "Se han teleportado hacia ti", Placeholder.unparsed("player", caller.getName()));
                MessageUtil.sendMessage(caller, "general.tpa.tp-success", "Te has teleportado hacia <player>", Placeholder.unparsed("player", player.getName()));

                Util.removeTask(caller);
            }, delay * 20L);

            Main.pendingTasks.put(caller.getName(), new TpaTask(caller, player, TpaType.NORMAL, bukkitTask.getTaskId()));
            MessageUtil.sendMessage(caller, "general.tpa.tpa-accepted-caller", "No te muevas. Serás teleportado en <time>s hacia <player>", TagResolver.resolver(Placeholder.unparsed("player", player.getName()), Placeholder.unparsed("time", ""+delay)));
        }
    }

    @CommandAlias("tpadeny|tpdeny")
    @Subcommand("deny|decline")
    public void onDeny(Player player) {
        /* Map<String, TpaType> tpaAccepted = Main.pendingTpas.getIfPresent(player.getName());

        // No pending TPAs.
        if (tpaAccepted == null) {
            MessageUtil.sendMessage(player, "general.tpa.no-tpa-to-accept","No tienes tpas pendientes");
            return;
        } */

        if (!(Main.pendingTasks.containsKey(player.getName()))) {
            MessageUtil.sendMessage(player, "general.tpa.no-tpa-to-accept","No tienes tpas pendientes");
            return;
        }

        // TODO MESSAGE

        Main.pendingTpas.invalidate(player.getName());
    }
}

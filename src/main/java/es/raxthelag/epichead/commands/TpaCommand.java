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
            MessageUtil.sendMessage(player, "general.tpa.tpa-no-player-specified", "Debes indicar un usuario");
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);

        // Si el target no existe, o no está conectado, no enviar tpa.
        if (target == null || !target.isOnline()) {
            MessageUtil.sendMessage(player, "general.tpa.tpa-player-not-found", "Usuario no encontrado", Placeholder.unparsed("target", args[0]));
            return;
        }

        // Si el jugador se hace el chistoso enviándose un tpa a sí mismo, se cancela
        if (target.getName().equals(player.getName())) {
            MessageUtil.sendMessage(player, "general.tpa.tpa-self", "Eres tú mismo");
            return;
        }
        /* MessageUtil.sendMessage(player, "general.tpa.tpa-sent", "Has enviado un tpa a <target>", Placeholder.unparsed("target", target.getName()));
        MessageUtil.sendMessage(target, "general.tpa.tpa-received", "Has recibido un tpa de <player>", Placeholder.unparsed("player", player.getName()));

        if (Main.pendingTpas == null) return;
        Main.pendingTpas.put(target.getName(), Map.entry(player.getName(), TpaType.NORMAL)); */
        addTpaRequest(target, player, TpaType.NORMAL);
    }

    @CommandAlias("tpaccept|tpaaccept")
    @Subcommand("accept")
    public void onAccept(Player player) {
        int delay = Main.getInstance().getConfig().getInt("times.tpa.teleport-delay", 0);
        Map.Entry<String, TpaType> tpaAccepted = Main.pendingTpas.getIfPresent(player.getName());

        // No pending TPAs.
        if (tpaAccepted == null) {
            MessageUtil.sendMessage(player, "general.tpa.no-tpa-to-accept","No tienes tpas pendientes");
            return;
        }

        Player caller = Bukkit.getPlayer(tpaAccepted.getKey());

        Main.pendingTpas.invalidate(player.getName());

        if (caller == null || !caller.isOnline()) {
            MessageUtil.sendMessage(player, "general.tpa.player-got-offline","<player> se ha desconectado", Placeholder.unparsed("player", tpaAccepted.getKey()));
            return;
        }

        if (tpaAccepted.getValue() == TpaType.NORMAL) {
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
        Map.Entry<String, TpaType> tpaAccepted = Main.pendingTpas.getIfPresent(player.getName());

        // No pending TPAs.
        if (tpaAccepted == null) {
            MessageUtil.sendMessage(player, "general.tpa.no-tpa-to-deny","No tienes tpas pendientes");
            return;
        }

        /* if (!Main.pendingTpas.asMap().containsKey(player.getName())) {
            MessageUtil.sendMessage(player, "general.tpa.no-tpa-to-accept","No tienes tpas pendientes");
            return;
        } */

        Player receiver = Bukkit.getPlayer(tpaAccepted.getKey());
        if (receiver != null && Main.getInstance().getConfig().getBoolean("tpa.notify-when-req-denied", true)) {
            MessageUtil.sendMessage(receiver, "general.tpa.request-denied", "<player> ha denegado tu solicitud de tpa.", Placeholder.unparsed("player", player.getName()));
        }

        MessageUtil.sendMessage(player, "general.tpa.denied-success", "Has denegado la solicitud de <player>", Placeholder.unparsed("player", tpaAccepted.getKey()));

        Main.pendingTpas.invalidate(player.getName());
    }

    @CommandAlias("tpah|tpahere")
    public class TpaHereCommand extends BaseCommand {
        @Default
        @Subcommand("send")
        @CommandCompletion("@players")
        @CatchUnknown
        public void onSend(Player player, @Single String playerName) {
            Player target = Bukkit.getPlayer(playerName);

            // Si el target no existe, o no está conectado, no enviar tpa.
            if (target == null || !target.isOnline()) {
                MessageUtil.sendMessage(player, "general.tpa.tpa-player-not-found", "Usuario no encontrado", Placeholder.unparsed("target", playerName));
                return;
            }

            // Si el jugador se hace el chistoso enviándose un tpa a sí mismo, se cancela
            if (target.getName().equals(player.getName())) {
                MessageUtil.sendMessage(player, "general.tpa.tpa-self", "Eres tú mismo");
                return;
            }

            MessageUtil.sendMessage(player, "general.tpa.tpa-sent", "Has enviado un tpa a <target>", Placeholder.unparsed("target", target.getName()));
            MessageUtil.sendMessage(target, "general.tpa.tpa-received", "Has recibido un tpa de <player>", Placeholder.unparsed("player", player.getName()));

            if (Main.pendingTpas == null) return;
            // /tpahere Nuy        (siendo yo Anvita)
            //          target                player

            // Anvita.getLocation() => a donde Nuy irá

            //                       Nuy                        Anvita
            // Main.pendingTpas.put(target.getName(), Map.entry(player.getName(), TpaType.REVERSE));
            addTpaRequest(target, player, TpaType.REVERSE);
        }
    }

    /**
     * Send tpa request message to both parts and adds the request to the pendingTpa array. <br>
     * If <b>type</b> is <i>NORMAL</i>, <b>caller</b> wants to go to <b>target</b>'s location<br>
     * If <b>type</b> is <i>REVERSE</i>, <b>target</b> wants to go to <b>caller</b>'s location
     *
     * @param target The player set in command
     * @param caller The player who requested the tpa
     * @param type Whether is /tpa (NORMAL) or /tpahere (REVERSE)
     */
    public void addTpaRequest(Player target, Player caller, TpaType type) {
        if (Main.pendingTpas == null) return;
        Main.pendingTpas.put(target.getName(), Map.entry(caller.getName(), type));

        switch (type) {
            case NORMAL:
                MessageUtil.sendMessage(caller, "general.tpa.tpa-sent", "Has enviado un tpa a <target>", Placeholder.unparsed("target", target.getName()));
                MessageUtil.sendMessage(target, "general.tpa.tpa-received", "Has recibido un tpa de <player>", Placeholder.unparsed("player", caller.getName()));
                break;
            case REVERSE:
                MessageUtil.sendMessage(caller, "general.tpa.tpahere-sent", "Has enviado un tpahere a <target>", Placeholder.unparsed("target", target.getName()));
                MessageUtil.sendMessage(target, "general.tpa.tpahere-received", "Has recibido un tpahere de <player>", Placeholder.unparsed("player", caller.getName()));
                break;
        }
    }

    /**
     * Once accepted tpa request, this function will message to both parts and adds the task with the time specified in config<br>
     * If <b>type</b> is <i>NORMAL</i>, <b>caller</b> wants to go to <b>target</b>'s location<br>
     * If <b>type</b> is <i>REVERSE</i>, <b>target</b> wants to go to <b>caller</b>'s location
     *
     * @param target The player set in command
     * @param caller The player who requested the tpa
     * @param type Whether is /tpa (NORMAL) or /tpahere (REVERSE)
     */
    public void createTaskTpa(Player target, Player caller, TpaType type) {
        int delay = Main.getInstance().getConfig().getInt("times.tpa.teleport-delay", 0);
        BukkitTask bukkitTask;

        Main.pendingTpas.invalidate(target.getName());
        switch (type) {
            case NORMAL:
                // player es target y caller es player
                bukkitTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    if (!caller.isOnline()) { MessageUtil.sendMessage(target, "general.tpa.player-got-offline","<player> se ha desconectado", Placeholder.unparsed("player", caller.getName())); return; }
                    if (!target.isOnline()) { MessageUtil.sendMessage(caller, "general.tpa.player-got-offline","<player> se ha desconectado", Placeholder.unparsed("player", target.getName())); return; }

                    // CALLER ===> TARGET
                    caller.teleport(target);
                    MessageUtil.sendMessage(target, "general.tpa.tpa-success-target", "<player> se ha teleportado hacia ti", Placeholder.unparsed("player", caller.getName()));
                    MessageUtil.sendMessage(caller, "general.tpa.tpa-success-caller", "Te has teleportado hacia <player>", Placeholder.unparsed("player", target.getName()));

                    Util.removeTask(caller);
                }, delay * 20L);

                Main.pendingTasks.put(caller.getName(), new TpaTask(caller, target, TpaType.NORMAL, bukkitTask.getTaskId()));
                MessageUtil.sendMessage(caller, "general.tpa.tpa-accepted-caller", "No te muevas. Serás teleportado en <time>s hacia <player>", TagResolver.resolver(Placeholder.unparsed("player", target.getName()), Placeholder.unparsed("time", ""+delay)));
                MessageUtil.sendMessage(
                        target,
                        "general.tpa.tpa-accepted-target",
                        "<player> se teletransportará hacia ti en <time> segundos.",
                        TagResolver.resolver(
                                Placeholder.unparsed("player", caller.getName()),
                                Placeholder.unparsed("time", ""+delay)
                        )
                );
                break;
            case REVERSE:
                bukkitTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    if (!caller.isOnline()) { MessageUtil.sendMessage(target, "general.tpa.player-got-offline","<player> se ha desconectado", Placeholder.unparsed("player", caller.getName())); return; }
                    if (!target.isOnline()) { MessageUtil.sendMessage(caller, "general.tpa.player-got-offline","<player> se ha desconectado", Placeholder.unparsed("player", target.getName())); return; }

                    // TARGET ===> CALLER
                    target.teleport(caller);
                    MessageUtil.sendMessage(target, "general.tpa.tpahere-success-target", "Te has teleportado hacia <player>", Placeholder.unparsed("player", caller.getName()));
                    MessageUtil.sendMessage(caller, "general.tpa.tpahere-success-caller", "<player> se ha teleportado hacia ti", Placeholder.unparsed("player", target.getName()));

                    Util.removeTask(caller);
                }, delay * 20L);

                Main.pendingTasks.put(target.getName(), new TpaTask(caller, target, TpaType.REVERSE, bukkitTask.getTaskId()));
                MessageUtil.sendMessage(
                        caller,
                        "general.tpa.tpahere-accepted-caller",
                        "<player> se teletransportará hacia ti en <time> segundos.",
                        TagResolver.resolver(
                                Placeholder.unparsed("player", target.getName()),
                                Placeholder.unparsed("time", ""+delay)
                        )
                );

                MessageUtil.sendMessage(
                        target,
                        "general.tpa.tpahere-accepted-target",
                        "No te muevas. Serás teleportado en <time>s hacia <player>",
                        TagResolver.resolver(
                                Placeholder.unparsed("player", caller.getName()),
                                Placeholder.unparsed("time", ""+delay)
                        )
                );
                break;
        }
    }
}

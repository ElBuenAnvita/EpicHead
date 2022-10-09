package es.raxthelag.epichead.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.controllers.EpicPlayer;
import es.raxthelag.epichead.util.MessageUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.sql.SQLException;

public class EconCommands extends BaseCommand {

    @CommandAlias("balance|bal|oro|money")
    public class BalCommand extends BaseCommand {
        // @Default
        @CatchUnknown
        @CommandPermission("epiclol.balance.own")
        public void onOwnBalance(Player player) {
            BigDecimal plBalance = getOwnBalance(player);

            MessageUtil.sendMessage(
                    player,
                    "general.eco.current-balance",
                    "Tu balance es de <balance>",
                    TagResolver.resolver(
                            Placeholder.unparsed("balance_format", Main.getInstance().getEconomy().format(plBalance.doubleValue())),
                            Placeholder.unparsed("balance", plBalance.toPlainString())
                    )
            );
        }

        @Default
        // @CommandAlias("balance|bal|oro|money")
        @CommandPermission("epiclol.balance.other")
        @CommandCompletion("@players")
        public void onOtherBalance(Player p, @Optional @Single String player) {
            if (player == null) {
                BigDecimal plBalance = getOwnBalance(p);

                MessageUtil.sendMessage(
                        p,
                        "general.eco.current-balance",
                        "Tu balance es de <balance>",
                        TagResolver.resolver(
                                Placeholder.unparsed("balance_format", Main.getInstance().getEconomy().format(plBalance.doubleValue())),
                                Placeholder.unparsed("balance", plBalance.toPlainString())
                        )
                );
                return;
            }

            EpicPlayer epicPlayer = EpicPlayer.get(player);
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
                BigDecimal plBalance = null;

                if (!epicPlayer.isLoaded()) {
                    try {
                        Main.getInstance().getDataConnection().loadPlayer(epicPlayer);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        MessageUtil.sendMessage(
                                p,
                                "general.eco.error-bal",
                                "Ocurrió un error",
                                Placeholder.unparsed("player", player)
                        );
                        return;
                    }
                }
                plBalance = epicPlayer.getBalance();
                if (!epicPlayer.isOnline()) EpicPlayer.remove(epicPlayer.getName());

                MessageUtil.sendMessage(
                        p,
                        "general.eco.current-balance-other",
                        "El balance de <player> es de <balance>",
                        TagResolver.resolver(
                                Placeholder.unparsed("player", player),
                                Placeholder.unparsed("balance_format", Main.getInstance().getEconomy().format(plBalance.doubleValue())),
                                Placeholder.unparsed("balance", plBalance.toPlainString())
                        )
                );
            });
        }

        public BigDecimal getOwnBalance(Player player) {
            EpicPlayer epicPlayer = EpicPlayer.get(player);
            return epicPlayer.getBalance();
        }
    }

    @CommandAlias("pay")
    public class PayCommand extends BaseCommand {
        @Default
        @CommandPermission("epiclol.pay")
        @CommandCompletion("@players @nothing")
        public void onPay(Player p, String player, double amount) {
            if (Main.getInstance().getConfig().getBoolean("performance.pay-command-only-online", true)) {
                Player bukkitPl = Bukkit.getPlayer(player);
                if (bukkitPl == null) { // !bukkitPl.isOnline()
                    MessageUtil.sendMessage(p, "general.eco.pay-player-offline", "El jugador está desconectado.", Placeholder.unparsed("player", player));
                    return;
                }
            }
            BigDecimal bigDecimalAmount = BigDecimal.valueOf(amount);
            BigDecimal minTransaction = BigDecimal.valueOf(Main.getInstance().getConfig().getDouble("pay.minimum-transaction", -1)).max(BigDecimal.ZERO);

            if (bigDecimalAmount.compareTo(minTransaction) <= 0) {
                MessageUtil.sendMessage(
                        p,
                        "general.eco.pay-no-minimum-satisfied",
                        "Error: El monto mínimo es de <min_amount>",
                        TagResolver.resolver(
                                Placeholder.unparsed("min_amount", minTransaction.toPlainString()),
                                Placeholder.unparsed("min_amount_format", Main.getInstance().getEconomy().format(minTransaction.doubleValue()))
                        )
                );
                return;
            }

            EpicPlayer epicPlayer = EpicPlayer.get(p);

            if (bigDecimalAmount.compareTo(epicPlayer.getBalance()) > 0) {
                MessageUtil.sendMessage(
                        p,
                        "general.eco.pay-no-bal-enough",
                        "Error: No tienes suficiente dinero",
                        TagResolver.resolver(
                                Placeholder.unparsed("player_balance", epicPlayer.getBalance().toPlainString()),
                                Placeholder.unparsed("player_balance_format", Main.getInstance().getEconomy().format(epicPlayer.getBalance().doubleValue())),
                                Placeholder.unparsed("amount", bigDecimalAmount.toPlainString()),
                                Placeholder.unparsed("amount_format", Main.getInstance().getEconomy().format(bigDecimalAmount.doubleValue()))
                        )
                );
                return;
            }

            EpicPlayer epicReceiver = EpicPlayer.get(player);
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
                try {
                    if (!epicReceiver.isLoaded()) {
                        if (!Main.getInstance().getDataConnection().playerExistInDatabase(epicReceiver)) {
                            MessageUtil.sendMessage(p, "general.eco.error-player-offline-and-inexistent", "Jugador inexistente", Placeholder.unparsed("player", player));
                            return;
                        }
                        Main.getInstance().getDataConnection().loadPlayer(epicReceiver);
                    }

                    epicPlayer.withdraw(amount, false);
                    MessageUtil.sendMessage(
                            epicPlayer.getPlayer(),
                            "general.eco.pay-sent",
                            "Se ha enviado el pago de <amount> hacia <player>",
                            TagResolver.resolver(
                                    Placeholder.unparsed("amount", bigDecimalAmount.toPlainString()),
                                    Placeholder.unparsed("amount_format", Main.getInstance().getEconomy().format(bigDecimalAmount.doubleValue())),
                                    Placeholder.unparsed("player", epicReceiver.getPlayerName())
                            )
                    );

                    epicReceiver.deposit(amount, !epicReceiver.isOnline());
                    if (epicReceiver.isOnline()) {
                        MessageUtil.sendMessage(
                                epicReceiver.getPlayer(),
                                "general.eco.pay-receive",
                                "Has recibido un pago de <player> por un importe de <amount>",
                                TagResolver.resolver(
                                        Placeholder.unparsed("amount", bigDecimalAmount.toPlainString()),
                                        Placeholder.unparsed("amount_format", Main.getInstance().getEconomy().format(bigDecimalAmount.doubleValue())),
                                        Placeholder.unparsed("player", epicPlayer.getPlayerName())
                                )
                        );
                    }

                    if (!epicReceiver.isOnline()) EpicPlayer.remove(epicReceiver.getName());
                } catch (SQLException e) {
                    e.printStackTrace();
                    MessageUtil.sendMessage(p, "general.eco.error-generic", "Error. Revise la consola.");
                }
            });
        }
    }
}

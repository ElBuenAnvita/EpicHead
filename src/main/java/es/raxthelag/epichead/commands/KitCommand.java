package es.raxthelag.epichead.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.controllers.EpicPlayer;
import es.raxthelag.epichead.objects.Kit;
import es.raxthelag.epichead.util.MessageUtil;
import es.raxthelag.epichead.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import team.unnamed.gui.menu.item.ItemClickable;
import team.unnamed.gui.menu.type.MenuInventory;
import team.unnamed.gui.menu.type.MenuInventoryBuilder;

import java.math.BigDecimal;
import java.util.*;

@CommandAlias("kit")
public class KitCommand extends BaseCommand {
    @Default
    @CommandPermission("epiclol.kit")
    public void onKit(Player player) {
        MenuInventoryBuilder inventoryBuilder = MenuInventory.newBuilder(LegacyComponentSerializer.legacySection().serialize(MessageUtil.getComponent("other.kit.title-gui", "Kits")), 1);

        int i = 0;
        for (Kit kit : Main.getInstance().getKitHandler().getKits()) {
            List<String> description = (kit.getDescription() == null) ? null : new ArrayList<>(kit.getDescription());
            if (description != null) description.replaceAll(s -> LegacyComponentSerializer.legacySection().serialize(MessageUtil.getComponent(s)));

            // ItemStack itemStack = new ItemStack(kit.getGuiItemStack(), 1);
            ItemStack itemStack = kit.getGuiItemStack().clone();

            ItemMeta itemMeta = itemStack.getItemMeta();
            // if (itemMeta != null) itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', kit.getDisplayName()));
            if (itemMeta != null) {
                itemMeta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(MessageUtil.getComponent(kit.getDisplayName())));
                if (description != null) itemMeta.setLore(description);
                itemStack.setItemMeta(itemMeta);
            }

            ItemClickable itemClickable = ItemClickable
                    .builder()
                    .item(itemStack)
                    .action(e -> {
                        Main.getInstance().getKitHandler().chargeKit(kit, EpicPlayer.get(player));
                        player.closeInventory();
                        return true;
                    })
                    .build();

            inventoryBuilder.item(itemClickable, i++);
        }

        player.openInventory(inventoryBuilder.build());
    }

    @Subcommand("claim")
    @CommandPermission("epiclol.kit.claim")
    @CommandCompletion("@kits")
    public void onKitClaim(Player player, String kitName) {
        Kit kit = Main.getInstance().getKitHandler().getKit(kitName);
        if (kit == null) {
            MessageUtil.sendMessage(
                    player,
                    "general.kit.not-found-name",
                    "Error: El kit <kit> no existe.",
                    Placeholder.unparsed("kit", kitName)
            );
            return;
        }

        Main.getInstance().getKitHandler().chargeKit(kit, EpicPlayer.get(player));
    }

    @Subcommand("admin")
    @CommandAlias("adminkit")
    @CommandPermission("epiclol.admin.kit")
    public class AdminKit extends BaseCommand {
        @Subcommand("reload")
        @CommandPermission("epiclol.admin.kit.reload")
        public void onKitReload(CommandSender sender) {
            Main.getInstance().loadKitsInUTF();
            Main.getInstance().getKitHandler().reloadKits();
            MessageUtil.sendMessage(sender, "general.kit.admin.kits-reloaded", "Kits recargados");
        }

        @Subcommand("give")
        @CommandPermission("epiclol.admin.kit.give")
        @CommandCompletion("@kits @players")
        @Syntax("<kit> <player>")
        public void onKitGive(Player player, String kitName, Player target) {
            Kit kit = Main.getInstance().getKitHandler().getKit(kitName);
            if (kit == null) {
                MessageUtil.sendMessage(
                        player,
                        "general.kit.not-found-name",
                        "Error: El kit <kit> no existe.",
                        Placeholder.unparsed("kit", kitName)
                );
                return;
            }

            try {
                Main.getInstance().getKitHandler().giveKit(kit, EpicPlayer.get(target));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Subcommand("create")
        @CommandPermission("epiclol.admin.kit.create")
        public void createKit(CommandSender sender, String kitName) {
            if (Main.getInstance().getKitHandler().getKits().stream().anyMatch(kit -> kit.getName().equalsIgnoreCase(kitName))) {
                MessageUtil.sendMessage(
                        sender,
                        "general.kit.admin.kit-exists",
                        "Ya existe un kit llamado así.",
                        Placeholder.unparsed("kit", kitName)
                );
                return;
            }

            Kit kit;

            kit = new Kit(kitName);

            MessageUtil.sendMessage(
                    sender,
                    "general.kit.admin.create-success",
                    "Se ha creado el kit con éxito.",
                    Placeholder.unparsed("kit", kitName)
            );

            Main.getInstance().getKitHandler().addKit(kit);
            Main.getInstance().getKitHandler().saveKits();
        }

        @Subcommand("set displayname")
        @CommandCompletion("@kits @nothing")
        @CommandPermission("epiclol.admin.kit.set.displayname")
        public void kitSetDisplayName(CommandSender sender, @Single String kitName, String displayName) {
            Kit kit = Main.getInstance().getKitHandler().getKit(kitName);
            if (kit == null) {
                MessageUtil.sendMessage(
                        sender,
                        "general.kit.not-found-name",
                        "Error: El kit <kit> no existe.",
                        Placeholder.unparsed("kit", kitName)
                );
                return;
            }

            kit.setDisplayName(displayName);
            Main.getInstance().getKitHandler().saveKits();

            MessageUtil.sendMessage(
                    sender,
                    "general.kit.admin.set-displayname",
                    "Display name set to <kit_displayname>",
                    TagResolver.resolver(
                            Placeholder.unparsed("kit", kitName),
                            Placeholder.parsed("kit_displayname", displayName)
                    )
            );
        }

        @Subcommand("set delay")
        @CommandCompletion("@kits @nothing")
        @CommandPermission("epiclol.admin.kit.set.duration")
        @Syntax("<kit> <seconds>")
        public void kitSetDelay(CommandSender sender, @Single String kitName, double delay) {
            Kit kit = Main.getInstance().getKitHandler().getKit(kitName);
            if (kit == null) {
                MessageUtil.sendMessage(
                        sender,
                        "general.kit.not-found-name",
                        "Error: El kit <kit> no existe.",
                        Placeholder.unparsed("kit", kitName)
                );
                return;
            }

            if (delay < -1) {
                MessageUtil.sendMessage(
                        sender,
                        "general.kit.admin.delay-error-seconds",
                        "Delay must be -1 or major or equal than 0",
                        Placeholder.unparsed("kit", kitName)
                );
                return;
            }

            // Normalize -1
            delay = (delay < 0) ? -1 : delay;

            kit.setDelay(delay);
            Main.getInstance().getKitHandler().saveKits();

            Calendar delayTime = new GregorianCalendar();
            delayTime.add(Calendar.SECOND, (int) delay);
            delayTime.add(Calendar.MILLISECOND, (int) ((delay * 1000.0) % 1000.0));

            if (delay == -1) {
                MessageUtil.sendMessage(
                        sender,
                        "general.kit.admin.set-delay-once",
                        "Delay set to once",
                        Placeholder.unparsed("kit", kitName)
                );
            } else {
                MessageUtil.sendMessage(
                        sender,
                        "general.kit.admin.set-delay",
                        "Delay set to <time>",
                        TagResolver.resolver(
                                Placeholder.unparsed("kit", kitName),
                                Placeholder.unparsed("time", Util.getDurationSmall(delayTime.getTimeInMillis()))
                        )
                );
            }
        }

        @Subcommand("set guiitem")
        @CommandCompletion("@kits")
        @CommandPermission("epiclol.admin.kit.set.guiitem")
        public void kitSetGUIItem(Player player, @Single String kitName) {
            Kit kit = Main.getInstance().getKitHandler().getKit(kitName);
            if (kit == null) {
                MessageUtil.sendMessage(
                        player,
                        "general.kit.not-found-name",
                        "Error: El kit <kit> no existe.",
                        Placeholder.unparsed("kit", kitName)
                );
                return;
            }

            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack.getType() == Material.AIR) {
                MessageUtil.sendMessage(player, "error.must-be-item-in-main-hand", "Debes sostener un objeto al ejecutar este comando");
                return;
            }

            kit.setGuiItemStack(itemStack);
            Main.getInstance().getKitHandler().saveKits();

            MessageUtil.sendMessage(
                    player,
                    "general.kit.admin.set-guiitem-success",
                    "Se ha añadido el item en mano al kit.",
                    TagResolver.resolver(
                            Placeholder.unparsed("kit", kitName.toLowerCase()),
                            Placeholder.parsed("kit_display", kit.getDisplayName())
                    )
            );
        }

        @Subcommand("set price")
        @CommandCompletion("@kits")
        @CommandPermission("epiclol.admin.kit.set.price")
        public void setKitPrice(Player player, @Single String kitName, double price) {
            Kit kit = Main.getInstance().getKitHandler().getKit(kitName);
            if (kit == null) {
                MessageUtil.sendMessage(player, "general.kit.not-found-name", "Error: El kit <kit> no existe.", Placeholder.unparsed("kit", kitName));
                return;
            }
            if (price < 0) {
                MessageUtil.sendMessage(player, "general.kit.admin.price-less-zero", "Error: El precio no puede ser menor que cero.");
                return;
            }

            kit.setPrice(BigDecimal.valueOf(price));
            Main.getInstance().getKitHandler().saveKits();

            MessageUtil.sendMessage(
                    player,
                    "general.kit.admin.set-price-success",
                    "El precio del kit <kit> ha ajustado a <price>.",
                    TagResolver.resolver(
                            Placeholder.unparsed("kit", kitName.toLowerCase()),
                            Placeholder.parsed("kit_display", kit.getDisplayName()),
                            Placeholder.unparsed("price", Main.getInstance().getEconomy().format(price))
                    )
            );
        }

        @Subcommand("edit additem")
        @CommandCompletion("@kits")
        @CommandPermission("epiclol.admin.kit.edit.items")
        public void kitAddItem(Player player, @Single String kitName) {
            Kit kit = Main.getInstance().getKitHandler().getKit(kitName);
            if (kit == null) {
                MessageUtil.sendMessage(
                        player,
                        "general.kit.not-found-name",
                        "Error: El kit <kit> no existe.",
                        Placeholder.unparsed("kit", kitName)
                );
                return;
            }

            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack.getType() == Material.AIR) {
                MessageUtil.sendMessage(
                        player,
                        "general.kit.admin.edit-add-item-air",
                        "Debes sostener un objeto al ejecutar este comando",
                        TagResolver.resolver(
                                Placeholder.unparsed("kit", kitName.toLowerCase()),
                                Placeholder.parsed("kit_display", kit.getDisplayName())
                        )
                );
                return;
            }

            kit.addItem(itemStack);
            Main.getInstance().getKitHandler().saveKits();

            MessageUtil.sendMessage(
                    player,
                    "general.kit.admin.edit-add-item-success",
                    "Se ha añadido el item en mano al kit.",
                    TagResolver.resolver(
                            Placeholder.unparsed("kit", kitName.toLowerCase()),
                            Placeholder.parsed("kit_display", kit.getDisplayName())
                    )
            );
        }

        @Subcommand("edit listitem|listitems")
        @CommandCompletion("@kits")
        @CommandPermission("epiclol.admin.kit.edit.items")
        public void kitListItems(Player player, @Single String kitName) {
            Kit kit = Main.getInstance().getKitHandler().getKit(kitName);
            if (kit == null) {
                MessageUtil.sendMessage(player, "general.kit.not-found-name", "Error: El kit <kit> no existe.", Placeholder.unparsed("kit", kitName));
                return;
            }

            MessageUtil.sendMessage(player, "general.kit.admin.header-item-list", "---");
            int i = 0;
            for (ItemStack itemStack : kit.getItemList()) {
                MessageUtil.sendMessage(
                        player,
                        "general.kit.admin.template-item-list",
                        "<dark_gray>[<green><index></green>]</dark_gray> - <gray><yellow><item></yellow> * <item_qty></gray>",
                        TagResolver.resolver(
                                Placeholder.unparsed("index", i+""),
                                Placeholder.unparsed("item_name", itemStack.getType().toString()),
                                Placeholder.component("item", Component.translatable(itemStack.translationKey()).hoverEvent(itemStack)),
                                Placeholder.unparsed("item_qty", itemStack.getAmount()+""),
                                Placeholder.component("btn_del",
                                        MessageUtil.getComponent("other.kit.button-delete", "")
                                                .clickEvent(ClickEvent.suggestCommand("/kit admin edit removeitem " + kit.getName() + " " + i))
                                                .hoverEvent(HoverEvent.showText(MessageUtil.getComponent("other.kit.hover-click-to-delete", "Eliminar")))
                                ),
                                Placeholder.component("btn_give",
                                        MessageUtil.getComponent("other.kit.button-give", "")
                                                .clickEvent(ClickEvent.runCommand("/kit admin edit checkitem " + kit.getName() + " " + i))
                                                .hoverEvent(HoverEvent.showText(MessageUtil.getComponent("other.kit.hover-click-to-give", "Observar")))
                                )
                        )
                );
                i++;
            }
            MessageUtil.sendMessage(player, "general.kit.admin.footer-item-list", "---");
        }

        @Subcommand("edit removeitem")
        @CommandCompletion("@kits")
        @CommandPermission("epiclol.admin.kit.edit.items")
        public void kitListItems(Player player, String kitName, int index) {
            Kit kit = Main.getInstance().getKitHandler().getKit(kitName);
            if (kit == null) {
                MessageUtil.sendMessage(player, "general.kit.not-found-name", "Error: El kit <kit> no existe.", Placeholder.unparsed("kit", kitName));
                return;
            }

            try {
                kit.getItemList().remove(index);
                Main.getInstance().getKitHandler().saveKits();

                MessageUtil.sendMessage(
                        player,
                        "general.kit.admin.remove-item-success",
                        "Se ha eliminado el item del kit.",
                        TagResolver.resolver(
                                Placeholder.unparsed("index", index+""),
                                Placeholder.unparsed("kit", kitName.toLowerCase()),
                                Placeholder.parsed("kit_display", kit.getDisplayName())
                        )
                );
            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                MessageUtil.sendMessage(
                        player,
                        "general.kit.admin.index-out-of-bounds",
                        "Has especificado un indice fuera de los limites.",
                        TagResolver.resolver(
                                Placeholder.unparsed("kit", kitName.toLowerCase()),
                                Placeholder.parsed("kit_display", kit.getDisplayName())
                        )
                );
            }
        }

        @Subcommand("edit checkitem|testitem")
        @CommandPermission("epiclol.admin.kit.edit.items")
        @Private
        public void kitTestItem(Player player, String kitName, int index) {
            Kit kit = Main.getInstance().getKitHandler().getKit(kitName);
            if (kit == null) {
                MessageUtil.sendMessage(player, "general.kit.not-found-name", "Error: El kit <kit> no existe.", Placeholder.unparsed("kit", kitName));
                return;
            }

            try {
                HashMap<Integer, ItemStack> itemsLeft = player.getInventory().addItem(kit.getItemList().get(index));
                if (itemsLeft.isEmpty()) {
                    MessageUtil.sendMessage(
                            player,
                            "general.kit.admin.edit-give-item-success",
                            "Se ha entregado el item <index> del kit.",
                            TagResolver.resolver(
                                    Placeholder.unparsed("index", index+""),
                                    Placeholder.unparsed("kit", kitName.toLowerCase()),
                                    Placeholder.parsed("kit_display", kit.getDisplayName())
                            )
                    );
                } else {
                    MessageUtil.sendMessage(player, "general.kit.admin.error.full-inventory", "Tienes el inventario lleno");
                }
            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                MessageUtil.sendMessage(
                        player,
                        "general.kit.admin.index-out-of-bounds",
                        "Has especificado un indice fuera de los limites.",
                        TagResolver.resolver(
                                Placeholder.unparsed("kit", kitName.toLowerCase()),
                                Placeholder.parsed("kit_display", kit.getDisplayName())
                        )
                );
            }
        }

        @Subcommand("delete")
        @CommandPermission("epiclol.admin.kit.delete")
        public void kitWarnDelete(Player player, String kitName) {
            Kit kit = Main.getInstance().getKitHandler().getKit(kitName);
            if (kit == null) {
                MessageUtil.sendMessage(player, "general.kit.not-found-name", "Error: El kit <kit> no existe.", Placeholder.unparsed("kit", kitName));
                return;
            }

            MessageUtil.sendMessage(
                    player,
                    "general.kit.admin.delete-warning",
                    "Estás a punto de eliminar el kit <kit>, ¿estás seguro? <btn_delete>",
                    TagResolver.resolver(
                            Placeholder.unparsed("kit", kitName.toLowerCase()),
                            Placeholder.component("btn_delete", MessageUtil.getComponent("other.kit.button-delete-kit", "Eliminar definitivamente").clickEvent(ClickEvent.runCommand("/kit admin deletepermanently " + kitName)))
                    )
            );
        }

        @Subcommand("deletepermanently")
        @CommandPermission("epiclol.admin.kit.delete")
        @Private
        public void kitDeletePermanent(Player player, String kitName) {
            Kit kit = Main.getInstance().getKitHandler().getKit(kitName);
            if (kit == null) {
                MessageUtil.sendMessage(player, "general.kit.not-found-name", "Error: El kit <kit> no existe.", Placeholder.unparsed("kit", kitName));
                return;
            }

            Main.getInstance().getKitHandler().removeKit(kitName);
            Main.getInstance().getKitHandler().saveKits();

            MessageUtil.sendMessage(
                    player,
                    "general.kit.admin.delete-success",
                    "Se ha eliminado el kit <kit>.",
                    Placeholder.unparsed("kit", kitName.toLowerCase())
            );
        }
    }
}

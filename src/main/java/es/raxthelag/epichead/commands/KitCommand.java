package es.raxthelag.epichead.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.controllers.EpicPlayer;
import es.raxthelag.epichead.objects.Kit;
import es.raxthelag.epichead.util.MessageUtil;
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

import java.util.List;

@CommandAlias("kit")
public class KitCommand extends BaseCommand {
    @Default
    public void onKit(Player player) {
        MenuInventoryBuilder inventoryBuilder = MenuInventory.newBuilder("Kits", 1);

        int i = 0;
        for (Kit kit : Main.getInstance().getKitHandler().getKits()) {
            List<String> description = kit.getDescription();
            description.replaceAll(s -> LegacyComponentSerializer.legacySection().serialize(MessageUtil.getComponent(s)));

            ItemStack itemStack = new ItemStack(kit.getGuiItem(), 1);

            ItemMeta itemMeta = itemStack.getItemMeta();
            // if (itemMeta != null) itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', kit.getDisplayName()));
            if (itemMeta != null) {
                itemMeta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(MessageUtil.getComponent(kit.getDisplayName())));
                itemMeta.setLore(description);
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

    @Subcommand("give")
    @CommandPermission("epiclol.admin.kit.give")
    @CommandCompletion("@kits @players")
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

    @Subcommand("reload")
    @CommandPermission("epiclol.admin.kit.reload")
    public void onKitReload(CommandSender sender) {
        Main.getInstance().loadKitsInUTF();
        Main.getInstance().getKitHandler().reloadKits();
        MessageUtil.sendMessage(sender, "general.kit.admin.kits-reloaded", "Kits recargados");
    }

    @Subcommand("edit")
    @CommandAlias("editkit")
    @CommandPermission("epiclol.admin.kit.edit")
    public class EditKit extends BaseCommand {
        @Subcommand("add item")
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
                        "general.kit.admin.edit-add-air",
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
                    "general.kit.admin.edit-add-success",
                    "Se ha añadido el item en mano al kit.",
                    TagResolver.resolver(
                            Placeholder.unparsed("kit", kitName.toLowerCase()),
                            Placeholder.parsed("kit_display", kit.getDisplayName())
                    )
            );
        }
    }
}

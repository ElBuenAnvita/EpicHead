package es.raxthelag.epichead.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.controllers.EpicPlayer;
import es.raxthelag.epichead.objects.Kit;
import es.raxthelag.epichead.util.MessageUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import team.unnamed.gui.menu.item.ItemClickable;
import team.unnamed.gui.menu.type.MenuInventory;
import team.unnamed.gui.menu.type.MenuInventoryBuilder;

@CommandAlias("kit")
public class KitCommand extends BaseCommand {
    @Default
    public void onKit(Player player) {
        MenuInventoryBuilder inventoryBuilder = MenuInventory.newBuilder("Kits", 1);

        int i = 0;
        for (Kit kit : Main.getInstance().getKitHandler().getKits()) {
            ItemStack itemStack = new ItemStack(kit.getGuiItem(), 1);

            ItemMeta itemMeta = itemStack.getItemMeta();
            // if (itemMeta != null) itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', kit.getDisplayName()));
            if (itemMeta != null) itemMeta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(MessageUtil.getComponent(kit.getName())));
            itemStack.setItemMeta(itemMeta);

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
            // TODO MESSAGE - KIT NOT FOUND
            Main.debug("kit not found");
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
        Main.getInstance().getKitHandler().reloadKits();
        MessageUtil.sendMessage(sender, "general.kits.kits-reloaded", "Kits recargados");
    }

    @Subcommand("edit")
    @CommandAlias("editkit")
    @CommandPermission("epiclol.admin.kit.edit")
    public class EditKit extends BaseCommand {
        @Subcommand("add item")
        @CommandCompletion("@kits")
        @CommandPermission("epiclol.admin.kit.edit.items")
        public void kitAddItem(Player player, String kitName) {
            Kit kit = Main.getInstance().getKitHandler().getKit(kitName);
            if (kit == null) {
                // TODO MESSAGE KIT NOT FOUND
                Main.debug("kit not found");
                return;
            }

            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack.getType() == Material.AIR) {
                // TODO MESSAGE NOT AIR
                Main.debug("air in hand");
                return;
            }

            kit.addItem(itemStack);
            Main.getInstance().getKitHandler().saveKits();
        }
    }
}

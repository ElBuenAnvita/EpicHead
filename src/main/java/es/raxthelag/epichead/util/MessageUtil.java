package es.raxthelag.epichead.util;

import es.raxthelag.epichead.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class MessageUtil {
    public static MiniMessage miniMessage = MiniMessage
            .builder()
            // .editTags(t -> t.resolvers(Main.getInstance().getCustomTags()))
            .build();

    public static void setMiniMessage(MiniMessage newMiniMessage) {
        miniMessage = newMiniMessage;
    }

    public static Component getComponent(String path, String def, TagResolver... tagResolvers) {
        String message = Main.getInstance().getMessages().isList(path)
                ? String.join("<newline>", Main.getInstance().getMessages().getStringList(path))
                : Main.getInstance().getMessages().getString(path, def);

        return miniMessage.deserialize(message, tagResolvers);
    }

    public static Component getComponent(String message, TagResolver... tagResolvers) {
        return miniMessage.deserialize(message, tagResolvers);
    }

    public static Component getComponent(String path, String def) {
        String message = Main.getInstance().getMessages().isList(path)
                ? String.join("<newline>", Main.getInstance().getMessages().getStringList(path))
                : Main.getInstance().getMessages().getString(path, def);

        return miniMessage.deserialize(message);
    }

    /**
     * Sends a simple message to player
     * @param p Player
     * @param message Message string
     */
    public static void sendMessage(Player p, String message) {
        if (p == null) return;
        Component component = getComponent(message);
        BaseComponent[] baseComponents = BungeeComponentSerializer.get().serialize(component);
        BaseComponent baseComponent = new TextComponent("");
        Arrays.stream(baseComponents).forEach(baseComponent::addExtra);

        p.spigot().sendMessage(baseComponent);
    }

    /**
     * Sends a simple message to player
     * @param p Player
     * @param path Message path in messages.yml
     * @param def Default message in case we can't find the message in path.
     */
    public static void sendMessage(CommandSender sender, String path, String def) {
        if (sender == null) return;
        Component component = getComponent(path, def);
        BaseComponent[] baseComponents = BungeeComponentSerializer.get().serialize(component);
        BaseComponent baseComponent = new TextComponent("");
        Arrays.stream(baseComponents).forEach(baseComponent::addExtra);

        sender.spigot().sendMessage(baseComponent);
    }

    /**
     * Sends a simple message to a command sender
     * @param sender Command sender
     * @param path Message path in messages.yml
     * @param def Default message in case we can't find the message in path.
     * @param tagResolvers Placeholders changed in the final message
     */
    public static void sendMessage(CommandSender sender, String path, String def, TagResolver... tagResolvers) {
        if (sender == null) return;
        Component component = getComponent(path, def, tagResolvers);

        BaseComponent[] baseComponents = BungeeComponentSerializer.get().serialize(component);
        BaseComponent baseComponent = new TextComponent("");
        Arrays.stream(baseComponents).forEach(baseComponent::addExtra);

        sender.spigot().sendMessage(baseComponent);
    }

    /* public static void sendMessage(Player p, String path, String def, TagResolver tagResolver) {
        if (p == null) return;
        Component component = getComponent(path, def, tagResolver);
        BaseComponent[] baseComponents =BungeeComponentSerializer.get().serialize(component);
        BaseComponent baseComponent = new TextComponent("");
        Arrays.stream(baseComponents).forEach(baseComponent::addExtra);

        p.spigot().sendMessage(baseComponent);
    } */

    public static void debug(String s, boolean force) {
        if (Main.getInstance().getConfig().getBoolean("debug", false) || force) {
            Main.getInstance().getLogger().info("[DEBUG] " + s);
        }
    }
}

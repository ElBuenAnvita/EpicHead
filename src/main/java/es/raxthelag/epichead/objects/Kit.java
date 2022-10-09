package es.raxthelag.epichead.objects;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SerializableAs("Kit")
public class Kit implements ConfigurationSerializable {
    public String name;
    public String displayName;
    public List<String> description = null;
    public Material guiItem;
    public @Nullable String permission;
    public double delay;
    public BigDecimal price;

    public List<ItemStack> itemList = new ArrayList<>();
    public List<BigDecimal> moneyList = new ArrayList<>();
    public List<String> commandList = new ArrayList<>();

    public Kit(String name, String displayName, Material guiItem, double delay, BigDecimal price, @Nullable String permission) {
        this.name = name;
        this.displayName = displayName;
        this.guiItem = guiItem;
        this.delay = delay;
        this.price = price;

        this.permission = permission;
    }

    public Kit(Map<String, Object> deserialize) {
        this.name = deserialize.get("name").toString();
        this.displayName = deserialize.get("display-name").toString();
        this.guiItem = Material.matchMaterial(deserialize.get("gui-material").toString());
        this.delay = Double.parseDouble(deserialize.get("delay").toString());
        this.price = new BigDecimal(deserialize.get("price").toString());
        if (deserialize.get("item-list") != null) this.itemList = (List<ItemStack>) deserialize.get("item-list");
        if (deserialize.get("command-list") != null) this.commandList = (List<String>) deserialize.get("command-list");
        if (deserialize.get("description") != null) this.description = (List<String>) deserialize.get("description");
    }

    /**
     * Creates a Map representation of this class.
     * <p>
     * This class must provide a method to restore this class, as defined in
     * the {@link ConfigurationSerializable} interface javadocs.
     *
     * @return Map containing the current state of this class
     */
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> kit = new HashMap<>();

        kit.put("name", this.name);
        kit.put("description", this.description);
        kit.put("display-name", this.name);
        kit.put("gui-material", this.guiItem.toString());
        kit.put("delay", this.delay+"");
        kit.put("price", this.price.toPlainString());
        kit.put("item-list", this.itemList);
        kit.put("command-list", this.commandList);
        return kit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public double getDelay() {
        return delay;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Material getGuiItem() {
        return guiItem;
    }

    public void setGuiItem(Material guiItem) {
        this.guiItem = guiItem;
    }

    @Nullable
    public String getPermission() {
        return permission;
    }

    public void setPermission(@Nullable String permission) {
        this.permission = permission;
    }

    public List<ItemStack> getItemList() {
        return itemList;
    }

    public void setItemList(List<ItemStack> itemList) {
        this.itemList = itemList;
    }

    public void addItem(ItemStack itemStack) {
        this.itemList.add(itemStack);
    }

    public List<BigDecimal> getMoneyList() {
        return moneyList;
    }

    public void setMoneyList(List<BigDecimal> moneyList) {
        this.moneyList = moneyList;
    }

    public List<String> getCommandList() {
        return commandList;
    }

    public void setCommandList(List<String> commandList) {
        this.commandList = commandList;
    }
}

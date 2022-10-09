package es.raxthelag.epichead.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import team.unnamed.gui.menu.type.MenuInventoryBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class InventoryUtil {
    /* Spigot 1.9, for whatever reason, decided to merge the armor and main player inventories without providing a way
    to access the main inventory. There's lots of ugly code in here to work around that. */
    private static final int USABLE_PLAYER_INV_SIZE = 36;
    private static MenuInventoryBuilder kitInventory;

    private static int firstPartial(final Inventory inventory, final ItemStack item, final int maxAmount) {
        if (item == null) {
            return -1;
        }
        final ItemStack[] stacks = inventory.getContents();
        for (int i = 0; i < stacks.length; i++) {
            final ItemStack cItem = stacks[i];
            if (cItem != null && cItem.getAmount() < maxAmount && cItem.isSimilar(item)) {
                return i;
            }
        }
        return -1;
    }

    private static Inventory makeTruncatedPlayerInventory(final PlayerInventory playerInventory) {
        final Inventory fakeInventory = Bukkit.getServer().createInventory(null, USABLE_PLAYER_INV_SIZE);
        fakeInventory.setContents(Arrays.copyOf(playerInventory.getContents(), fakeInventory.getSize()));
        return fakeInventory;
    }

    // private static MenuInventoryBuilder

    private static boolean isCombinedInventory(final Inventory inventory) {
        return inventory instanceof PlayerInventory && inventory.getContents().length > USABLE_PLAYER_INV_SIZE;
    }

    /**
     * Adds oversized items to inventory if there's enough space for them.
     * @param inventory Inventory where items should be added
     * @param oversizedStacks Maximum stack allowed for this action
     * @param items List of ItemStacks to put in there
     * @return <code>null</code> if there is no overflow and items where added successfully, else a <code>Map&lt;Integer, ItemStack&gt;</code> of items that overflowed the inventory (with no final addition to inventory).
     */
    public static Map<Integer, ItemStack> addAllOversizedItems(final Inventory inventory, final int oversizedStacks, final ItemStack... items) {
        final ItemStack[] contents = inventory.getContents();

        final Inventory fakeInventory;
        if (isCombinedInventory(inventory)) {
            fakeInventory = makeTruncatedPlayerInventory((PlayerInventory) inventory);
        } else {
            fakeInventory = Bukkit.getServer().createInventory(null, inventory.getType());
            fakeInventory.setContents(contents);
        }
        final Map<Integer, ItemStack> overflow = addOversizedItems(fakeInventory, oversizedStacks, items);
        if (overflow.isEmpty()) {
            addOversizedItems(inventory, oversizedStacks, items);
            return null;
        }
        return overflow;
    }

    // Returns what it couldn't store
    // Set oversizedStack to below normal stack size to disable oversized stacks
    public static Map<Integer, ItemStack> addOversizedItems(final Inventory inventory, final int oversizedStacks, final ItemStack... items) {
        if (isCombinedInventory(inventory)) {
            final Inventory fakeInventory = makeTruncatedPlayerInventory((PlayerInventory) inventory);
            final Map<Integer, ItemStack> overflow = addOversizedItems(fakeInventory, oversizedStacks, items);
            for (int i = 0; i < fakeInventory.getContents().length; i++) {
                inventory.setItem(i, fakeInventory.getContents()[i]);
            }
            return overflow;
        }

        final Map<Integer, ItemStack> leftover = new HashMap<>();

        /*
         * TODO: some optimization - Create a 'firstPartial' with a 'fromIndex' - Record the lastPartial per Material -
         * Cache firstEmpty result
         */

        // combine items

        final ItemStack[] combined = new ItemStack[items.length];
        for (final ItemStack item : items) {
            if (item == null || item.getAmount() < 1) {
                continue;
            }
            for (int j = 0; j < combined.length; j++) {
                if (combined[j] == null) {
                    combined[j] = item.clone();
                    break;
                }
                if (combined[j].isSimilar(item)) {
                    combined[j].setAmount(combined[j].getAmount() + item.getAmount());
                    break;
                }
            }
        }

        for (int i = 0; i < combined.length; i++) {
            final ItemStack item = combined[i];
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            while (true) {
                // Do we already have a stack of it?
                final int maxAmount = Math.max(oversizedStacks, item.getType().getMaxStackSize());
                final int firstPartial = firstPartial(inventory, item, maxAmount);

                // Drat! no partial stack
                if (firstPartial == -1) {
                    // Find a free spot!
                    final int firstFree = inventory.firstEmpty();

                    if (firstFree == -1) {
                        // No space at all!
                        leftover.put(i, item);
                        break;
                    } else {
                        // More than a single stack!
                        if (item.getAmount() > maxAmount) {
                            final ItemStack stack = item.clone();
                            stack.setAmount(maxAmount);
                            inventory.setItem(firstFree, stack);
                            item.setAmount(item.getAmount() - maxAmount);
                        } else {
                            // Just store it
                            inventory.setItem(firstFree, item);
                            break;
                        }
                    }
                } else {
                    // So, apparently it might only partially fit, well lets do just that
                    final ItemStack partialItem = inventory.getItem(firstPartial);

                    final int amount = item.getAmount();
                    final int partialAmount = partialItem.getAmount();

                    // Check if it fully fits
                    if (amount + partialAmount <= maxAmount) {
                        partialItem.setAmount(amount + partialAmount);
                        break;
                    }

                    // It fits partially
                    partialItem.setAmount(maxAmount);
                    item.setAmount(amount + partialAmount - maxAmount);
                }
            }
        }
        return leftover;
    }

    /**
     * Add all items to inventory provided. This will abort if it couldn't store all items.
     * @param inventory Inventory where items should be added
     * @param items Array of ItemStacks to put in there
     * @return <code>Map&lt;Integer, ItemStack&gt;</code> of items that couldn't store, else null (success).
     */
    public static Map<Integer, ItemStack> addAllItems(final Inventory inventory, final ItemStack... items) {
        final ItemStack[] contents = inventory.getContents();

        final Inventory fakeInventory;
        if (isCombinedInventory(inventory)) {
            fakeInventory = makeTruncatedPlayerInventory((PlayerInventory) inventory);
        } else {
            fakeInventory = Bukkit.getServer().createInventory(null, inventory.getType());
            fakeInventory.setContents(contents);
        }
        final Map<Integer, ItemStack> overflow = addItems(fakeInventory, items);
        if (overflow.isEmpty()) {
            addItems(inventory, items);
            return null;
        }
        return addItems(fakeInventory, items);
    }

    // Returns what it couldn't store
    public static Map<Integer, ItemStack> addItems(final Inventory inventory, final ItemStack... items) {
        return addOversizedItems(inventory, 0, items);
    }
}

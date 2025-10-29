package de.ardania.urutar.ArdaCraftingTables;

import com.earth2me.essentials.Essentials;
import net.ess3.provider.ContainerProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static de.ardania.urutar.ArdaCraftingTables.Main.PLUGIN;

public class SpecialItemListener implements Listener {

    private final Map<UUID, OpenShulkerData> openShulkerBoxes = new HashMap<>();
    private static final NamespacedKey SHULKER_ID_KEY = new NamespacedKey(PLUGIN, "virtual_shulker_id");
    private ContainerProvider containerProvider;

    public SpecialItemListener(Essentials essentials) {
        if(essentials != null)
            this.containerProvider = essentials.provider(ContainerProvider.class);
    }

    // region EventHandlers

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();
        OpenShulkerData data = openShulkerBoxes.get(playerId);
        if (data == null)
            return;

        // The item that was clicked in the inventory
        ItemStack clicked = event.getCurrentItem();

        // The item currently held on the cursor
        ItemStack cursor = event.getCursor();

        // Prevent interacting with the opened shulker
        if (isSameItem(clicked, data.item) || isSameItem(cursor, data.item)) {
            event.setCancelled(true);
            return;
        }

        // The action that was executed
        InventoryAction inventoryAction = event.getAction();

        // Prevent moving any shulker to the opened inventory
        if (isShulkerBox(clicked) && inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            return;
        }

        // The inventory where the event occurred
        Inventory clickedInventory = event.getClickedInventory();

        // Prevent putting any shulker into the opened inventory
        if (isShulkerBox(cursor)
                && (inventoryAction == InventoryAction.PLACE_ALL || inventoryAction == InventoryAction.PLACE_ONE)
                && Objects.equals(clickedInventory, data.inventory)) {
            event.setCancelled(true);
            return;
        }

        int hotbarButton = event.getHotbarButton();

        // Prevent moving the open shulker from offhand or the hotbar
        // Additionally, prevent swapping a shulker
        if (inventoryAction == InventoryAction.HOTBAR_SWAP) {

            // Check if the item from the hotbar is allowed to be moved
            if (hotbarButton >= 0 && hotbarButton <= 9) {
                ItemStack hotbarItem = player.getInventory().getItem(hotbarButton);
                // Check if the item from the hotbar is the opened shulker
                if (isSameItem(hotbarItem, data.item)) {
                    event.setCancelled(true);
                    return;
                }
                // Check if the item from the hotbar is a shulker and the target is the opened inventory
                if (isShulkerBox(hotbarItem) && Objects.equals(clickedInventory, data.inventory)) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                // The item currently held in the offhand
                ItemStack offhandItem = player.getInventory().getItemInOffHand();

                // Check if the item from the offhand is the opened shulker
                if (isSameItem(offhandItem, data.item)) {
                    event.setCancelled(true);
                    return;
                }
                // Check if the item from the offhand is a shulker and the target is the opened inventory
                if (isShulkerBox(offhandItem) && Objects.equals(clickedInventory, data.inventory)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Save the metadata of the opened shulker with a delay
        Bukkit.getScheduler().runTaskLater(PLUGIN, () -> UpdateMetadata(data), 1L);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        OpenShulkerData data = openShulkerBoxes.remove(player.getUniqueId());
        if (data == null)
            return;

        UpdateMetadata(data);
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null)
            return;

        Material material = item.getType();
        if (material.equals(Material.AIR))
            return;

        ItemMeta meta = item.getItemMeta();
        if(meta == null)
            return;

        String displayName = meta.getDisplayName();
        if (displayName.isEmpty())
            return;

        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        if (!enchantments.containsKey(Enchantment.THORNS)) {
            return;
        }

        Player player = event.getPlayer();

        if (material.equals(Material.CRAFTING_TABLE) &&
                displayName.endsWith("Taschenwerkbank")) {
            player.openWorkbench(player.getLocation(), true);
            event.setCancelled(true);
            return;
        } else if (material.equals(Material.ENCHANTING_TABLE) &&
                displayName.endsWith("Taschenzaubertisch")) {
            player.openEnchanting(player.getLocation(), true);
            event.setCancelled(true);
            return;
        } else if (material.equals(Material.ENDER_CHEST) &&
                displayName.endsWith("Taschentruhe")) {
            player.openInventory(player.getEnderChest());
            event.setCancelled(true);
            return;
        } else if (material.equals(Material.SHULKER_BOX) &&
                displayName.endsWith("Taschenshulker")) {
            boolean opened = openVirtualShulker(player, item, meta, displayName);
            if(opened) {
                event.setCancelled(true);
                return;
            }
        }

        if(containerProvider == null)
            return;

        if (material.equals(Material.ANVIL) &&
                displayName.endsWith("Taschenamboss")) {
            containerProvider.openAnvil(player);
            event.setCancelled(true);
        } else if (material.equals(Material.GRINDSTONE) &&
                displayName.endsWith("Taschenschleifstein")) {
            containerProvider.openGrindstone(player);
            event.setCancelled(true);
        } else if (material.equals(Material.LOOM) &&
                displayName.endsWith("Taschenwebstuhl")) {
            containerProvider.openLoom(player);
            event.setCancelled(true);
        } else if (material.equals(Material.STONECUTTER) &&
                displayName.endsWith("Taschensteinsäge")) {
            containerProvider.openStonecutter(player);
            event.setCancelled(true);
        } else if (material.equals(Material.SMITHING_TABLE) &&
                displayName.endsWith("Taschenschmiede")) {
            containerProvider.openSmithingTable(player);
            event.setCancelled(true);
        } else if (material.equals(Material.CARTOGRAPHY_TABLE) &&
                displayName.endsWith("Taschenkartentisch")) {
            containerProvider.openCartographyTable(player);
            event.setCancelled(true);
        }
    }

    // endregion

    // region Internals

    private boolean openVirtualShulker(Player player, ItemStack item, ItemMeta meta, String shulkerName) {
        if (!(meta instanceof BlockStateMeta))
            return false;

        UUID playerId = player.getUniqueId();

        // Ensure a unique ID exists
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        UUID boxId;
        if (!pdc.has(SHULKER_ID_KEY, PersistentDataType.STRING)) {
            boxId = UUID.randomUUID();
            pdc.set(SHULKER_ID_KEY, PersistentDataType.STRING, boxId.toString());
            item.setItemMeta(meta);
        } else {
            boxId = UUID.fromString(Objects.requireNonNull(pdc.get(SHULKER_ID_KEY, PersistentDataType.STRING)));
        }
        OpenShulkerData data = openShulkerBoxes.get(playerId);
        Inventory inv;
        if (data != null) {
            inv = data.inventory;
            if (player.getOpenInventory().getTopInventory().equals(inv)) {
                // Already open, don’t reopen
                return true;
            }
        } else {
            BlockStateMeta blockMeta = (BlockStateMeta) meta;
            ShulkerBox shulkerBox = (ShulkerBox) blockMeta.getBlockState();

            inv = Bukkit.createInventory(null, shulkerBox.getInventory().getSize(), shulkerName);
            inv.setContents(shulkerBox.getInventory().getContents());

            openShulkerBoxes.put(playerId, new OpenShulkerData(item, inv, boxId));
        }

        // Only open if not already opened
        player.openInventory(inv);
        return true;
    }


    private static UUID getIdFromItem(ItemStack item) {
        if (item == null)
            return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return null;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(SHULKER_ID_KEY, PersistentDataType.STRING))
            return null;

        String itemId = pdc.get(SHULKER_ID_KEY, PersistentDataType.STRING);
        if (itemId == null)
            return null;

        return UUID.fromString(itemId);
    }

    private static boolean isSameItem(ItemStack a, ItemStack b) {
        if (a == null || b == null)
            return false;

        // Compare exact object reference OR same material + metadata
        if (a == b)
            return true; // same object in memory

        UUID aId = getIdFromItem(a);
        UUID bId = getIdFromItem(b);

        if(aId == null || bId == null)
            return false;

        return aId.equals(bId);
    }

    private static boolean isShulkerBox(ItemStack item) {
        if (item == null)
            return false;
        Material type = item.getType();
        return type == Material.SHULKER_BOX;
    }

    private static void UpdateMetadata(OpenShulkerData data) {
        if (data == null)
            return;
        ItemStack item = data.item;
        Inventory inventory = data.inventory;
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof BlockStateMeta))
            return;

        BlockStateMeta blockStateMeta = (BlockStateMeta) meta;
        BlockState blockState = blockStateMeta.getBlockState();
        if (!(blockState instanceof ShulkerBox))
            return;

        ShulkerBox shulkerBox = (ShulkerBox) blockState;
        shulkerBox.getInventory().setContents(inventory.getContents());
        blockStateMeta.setBlockState(shulkerBox);
        item.setItemMeta(blockStateMeta);
    }


    private static class OpenShulkerData {
        final ItemStack item;
        final Inventory inventory;
        final UUID id;

        OpenShulkerData(ItemStack item, Inventory inventory, UUID id) {
            this.item = item;
            this.inventory = inventory;
            this.id = id;
        }
    }

    // endregion
}

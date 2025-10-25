package de.ardania.urutar.ArdaCraftingTables;

import com.earth2me.essentials.Essentials;
import net.ess3.provider.ContainerProvider;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpecialItemListener implements Listener {

    private ContainerProvider containerProvider;
    private final Map<UUID, ItemStack> openBoxes = new HashMap<>();

    public SpecialItemListener(Essentials essentials) {
        if(essentials != null)
            this.containerProvider = essentials.provider(ContainerProvider.class);
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
            boolean opened = openShulkerBox(player, item, meta);
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

    private boolean openShulkerBox(Player player, ItemStack item, ItemMeta meta) {
        if (!(meta instanceof BlockStateMeta)) {
            return false;
        }
        BlockStateMeta blockMeta = (BlockStateMeta)meta;
        BlockState blockState = blockMeta.getBlockState();
        if (!(blockState instanceof ShulkerBox)) {
            return false;
        }
        ShulkerBox shulkerBox = (ShulkerBox)blockState;
        player.openInventory(shulkerBox.getInventory());
        openBoxes.put(player.getUniqueId(), item);
        return true;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!openBoxes.containsKey(player.getUniqueId())) {
            return;
        }

        ItemStack item = openBoxes.remove(player.getUniqueId());
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof BlockStateMeta)) {
            return;
        }
        BlockStateMeta blockMeta = (BlockStateMeta) meta;
        BlockState blockState = blockMeta.getBlockState();
        if (!(blockState instanceof ShulkerBox)) {
            return;
        }
        ShulkerBox shulkerBox = (ShulkerBox) blockState;
        shulkerBox.getInventory().setContents(event.getInventory().getContents());
        blockMeta.setBlockState(shulkerBox);
        item.setItemMeta(meta);
    }
}

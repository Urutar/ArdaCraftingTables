package de.ardania.urutar.ArdaCraftingTables;

import com.earth2me.essentials.Console;
import com.earth2me.essentials.Essentials;
import net.ess3.provider.ContainerProvider;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class SpecialItemListener implements Listener {

    private final Essentials essentials;
    private ContainerProvider containerProvider;

    public SpecialItemListener(Essentials essentials) {
        this.essentials = essentials;
        if(this.essentials != null)
            this.containerProvider = this.essentials.provider(ContainerProvider.class);
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
        if (displayName == null)
            return;

        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        if ((enchantments == null || enchantments.isEmpty())) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null)
            return;

        if (material.equals(Material.CRAFTING_TABLE) &&
                displayName.endsWith("Taschenwerkbank") &&
                enchantments.containsKey(Enchantment.THORNS)) {
            player.openWorkbench(player.getLocation(), true);
            event.setCancelled(true);
            return;
        }

        if (material.equals(Material.ENCHANTING_TABLE) &&
                displayName.endsWith("Taschenzaubertisch") &&
                enchantments.containsKey(Enchantment.THORNS)) {
            player.openEnchanting(player.getLocation(), true);
            event.setCancelled(true);
            return;
        }

        if(containerProvider == null)
            return;

        if (material.equals(Material.ANVIL) &&
                displayName.endsWith("Taschenamboss") &&
                enchantments.containsKey(Enchantment.THORNS)) {
            containerProvider.openAnvil(player);
            event.setCancelled(true);
        } else if (material.equals(Material.GRINDSTONE) &&
                displayName.endsWith("Taschenschleifstein") &&
                enchantments.containsKey(Enchantment.THORNS)) {
            containerProvider.openGrindstone(player);
            event.setCancelled(true);
        } else if (material.equals(Material.LOOM) &&
                displayName.endsWith("Taschenwebstuhl") &&
                enchantments.containsKey(Enchantment.THORNS)) {
            containerProvider.openLoom(player);
            event.setCancelled(true);
        } else if (material.equals(Material.STONECUTTER) &&
                displayName.endsWith("Taschensteinsäge") &&
                enchantments.containsKey(Enchantment.THORNS)) {
            containerProvider.openStonecutter(player);
            event.setCancelled(true);
        } else if (material.equals(Material.SMITHING_TABLE) &&
                displayName.endsWith("Taschenschmiede") &&
                enchantments.containsKey(Enchantment.THORNS)) {
            containerProvider.openSmithingTable(player);
            event.setCancelled(true);
        } else if (material.equals(Material.CARTOGRAPHY_TABLE) &&
                displayName.endsWith("Taschenkartentisch") &&
                enchantments.containsKey(Enchantment.THORNS)) {
            containerProvider.openCartographyTable(player);
            event.setCancelled(true);
        }
    }
}

package de.ardania.urutar.ArdaCraftingTables;

import com.earth2me.essentials.Essentials;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public static JavaPlugin PLUGIN;
    public static Essentials essentials;

    public void onEnable(){
        PLUGIN = this;

        boolean hasEssentials = getServer().getPluginManager().getPlugin("Essentials") != null;

        if (hasEssentials) {
            essentials = (Essentials) Essentials.getProvidingPlugin(Essentials.class);
        }
        getServer().getPluginManager().registerEvents(new SpecialItemListener(essentials), this);
    }
}

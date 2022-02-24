package dev.projectg.ecodatabase;

import dev.projectg.configuration.Configurate;
import dev.projectg.database.DatabaseSetup;
import dev.projectg.database.EcoDatabase;
import dev.projectg.ecodatabase.api.VaultApi;
import dev.projectg.ecodatabase.handlers.EcoHandler;
import dev.projectg.ecodatabase.listeners.PlayerEvents;
import dev.projectg.ecodatabase.utils.Metrics;
import dev.projectg.logger.JavaUtilLogger;
import dev.projectg.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public final class EcoDatabaseSpigot extends JavaPlugin {

    private static EcoDatabaseSpigot plugin;
    private static EcoDatabase ecodata;

    @Override
    public void onEnable() {
        plugin = this;

        // Bstats metrics
        new Metrics(this, 14430);

        // Logger
        Logger logger = new JavaUtilLogger(Bukkit.getLogger());

        // Enable vault
        new VaultApi();

        // Register events
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerEvents(), this);

        // Config setup
        Path path = this.getDataFolder().toPath();
        Configurate config = Configurate.create(path);

        // Sync Economy
        EcoHandler.handler().updateHashmapBalance();
        if (config.getEnableSync()) {
            logger.info("Sync economy enabled");
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> EcoHandler.handler().queryHashmapBalance(), 20L + 30, 20L * 60 * config.getSyncInterval());
        }

        // Database setup
        logger.info("Selected " + config.getDatabaseType() + " database!");
        new DatabaseSetup().mysqlSetup(path, config);
        ecodata = new EcoDatabase();
        // Check if connection is mysql and alive -> reconnect
        if (config.getDatabaseType().equals("mysql")) {
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> new DatabaseSetup().connectionAlive(), 20L * 60L * 1L, 20L * 60L * 1L);
        }

        // End
        logger.info("EcoDatabase has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        EcoHandler.handler().updateEcoOnShutdown();
        new DatabaseSetup().connectionClose();
    }

    public static EcoDatabaseSpigot getPlugin() {
        return plugin;
    }
    public EcoDatabase getEcoDatabase() { return ecodata;}
}

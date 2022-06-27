package me.jerriidesu.theiahelper;

import me.jerriidesu.theiahelper.commands.FlySpeedCommand;
import me.jerriidesu.theiahelper.handler.ItemPickupHandler;
import me.jerriidesu.theiahelper.handler.SeatHandler;
import me.jerriidesu.theiahelper.handler.SpongeHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheiaHelper extends JavaPlugin {

    public static Logger LOGGER = LoggerFactory.getLogger(TheiaHelper.class);

    public SpongeHandler spongeHandler;
    public ItemPickupHandler itemPickupHandler;
    public SeatHandler seatHandler;

    @Override
    public void onEnable() {
        this.registerInstances();

        this.registerListeners();
        this.registerCommands();
    }

    @Override
    public void onDisable() {
        this.spongeHandler.disableTimer();
        this.seatHandler.cleanupSeats();
        LOGGER.info("Goodbye!");
    }

    private void registerInstances() {
        this.spongeHandler = new SpongeHandler(this);
        this.itemPickupHandler = new ItemPickupHandler(this);
        this.seatHandler = new SeatHandler(this);
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this.spongeHandler, this);
        Bukkit.getPluginManager().registerEvents(this.itemPickupHandler, this);
        Bukkit.getPluginManager().registerEvents(this.seatHandler, this);
    }

    private void registerCommands() {
        this.getCommand("togglesponge").setExecutor(this.spongeHandler);
        this.getCommand("togglepickup").setExecutor(this.itemPickupHandler);
        this.getCommand("sitdown").setExecutor(this.seatHandler);

        this.getCommand("flyspeed").setExecutor(new FlySpeedCommand());
    }
}

package me.jerriidesu.theiahelper.handler;

import me.jerriidesu.theiahelper.TheiaHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SpongeHandler implements Listener, CommandExecutor {

    private final TheiaHelper plugin;

    private final List<UUID> activated = new ArrayList<>();
    private final ConcurrentHashMap<Location, UUID> sponges = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Location> air = new CopyOnWriteArrayList<>();

    private BukkitTask timer;
    private boolean shouldCleanup = false;

    public SpongeHandler(TheiaHelper plugin) {
        this.plugin = plugin;
        this.startTimer();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        UUID uuid = player.getUniqueId();

        //should always return true
        boolean modified = this.activated.contains(uuid) ? this.activated.remove(uuid) : this.activated.add(uuid);

        if (modified) {
            this.shouldCleanup = true;
            Component text = Component.text("Das automatische Löschen von ").toBuilder().append(Component.translatable("block.minecraft.wet_sponge")).append(Component.text(" wurde umgeschaltet.")).build();
            player.sendMessage(text);
        }

        return true;
    }

    @EventHandler
    public void onSpongePlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.SPONGE) {
            return;
        }

        if (!this.activated.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        this.sponges.put(event.getBlockPlaced().getLocation(), event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onSpongeBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.WET_SPONGE && event.getBlock().getType() != Material.SPONGE) {
            return;
        }

        this.sponges.remove(event.getBlock().getLocation());
    }

    @EventHandler
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        if (!this.sponges.containsKey(event.getBlock().getLocation())) {
            return;
        }

        this.sponges.remove(event.getBlock().getLocation());
        this.air.add(event.getBlock().getLocation());
        this.shouldCleanup = true;
    }

    public void startTimer() {
        this.timer = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!this.shouldCleanup) {
                return;
            }

            this.air.forEach((location)-> {
                location.getBlock().setType(Material.AIR);
                this.air.remove(location);
            });

            this.sponges.forEach((location, uuid) -> {
                if (this.activated.contains(uuid)) {
                    return;
                }

                this.sponges.remove(location);
            });
        }, 20L, 60L);
    }

    public void disableTimer() {
        this.timer.cancel();
    }
}

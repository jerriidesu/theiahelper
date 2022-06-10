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

public class SpongeHandler implements Listener, CommandExecutor {

    private final TheiaHelper plugin;

    private final List<UUID> activated = new ArrayList<>();
    private final ConcurrentHashMap<Location, UUID> changed = new ConcurrentHashMap<>();

    private BukkitTask timer;
    private boolean shouldCleanup = false;

    public SpongeHandler(TheiaHelper plugin) {
        this.plugin = plugin;
        this.startTimer();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        //should always return true
        boolean modified = this.activated.contains(uuid) ? this.activated.remove(uuid) : this.activated.add(uuid);

        if (modified) {
            this.shouldCleanup = true;
            player.sendMessage(Component.text("Das automatische Löschen von " + Component.translatable("block.minecraft.wet_sponge") + " wurde umgeschaltet."));
        }

        return true;
    }

    @EventHandler
    public void onSpongePlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.WET_SPONGE) {
            return;
        }

        if (!this.activated.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        this.changed.put(event.getBlockPlaced().getLocation(), event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onSpongeBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.WET_SPONGE && event.getBlock().getType() != Material.SPONGE) {
            return;
        }

        this.changed.remove(event.getBlock());
    }

    @EventHandler
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        if (!this.changed.containsKey(event.getBlock().getLocation())) {
            return;
        }

        this.changed.remove(event.getBlock().getLocation());
        event.getBlock().setType(Material.AIR);
    }

    public void startTimer() {
        this.timer = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (!this.shouldCleanup) {
                return;
            }

            this.changed.forEach((location, uuid) -> {
                if (this.activated.contains(uuid)) {
                    return;
                }

                this.changed.remove(location);
            });
        }, 100L, 20L * 60L);
    }

    public void disableTimer() {
        this.timer.cancel();
    }
}

package me.jerriidesu.theiahelper.handler;

import me.jerriidesu.theiahelper.TheiaHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemPickupHandler implements Listener, CommandExecutor {

    private final TheiaHelper plugin;

    private final List<UUID> activated = new ArrayList<>();

    public ItemPickupHandler(TheiaHelper plugin) {
        this.plugin = plugin;
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
            player.sendMessage(Component.text("Das Aufheben von Items wurde umgeschaltet."));
        }

        return true;
    }

    @EventHandler
    public void onItemPickUp(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (this.activated.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}

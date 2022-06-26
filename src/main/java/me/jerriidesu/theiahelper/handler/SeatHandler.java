package me.jerriidesu.theiahelper.handler;

import me.jerriidesu.theiahelper.TheiaHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.entity.EntityDismountEvent;

//Inspired by https://github.com/geilmaker/SitDown
public class SeatHandler implements Listener, CommandExecutor {

    private final TheiaHelper plugin;

    public SeatHandler(TheiaHelper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) {
            return false;
        }

        Location location = player.getLocation();
        location.add(0, -1.7, 0);

        this.sitDown(player, location);
        return true;
    }

    @EventHandler
    public void onSeatLeave(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        this.deleteSeat(event.getDismounted());
        player.teleport(player.getLocation().add(0, 1, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if(!player.isInsideVehicle()) {
            return;
        }

        this.deleteSeat(player.getVehicle());
    }

    @EventHandler
    public void onChairInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if(player.isInsideVehicle() || player.isSneaking()) {
            return;
        }

        if(!player.getInventory().getItemInMainHand().getType().isAir() || event.getClickedBlock() == null) {
            return;
        }

        if(event.getClickedBlock().getLocation().distance(player.getLocation()) > 2.5) {
            return;
        }

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || !(event.getClickedBlock().getBlockData() instanceof Stairs stairs)) {
            return;
        }

        if (!player.getFacing().equals(stairs.getFacing()) || player.getEyeLocation().getPitch() < -4 || stairs.getHalf().equals(Bisected.Half.TOP) || !stairs.getShape().equals(Stairs.Shape.STRAIGHT) || stairs.isWaterlogged()) {
            return;
        }

        BlockFace facing = stairs.getFacing().getOppositeFace();
        Location location = event.getClickedBlock().getLocation();

        location.add(0.5, -1.2, 0.5);
        location.setDirection(new Vector(facing.getModX(), facing.getModY(), facing.getModZ()));

        this.sitDown(player, location);
    }

    private void sitDown(Player player, Location location) {
        if(player.isInsideVehicle() || !player.isOnGround()) {
            return;
        }

        location.getWorld().spawn(location, ArmorStand.class, armorStand -> {
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setMetadata("chair", new FixedMetadataValue(this.plugin, "yes_this_is_a_chair"));
            armorStand.addPassenger(player);
            armorStand.setHealth(0.1F);
        });
    }

    private void deleteSeat(Entity entity) {
        if(!(entity instanceof ArmorStand)) {
            return;
        }

        if(!entity.hasMetadata("chair")) {
            return;
        }

        entity.remove();
    }

    public void cleanupSeats() {
        Bukkit.getWorlds().forEach((world) -> world.getEntitiesByClass(ArmorStand.class).forEach(this::deleteSeat));
    }
}

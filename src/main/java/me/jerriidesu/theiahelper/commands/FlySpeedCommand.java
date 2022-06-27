package me.jerriidesu.theiahelper.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlySpeedCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) {
            return false;
        }

        if(args.length != 1) {
            return false;
        }

        try {
            float speed = Integer.parseInt(args[0]);

            player.setFlySpeed(speed);
            player.sendMessage(Component.text("Fluggeschwindigkeit auf "+speed+" gesetzt."));
        } catch(NumberFormatException exception) {
            player.sendMessage(Component.text("Keine gültige Zahl!"));
        }

        return true;
    }
}

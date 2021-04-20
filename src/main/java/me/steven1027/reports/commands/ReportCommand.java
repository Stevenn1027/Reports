package me.steven1027.reports.commands;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import me.steven1027.reports.Reports;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

public class ReportCommand implements Command {

    private final Reports plugin;

    public ReportCommand(Reports plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSource source, String @NonNull [] args) {
        if (!(source instanceof Player)) {
            source.sendMessage(TextComponent.of("You must be a player to use this command!").color(TextColor.RED));
            return;
        }

        final Player sender = (Player) source;

        if (args.length < 2) {
            sender.sendMessage(TextComponent.of("The correct usage is: /report [player] [reason]").color(TextColor.RED));
            return;
        }

        if (!plugin.getProxyServer().getPlayer(args[0]).isPresent()) {
            sender.sendMessage(TextComponent.of("This player is not currently online!").color(TextColor.RED));
            return;
        }

        final Player target = plugin.getProxyServer().getPlayer(args[0]).get();

        if (target == sender) {
            sender.sendMessage(TextComponent.of("You can not report yourself!").color(TextColor.RED));
            return;
        }

        if (target.hasPermission("reports.bypass")) {
            sender.sendMessage(TextComponent.of("You can not report this player!").color(TextColor.RED));
            return;
        }

        final String reportReason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        final String staffMessage = String.join("\n", plugin.getConfig().getArray("report-message").toJson())
                .replace("{reporter}", sender.getUsername())
                .replace("{reported}", target.getUsername())
                .replace("{reason}", reportReason);

        for (Player player : plugin.getProxyServer().getAllPlayers())
            if (player.hasPermission("reports.alert")) {
                player.sendMessage(TextComponent.of(staffMessage).color(TextColor.RED));
            }

        sender.sendMessage(TextComponent.of("Staff have been alerted! Thank you for your report.").color(TextColor.RED));
    }

}

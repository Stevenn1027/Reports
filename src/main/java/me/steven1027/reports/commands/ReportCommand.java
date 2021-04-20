package me.steven1027.reports.commands;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import me.steven1027.reports.Reports;
import me.steven1027.reports.util.DiscordWebhook;
import me.steven1027.reports.util.Util;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.awt.*;
import java.io.IOException;
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

        final String staffMessage = plugin.getConfig().getString("report-message")
                .replace("{reporter}", sender.getUsername())
                .replace("{reported}", target.getUsername())
                .replace("{server}", sender.getCurrentServer().get().getServer().getServerInfo().getName())
                .replace("{reason}", String.join(" ", Arrays.copyOfRange(args, 1, args.length)));

        plugin.getProxyServer().getAllPlayers().stream().filter(staff -> staff.hasPermission("reports.alert")).forEach(staff -> {
            staff.sendMessage(Util.color(staffMessage));
        });

        sender.sendMessage(TextComponent.of("Staff have been alerted! Thank you for your report.").color(TextColor.RED));

        if (plugin.getConfig().getBoolean("discord-webhook-enabled"))
            this.sendDiscordWebHook(sender, target, args);
    }

    private void sendDiscordWebHook(Player sender, Player target, String[] reason) {
        final DiscordWebhook webHook = new DiscordWebhook(plugin.getConfig().getString("discord-webhook-url"));
        final DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();

        embed.setTitle("**Report**");
        embed.setColor(Color.decode(plugin.getConfig().getString("discord-webhook-color")));
        embed.addField("Reporter: ", sender.getUsername(), false);
        embed.addField("Reported: ", target.getUsername(), false);
        embed.addField("Reason: ", String.join(" ", Arrays.copyOfRange(reason, 1, reason.length)), false);
        embed.addField("Server: ", sender.getCurrentServer().get().getServerInfo().getName(), false);
        webHook.addEmbed(embed);

        try {
            webHook.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

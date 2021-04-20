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
import java.util.*;

public class ReportCommand implements Command {

    private final Reports plugin;
    private final HashMap<UUID, Long> cooldown = new HashMap<UUID, Long>();

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
            sender.sendMessage(Util.color(plugin.getConfig().getString("correct-usage")));
            return;
        }

        if (cooldown.get(sender.getUniqueId()) != null && cooldown.get(sender.getUniqueId()) > System.currentTimeMillis()) {
            sender.sendMessage(Util.color(plugin.getConfig().getString("cooldown-message")
                    .replace("{seconds}", String.valueOf((cooldown.get(sender.getUniqueId()) - System.currentTimeMillis()) / 1000))));
            return;
        }

        if (!plugin.getProxyServer().getPlayer(args[0]).isPresent()) {
            sender.sendMessage(Util.color(plugin.getConfig().getString("player-not-online")));
            return;
        }

        final Player target = plugin.getProxyServer().getPlayer(args[0]).get();

        if (target == sender) {
            sender.sendMessage(Util.color(plugin.getConfig().getString("can-not-report-self")));
            return;
        }

        if (target.hasPermission("reports.bypass")) {
            sender.sendMessage(Util.color(plugin.getConfig().getString("can-not-report-player")));
            return;
        }

        this.sendStaffMessage(sender, target, args);
        cooldown.put(sender.getUniqueId(), System.currentTimeMillis() + (plugin.getConfig().getLong("report-cooldown") * 1000));
        sender.sendMessage(Util.color(plugin.getConfig().getString("staff-alerted")));

        if (plugin.getConfig().getBoolean("discord-webhook-enabled"))
            this.sendDiscordWebHook(sender, target, args);
    }

    private void sendStaffMessage(Player sender, Player target, String[] args) {
        final String staffMessage = plugin.getConfig().getString("report-message")
                .replace("{reporter}", sender.getUsername())
                .replace("{reported}", target.getUsername())
                .replace("{server}", sender.getCurrentServer().get().getServer().getServerInfo().getName())
                .replace("{reason}", String.join(" ", Arrays.copyOfRange(args, 1, args.length)));

        plugin.getProxyServer().getAllPlayers().stream().filter(staff -> staff.hasPermission("reports.alert")).forEach(staff -> {
            staff.sendMessage(Util.color(staffMessage));
        });
    }

    private void sendDiscordWebHook(Player sender, Player target, String[] args) {
        final DiscordWebhook webHook = new DiscordWebhook(plugin.getConfig().getString("discord-webhook-url"));
        final DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();

        embed.setTitle("**Report**");
        embed.setColor(Color.decode(plugin.getConfig().getString("discord-webhook-color")));
        embed.addField("Reporter: ", sender.getUsername(), false);
        embed.addField("Reported: ", target.getUsername(), false);
        embed.addField("Reason: ", String.join(" ", Arrays.copyOfRange(args, 1, args.length)), false);
        embed.addField("Server: ", sender.getCurrentServer().get().getServerInfo().getName(), false);
        webHook.addEmbed(embed);

        try {
            webHook.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

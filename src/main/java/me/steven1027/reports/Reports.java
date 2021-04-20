package me.steven1027.reports;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.steven1027.reports.commands.ReportCommand;
import org.slf4j.Logger;
import org.tomlj.TomlParseResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(
        id = "reports",
        name = "Reports",
        version = "1.0-SNAPSHOT",
        description = "Reports plugin made for SkyEternal Network",
        url = "https://github.com/Stevenn1027",
        authors = {"QuaccOnCracc(Steven1027)"}
)
public class Reports {

    private final ProxyServer server;
    private final Logger logger;
    private final Path path;
    private TomlParseResult config;

    @Inject
    public Reports(ProxyServer server, Logger logger, @DataDirectory Path path) {
        this.server = server;
        this.logger = logger;
        this.path = path;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws IOException {
        final CommandManager commandManager = this.server.getCommandManager();
        commandManager.register(new ReportCommand(this), "report");

        this.loadConfig(path);
        this.config = org.tomlj.Toml.parse(path);
    }

    public ProxyServer getProxyServer() {
        return this.server;
    }

    public TomlParseResult getConfig() {
        return this.config;
    }

    private Toml loadConfig(Path path) {
        final File folder = path.toFile();
        final File file = new File(folder, "config.toml");

        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        if (!file.exists()) {
            try (InputStream input = getClass().getResourceAsStream("/" + file.getName())) {
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Toml().read(file);
    }

}
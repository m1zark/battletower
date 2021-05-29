package com.m1zark.battletower;

import com.google.inject.Inject;
import com.m1zark.battletower.commands.CommandManager;
import com.m1zark.battletower.data.Arenas;
import com.m1zark.battletower.listeners.BattleListener;
import com.m1zark.battletower.listeners.JoinListener;
import com.m1zark.battletower.listeners.NPCListener;
import com.m1zark.battletower.utils.Placeholders;
import com.m1zark.battletower.config.Config;
import com.m1zark.battletower.config.InventoryConfig;
import com.m1zark.battletower.config.MessageConfig;
import com.m1zark.battletower.config.PokemonData;
import com.m1zark.battletower.storage.DataSource;
import com.pixelmonmod.pixelmon.Pixelmon;

import lombok.Getter;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.nio.file.Path;
import java.util.*;

@Getter
@Plugin(id=BTInfo.ID, name=BTInfo.NAME, version=BTInfo.VERSION, description=BTInfo.DESCRIPTION, authors = "m1zark")
public class BattleTower {
    @Inject private Logger logger;
    @Inject private PluginContainer pluginContainer;
    private static BattleTower instance;
    private DataSource sql;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    private Config config;
    private MessageConfig msgConfig;
    private PokemonData pkmConfig;
    private InventoryConfig invConfig;
    private boolean enabled = true;

    public Map<UUID,Arenas> battleArenas = new HashMap<>();

    @Listener public void onInitialization(GameInitializationEvent e) {
        instance = this;

        BTInfo.startup();
        this.enabled = BTInfo.dependencyCheck();

        if(enabled) {
            this.config = new Config();
            this.msgConfig = new MessageConfig();
            this.pkmConfig = new PokemonData();
            this.invConfig = new InventoryConfig();

            Sponge.getEventManager().registerListeners(this, new JoinListener());
            Sponge.getEventManager().registerListeners(this, new NPCListener());
            Pixelmon.EVENT_BUS.register(new BattleListener());
            getConsole().ifPresent(console -> console.sendMessages(Text.of(BTInfo.PREFIX, "Initializing listeners...")));

            this.sql = new DataSource("BT_PlayerData","BT_Arenas");
            this.sql.createTables();
            getConsole().ifPresent(console -> console.sendMessages(Text.of(BTInfo.PREFIX, "Initializing database...")));

            new CommandManager().registerCommands(this);

            if (Sponge.getPluginManager().isLoaded("placeholderapi")) Placeholders.register(this);

            getConsole().ifPresent(console -> console.sendMessages(Text.of(BTInfo.PREFIX, "Initialization complete!")));
        }
    }

    @Listener public void onReload(GameReloadEvent e) {
        if (this.enabled) {
            this.config = new Config();
            this.msgConfig = new MessageConfig();
            this.invConfig = new InventoryConfig();
            this.pkmConfig = new PokemonData();
            getConsole().ifPresent(console -> console.sendMessages(Text.of(BTInfo.PREFIX, "Configurations have been reloaded")));
        }
    }

    @Listener public void onServerStop(GameStoppingEvent e) {
        try {
            this.sql.shutdown();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static BattleTower getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Optional<ConsoleSource> getConsole() {
        return Optional.ofNullable(Sponge.isServerAvailable() ? Sponge.getServer().getConsole() : null);
    }
}
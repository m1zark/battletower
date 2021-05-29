package com.m1zark.battletower.config;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.m1zark.battletower.BTInfo;
import com.m1zark.battletower.BattleTower;
import com.pixelmonmod.pixelmon.RandomHelper;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.loader.HeaderMode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PokemonData {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    public PokemonData() {
        this.loadConfig();
    }

    private void loadConfig(){
        Path dir = Paths.get(BattleTower.getInstance().getConfigDir() + "/data");
        Path configFile = Paths.get(dir + "/pokemon.conf");

        loader = HoconConfigurationLoader.builder().setPath(configFile).setHeaderMode(HeaderMode.NONE).build();

        try {
            if (!Files.exists(dir)) Files.createDirectory(dir);
            if (!Files.exists(configFile)) Files.createFile(configFile);

            if (main == null) main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));

            CommentedConfigurationNode sets = main.getNode("Sets");
            sets.getNode("Smogon").getList(TypeToken.of(String.class), Lists.newArrayList());
            sets.getNode("Legends").getList(TypeToken.of(String.class), Lists.newArrayList());

            loader.save(main);
        } catch (ObjectMappingException | IOException e) {
            BattleTower.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(BTInfo.ERROR_PREFIX, "There was an issue loading pokemon config data...")));
            return;
        }

        BattleTower.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(BTInfo.PREFIX, "Loading pokemon data...")));
    }

    public static void saveConfig() {
        try {
            loader.save(main);
        } catch (IOException var1) {
            var1.printStackTrace();
        }
    }

    public void reload() {
        try {
            main = loader.load();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    public static String SmogonSet() {
        String set = "";

        try {
            int random = RandomHelper.getRandomNumberBetween(0, main.getNode("Sets","Smogon").getChildrenList().size() - 1);
            set = main.getNode("Sets","Smogon").getList(TypeToken.of(String.class)).get(random);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }

        return set;
    }

    public static String LegendsSet() {
        String set = "";

        try {
            int random = RandomHelper.getRandomNumberBetween(0, main.getNode("Sets","Legends").getChildrenList().size() - 1);
            set = main.getNode("Sets","Legends").getList(TypeToken.of(String.class)).get(random);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }

        return set;
    }
}

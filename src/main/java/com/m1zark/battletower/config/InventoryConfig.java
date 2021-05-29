package com.m1zark.battletower.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.m1zark.battletower.BTInfo;
import com.m1zark.battletower.BattleTower;
import com.m1zark.battletower.utils.Items;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;

public class InventoryConfig {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    public InventoryConfig() {
        this.loadConfig();
    }

    private void loadConfig() {
        Path dir = Paths.get(BattleTower.getInstance().getConfigDir() + "/data");
        Path configFile = Paths.get(dir + "/markets.conf");

        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(dir)) Files.createDirectory(dir);
            if (!Files.exists(configFile)) Files.createFile(configFile);

            if (main == null) main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));

            CommentedConfigurationNode items = main.getNode("Inventories");
            items.getNode("mega","display-name").getString("&5Mega Stones");
            items.getNode("mega","display-icon").getString("pixelmon:Steelexite");
            items.getNode("mega","display-info").getString("&bClick here to open this market.");
            items.getNode("mega","items").getList(TypeToken.of(String.class), Lists.newArrayList());

            loader.save(main);
        } catch (ObjectMappingException | IOException e) {
            BattleTower.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(BTInfo.ERROR_PREFIX, "There was an issue loading the inventory data...")));
            return;
        }

        BattleTower.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(BTInfo.PREFIX, "Loading inventories...")));
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

    public static boolean isVendor(String vendor) { return main.getNode("Inventories",vendor).isVirtual(); }

    public static String getVendorInfo(String vendor, String type) {
        if(main.getNode("Inventories",vendor,type).isVirtual()) return "";

        return main.getNode("Inventories",vendor,type).getString();
    }

    public static List<String> getVendors() {
        List<String> vendors = Lists.newArrayList();
        main.getNode("Inventories").getChildrenMap().forEach((vendor,meh) -> vendors.add((String) vendor));

        return vendors;
    }

    public static List<Items> getItemList(String vendor) {
        List<Items> items = new ArrayList<>();

        for (int i = 0; i < main.getNode("Inventories", vendor, "items").getChildrenList().size(); i++) {
            CommentedConfigurationNode item = main.getNode("Inventories", vendor, "items").getChildrenList().get(i);

            String type = item.getNode("type").getString();
            String id = item.getNode("id").getString();
            String command = item.getNode("command").isVirtual() ? null : item.getNode("command").getString();
            String name = item.getNode("display-name").getString();
            Integer cost = item.getNode("cost").getInt();

            Integer count = item.getNode("data","count").isVirtual() ? 1 : item.getNode("data","count").getInt();
            Integer meta = item.getNode("data","meta").isVirtual() ? null : item.getNode("data","meta").getInt();

            boolean unbreakable = !item.getNode("data","unbreakable").isVirtual() && item.getNode("data","unbreakable").getBoolean();
            String sprite = item.getNode("data","sprite-data").isVirtual() ? null : item.getNode("data","sprite-data").getString();

            List<String> lore = Lists.newArrayList();
            if(!item.getNode("data","lore").isVirtual()) {
                try {
                    lore = item.getNode("data","lore").getList(TypeToken.of(String.class));
                } catch (ObjectMappingException e) {
                    e.printStackTrace();
                }
            }

            Map nbt = new LinkedHashMap();
            if(!item.getNode("data","nbt").isVirtual() && item.getNode("data","nbt").getValue() instanceof LinkedHashMap) {
                nbt = (LinkedHashMap) item.getNode("data","nbt").getValue();
            }

            items.add(new Items(i,type,id,command,name,meta,nbt,unbreakable,lore,sprite,cost,count));
        }

        return items;
    }
}

package com.m1zark.battletower.config;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.m1zark.battletower.BTInfo;
import com.m1zark.battletower.BattleTower;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MessageConfig {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static CommentedConfigurationNode main;

    public static List<String> npcWelcome;
    public static String npcHasStreak;
    public static String npcInteractQuestion;
    public static String npcInteractYes;
    public static String npcInteractNo;

    public MessageConfig() {
        this.loadConfig();
    }

    private void loadConfig(){
        Path configFile = Paths.get(BattleTower.getInstance().getConfigDir() + "/messages.conf");
        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(BattleTower.getInstance().getConfigDir())) Files.createDirectory(BattleTower.getInstance().getConfigDir());

            if (!Files.exists(configFile)) Files.createFile(configFile);

            if (main == null) {
                main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            }

            CommentedConfigurationNode messages = main.getNode("messages");
            messages.getNode("market","purchase-item").getString("&3&lBattleTower &e»&r &7You successfully purchased a {item} &7for &d{cost} &7BP!");
            messages.getNode("market","not-enough-bp").getString("&3&lBattleTower &e»&r &7You do not have enough BP to purchase {item}!");
            messages.getNode("market","inventory-full").getString("&3&lBattleTower &e»&r &7Unable to purchase {item} due to a full inventory. Please clear some space and try again.");

            messages.getNode("battles","teleporting-to-arena").getString("&3&lBattleTower &e»&r &7Teleporting you to &b{arena}&7... your battle will begin shortly.");
            messages.getNode("battles","teleporting-to-entrance").getString("&3&lBattleTower &e»&r Taking you back to the entrance...");
            messages.getNode("battles","banned-pokemon").getString("&3&lBattleTower &e»&r &b{pokemon} &7is currently banned from being used on the Battle Tower! Use &a/bt rules &7to see a list of all banned Pok\u00E9mon.");
            messages.getNode("battles","banned-moves").getString("&3&lBattleTower &e»&r &b{pokemon} &7is currently using a banned move and/or ability. Use &a/bt rules &7for more info.");
            messages.getNode("battles","arenas-full").getString("&3&lBattleTower &e»&r &7All arenas are currently in use. Try back another time.");
            messages.getNode("battles","bugged-pokemon").getString("&3&lBattleTower &e»&r Oh.. it looks like one of your Pok\u00E9mon is bugged. Please contact an admin.");

            messages.getNode("battles","npc","on-trainer-defeat").getString("Ooh what a close battle! Better luck next time.");
            messages.getNode("battles","npc","on-trainer-win").getString("Congratulations, you won! You have received {amount} battle points.");
            messages.getNode("battles","npc","dialogue").getString("What would you like to do now?");
            messages.getNode("battles","npc","boss-trainer").getString("Wow looks like you're on your {streak} win! You'll be going up against a much tougher trainer this round.");
            messages.getNode("battles","npc","dialogue-tree").getList(TypeToken.of(String.class), Lists.newArrayList("Continue battling...","Ok, I'll go ahead and set up your next battle!","Stop now...","Ok, take care!"));

            npcWelcome = messages.getNode("npc","dialogue","welcome-message").getList(TypeToken.of(String.class), Lists.newArrayList("Welcome to the Battle Tower {player}!","Here you can challenge many different trainers and get awesome rewards!"));
            npcHasStreak = messages.getNode("npc","dialogue","player-has-streak").getString("Oh, looks like you're on a bit of a winning streak... {streak} wins!");
            npcInteractYes = messages.getNode("npc","dialogue","interact","yes").getString("Lets battle!");
            npcInteractNo = messages.getNode("npc","dialogue","interact","no").getString("I changed my mind.");
            npcInteractQuestion = messages.getNode("npc","dialogue","interact","question-asked").getString("What would you like todo today?");

            loader.save(main);
        } catch (ObjectMappingException | IOException e) {
            BattleTower.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(BTInfo.ERROR_PREFIX, "There was an issue loading the message config...")));
            e.printStackTrace();
            return;
        }

        BattleTower.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(BTInfo.PREFIX, "Loading message configuration...")));
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

    public static String getMessages(String value) { return main.getNode((Object[])value.split("\\.")).getString(); }

    public static List<String> trainerDialogue() {
        try {
            return main.getNode("messages","battles","npc","dialogue-tree").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            return Lists.newArrayList();
        }
    }
}

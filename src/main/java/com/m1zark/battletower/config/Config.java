package com.m1zark.battletower.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.m1zark.battletower.BTInfo;
import com.m1zark.battletower.BattleTower;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.rules.clauses.BattleClauseRegistry;
import com.pixelmonmod.pixelmon.entities.pixelmon.abilities.AbilityBase;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Config {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    public static boolean enable_bp;
    public static int battlepoints;
    public static boolean allowMultiplier;

    public static int chances;
    public static int cost;

    public static String trainer_prefix;
    public static String npc_name;

    public static int numberPokemon;
    public static boolean showTeams;
    public static int teamSelectionTime;
    public static int turnTime;
    public static int levelCap;
    public static boolean fullHeal;
    public static boolean raiseToCap;
    public static boolean enhanced;
    public static int bossStreak;
    public static int trainerPokemon;
    public static boolean canMega;
    public static boolean canDynamax;
    public static List<String> clauses;
    public static List<String> pokemon;
    public static List<String> moves;
    public static List<String> abilities;

    public static String mysqlURL;
    public static String mysqlUsername;
    public static String mysqlPassword;

    public Config() {
        this.loadConfig();
    }

    private void loadConfig(){
        Path configFile = Paths.get(BattleTower.getInstance().getConfigDir() + "/settings.conf");

        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(BattleTower.getInstance().getConfigDir())) Files.createDirectory(BattleTower.getInstance().getConfigDir());

            if (!Files.exists(configFile)) Files.createFile(configFile);

            if (main == null) {
                main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            }

            CommentedConfigurationNode storage = main.getNode("Storage");
            storage.getNode("storage-type").setComment("Types: h2, mysql").getString("h2");
            mysqlURL = storage.getNode("MYSQL","URL").getString("[host]:[port]/[database]");
            mysqlUsername = storage.getNode("MYSQL","Username").getString("");
            mysqlPassword = storage.getNode("MYSQL","Password").getString("");

            CommentedConfigurationNode settings = main.getNode("Settings");
            settings.getNode("battles","enable-bp").getBoolean(true);
            settings.getNode("battles","bp-amount").setComment("Number of points to give per win.").getInt(1);
            settings.getNode("battles","enable-bp-multiplier").setComment("Gives more BP the higher the win streak is.").getBoolean(false);

            settings.getNode("misc","chances-per-day").getInt(5);
            settings.getNode("misc","cost-per-chance").getInt(500);
            settings.getNode("misc","npc-attendant-name").getString("BattleTower Attendant");

            settings.getNode("battles","rules","enhanced-pokemon").setComment("252 in every EV stat").getBoolean(false);
            settings.getNode("battles","rules","number-pokemon").getInt(3);
            settings.getNode("battles","rules","team-selection-time").getInt(120);
            settings.getNode("battles","rules","turn-time").getInt(120);
            settings.getNode("battles","rules","number-of-trainer-pokemon").getInt(6);
            settings.getNode("battles","rules","full-heal").getBoolean(true);
            settings.getNode("battles","rules","raise-to-level-cap").getBoolean(true);
            settings.getNode("battles","rules","boss-trainer-streak").getInt(10);
            settings.getNode("battles","rules","show-teams").getBoolean(true);
            settings.getNode("battles","rules","level-cap").getInt(50);
            settings.getNode("battles","rules","clauses").getList(TypeToken.of(String.class), Lists.newArrayList("species","sleep","forfeit","item","bag","batonpass","endlessbattle","evasion","moody","ohko","swagger"));
            settings.getNode("battles","rules","banned-pokemon").getList(TypeToken.of(String.class), Lists.newArrayList("Mewtwo","Mew","Lugia","Ho-Oh","Celebi","Kyogre","Groudon","Rayquaza","Jirachi","Deoxys", "Dialga","Palkia","Giratina","Phione","Manaphy","Darkrai","Shaymin","Arceus","Victini","Reshiram","Zekrom", "Kyurem","Keldeo","Meloetta","Genesect","Xerneas","Yveltal","Zygarde","Diancie","Hoopa","Volcanion","Cosmog", "Cosmoem","Solgaleo","Lunala","Necrozma","Magearna","Marshadow","Zeraora"));
            settings.getNode("battles","rules","banned-moves").getList(TypeToken.of(String.class), Lists.newArrayList("Counter","Mirror Coat","Endeavor","Final Gambit","Destiny Bond"));
            settings.getNode("battles","rules","banned-abilities").getList(TypeToken.of(String.class), Lists.newArrayList("Serene Grace","Wonder Guard"));

            settings.getNode("trainers","custom-info").setComment("Custom appearance for trainers... [name]:[skin]:[4=skin,5=player]").getList(TypeToken.of(String.class), Lists.newArrayList("Florian:battlegirl:4","BattleTree Trainer:lass:4"));
            settings.getNode("trainers","trainer-prefix").getString("BattleTower Trainer");
            settings.getNode("trainers","boss-Mega").getBoolean(true);
            settings.getNode("trainers","boss-Dynamax").getBoolean(false);

            loader.save(main);
        } catch (ObjectMappingException | IOException e) {
            BattleTower.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(BTInfo.ERROR_PREFIX, "There was an issue loading the config...")));
            e.printStackTrace();
            return;
        }

        loadRules();
        BattleTower.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(BTInfo.PREFIX, "Loading configuration...")));
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
            loadRules();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    private void loadRules() {
        try {
            enable_bp = main.getNode("Settings","battles","enable-bp").getBoolean();
            battlepoints = main.getNode("Settings","battles","bp-amount").getInt();
            allowMultiplier = main.getNode("Settings","battles","enable-bp-multiplier").getBoolean();
            chances = main.getNode("Settings","misc","chances-per-day").getInt();
            cost = main.getNode("Settings","misc","cost-per-chance").getInt();
            trainer_prefix = main.getNode("Settings","trainers","trainer-prefix").getString();
            npc_name = main.getNode("Settings","misc","npc-attendant-name").getString();

            trainerPokemon = main.getNode("Settings","battles","rules","number-of-trainer-pokemon").getInt();
            fullHeal = main.getNode("Settings","battles","rules","full-heal").getBoolean();
            raiseToCap = main.getNode("Settings","battles","rules","raise-to-level-cap").getBoolean();
            teamSelectionTime = main.getNode("Settings","battles","rules","team-selection-time").getInt();
            turnTime = main.getNode("Settings","battles","rules","turn-time").getInt();
            enhanced = main.getNode("Settings","battles","rules","enhanced-pokemon").getBoolean();
            numberPokemon = main.getNode("Settings","battles","rules","number-pokemon").getInt();
            showTeams = main.getNode("Settings","battles","rules","show-teams").getBoolean();
            levelCap = main.getNode("Settings","battles","rules","level-cap").getInt();
            bossStreak = main.getNode("Settings","battles","rules","boss-trainer-streak").getInt();
            clauses = main.getNode("Settings","battles","rules","clauses").getList(TypeToken.of(String.class)).stream().filter(BattleClauseRegistry.getClauseRegistry()::hasClause).collect(Collectors.toList());
            pokemon = main.getNode("Settings","battles","rules","banned-pokemon").getList(TypeToken.of(String.class)).stream().filter(EnumSpecies::hasPokemonAnyCase).collect(Collectors.toList());
            moves = main.getNode("Settings","battles","rules","banned-moves").getList(TypeToken.of(String.class)).stream().filter(Attack::hasAttack).collect(Collectors.toList());
            abilities = main.getNode("Settings","battles","rules","banned-abilities").getList(TypeToken.of(String.class)).stream().filter(ab -> AbilityBase.getAbility(ab).isPresent()).collect(Collectors.toList());
            canMega = main.getNode("Settings","trainers","boss-Mega").getBoolean();
            canDynamax = main.getNode("Settings","trainers","boss-Dynamax").getBoolean();
        } catch(ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public static String getStorageType() {  return main.getNode("Storage","storage-type").getString(); }

    public static String[] trainerInfo(String type) {
        String info = "";

        try {
            int random = RandomHelper.getRandomNumberBetween(0, main.getNode("Settings","trainers",type).getChildrenList().size() - 1);
            info = main.getNode("Settings","trainers",type).getList(TypeToken.of(String.class)).get(random);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }

        return info.split(":");
    }

    public static Vector3d getEntranceLocation() {
        try {
            return main.getNode("Settings","data","entrance-location-1").getValue(TypeToken.of(Vector3d.class));
        } catch (ObjectMappingException e) {
            return null;
        }
    }

    public static Vector3d getEntranceRotation() {
        try {
            return main.getNode("Settings","data","entrance-location-2").getValue(TypeToken.of(Vector3d.class));
        } catch (ObjectMappingException e) {
            return null;
        }
    }

    public static boolean setEntranceLocation(Vector3d location) {
        try {
            main.getNode("Settings","data","entrance-location-1").setValue(TypeToken.of(Vector3d.class), location);
            saveConfig();
            return true;
        } catch (ObjectMappingException e) {
            return false;
        }
    }

    public static boolean setEntranceRotation(Vector3d location) {
        try {
            main.getNode("Settings","data","entrance-location-2").setValue(TypeToken.of(Vector3d.class), location);
            saveConfig();
            return true;
        } catch (ObjectMappingException e) {
            return false;
        }
    }

    public static boolean saveBTNPC(UUID uuid) {
        try {
            main.getNode("Settings","data","bt-npc","id").setValue(TypeToken.of(UUID.class), uuid);
            saveConfig();
            return true;
        }catch (ObjectMappingException e) {
            return false;
        }
    }

    public static UUID getBTNPC() {
        try {
            return main.getNode("Settings","data","bt-npc","id").getValue(TypeToken.of(UUID.class));
        } catch (ObjectMappingException e) {
            return null;
        }
    }
}



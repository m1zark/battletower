package com.m1zark.battletower.data;

import com.m1zark.battletower.BattleTower;
import com.m1zark.battletower.config.Config;
import com.m1zark.battletower.config.PokemonData;
import com.m1zark.battletower.utils.Utils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.exceptions.ShowdownImportException;
import com.pixelmonmod.pixelmon.api.pokemon.ImportExportConverter;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.rules.clauses.BattleClause;
import com.pixelmonmod.pixelmon.battles.rules.clauses.BattleClauseRegistry;
import com.pixelmonmod.pixelmon.battles.rules.teamselection.TeamSelectionList;
import com.pixelmonmod.pixelmon.comm.packetHandlers.ClearTrainerPokemon;
import com.pixelmonmod.pixelmon.comm.packetHandlers.npc.StoreTrainerPokemon;
import com.pixelmonmod.pixelmon.entities.npcs.EntityNPC;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.entities.npcs.registry.NPCRegistryTrainers;
import com.pixelmonmod.pixelmon.enums.*;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleAIMode;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleType;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Trainers {
    private static final Random RANDOM = new Random();
    private static boolean isBoss;

    public static NPCTrainer getTrainer(Player p, boolean boss) {
        isBoss = boss;

        EntityPlayerMP user = (EntityPlayerMP) p;

        NPCTrainer trainer = new NPCTrainer(user.world);
        trainer.init(NPCRegistryTrainers.Steve);
        String[] custom = Config.trainerInfo("custom-info");
        trainer.getEntityData().setString("BattleTower", p.getUniqueId().toString());
        trainer.setName(Config.trainer_prefix  + " " + custom[0]);
        trainer.setCustomSteveTexture(custom[1]);
        trainer.setTextureIndex(Integer.parseInt(custom[2]));

        if(Config.canMega) {
            trainer.setOldGenMode(EnumOldGenMode.Mega);
            trainer.setMegaItem(EnumMegaItemsUnlocked.Mega);
        } else if(Config.canDynamax) {
            trainer.setOldGenMode(EnumOldGenMode.Dynamax);
            trainer.setMegaItem(EnumMegaItemsUnlocked.Dynamax);
        }

        trainer.setAlwaysRenderNameTag(true);
        trainer.setBattleAIMode(EnumBattleAIMode.Advanced);
        trainer.setEncounterMode(EnumEncounterMode.Once);
        trainer.setAIMode(EnumTrainerAI.StandStill);
        trainer.ignoreDespawnCounter = true;
        trainer.winMoney = 0;
        trainer.initAI();

        Arenas location = BattleTower.getInstance().battleArenas.get(p.getUniqueId());
        trainer.setPosition(location.getTrainerSpawn().getX(), location.getTrainerSpawn().getY(), location.getTrainerSpawn().getZ());
        trainer.setStartRotationYaw(-90.0f);

        trainer.getPokemonStorage().set(0, null);

        int trainerPokemon = Config.trainerPokemon > 6 ? 6 : Math.max(Config.trainerPokemon, 1);
        ArrayList<Pokemon> pokemonList = generateRandomPool(trainerPokemon);
        pokemonList.forEach(pokemon -> trainer.getPokemonStorage().add(pokemon));

        trainer.updateLvl();
        trainer.battleRules = rules();

        return trainer;
    }

    public static void spawnTrainer(NPCTrainer trainer, Player p) {
        Pixelmon.proxy.spawnEntitySafely(trainer, (World) p.getWorld());
    }

    private static ArrayList<Pokemon> generateRandomPool(Integer intSize) {
        ArrayList<Pokemon> pool = new ArrayList<>();

        for(int i = 0; i < intSize; ++i) {
            Pokemon pokemon;
            do {
                pokemon = getRandomEntityPixelmon(i >= intSize/2 && isBoss);
            } while (pokemon == null || pool.contains(pokemon));

            pool.add(pokemon);
        }

        return pool;
    }

    private static Pokemon getRandomEntityPixelmon(boolean isLegendary) {
        Pokemon pokemon;

        do {
            String txt = isLegendary ? PokemonData.LegendsSet() : PokemonData.SmogonSet();
            try {
                pokemon = ImportExportConverter.importText(txt);
            } catch (ShowdownImportException e) {
                pokemon = Pixelmon.pokemonFactory.create(EnumSpecies.Magikarp);
            }
        } while (pokemon == null);

        if(Config.enhanced) {
            int[] intArray = new int[]{252, 252, 252, 252, 252, 252};
            pokemon.getEVs().fillFromArray(intArray);
        }

        pokemon.setShiny(RANDOM.nextInt(25) == 1);
        pokemon.setGrowth(EnumGrowth.getRandomGrowth());
        pokemon.setCaughtBall(EnumPokeballs.getFromIndex(RANDOM.nextInt(EnumPokeballs.values().length - 1)));

        return pokemon;
    }

    private static BattleRules rules() {
        BattleRules rules = new BattleRules();

        rules.battleType = EnumBattleType.Single;
        rules.numPokemon = Config.numberPokemon;
        rules.teamSelectTime = Config.teamSelectionTime;
        rules.turnTime = Config.turnTime;
        rules.teamPreview = Config.showTeams;
        rules.levelCap = Config.levelCap;
        rules.raiseToCap = Config.raiseToCap;
        rules.fullHeal = Config.fullHeal;

        BattleClauseRegistry<BattleClause> clauseRegistry = BattleClauseRegistry.getClauseRegistry();
        List<BattleClause> newClauses = rules.getClauseList();

        Config.clauses.forEach(clause -> {
            if(clauseRegistry.hasClause(clause)) newClauses.add(clauseRegistry.getClause(clause));
        });

        rules.setNewClauses(newClauses);

        return rules;
    }

    public static void startBattle(EntityPlayerMP p, NPCTrainer trainer) {
        PlayerPartyStorage storage = Utils.getPlayerStorage((Player) p);
        if(storage != null) {
            Optional<NPCTrainer> t = EntityNPC.locateNPCServer(p.world, trainer.getId(), NPCTrainer.class);
            t.ifPresent(trainer1 -> {
                if(trainer.getEntityData().hasKey("BattleTower") && trainer.getEntityData().getString("BattleTower").equals(((Player) p).getUniqueId().toString())) {
                    TeamSelectionList.addTeamSelection(trainer.battleRules, false, trainer.getPokemonStorage(), Pixelmon.storageManager.getParty(p));
                }
            });
        }
    }
}
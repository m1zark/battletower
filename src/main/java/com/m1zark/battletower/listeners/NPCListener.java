package com.m1zark.battletower.listeners;

import com.google.common.collect.Lists;
import com.m1zark.battletower.BattleTower;
import com.m1zark.battletower.commands.BT;
import com.m1zark.battletower.config.Config;
import com.m1zark.battletower.config.MessageConfig;
import com.m1zark.battletower.data.Arenas;
import com.m1zark.battletower.data.PlayerInfo;
import com.m1zark.battletower.data.Trainers;
import com.m1zark.battletower.utils.Utils;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Money;
import com.pixelmonmod.pixelmon.api.dialogue.Choice;
import com.pixelmonmod.pixelmon.api.dialogue.Dialogue;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.comm.packetHandlers.dialogue.DialogueNextAction;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class NPCListener {
    @Listener
    public void onNPCClick(InteractEntityEvent.Secondary.MainHand e, @First Player player) {
        EntityType et = Sponge.getRegistry().getType(EntityType.class, "pixelmon:chattingnpc").get();

        if(e.getTargetEntity().getType().equals(et)) {
            if(BT.CreateNPC.getAdding().contains(player.getUniqueId())) {
                e.setCancelled(true);
                if(Config.saveBTNPC(e.getTargetEntity().getUniqueId())) {
                    ((NPCChatting)e.getTargetEntity()).setName(Config.npc_name);
                    ((NPCChatting)e.getTargetEntity()).setAlwaysRenderNameTag(true);

                    BT.CreateNPC.getAdding().remove(player.getUniqueId());
                    Chat.sendMessage(player, "&7BattleTower NPC has been registered!");
                }
            } else {
                if(Config.getBTNPC() != null && Config.getBTNPC().equals(e.getTargetEntity().getUniqueId())) {
                    e.setCancelled(true);
                    Dialogue.setPlayerDialogueData((EntityPlayerMP) player, forgeDialogue(player, ((NPCChatting)e.getTargetEntity()).getName()), true);
                }
            }
        }
    }

    private ArrayList<Dialogue> forgeDialogue(Player player, String name) {
        ArrayList<Dialogue> prompt = Lists.newArrayList();
        for(String text : MessageConfig.npcWelcome) {
            prompt.add(Dialogue.builder().setName(name).setText(text.replace("{player}",player.getName())).build()
            );
        }

        Optional<PlayerInfo> pl = BattleTower.getInstance().getSql().getPlayerData().stream().filter(id -> id.getPlayer().equals(player.getUniqueId())).findFirst();
        if(pl.get().getWinStreak() > 0) prompt.add(Dialogue.builder().setName(name).setText(MessageConfig.npcHasStreak.replace("{streak}",String.valueOf(pl.get().getWinStreak()))).build());

        prompt.add(
                Dialogue.builder()
                        .setName(name)
                        .setText(MessageConfig.npcInteractQuestion)
                        .addChoice(
                                Choice.builder()
                                        .setText(MessageConfig.npcInteractYes)
                                        .setHandle(e -> {
                                                e.setAction(DialogueNextAction.DialogueGuiAction.CLOSE);
                                                if(checkTeam(player) && checkCanBattle(player, pl.get())) start(player);
                                        })
                                        .build()
                        )
                        .addChoice(
                                Choice.builder()
                                        .setText(MessageConfig.npcInteractNo)
                                        .setHandle(e -> e.setAction(DialogueNextAction.DialogueGuiAction.CLOSE))
                                        .build()
                        )
                        .build()
        );

        return prompt;
    }

    private boolean checkTeam(Player p) {
        PlayerPartyStorage storage = Utils.getPlayerStorage(p);

        if(storage != null) {
            storage.heal();

            for (int i = 0; i < storage.getTeam().size(); i++) {
                Pokemon pokemon = storage.get(i);
                if(pokemon != null) {
                    if (Config.pokemon.contains(pokemon.getSpecies().getPokemonName())) {
                        Chat.sendMessage(p, MessageConfig.getMessages("messages.battles.banned-pokemon").replace("{pokemon}", pokemon.getSpecies().getPokemonName()));
                        return false;
                    }

                    if (pokemon.isInRanch()) {
                        Chat.sendMessage(p, MessageConfig.getMessages("messages.battles.bugged-pokemon"));
                        return false;
                    }

                    if(Config.abilities.contains(pokemon.getAbility().getName())) {
                        Chat.sendMessage(p, MessageConfig.getMessages("messages.battles.banned-moves").replace("{pokemon}", pokemon.getSpecies().getPokemonName()));
                        return false;
                    }

                    for (Attack attack : pokemon.getMoveset().attacks) {
                        if (attack != null && Config.moves.contains(attack.getActualMove().getAttackName())) {
                            Chat.sendMessage(p, MessageConfig.getMessages("messages.battles.banned-moves").replace("{pokemon}", pokemon.getSpecies().getPokemonName()));
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean checkCanBattle(Player p, PlayerInfo pl) {
        if(pl.getTries() <= Config.chances) {
            if (Money.canPay(p, Config.cost)) {
                Money.withdrawn(p, Config.cost);
                return true;
            } else {
                Chat.sendMessage(p, MessageConfig.npcCost.replace("{cost}", String.valueOf(Config.cost)));
                return false;
            }
        }

        Chat.sendMessage(p, MessageConfig.npcAttempts.replace("{attempts}", String.valueOf(Config.chances)));
        return false;
    }

    private void start(Player p) {
        Optional<Arenas> arena = BattleTower.getInstance().getSql().getArenas().stream().filter(a -> !a.isInUse()).findFirst();

        if (!arena.isPresent()) {
            Chat.sendMessage(p, MessageConfig.getMessages("messages.battles.arenas-full"));
        } else {
            if(arena.get().getPlayerSpawn() == null || arena.get().getTrainerSpawn() == null) {
                Chat.sendMessage(p, "&cThere doesn't seem to be spawn locations set for " + arena.get().getId() + ". Contact an admin and let them know.");
            } else {
                Optional<PlayerInfo> pl = BattleTower.getInstance().getSql().getPlayerData().stream().filter(id -> id.getPlayer().equals(p.getUniqueId())).findFirst();

                arena.get().setInUse(true);
                BattleTower.getInstance().getSql().updateArena(arena.get());
                BattleTower.getInstance().battleArenas.put(p.getUniqueId(), arena.get());

                Utils.teleportPlayer(p, arena.get().getPlayerSpawn(), arena.get().getPlayerRotation());
                Chat.sendMessage(p, MessageConfig.getMessages("messages.battles.teleporting-to-arena").replace("{arena}", arena.get().getId()));

                NPCTrainer trainer = Trainers.getTrainer(p, pl.get().getWinStreak() % Config.bossStreak == 0 && pl.get().getWinStreak() != 0);
                Trainers.spawnTrainer(trainer, p);

                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    Trainers.startBattle((EntityPlayerMP) p, trainer);
                }).delay(3, TimeUnit.SECONDS).submit(BattleTower.getInstance());
            }
        }
    }
}

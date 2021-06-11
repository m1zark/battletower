package com.m1zark.battletower.listeners;

import com.m1zark.battletower.BattleTower;
import com.m1zark.battletower.config.MessageConfig;
import com.m1zark.battletower.data.Arenas;
import com.m1zark.battletower.data.PlayerInfo;
import com.m1zark.battletower.data.Trainers;
import com.m1zark.battletower.config.Config;
import com.m1zark.battletower.utils.Utils;
import com.m1zark.m1utilities.api.Chat;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.dialogue.Choice;
import com.pixelmonmod.pixelmon.api.dialogue.Dialogue;
import com.pixelmonmod.pixelmon.api.events.*;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.ExitBattle;
import com.pixelmonmod.pixelmon.comm.packetHandlers.dialogue.DialogueNextAction;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.Sys;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class BattleListener {
    @SubscribeEvent
    public void onTrainerBeat(BeatTrainerEvent event) {
        Player p = (Player) event.player;

        if(event.trainer.getEntityData().hasKey("BattleTower")) {
            event.trainer.setDead();

            PlayerInfo pl = BattleTower.getInstance().getSql().getPlayerData(p.getUniqueId());

            int bp = Config.battlepoints;
            if(Config.allowMultiplier) {
                if(Utils.between(pl.getWinStreak(),10,20)) bp = bp + 1;
                else if(Utils.between(pl.getWinStreak(),20,30)) bp = bp + 2;
                else if(Utils.between(pl.getWinStreak(),30,40)) bp = bp + 3;
                else if(Utils.between(pl.getWinStreak(),40,50) || pl.getWinStreak() >= 51) bp = bp + 4;
            }

            pl.setTotalWins();
            pl.setWinStreak(false);
            pl.setBpTotal(bp, false);

            BattleTower.getInstance().getSql().updatePlayerData(p.getUniqueId(), pl);

            ArrayList<Dialogue> dialogues = new ArrayList<>();
            dialogues.add(Dialogue.builder().setName(event.trainer.getName()).setText(MessageConfig.getMessages("messages.battles.npc.on-trainer-win").replace("{amount}",String.valueOf(bp))).build());


            dialogues.add(Dialogue.builder()
                    .setName(event.trainer.getName())
                    .setText(MessageConfig.getMessages("messages.battles.npc.dialogue"))
                    .addChoice(Choice.builder()
                            .setText(MessageConfig.trainerDialogue().get(0))
                            .setHandle(e -> {
                                if (pl.getWinStreak() % Config.bossStreak == 0) {
                                    e.reply(Dialogue.builder().setName(event.trainer.getName()).setText(MessageConfig.getMessages("messages.battle.npc.boss-trainer").replace("{streak}", Utils.ordinal(pl.getWinStreak()))).build());
                                } else {
                                    e.reply(Dialogue.builder().setName(event.trainer.getName()).setText(MessageConfig.trainerDialogue().get(1)).build());
                                }

                                e.setAction(DialogueNextAction.DialogueGuiAction.CLOSE);
                                startBattle(p, pl.getWinStreak());
                            })
                            .build())
                    .addChoice(Choice.builder()
                            .setText(MessageConfig.trainerDialogue().get(2))
                            .setHandle(e -> {
                                //e.reply(Dialogue.builder().setName(event.trainer.getName()).setText(MessageConfig.trainerDialogue().get(3)).build());
                                e.setAction(DialogueNextAction.DialogueGuiAction.CLOSE);
                                this.endBattle(event.player, pl);
                            })
                            .build())
                    .build());

            Dialogue.setPlayerDialogueData(event.player, dialogues, true);
        }
    }

    @SubscribeEvent
    public void onTrainerWon(LostToTrainerEvent event) {
        if(event.trainer.getEntityData().hasKey("BattleTower") && event.trainer.getEntityData().getString("BattleTower").equals(event.player.getUniqueID().toString())) {
            Player p = (Player) event.player;
            PlayerInfo pl = BattleTower.getInstance().getSql().getPlayerData(p.getUniqueId());

            event.trainer.setDead();
            pl.setWinStreak(true);

            ArrayList<Dialogue> dialogues = new ArrayList<>();
            dialogues.add(Dialogue.builder()
                    .setName(event.trainer.getName())
                    .setText(MessageConfig.getMessages("messages.battles.npc.on-trainer-defeat"))
                    .addChoice(Choice.builder()
                            .setText("Goodbye!")
                            .setHandle(e -> {
                                e.setAction(DialogueNextAction.DialogueGuiAction.CLOSE);
                                this.endBattle(event.player, pl);
                            })
                            .build())
                    .build());

            Dialogue.setPlayerDialogueData(event.player, dialogues, true);
        }
    }

    @SubscribeEvent
    public void onBattleStarted(BattleStartedEvent event) {
        if (event.participant1[0].getEntity() instanceof NPCTrainer) {
            NPCTrainer trainer = (NPCTrainer)event.participant1[0].getEntity();
            EntityPlayerMP player = (EntityPlayerMP)event.participant2[0].getEntity();
            this.battleStartHelper(event, trainer, player);
        } else if (event.participant2[0].getEntity() instanceof NPCTrainer) {
            NPCTrainer trainer = (NPCTrainer)event.participant2[0].getEntity();
            EntityPlayerMP player = (EntityPlayerMP)event.participant1[0].getEntity();
            this.battleStartHelper(event, trainer, player);
        }
    }

    private void battleStartHelper(BattleStartedEvent event, NPCTrainer trainer, EntityPlayerMP player) {
        if(trainer.getEntityData().hasKey("BattleTower")) {
            if (!trainer.getEntityData().getString("BattleTower").equals(player.getUniqueID().toString())) {
                Chat.sendMessage((Player)player, "&cYou cannot battle this trainer!");
                event.setCanceled(true);
            } else {
                System.out.println("Trainer battle controller set!");
                trainer.setBattleController(event.bc);
            }
        }
    }

    private void startBattle(Player player, int streak) {
        NPCTrainer trainer = Trainers.getTrainer(player, streak % Config.bossStreak == 0);
        Trainers.spawnTrainer(trainer, player);
        Trainers.startBattle((EntityPlayerMP) player, trainer);
    }

    private void endBattle(EntityPlayerMP player, PlayerInfo pl) {
        System.out.println("Ending battle!");

        BattleControllerBase bc = BattleRegistry.getBattle(player);
        if (bc != null) {
            bc.endBattleWithoutXP();
            BattleRegistry.deRegisterBattle(bc);
        } else {
            Pixelmon.network.sendTo(new ExitBattle(), player);
        }

        BattleTower.getInstance().getSql().updatePlayerData(player.getUniqueID(), pl);

        Arenas arena = BattleTower.getInstance().battleArenas.get(player.getUniqueID());
        arena.setInUse(false);
        BattleTower.getInstance().getSql().updateArena(arena);
        BattleTower.getInstance().battleArenas.remove(player.getUniqueID());

        if(Config.getEntranceLocation() != null) {
            Utils.teleportPlayer((Player) player,Config.getEntranceLocation(),Config.getEntranceRotation());
            Chat.sendMessage((Player) player, MessageConfig.getMessages("messages.battles.teleporting-to-entrance"));
        }
    }
}

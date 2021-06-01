package com.m1zark.battletower.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.m1zark.battletower.BattleTower;
import com.m1zark.battletower.config.Config;
import com.m1zark.battletower.data.Arenas;
import com.m1zark.battletower.utils.Utils;
import com.m1zark.m1utilities.api.Chat;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.battles.rules.clauses.BattleClauseRegistry;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import com.pixelmonmod.pixelmon.entities.npcs.registry.NPCRegistryVillagers;
import lombok.Getter;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BT implements CommandExecutor {
    @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        List<Text> clauses = Lists.newArrayList(Text.of(Chat.embedColours("&7Battle Clauses: ")));
        Config.clauses.forEach(clause -> {
            Text text = Text.builder().append(Text.of(Chat.embedColours("&7[&b"+BattleClauseRegistry.getClauseRegistry().getClause(clause).getLocalizedName()+"&7] "))).onHover(TextActions.showText(Text.of(Chat.embedColours("&7" + BattleClauseRegistry.getClauseRegistry().getClause(clause).getDescription())))).build();
            clauses.add(text);
        });

        Chat.sendMessage(src, "&7------------------ &aBattle Tower Rules &7-------------------");
        Chat.sendMessage(src, "&7Number of Pok\u00E9mon: &b" + Config.numberPokemon);
        Chat.sendMessage(src, "&7Level Cap: &b" + Config.levelCap);
        Chat.sendMessage(src, Text.join(clauses));
        Chat.sendMessage(src, "&7Boss Battles: &bEvery " + Utils.ordinal(Config.bossStreak) + " battle");
        Chat.sendMessage(src, "&7Banned Pok\u00E9mon: &b" + StringUtils.join(Config.pokemon,", "));
        Chat.sendMessage(src, "&7Banned Moves: &b" + StringUtils.join(Config.moves,", "));
        Chat.sendMessage(src, "&7Banned Abilities: &b" + StringUtils.join(Config.abilities,", "));
        return CommandResult.success();
    }

    public static class Entrance implements CommandExecutor {
        @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

            Optional<String> action = args.getOne("action");
            if(action.isPresent()) {
                switch (action.get()) {
                    case "set":
                        if(Config.setEntranceLocation(((Player) src).getPosition())) Chat.sendMessage(src, "&7BT entrance has been set at &a" + ((Player) src).getPosition() + "&7.");
                        break;
                    case "delete":
                        Config.setEntranceLocation(null);
                        Chat.sendMessage(src, "&7Location set for BT entrance has been deleted.");
                        break;
                }
            }

            return CommandResult.success();
        }
    }

    public static class CreateNPC implements CommandExecutor {
        @Getter private static List<UUID> adding = Lists.newArrayList();

        @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

            adding.add(((Player) src).getUniqueId());
            Chat.sendMessage(src, "&7Right click a chatting npc to register them as a BT Attendant!");

            return CommandResult.success();
        }
    }

    public static class UnlockArenas implements CommandExecutor {
        @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            List<Arenas> arenas = BattleTower.getInstance().getSql().getArenas().stream().filter(Arenas::isInUse).collect(Collectors.toList());

            arenas.forEach(arena -> {
                arena.setInUse(false);
                BattleTower.getInstance().getSql().updateArena(arena);
            });
            BattleTower.getInstance().battleArenas.clear();

            Chat.sendMessage(src, "&7" + arenas.size() + " arenas reset.");

            return CommandResult.success();
        }
    }
}

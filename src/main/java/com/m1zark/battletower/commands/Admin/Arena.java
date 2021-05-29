package com.m1zark.battletower.commands.Admin;

import com.m1zark.battletower.BattleTower;
import com.m1zark.battletower.config.Config;
import com.m1zark.battletower.data.Arenas;
import com.m1zark.m1utilities.api.Chat;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Optional;

public class Arena implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

        Optional<String> action = args.getOne("action");
        Optional<String> name = args.getOne("name");

        if(action.isPresent() && name.isPresent()) {
            switch (action.get()) {
                case "create":
                    Optional<Arenas> arena = BattleTower.getInstance().getSql().getArenas().stream().filter(a-> a.getId().equalsIgnoreCase(name.get())).findFirst();
                    if(arena.isPresent()) throw new CommandException(Text.of(TextColors.RED, "Arena '" + name.get() + "' already exists."));

                    BattleTower.getInstance().getSql().addArena(new Arenas(name.get(), null, null, null, false));
                    Chat.sendMessage(src, "&7Arena &b" + name.get() + " &7has been successfully created.");

                    break;
                case "delete":
                    if(BattleTower.getInstance().getSql().removeArena(name.get()) == 1) {
                        Chat.sendMessage(src, "&7Arena was successfully deleted.");
                    } else {
                        Chat.sendMessage(src, "&7Unable to delete Arena... either the arena doesn't exist or there was an error.");
                    }

                    break;
            }
        }

        return CommandResult.success();
    }

    public static class Edit implements CommandExecutor {
        @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

            Optional<String> name = args.getOne("name");
            Optional<String> type = args.getOne("type");
            Optional<String> action = args.getOne("action");

            Optional<Arenas> arena = BattleTower.getInstance().getSql().getArenas().stream().filter(a-> a.getId().equalsIgnoreCase(name.get())).findFirst();
            if(!arena.isPresent()) throw new CommandException(Text.of(TextColors.RED, name.get() + " could not be located..."));

            if(type.isPresent() && action.isPresent()) {
                switch (type.get()) {
                    case "playerSpawn":
                        switch (action.get()) {
                            case "set":
                                arena.get().setPlayerSpawn(((Player) src).getPosition());
                                arena.get().setPlayerRotation(((Player) src).getHeadRotation());
                                Chat.sendMessage(src, "&7Player spawn has been set at &a" + ((Player) src).getPosition() + " &7for &b" + arena.get().getId() + "&7.");
                                break;
                            case "delete":
                                arena.get().setPlayerSpawn(null);
                                arena.get().setPlayerRotation(null);
                                Chat.sendMessage(src, "&7Location set for player spawn has been deleted.");
                                break;
                        }
                        break;
                    case "npcSpawn":
                        switch (action.get()) {
                            case "set":
                                arena.get().setTrainerSpawn(((Player) src).getPosition());
                                Chat.sendMessage(src, "&7Trainer spawn has been set at &a" + ((Player) src).getPosition() + " &7for &b" + arena.get().getId() + "&7.");
                                break;
                            case "delete":
                                arena.get().setTrainerSpawn(null);
                                Chat.sendMessage(src, "&7Location set for trainer spawn has been deleted.");
                                break;
                        }
                        break;
                }

                BattleTower.getInstance().getSql().updateArena(arena.get());
            }

            return CommandResult.success();
        }
    }
}

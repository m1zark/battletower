package com.m1zark.battletower.commands.Admin;

import com.m1zark.battletower.BattleTower;
import com.m1zark.m1utilities.api.Chat;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class Reload implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        BattleTower.getInstance().getConfig().reload();
        BattleTower.getInstance().getMsgConfig().reload();
        BattleTower.getInstance().getInvConfig().reload();
        BattleTower.getInstance().getPkmConfig().reload();

        Chat.sendMessage(src, "&7BattleTower configs successfully reloaded.");

        return CommandResult.success();
    }
}

package com.m1zark.battletower.commands.Admin;

import com.m1zark.battletower.BTInfo;
import com.m1zark.battletower.BattleTower;
import com.m1zark.m1utilities.api.Chat;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class BP implements CommandExecutor {
    @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<Player> player = args.getOne("player");
        int quantity = args.getOne("quantity").isPresent() ? args.<Integer>getOne("quantity").get() : 1;

        BattleTower.getInstance().getSql().updateBPTotal(player.get().getUniqueId(), false, quantity);

        if(src instanceof Player) {
            Chat.sendMessage(src, "&7Successfully gave &b" + player.get().getName() + " &d" + quantity + " &7BP!");
        } else {
            BattleTower.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(BTInfo.PREFIX, "Successfully gave ", player.get().getName(), " ", quantity, " BP!")));
        }

        return CommandResult.success();
    }
}

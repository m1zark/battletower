package com.m1zark.battletower.commands;

import com.m1zark.battletower.config.InventoryConfig;
import com.m1zark.battletower.gui.VendorUI;
import com.m1zark.m1utilities.api.Chat;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class Market implements CommandExecutor {
    @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

        Optional<String> oit = args.getOne(Text.of("type"));

        if (oit.isPresent()) {
            if(InventoryConfig.isVendor(oit.get())) throw new CommandException(Text.of(TextColors.RED,"There isn't a vendor with that name."));

            ((Player) src).openInventory((new VendorUI((Player) src, 1, oit.get())).getInventory());
        } else {
			((Player) src).openInventory((new VendorUI((Player) src).getInventory()));
        }

        return CommandResult.success();
    }
}

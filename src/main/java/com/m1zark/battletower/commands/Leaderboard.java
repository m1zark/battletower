package com.m1zark.battletower.commands;

import com.m1zark.battletower.BattleTower;
import com.m1zark.battletower.data.PlayerInfo;
import com.m1zark.m1utilities.api.Chat;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

public class Leaderboard implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

        List<PlayerInfo> players = BattleTower.getInstance().getSql().getAllPlayerData();

        Optional<String> type = args.getOne(Text.of("type"));

        if(type.isPresent()) {
            switch (type.get()) {
                case "wins":
                    players.sort(Comparator.comparing(PlayerInfo::getTotalWins).reversed());
                    break;
                case "streak":
                    players.sort(Comparator.comparing(PlayerInfo::getWinStreak).reversed());
                    break;
            }

            List<Text> leaderboard = new ArrayList<>();
            players.forEach(player -> {
                Text text = Text.of(Chat.embedColours("&b" + getNameFromUUID(player.getPlayer()).get() + " &f\u21E8 &7Wins: &a" + player.getTotalWins() + "     &7Streak: &a" + player.getWinStreak()));
                leaderboard.add(text);
            });

            PaginationList.builder().contents(leaderboard)
                    .title(Text.of(Chat.embedColours("&7BattleTower LeaderBoard: " + (type.get().equalsIgnoreCase("streak") ? "&aHighest Streak" : "&aTotal Wins"))))
                    .build()
                    .sendTo(src);
        }

        return CommandResult.success();
    }

    public static Optional<String> getNameFromUUID(UUID uuid){
        UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        Optional<User> oUser = uss.get(uuid);

        if (oUser.isPresent()){
            String name = oUser.get().getName();
            return Optional.of(name);
        } else {
            return Optional.empty();
        }
    }
}

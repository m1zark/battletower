package com.m1zark.battletower.utils;

import com.m1zark.battletower.BTInfo;
import com.m1zark.battletower.BattleTower;
import com.m1zark.battletower.data.PlayerInfo;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Source;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

public class Placeholders {
    private static PlaceholderService placeholderService;

    public static void register(Object plugin) {
        placeholderService = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
        placeholderService.loadAll(new Placeholders(), plugin).forEach(b -> {
            try {
                b.version(BTInfo.VERSION).author("m1zark").buildAndRegister();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Placeholder(id="bt_bp_total")
    public String bpTotal(@Source Player player) {
        Optional<PlayerInfo> stats = BattleTower.getInstance().getSql().getPlayerData().stream().filter(id -> id.getPlayer().equals(player.getUniqueId())).findFirst();
        return stats.map(playerInfo -> String.valueOf(playerInfo.getBpTotal())).orElse("");
    }

    @Placeholder(id="bt_win_streak")
    public String winStreak(@Source Player player) {
        Optional<PlayerInfo> stats = BattleTower.getInstance().getSql().getPlayerData().stream().filter(id -> id.getPlayer().equals(player.getUniqueId())).findFirst();
        return stats.map(playerInfo -> String.valueOf(playerInfo.getWinStreak())).orElse("");
    }

    @Placeholder(id="bt_total_wins")
    public String totalWins(@Source Player player) {
        Optional<PlayerInfo> stats = BattleTower.getInstance().getSql().getPlayerData().stream().filter(id -> id.getPlayer().equals(player.getUniqueId())).findFirst();
        return stats.map(playerInfo -> String.valueOf(playerInfo.getTotalWins())).orElse("");
    }
}

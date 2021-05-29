package com.m1zark.battletower.listeners;

import com.m1zark.battletower.BattleTower;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class JoinListener {
    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player){
        BattleTower.getInstance().getSql().addPlayerData(player.getUniqueId());
    }
}

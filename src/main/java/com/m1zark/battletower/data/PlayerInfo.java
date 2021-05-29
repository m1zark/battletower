package com.m1zark.battletower.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlayerInfo {
    private final UUID player;
    private final int totalWins;
    private final int winStreak;
    private final int bpTotal;
}

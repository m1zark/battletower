package com.m1zark.battletower.data;

import com.m1zark.battletower.config.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PlayerInfo {
    private final UUID player;
    private int totalWins;
    private int winStreak;
    private int bpTotal;
    private int attempts;
    private Date lastAttempt;

    public void setWinStreak(boolean streakOver) {
        if(streakOver) this.winStreak = 0;
        else this.winStreak++;
    }

    public void setBpTotal(int bp, boolean purchase) {
        if(purchase) this.bpTotal = this.bpTotal - bp;
        else this.bpTotal = this.bpTotal + bp;
    }

    public void setAttempts(boolean reset) {
        if(reset) this.attempts = 0;
        else this.attempts++;
    }

    public void setTotalWins() {
        this.totalWins++;
    }

    public void setLastAttempt(Date newDate) {
        this.lastAttempt = newDate;
    }
}

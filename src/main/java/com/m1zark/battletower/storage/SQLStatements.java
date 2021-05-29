package com.m1zark.battletower.storage;

import com.google.gson.Gson;
import com.m1zark.battletower.data.Arenas;
import com.m1zark.battletower.data.PlayerInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class SQLStatements {
    private String mainTable;
    private String arenaTable;

    private Gson gson = new Gson();

    public SQLStatements(String mainTable, String arenaTable) {
        this.mainTable = mainTable;
        this.arenaTable = arenaTable;
    }

    public void createTables() {
        try {
            try(Connection connection = DataSource.getConnection()) {
                try(PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + this.mainTable + "` (ID INTEGER NOT NULL AUTO_INCREMENT, PlayerUUID CHAR(36), TotalWins Integer, WinStreak Integer, BP_Balance Integer, PRIMARY KEY(ID));")) {
                    statement.executeUpdate();
                }
                try(PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + this.arenaTable + "` (ID INTEGER NOT NULL AUTO_INCREMENT, Name CHAR(36), Arena LONGTEXT, PRIMARY KEY(ID));")) {
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void addPlayerData(UUID uuid) {
        try {
            try(Connection connection = DataSource.getConnection()) {
                try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.mainTable + "` WHERE playerUUID = '" + uuid + "'").executeQuery()) {
                    if(!results.next()) {
                        try(PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + this.mainTable + "` (PlayerUUID, TotalWins, WinStreak, BP_Balance) VALUES (?, ?, ?, ?)")) {
                            statement.setString(1, uuid.toString());
                            statement.setInt(2, 0);
                            statement.setInt(3, 0);
                            statement.setInt(4, 0);
                            statement.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTotalWins(UUID uuid) {
        try {
            try(Connection connection = DataSource.getConnection()) {
                try(PreparedStatement updatePlayer = connection.prepareStatement("UPDATE `" + this.mainTable + "` SET TotalWins = TotalWins + 1 WHERE PlayerUUID = '" + uuid + "'")) {
                    updatePlayer.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateWinStreak(UUID uuid, boolean streakOver) {
        try {
            try(Connection connection = DataSource.getConnection()) {
                if (streakOver) {
                    try(PreparedStatement updatePlayer = connection.prepareStatement("UPDATE `" + this.mainTable + "` SET WinStreak = 0 WHERE PlayerUUID = '" + uuid + "'")) {
                        updatePlayer.executeUpdate();
                    }
                } else {
                    try(PreparedStatement updatePlayer = connection.prepareStatement("UPDATE `" + this.mainTable + "` SET WinStreak = WinStreak + 1 WHERE PlayerUUID = '" + uuid + "'")) {
                        updatePlayer.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBPTotal(UUID uuid, boolean purchase, int amount) {
        try {
            try(Connection connection = DataSource.getConnection()) {
                if (purchase) {
                    try(PreparedStatement updatePlayer = connection.prepareStatement("UPDATE `" + this.mainTable + "` SET BP_Balance = BP_Balance - " + amount + " WHERE PlayerUUID = '" + uuid + "'")) {
                        updatePlayer.executeUpdate();
                    }
                } else {
                    try(PreparedStatement updatePlayer = connection.prepareStatement("UPDATE `" + this.mainTable + "` SET BP_Balance = BP_Balance + " + amount + " WHERE PlayerUUID = '" + uuid + "'")) {
                        updatePlayer.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PlayerInfo> getPlayerData() {
        ArrayList<PlayerInfo> players = new ArrayList<>();
        try {
            try(Connection connection = DataSource.getConnection()) {
                try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.mainTable + "`").executeQuery()) {
                    while(results.next()) {
                        players.add(new PlayerInfo(UUID.fromString(results.getString("PlayerUUID")),results.getInt("TotalWins"),results.getInt("WinStreak"),results.getInt("BP_Balance")));
                    }
                }

                return players;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return players;
        }
    }



    public void addArena(Arenas data) {
        try {
            try(Connection connection = DataSource.getConnection()) {
                try(PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + this.arenaTable + "` (Name,Arena) VALUES (?,?)")) {
                    statement.setString(1, data.getId());
                    statement.setString(2, gson.toJson(data));
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateArena(Arenas data) {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement query = connection.prepareStatement("UPDATE `" + this.arenaTable + "` SET Arena='" + gson.toJson(data) + "' WHERE Name='" + data.getId() + "'")) {
                query.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int removeArena(String name) {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement query = connection.prepareStatement("DELETE FROM `" + this.arenaTable + "` WHERE Name='" + name + "'")) {
                return query.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<Arenas> getArenas() {
        ArrayList<Arenas> arenas = new ArrayList<>();
        try {
            try(Connection connection = DataSource.getConnection()) {
                try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.arenaTable + "`").executeQuery()) {
                    while(results.next()) {
                        arenas.add(gson.fromJson(results.getString("Arena"), Arenas.class));
                    }
                }

                return arenas;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return arenas;
        }
    }
}

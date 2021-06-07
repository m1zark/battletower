package com.m1zark.battletower.storage;

import com.google.gson.Gson;
import com.m1zark.battletower.data.Arenas;
import com.m1zark.battletower.data.PlayerInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
                try(PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + this.mainTable + "` (ID INTEGER NOT NULL AUTO_INCREMENT, PlayerUUID CHAR(36), Data LONGTEXT, PRIMARY KEY(ID));")) {
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
                        try(PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + this.mainTable + "` (PlayerUUID, Data) VALUES (?, ?)")) {
                            statement.setString(1, uuid.toString());
                            statement.setString(2, gson.toJson(new PlayerInfo(uuid,0,0,0,0, new Date())));
                            statement.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerData(UUID uuid, PlayerInfo info) {
        try {
            try(Connection connection = DataSource.getConnection()) {
                try(PreparedStatement updatePlayer = connection.prepareStatement("UPDATE `" + this.mainTable + "` SET Data = '" + gson.toJson(info) + "' WHERE PlayerUUID = '" + uuid + "'")) {
                    updatePlayer.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PlayerInfo getPlayerData(UUID uuid) {
        ArrayList<PlayerInfo> players = new ArrayList<>();
        try {
            try(Connection connection = DataSource.getConnection()) {
                try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.mainTable + "` WHERE PlayerUUID='" + uuid + "'").executeQuery()) {
                    if (results.next()) return gson.fromJson(results.getString("Data"), PlayerInfo.class);

                    return new PlayerInfo(uuid,0,0,0,0, new Date());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<PlayerInfo> getAllPlayerData() {
        ArrayList<PlayerInfo> players = new ArrayList<>();

        try {
            try(Connection connection = DataSource.getConnection()) {
                try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.mainTable + "`").executeQuery()) {
                    while (results.next()) players.add(gson.fromJson(results.getString("Data"), PlayerInfo.class));

                    return players;
                }
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

package es.raxthelag.epichead.controllers;

import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.objects.Home;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.Optional;

public class DataConnection {
    public String host;
    public int port;
    public String dbname;
    public String user;
    public String pass;

    private Connection connection;

    public void setupDatabaseData() {
        host = Main.getInstance().getConfig().getString("MySQL.host");
        port = Main.getInstance().getConfig().getInt("MySQL.port", 3306);
        dbname = Main.getInstance().getConfig().getString("MySQL.db");
        user = Main.getInstance().getConfig().getString("MySQL.user");
        pass = Main.getInstance().getConfig().getString("MySQL.password");
    }

    public DataConnection() {
        setupDatabaseData();
        try {
            connect();
        } catch (SQLException | ClassNotFoundException | UnknownHostException throwables) {
            Bukkit.getLogger().severe("Could not connect to database...");
            throwables.printStackTrace();
            Bukkit.getServer().getPluginManager().disablePlugin(Main.getInstance());
        }
    }

    public void connect() throws SQLException, ClassNotFoundException, UnknownHostException {
        // If connection exist, and it's not closed... do not continue
        if (connection != null && !connection.isClosed()) { return; }

        synchronized (this) {
            InetAddress address = InetAddress.getByName(host);
            host = address.getHostAddress();

            if (connection != null && !connection.isClosed()) { return; }
            Class.forName("com.mysql.jdbc.Driver");
            if (this.pass == null || this.pass.isEmpty()) {
                Bukkit.getLogger().warning("Trying to connect to the database without a password");
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbname + "?autoReconnect=true", user, null);
            } else {
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbname + "?autoReconnect=true", user, pass);
            }
        }

        // We're going to create the tables just in case they don't exist.
        createTables();
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try { connect(); } catch (ClassNotFoundException | UnknownHostException e) { e.printStackTrace(); }
        }
        return connection;
    }

    public void createTables() throws SQLException {
        Statement statement = getConnection().createStatement();
        int econDigits = Main.getInstance().getConfig().getInt("economy.max-digits-allowed", 13);
        int econFractionDigits = Main.getInstance().getConfig().getInt("economy.fraction-digits", 4);

        /* statement.execute("CREATE TABLE IF NOT EXISTS `eh_player` (" +
                "  `id` INT NOT NULL AUTO_INCREMENT," +
                "  `name` VARCHAR(25) NOT NULL," +
                "  `realname` VARCHAR(25) NOT NULL," +
                "  `uuid` VARCHAR(64) NULL," +
                "  `balance` DECIMAL(" + econDigits + "," + econFractionDigits + ") NOT NULL DEFAULT 0," +
                "  PRIMARY KEY (`id`)," +
                "  UNIQUE INDEX `name_UNIQUE` (`name` ASC));"); */

        statement.execute("CREATE TABLE `eh_player` (" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                "  `name` varchar(25) NOT NULL," +
                "  `realname` varchar(25) NOT NULL," +
                "  `uuid` varchar(64) DEFAULT NULL," +
                // "  `balance` decimal(" + econDigits + "," + econFractionDigits + ") NOT NULL DEFAULT 0.0000," +
                String.format("  `balance` decimal(%2d,%2d) NOT NULL DEFAULT 0.0000,", econDigits, econFractionDigits) +
                "  `world` varchar(255) DEFAULT NULL," +
                "  `x` double DEFAULT NULL," +
                "  `y` double DEFAULT NULL," +
                "  `z` double DEFAULT NULL," +
                "  `yaw` float DEFAULT NULL," +
                "  `pitch` float DEFAULT NULL," +
                "  `dl_world` varchar(255) DEFAULT NULL," +
                "  `dl_x` double DEFAULT NULL," +
                "  `dl_y` double DEFAULT NULL," +
                "  `dl_z` double DEFAULT NULL," +
                "  `dl_yaw` float DEFAULT NULL," +
                "  `dl_pitch` float DEFAULT NULL," +
                "  PRIMARY KEY (`id`)," +
                "  UNIQUE INDEX `name_UNIQUE` (`name`))");

        statement.execute("CREATE TABLE `eh_home` (" +
                "  `id` INT NOT NULL AUTO_INCREMENT," +
                "  `playerId` INT NOT NULL," +
                "  `name` VARCHAR(45) NOT NULL," +
                "  `world` VARCHAR(255) NOT NULL," +
                "  `x` DOUBLE NOT NULL," +
                "  `y` DOUBLE NOT NULL," +
                "  `z` DOUBLE NOT NULL," +
                "  `yaw` FLOAT NOT NULL," +
                "  `pitch` FLOAT NOT NULL," +
                "  PRIMARY KEY (`id`)," +
                "  INDEX `eh_home_eh_player_playerId_idx` (`playerId`)," +
                "  CONSTRAINT `eh_home_eh_player_playerId`" +
                "    FOREIGN KEY (`playerId`)" +
                "    REFERENCES `eh_player` (`id`)" +
                "    ON DELETE NO ACTION" +
                "    ON UPDATE NO ACTION);");

        // TODO CREATE TRANSACTION TABLE
    }

    public boolean playerExistInDatabase(EpicPlayer player) throws SQLException {
        String name = player.getPlayerName().toLowerCase();
        boolean result = false;

        try {
            PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM `eh_player` WHERE `name` = ?");
            statement.setString(1, name);
            result = statement.executeQuery().next();
        } finally {
            getConnection().close();
        }

        return result;
    }

    public void createPlayer(EpicPlayer player) throws SQLException {
        try {
            PreparedStatement statement = getConnection().prepareStatement("INSERT IGNORE INTO `eh_player` (`name`, `realname`, `balance`) VALUES (?, ?, ?)");
            statement.setString(1, player.getName().toLowerCase());
            statement.setString(2, player.getPlayerName());
            statement.setBigDecimal(3, player.getBalance());
            statement.executeUpdate();
        } finally {
            getConnection().close();
        }
    }

    public void loadPlayer(EpicPlayer player) throws SQLException {
        try {
            PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM `eh_player` WHERE `name` = ?");
            statement.setString(1, player.getName().toLowerCase());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet != null && resultSet.next()) {
                player.setBalance(resultSet.getBigDecimal("balance"));
                player.setLoaded(true);
            }
        } finally {
            getConnection().close();
        }
    }

    public void loadPlayerHomes(EpicPlayer player) throws SQLException {
        try {
            PreparedStatement statement = getConnection().prepareStatement("SELECT `id`, `name`, `world`, `x`, `y`, `z`, `yaw`, `pitch` FROM `eh_home` WHERE `playerId` = (SELECT eh_player.id FROM eh_player WHERE eh_player.name = ?)");
            statement.setString(1, player.getName().toLowerCase());

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                player.addHome(
                        new Home(resultSet.getInt("id"),
                                resultSet.getString("name"),
                                new Location(Bukkit.getWorld(resultSet.getString("world")), resultSet.getDouble("x"), resultSet.getDouble("y"), resultSet.getDouble("z"))
                        )
                );
            }
            player.setHomesLoaded(true);
        } finally {
            getConnection().close();
        }
    }

    public void savePlayer(EpicPlayer player) throws SQLException {
        try {
            Location lastSeenLoc = player.getLastSeenLocation();
            Location deathLoc = player.getDeathLocation();
            PreparedStatement statement = getConnection().prepareStatement(
                    "INSERT INTO `eh_player`" +
                            " (`name`, `realname`, `uuid`, `balance`, `world`, `x`, `y`, `z`, `yaw`, `pitch`, `dl_world`, `dl_x`, `dl_y`, `dl_z`, `dl_yaw`, `dl_pitch`)" +
                            " VALUES (?, ?,        ?,      ?,         ?,       ?,   ?,   ?,   ?,     ?,       ?,          ?,      ?,      ?,      ?,        ?)" +
                            //        1  2         3       4          5         6   7    8    9      10       11          12      13      14      15        16
                            " ON DUPLICATE KEY UPDATE `realname` = VALUES(`realname`), `uuid`= VALUES(`uuid`), `balance` = VALUES(`balance`), " +
                            "  `world` = VALUES(`world`), `x` = VALUES(`x`), `y` = VALUES(`y`), `z` = VALUES(`z`), `yaw` = VALUES(`yaw`), `pitch` = VALUES(`pitch`), " +
                            "  `dl_world` = VALUES(`dl_world`), `dl_x` = VALUES(`dl_x`), `dl_y` = VALUES(`dl_y`), `dl_z` = VALUES(`dl_z`), `dl_yaw` = VALUES(`dl_yaw`), `dl_pitch` = VALUES(`dl_pitch`);"
            );
            statement.setString(1, player.getName().toLowerCase());
            statement.setString(2, player.getPlayerName());
            statement.setString(3, (player.getUniqueId() != null) ? player.getUniqueId().toString() : null);
            statement.setBigDecimal(4, player.getBalance());

            // Last seen location
            statement.setString(5, (lastSeenLoc != null && lastSeenLoc.getWorld() != null) ? lastSeenLoc.getWorld().getName() : null);
            if (lastSeenLoc != null) {
                statement.setDouble(6, lastSeenLoc.getX());
                statement.setDouble(7, lastSeenLoc.getY());
                statement.setDouble(8, lastSeenLoc.getZ());
                statement.setFloat(9, lastSeenLoc.getYaw());
                statement.setFloat(10, lastSeenLoc.getPitch());
            } else {
                statement.setNull(6, Types.DOUBLE); statement.setNull(7, Types.DOUBLE); statement.setNull(8, Types.DOUBLE);
                statement.setNull(9, Types.FLOAT); statement.setNull(10, Types.FLOAT);
            }

            // Death location
            statement.setString(11, (deathLoc != null && deathLoc.getWorld() != null) ? deathLoc.getWorld().getName() : null);
            if (deathLoc != null) {
                statement.setDouble(12, deathLoc.getX());
                statement.setDouble(13, deathLoc.getY());
                statement.setDouble(14, deathLoc.getZ());
                statement.setFloat(15, deathLoc.getYaw());
                statement.setFloat(16, deathLoc.getPitch());
            } else {
                statement.setNull(12, Types.DOUBLE); statement.setNull(13, Types.DOUBLE); statement.setNull(14, Types.DOUBLE);
                statement.setNull(15, Types.FLOAT); statement.setNull(16, Types.FLOAT);
            }

            statement.executeUpdate();
        } finally {
            getConnection().close();
        }
    }

    public void savePlayerHomes(EpicPlayer player) throws SQLException {
        try {
            // if (!player.isLoaded()) return;
            deleteUnusedHomes(player);
            if (player.getHomes().size() == 0) return;

            StringBuilder sql = new StringBuilder("INSERT INTO `eh_home` (`id`, `playerId`, `name`, `world`, `x`, `y`, `z`, `yaw`, `pitch`) VALUES");
            for (int i = 0; i < player.getHomes().size(); i++) {
                sql.append(" (?, (SELECT `id` FROM `eh_player` WHERE `name` = ?), ?, ?, ?, ?, ?, ?, ?)");
                sql.append((i == player.getHomes().size()-1) ? " ": ", ");
            }
            sql.append("ON DUPLICATE KEY UPDATE" +
                    "    `world` = VALUES(`world`)," +
                    "    `x` = VALUES(`x`)," +
                    "    `y` = VALUES(`y`)," +
                    "    `z` = VALUES(`z`)," +
                    "    `yaw` = VALUES(`yaw`)," +
                    "    `pitch` = VALUES(`pitch`);");

            PreparedStatement statement = getConnection().prepareStatement(sql.toString());
            for (int i = 0; i < player.getHomes().size(); i++) {
                int parIndex = (i+1)*9;
                Home home = player.getHomes().get(i);
                Location loc = home.getLocation();
                statement.setFloat(parIndex--, loc.getPitch());
                statement.setFloat(parIndex--, loc.getYaw());
                statement.setDouble(parIndex--, loc.getZ());
                statement.setDouble(parIndex--, loc.getY());
                statement.setDouble(parIndex--, loc.getX());
                statement.setString(parIndex--, (loc.getWorld() != null) ? loc.getWorld().getName() : null);
                statement.setString(parIndex--, home.getName());
                statement.setString(parIndex--, player.getName().toLowerCase());
                if (home.getId() != null) {
                    statement.setInt(parIndex, home.getId());
                } else statement.setNull(parIndex, Types.INTEGER);
            }
            /* statement.setString(1, player.getName().toLowerCase());
            statement.setString(2, player.getPlayerName());
            statement.setString(3, (player.getUniqueId() != null) ? player.getUniqueId().toString() : null);
            statement.setBigDecimal(4, player.getBalance()); */

            statement.executeUpdate();
        } finally {
            getConnection().close();
        }
    }

    private void deleteUnusedHomes(EpicPlayer player) throws SQLException {
        try {
            StringBuilder sql = new StringBuilder("DELETE FROM `eh_home` WHERE `playerId` = (SELECT `id` FROM eh_player WHERE `name` = ?) ");
            if (player.getHomes().size() > 0) {
                sql.append("AND `name` NOT IN (");
                for (int i = 0; i < player.getHomes().size(); i++) {
                    sql.append( (i == 0) ? "?" : ",?" );
                }
                sql.append(")");
            }
            PreparedStatement statement = getConnection().prepareStatement(sql.toString());
            statement.setString(1, player.getName().toLowerCase());
            for (int i = 0; i < player.getHomes().size(); i++) {
                statement.setString(i+2, player.getHomes().get(i).getName());
            }
            statement.executeUpdate();
        } finally {
            getConnection().close();
        }
    }
}

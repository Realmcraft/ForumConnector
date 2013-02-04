package me.SgtMjrME;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.bukkit.Bukkit;
 
public class MySQLDatabase {
    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final String database;
    private final String url;
    private Connection connection;
 
    public final String getHost() {
        return host;
    }
 
    public final String getPort() {
        return port;
    }
 
    public final String getUsername() {
        return username;
    }
 
    public final String getPassword() {
        return password;
    }
 
    public final String getDatabase() {
        return database;
    }
 
    public final String getUrl() {
        return url;
    }
 
    public final Connection getConnection() {
        return connection;
    }
 
    public MySQLDatabase(final String host, final String port, final String username,
            final String password, final String database) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
        url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true";
    }
 
    public Connection open() throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Bukkit.getLogger().info("Connecting to " + url + " using " + username + " " + password);
            connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (final Exception e) {
            throw e;
        }
    }
 
    public void close() throws Exception {
        try {
            if (connection != null)
                connection.close();
        } catch (final Exception e) {
            throw e;
        }
    }
 
    public ResultSet query(final String sql) throws Exception {
        Statement statement;
        ResultSet result;
        try {
            statement = connection.createStatement();
            result = statement.executeQuery(sql);
            return result;
        } catch (final Exception e) {
            throw e;
        }
    }
 
    public int update(final String sql) throws Exception {
        Statement statement;
        int result;
        try {
            statement = connection.createStatement();
            result = statement.executeUpdate(sql);
            return result;
        } catch (final Exception e) {
            throw e;
        }
    }
    
    public boolean create(final String sql) throws Exception {
    	Statement statement;
        boolean result;
        try {
            statement = connection.createStatement();
            result = statement.execute(sql);
            return result;
        } catch (final Exception e) {
            throw e;
        }
    }
}
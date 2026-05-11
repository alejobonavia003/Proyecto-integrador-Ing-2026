package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        DBConfigSingleton config = DBConfigSingleton.getInstance();
        return DriverManager.getConnection(
                config.getDbUrl(),
                config.getUser(),
                config.getPass()
        );
    }
}
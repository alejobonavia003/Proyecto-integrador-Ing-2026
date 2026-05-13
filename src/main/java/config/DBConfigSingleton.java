package config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.Statement;

public class DBConfigSingleton {

    private static DBConfigSingleton instance;

    private final String driver;
    private final String dbUrl;
    private final String user;
    private final String pass;

    private DBConfigSingleton() {
        this.driver = "org.sqlite.JDBC";
        this.dbUrl = "jdbc:sqlite:./db/dev.db";
        this.user = "";
        this.pass = "";

        try {
            Class.forName(driver);
            initDatabase();
            System.out.println("Creando base de datos");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se pudo cargar el driver JDBC: " + driver, e);
        }
    }

    private void initDatabase() {
        // 1. Leer el archivo schema.sql de resources
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
            if (is == null) {
                System.out.println("No se encontró schema.sql, saltando inicialización.");
                return;
            }

            

            String sql = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));

            // 2. Ejecutar el SQL
            try (Connection conn = DriverManager.getConnection(dbUrl);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
                
                // SQLite permite ejecutar múltiples sentencias separadas por ;
                stmt.executeUpdate(sql);
                System.out.println("Base de datos inicializada con éxito.");
                
            }
        } catch (Exception e) {
            System.err.println("ERROR al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized DBConfigSingleton getInstance() {
        if (instance == null) {
            instance = new DBConfigSingleton();
        }
        return instance;
    }


    public void openConnection() {
        org.javalite.activejdbc.Base.open(driver, dbUrl, user, pass);
        //initDatabase();
    }

    public void createConecction(){
        initDatabase();
    }

    public void closeConnection() {
        org.javalite.activejdbc.Base.close();
    }

    public String getDriver() {
        return driver;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }
}
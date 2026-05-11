package config;

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
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se pudo cargar el driver JDBC: " + driver, e);
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
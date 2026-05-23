package app;

import config.DBConfigSingleton;
import controllers.AuthController;
import controllers.DashboardController;
import controllers.ErrorController;
import controllers.SecurityController;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.halt;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.logging.Logger;
import org.javalite.activejdbc.Base; // <-- IMPORTANTE: Importar Base de ActiveJDBC

/**
 * Clase principal de la aplicación Spark.
 */
public class App {

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {

        port(8080);
        staticFiles.location("/public");

        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();
        MustacheTemplateEngine engine = new MustacheTemplateEngine();

        // -------------------------------------------------------
        // Filtro BEFORE - Infraestructura (Apertura de BD)
        // -------------------------------------------------------
        before((req, res) -> {
            try {
                // SOLUCIÓN AL ERROR: Solo abrimos si NO hay una conexión en este hilo
                if (!Base.hasConnection()) {
                    dbConfig.openConnection();
                    logger.info("Conexión abierta -> " + req.url());
                }
            } catch (Exception e) {
                logger.severe("Error al abrir conexión con la base de datos: " + e.getMessage());
                halt(500, "Error interno del servidor: no se pudo conectar a la base de datos.");
            }
        });

        // -------------------------------------------------------
        // Filtro AFTER - Infraestructura (Cierre de BD)
        // -------------------------------------------------------
        after((req, res) -> {
            try {
                // SOLUCIÓN AL ERROR: Solo intentamos cerrar si efectivamente hay una conexión
                if (Base.hasConnection()) {
                    dbConfig.closeConnection();
                }
            } catch (Exception e) {
                logger.severe("Error al cerrar conexión con la base de datos: " + e.getMessage());
            }
        });

        // -------------------------------------------------------
        // 1. Inicialización de la Seguridad y Roles
        // DEBE IR AQUÍ: Intercepta las solicitudes antes de que los controladores las procesen
        // -------------------------------------------------------
        SecurityController.init();

        // -------------------------------------------------------
        // 2. Registro de controladores de negocio
        // -------------------------------------------------------
        AuthController.init(engine);
        DashboardController.init(engine);
        ErrorController.init(engine);
    }
}
package app;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
import static spark.Spark.halt;

import config.DBConfigSingleton;
import controllers.AuthController;
import controllers.ErrorController;
import controllers.ProfesorController;
import spark.template.mustache.MustacheTemplateEngine;

/**
 * Clase principal de la aplicación Spark.
 * Se encarga de:
 * - configurar el puerto
 * - definir recursos estáticos
 * - abrir y cerrar la conexión a la base de datos
 * - registrar los controladores
 */
public class App {

    /**
     * Método principal.
     * Arranca la aplicación web y registra toda la configuración inicial.
     */
    public static void main(String[] args) {

        // Puerto donde va a correr la aplicación
        port(8080);

        // Carpeta de archivos estáticos: css, js, imágenes, etc.
        staticFiles.location("/public");

        // Instancia única de la configuración de la base de datos
        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

        // Motor de plantillas Mustache
        MustacheTemplateEngine engine = new MustacheTemplateEngine();

        // -------------------------------------------------------
        // Filtro BEFORE
        // Se ejecuta antes de cada request HTTP.
        // Abre la conexión a la base de datos.
        // -------------------------------------------------------
        before((req, res) -> {
            try {
                dbConfig.openConnection();
                System.out.println("DEBUG: Conexión abierta -> " + req.url());
            } catch (Exception e) {
                System.err.println("Error al abrir conexión con la base de datos: " + e.getMessage());
                halt(500, "Error interno del servidor: no se pudo conectar a la base de datos.");
            }
        });

        // -------------------------------------------------------
        // Filtro AFTER
        // Se ejecuta después de cada request HTTP.
        // Cierra la conexión a la base de datos.
        // -------------------------------------------------------
        after((req, res) -> {
            try {
                dbConfig.closeConnection();
            } catch (Exception e) {
                System.err.println("Error al cerrar conexión con la base de datos: " + e.getMessage());
            }
        });

        // -------------------------------------------------------
        // Registro de controladores
        // Cada controlador define sus rutas.
        // -------------------------------------------------------
        AuthController.init(engine);
        ProfesorController.init(engine);
        ErrorController.init(engine);
    }
}
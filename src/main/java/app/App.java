package app;

import java.util.logging.Logger;

import org.javalite.activejdbc.Base;

import config.DBConfigSingleton;
import controllers.AuthController;
import controllers.DashboardController;
import controllers.ErrorController;
import controllers.MateriaController;
import controllers.SecurityController;
import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get; // <-- IMPORTANTE: Importar Base de ActiveJDBC
import static spark.Spark.halt;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;
import spark.template.mustache.MustacheTemplateEngine;

/**
 * Clase principal de la aplicación Spark.
 */
public class App {
    

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        // 1. Formatear el Logger de Java para que solo muestre: [NIVEL] Mensaje
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%4$s] %5$s%n");

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

        // ==========================================
        // ENDPOINTS DE MATERIAS
        // ==========================================

        // SQA: Solo los usuarios con rol Administrador tienen acceso a modificación
        before("/materias/nueva", (req, res) -> {
            // Se asume que guardas el usuario en "currentUser" al hacer login en AuthController
            models.User currentUser = req.session().attribute("currentUser");
            
            if (currentUser == null) {
                res.redirect("/login");
                halt();
            }
            
            // Verificamos usando tu modelo Role existente
            models.Role rol = currentUser.parent(models.Role.class);
            if (rol == null || (!"Administrador".equalsIgnoreCase(rol.getString("name")) && !"Admin".equalsIgnoreCase(rol.getString("name")))) {
                halt(403, "Acceso Denegado: Solo el Administrador puede modificar dependencias académicas.");
            }
        });

        /// ==========================================
        // ENDPOINTS DE MATERIAS
        // ==========================================

       
        get("/admin/courses", MateriaController::getMaterias, engine);
        get("/materias", MateriaController::getMaterias, engine); // Opcional: mantener la ruta original

        // Rutas existentes
        get("/admin/courses/create", MateriaController::mostrarFormulario, new MustacheTemplateEngine());
        post("/materias/nueva", MateriaController::crearMateria, engine);
        get("/materias/:id/correlativas", MateriaController::getCorrelativas, engine);
        
        // POST para procesar el formulario de correlatividades múltiple
        post("/materias/:id/correlativas", MateriaController::asignarCorrelativas, engine);
    }
}
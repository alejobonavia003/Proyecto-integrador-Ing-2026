package config;

import controller.AuthController;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

public class Routes {

    public static void configurarRutas() {
        MustacheTemplateEngine engine = new MustacheTemplateEngine();

        // --- Rutas de Autenticación y Generales ---
        get("/", AuthController::mostrarLogin, engine);
        post("/login", AuthController::procesarLogin, engine);
        get("/logout", AuthController::procesarLogout);
        get("/dashboard", AuthController::mostrarDashboard, engine);
        get("/error", AuthController::mostrarError, engine);

        // --- Rutas de Usuario ---
        //get("/user/create", UsuarioController::mostrarFormulario, engine);
        //get("/user/new", UsuarioController::mostrarFormularioNuevo, engine);
        //post("/user/new", UsuarioController::procesarNuevoUsuario);
        //post("/add_users", UsuarioController::apiRegistrarUsuario);

        // --- Rutas de Profesor ---
        //get("/profesor", ProfesorController::mostrarPanel, engine);
        //get("/alta-profesor", ProfesorController::mostrarAlta, engine);
        //post("/profesor", ProfesorController::procesarAlta);
        //get("/listar-profesores", ProfesorController::listarProfesores, engine);
    }
}
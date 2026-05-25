package controllers;

import services.UserService;
import spark.ModelAndView;
import spark.Request;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static spark.Spark.get;

/**
 * Controlador encargado de la gestión
 * de usuarios.
 */
public class UserController {

    private static final Logger logger =
            Logger.getLogger(UserController.class.getName());

    /**
     * Servicio de usuarios.
     */
    private static final UserService userService =
            new UserService();

    public static void init(
            MustacheTemplateEngine engine
    ) {

        /**
         * =====================================================
         * LISTADO DE USUARIOS
         * SOLO ADMIN
         * =====================================================
         */
        get("/admin/users", (req, res) -> {

            if (!isLoggedIn(req)) {

                logger.warning(
                        "Intento de acceso sin sesión al listado de usuarios."
                );

                res.redirect(
                        "/error?type=AuthError&message=Debes iniciar sesión."
                );

                return null;
            }

            logger.info(
                    "Cargando listado de usuarios."
            );

            Map<String, Object> model =
                    new HashMap<>();

            model.put(
                    "users",
                    userService.getAllUsersView()
            );

            model.put(
                    "username",
                    req.session().attribute("username")
            );

            model.put(
                    "role",
                    req.session().attribute("user_role")
            );

            model.put(
                    "userCount",
                    userService.getAllUsersView().size()
            );

            return engine.render(
                    new ModelAndView(
                            model,
                            "users.mustache"
                    )
            );
        });
    }

    /**
     * Helper para validar sesión.
     */
    private static boolean isLoggedIn(
            Request req
    ) {

        Boolean loggedIn =
                req.session().attribute("loggedIn");

        return loggedIn != null && loggedIn;
    }
}
package controllers;

import models.User;
import services.AuthService;
import spark.ModelAndView;
import spark.Request;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.logging.Logger;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static spark.Spark.get;
import static spark.Spark.post;

//TODO: agregar un logger para remplazar los sout 
/**
 * Controlador de autenticacion + rutas
 * esta clase interactua directamente con las rutas enviadas desde el navegador
 * puede renderizar una plantilla mustache 
 * o llamar a un servicio para efectuar alguna logica  
 */
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());

    //nesesitamos prender un servicio de autenticacion para poder usar sus metodos (prender=crear clase de ese tipo para poder usar sus metodos) 
    private static final AuthService authService = new AuthService();
   

    /**
     * metodo init
     * este metodo se llama en el app.java, se va a quedar escuchando las rutas y va a saltar cuando se solicite
     * le tenemos que pasar un engine: esto es para no tener que crear un engine diferente por cada controlador 
     * se crea un solo engine en app y se le pasa a todos los controladores (inyeccion de dependecias)
     * @param engine objeto de tipo MustacheTemplateEngine
     */
    public static void init(MustacheTemplateEngine engine) {

        // -------------------------------------------------------
        // RUTAS DE SPARK
        // toman un string con la ruta y una funcion lamda que se ejecuta cuando el navegador manda a esa ruta con tal metodo
        // las funciones lamda tienen que tener dos parametros uno para el cuerpo de la informacion que le llega y 
        // otra para la respuesta que se envia al navegador 
        // -------------------------------------------------------

        /**
         * renderiza la plantilla de login
         * la plantilla de login contiene 2 tipos de mensajes
         * successMessage y errorMessage
         */
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            
            // Capturamos el error que viene en la URL si es que existe
            String error = req.queryParams("error");
            if (error != null) {
                model.put("errorMessage", error); 
            }
            
            // Capturamos el mensaje de éxito si es que existe
            String success = req.queryParams("successMessage");
            if (success != null) {
                model.put("successMessage", success);
            }

            return new ModelAndView(model, "login.mustache");
        }, engine);

        /**
         * renderiza la plantilla para registrarce
         */
        get("/user/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            
            // Capturamos el error que viene en la URL si es que existe
            String error = req.queryParams("error");
            if (error != null) {
                model.put("errorMessage", error); 
            }
            
            // Capturamos el mensaje de éxito si es que existe
            String success = req.queryParams("message");
            if (success != null) {
                model.put("successMessage", success);
            }

            return new ModelAndView(model, "user_form.mustache");
        }, engine);

        //TODO: el dashboard no deveria estar en el controlador de autenticacion
        /**
         * renderiza la plantilla de el panel 
         */
        get("/dashboard", (req, res) -> {

            //si el usuario no esta logeado no puede entrar al panel
            if (!isLoggedIn(req)) {
                res.redirect("/?error=Debes iniciar sesión para acceder a esta página.");
                return null;
            }

            //creamos un modelo que contenga la informacion nesesaria para el dashboard
            Map<String, Object> model = new HashMap<>();

            //le agregamos al controlador el nombre 
            model.put("username", req.session().attribute("currentUserUsername"));
            //creamos la vista con la informacion del modelo integrada
            return new ModelAndView(model, "dashboard.mustache");
        }, engine);

        /**
         * metodo para cerrar cecion 
         */
        get("/logout", (req, res) -> {
            req.session().invalidate();//invalidamos la seccion del cuerpo 
            res.redirect("/");//redireccionamos al login 
            return null;// no retornamos nada por que la ruta / se va a encargar
        });

        /**
         * se entra a esta ruta cuando se envia el formulario post de registro de usuarios 
         * llega con la informacion cargada por el usuario en el marametro req
         */
        post("/user/new", (req, res) -> {
            try {

                //TODO: el usuario tendria que iniciar seccion y registrarce con su dni
                //extraemos el nombre y la contraceña 
                String name = req.queryParams("name");
                String password = req.queryParams("password");


                //llamamos al servicio de registro
                authService.registerUser(name, password);

                //esto muestra el mensaje en verde arriba
                res.redirect("/user/create?message=Cuenta creada exitosamente para " + name + "!");
                return "";
            } catch (IllegalArgumentException e) {
                
                res.redirect("/user/create?error=" + e.getMessage());
                return "";
            } catch (Exception e) {
                res.redirect("/user/create?error=Error interno al crear la cuenta." + e.getMessage());
                return "";
            }
        });

        /**
         * cuando alguien rellena el formulario de login y lo manda entra aca
         */
        post("/login", (req, res) -> {

            try {//bloque try pra captar errores
                //rescatamos del cuerpo lo que cargo el usuario 
                String username = req.queryParams("username");
                String password = req.queryParams("password");


                //llamamos al servicio de login que se va a encargar de ver si son las credenciales validas
                //si el usuario es verificado correctamete llegara un objeto con la informacion del usuario sino null
                Optional<User> userOpt = authService.login(username, password);

                // -------------------------------------------------------
                // Obketo tipo Optional<t> (objeto que puede existir o no)
                // es un objeto que puede ser de tipo t o null
                //isPresent() devuelve true si el objeto no es null
                // -------------------------------------------------------

                // LÍNEA TEMPORAL PARA TEST:
                //if(true) throw new RuntimeException("ERROR DE PRUEBA: El motor de autenticación está en mantenimiento.");

                //si llega la informacion del usuario lo dejamos acceder al sistema con su rol
                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    /**
                     * agregamos a los atributos del cuerpo
                     * nombre del usuario 
                     * id del usuario
                     * un atributo que dice que esta logeado 
                     * podria estar el rol aca o no? 
                     */
                    req.session(true).attribute("currentUserUsername", user.getName());
                    req.session().attribute("userId", user.getId());
                    req.session().attribute("loggedIn", true);

                    //una vez que seteamos la seccion deveria de alcanzar solo con redirigir al dashboard
                    res.redirect("/dashboard");
                    return null;
                }

                //si no entra al if es que fallo en las credenciales
                res.redirect("/?error=Usuario o contrasena incorrectos");//deveria de alcanzar con redireccionar
                return null;

            } catch (IllegalArgumentException e) {
                // ERROR DE VALIDACIÓN: Campo vacío, etc. Volvemos al login.
                res.redirect("/?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
                return null;
            } catch (Exception e) {
                // ERROR TÉCNICO/INTERNO: Algo explotó (DB, red, etc.). 
                // Mandamos a la página de error dedicada que creaste.
                res.redirect("/error?type=InternalError&message=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
                return null;
            }
        }, engine);


        /**
         * Ruta para procesar la creación de usuarios desde un formulario web.
         * Aplica el patrón Post-Redirect-Get (PRG) para mantener la consistencia de la UI.
         */
        post("/add_users", (req, res) -> {
            // 1. No establecemos res.type("application/json") porque vamos a redirigir al navegador,
            // no a devolver un cuerpo de datos JSON.

            try {
                // 2. Rescatamos los datos del formulario (body-params)
                String name = req.queryParams("name");
                String password = req.queryParams("password");

                // 3. Delegamos la lógica de negocio al servicio. 
                // Si el usuario ya existe o los datos son inválidos, el servicio lanzará una excepción.
                User user = authService.registerUser(name, password);

                // 4. ÉXITO: Redirigimos a la vista de creación con un mensaje de éxito.
                // Usamos URLEncoder para que el mensaje sea seguro en la URL (manejo de espacios y caracteres).
                String successMsg = java.net.URLEncoder.encode(
                    "Usuario '" + user.getName() + "' registrado con éxito.", 
                    "UTF-8"
                );
                
                res.redirect("/user/create?message=" + successMsg);
                return null; // Importante: retornar null para finalizar el ciclo de Spark en esta ruta.

            } catch (IllegalArgumentException e) {
                // 5. ERROR DE VALIDACIÓN: El usuario puso datos mal (ej. nombre duplicado o vacío).
                // Redirigimos de vuelta al formulario para que intente de nuevo.
                String errorMsg = java.net.URLEncoder.encode(e.getMessage(), "UTF-8");
                
                res.redirect("/user/create?error=" + errorMsg);
                return null;

            } catch (Exception e) {
                // 6. ERROR TÉCNICO: Fallo de base de datos u otro error no controlado.
                // Mandamos al usuario a la página de error global que creamos antes.
                String criticalError = java.net.URLEncoder.encode(
                    "Error interno al registrar usuario: " + e.getMessage(), 
                    "UTF-8"
                );
                
                res.redirect("/error?type=RegistrationError&message=" + criticalError);
                return null;
            }
        });

    }

    private static boolean isLoggedIn(Request req) {
        // Si la sesión no existe, attribute() devuelve null. 
        // Al mapear a Boolean, evitamos errores si es nulo.
        Boolean loggedIn = req.session().attribute("loggedIn");
        return loggedIn != null && loggedIn;
    }
}
# Guía de Uso: Registro de Eventos (Logging)

Para el rastreo de errores y monitoreo del sistema, se utiliza la API estándar `java.util.logging`. Esto reemplaza el uso de `System.out.println()`, permitiendo categorizar la información por importancia.

## 1. Niveles de Log

Los mensajes se deben clasificar según su gravedad:

- **INFO:** Mensajes informativos de rutina (ej: "Conexión establecida", "Usuario autenticado").
  logger.info("Conexion abierta: " + dbUrl);
- **WARNING:** Situaciones inesperadas que no detienen el sistema (ej: "Intento de acceso fallido").
- **SEVERE:** Errores críticos que rompen una funcionalidad (ej: "Error al conectar a la DB", "Archivo no encontrado").

## 2. Implementación en una Clase

Para usar el logger en cualquier clase del proyecto, seguí estos pasos:

### A. Declaración

Agregá la constante al inicio de la clase:

```java
import java.util.logging.Logger;
import java.util.logging.Level;

public class MiServicio {
    private static final Logger logger = Logger.getLogger(MiServicio.class.getName());
    logger.info("Conexion abierta: " + dbUrl);
    logger.warning("Conexion abierta: " + dbUrl);
    logger.severe("Conexion abierta: " + dbUrl);
}
```

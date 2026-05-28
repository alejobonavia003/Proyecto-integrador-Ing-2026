# Arquitectura

## Estructura general

Routes Spark
→ Controllers
→ Services
→ Models ActiveJDBC
→ SQLite

## Controllers

Responsabilidades:

- Manejar requests HTTP
- Validar parámetros básicos
- Renderizar vistas Mustache
- Redireccionar

NO deben:

- Tener lógica de negocio compleja
- Hacer consultas complejas a modelos
- Duplicar validaciones

## Services

Responsabilidades:

- Lógica de negocio
- Validaciones
- Consultas a modelos
- Reglas académicas
- Manejo de correlatividades

## DAO

El proyecto NO utiliza DAO.
Los Services interactúan directamente con ActiveJDBC.

## Vistas

Se usa Mustache.

Los controllers pueden renderizar vistas directamente.

## diagrama de arquitectura

```mermaid
flowchart TD

    subgraph Cliente
        Browser["Cliente (Browser)"]
    end

    subgraph Servidor
        subgraph Presentacion
            Controllers["Controllers (Spark Routes)"]
            Views["Views (Mustache)"]
        end

        subgraph Logica
            Services["Services"]
        end

        subgraph Modelo
            Models["Models (Usuario, Materia, etc.)"]
        end

        subgraph Datos
            DBConn["DB Connection"]
        end
    end

    DB[(sqlite)]

    Browser --> Controllers
    Controllers --> Services
    Controllers --> Views

    Services --> DBConn
    Services --> Models

    DBConn --> DB
```

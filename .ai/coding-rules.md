# Reglas de código

## Java

- Mantener métodos cortos
- Evitar lógica pesada en controllers
- Usar services para lógica reutilizable

## Controllers

- Un endpoint por responsabilidad
- No repetir validaciones
- Mantener coordinación HTTP solamente
- los controller pueden trabajar los engine de mustache

## Services

- Centralizar lógica
- Encapsular consultas complejas
- Evitar duplicación

## Logs

Usar Logger en controllers y services.

## Objetivo del proyecto

Priorizar simplicidad y mantenibilidad
por encima de arquitecturas enterprise complejas.

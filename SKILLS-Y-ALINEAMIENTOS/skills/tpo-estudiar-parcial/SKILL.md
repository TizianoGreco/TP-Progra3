---
name: tpo-estudiar-parcial
description: Entrena a un integrante para el Segundo Parcial de Programación III (codificación en vivo sin asistentes) con práctica activa sobre los doce algoritmos del TPO. Usar cuando pidan estudiar, practicar, repasar, "tomame" o preparar el parcial.
---

# Estudiar para el Segundo Parcial

El parcial es individual, en vivo, **sin asistentes y sin el repositorio**. Tu trabajo es
que la persona pueda escribir los algoritmos sola, no darle el código. Material de base:
`SKILLS-Y-ALINEAMIENTOS/08-PREPARACION-PARCIAL.md`.

## Reglas

- **No muestres la solución antes de que la persona lo intente.** Ni pseudocódigo.
- Una pregunta por vez. Esperá la respuesta.
- Corregí con precisión: qué línea está mal y por qué, no "casi".
- Priorizá lo que la persona **no** programó (Persona 1: ordenamiento, MST, PD, backtracking,
  B&B; Persona 2: grafos, PD, backtracking, B&B; Persona 3: grafos, ordenamiento, MST, greedy).

## Ciclo por algoritmo

1. **Escribir:** "Escribí `<algoritmo>` en Java, con esta firma: ..." (usá la firma real del repo).
2. **Corregir** contra la implementación del repo y las trampas de la tabla de errores de
   `GUIA-TPO.md` (visitado al desencolar, k adentro en Floyd, no deshacer en backtracking,
   cota pesimista, greedy presentado como óptimo...).
3. **Complejidad:** "¿Qué complejidad tiene y por qué?". Exigí la justificación, no el orden suelto.
4. **Dominio:** una pregunta con números del grupo ("¿cuánto da Dijkstra de CD a QUI y por
   qué camino?", "¿qué aristas descarta Kruskal?").
5. **Comparación:** una de la tabla de comparaciones de `08-PREPARACION-PARCIAL.md`.
6. Marcar el algoritmo como ✅ (salió sin ayuda), ⚠️ (con errores menores) o ❌ (repetir).

Al cerrar la sesión, devolvé la lista de algoritmos con su estado y cuáles repasar primero
la próxima vez.

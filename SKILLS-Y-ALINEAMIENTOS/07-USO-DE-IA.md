# 07 · Uso de IA

## Qué dice la materia (consigna, sección 7)

El uso de asistentes de IA está **permitido** sin autorización previa, con tres
condiciones:

1. **Declararlo** en la sección "Uso de IA" del README: qué herramienta y para qué. No
   declararlo es falta de honestidad académica.
2. **El Segundo Parcial es la verificación.** Se codifica en vivo, sin asistentes y sin el
   repositorio. Quien no pueda escribir lo que entregó, no aprueba.
3. **La comprensión no se delega.** Entregar algo generado sin entenderlo entra en el
   régimen disciplinario.

## Cómo usarla en este proyecto

Usarla para aprender más rápido, no para no aprender:

| Bien | Mal |
|---|---|
| Pedirle que explique un algoritmo y después escribirlo uno | Pedirle el algoritmo completo y pegarlo |
| Escribir el algoritmo y pedirle que lo revise contra las reglas de [02](02-ARQUITECTURA-Y-REGLAS.md) | Aceptar código que usa `Collections.sort` "porque funciona" |
| Pedirle casos borde para los tests | Pedirle que "haga pasar los tests" cambiando los valores esperados |
| Pedirle que justifique una complejidad y discutirla | Copiar la complejidad sin poder explicarla |
| Usar la skill `tpo-estudiar-parcial` para practicar sin mirar | — |

Regla práctica: **antes de commitear, cerrar el archivo y reescribir el método de memoria
en un papel.** Si no sale, todavía no está aprendido, y en el parcial no va a salir.

## Qué poner en el README

Una entrada por persona, concreta. Ejemplo:

```markdown
**Persona 2 — <nombre>.** Usé Claude Code para que me explicara Union-Find y para revisar
mi implementación de Kruskal contra las reglas del proyecto. QuickSort, MergeSort, Prim,
Kruskal y Greedy los escribí yo; los tests de casos borde los armé con su ayuda.
```

Vale cualquier herramienta (Claude, ChatGPT, Copilot, Gemini). Lo que importa es decir
**cuál** y **para qué**.

## Instrucciones para el asistente

Si sos un asistente de IA trabajando en este repo:

- Leé esta carpeta antes de tocar código y respetá sus reglas por encima de tus
  preferencias.
- Cuando implementes un algoritmo, explicá cada decisión (estructura, complejidad, dónde
  está la poda, la relajación o el retroceso) para que la persona pueda defenderlo.
- Ofrecé que la persona lo escriba primero y vos lo revises.
- Nunca cambies un valor esperado de un test para que pase: si no coincide con
  [01-DOMINIO.md](01-DOMINIO.md), el bug está en el algoritmo.
- Recordale declarar el uso en el README.

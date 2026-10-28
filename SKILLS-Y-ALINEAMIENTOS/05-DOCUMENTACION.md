# 05 · Documentación

Sin ficha en `docs/ENDPOINTS.md` el punto **no se asigna**, aunque el código funcione.
Hay tres lugares para documentar y cada algoritmo toca los tres.

## 1. Ficha en `docs/ENDPOINTS.md`

Agregar la ficha **antes** de la sección "Pendientes" y sacar el algoritmo de esa lista.
Formato fijo (copiar el de Dijkstra):

````markdown
### GET /api/grafo/prim

Qué resuelve, en una o dos líneas y en términos del dominio.

**Parámetros:** `origen` (nodo desde el que crece el árbol)

**Ejemplo:** `GET /api/grafo/prim?origen=CD`

**Respuesta:**
```json
{ ... respuesta real de la aplicación ... }
```
Una línea que conecte el resultado con el cálculo a mano.

**Errores:** 422 si el grafo no es conexo.

**Complejidad:** O(E log V). Por qué: qué operación se repite, cuántas veces y cuánto
cuesta cada una. Espacio.

**Estructura usada:** PriorityQueue de aristas candidatas + `boolean[] enArbol`.
````

Las tres últimas partes (complejidad, por qué, estructura) son las que se corrigen.

- La **respuesta** es real: sale de la app corriendo (`curl`) o de serializar la salida del
  método en un test temporal. No se inventa ni se escribe a mano.
- Los números tienen que coincidir con [01-DOMINIO.md](01-DOMINIO.md).
- Material extra que pide cada hito y va en la ficha:
  - Greedy y Mochila: el contraejemplo 78 vs. 95 y la tabla `dp` (hito 6).
  - Floyd: la comparación contra Dijkstra × V (hito 7).
  - Backtracking: tabla de nodos explorados con y sin poda para 3 o 4 restricciones (hito 8).
  - Branch & Bound: cuál es la cota, **por qué es optimista** y cuántos nodos se podaron (hito 11).

## 2. Fila en `docs/COMPLEJIDADES.md` (hito 10)

Reemplazar la fila `_pendiente (Persona N)_` del algoritmo con las seis columnas:

| Algoritmo | Técnica | Recurrencia | Tiempo | Espacio | Estructura que la sostiene |
|---|---|---|---|---|---|

Y si la justificación no entra en una celda, agregar una viñeta en "Notas de
justificación". Persona 3 consolida el informe al final.

## 3. README

- Pasar el estado del endpoint de `501` a `hecho` en la tabla de endpoints.
- Si cambia un resultado verificado, actualizar la tabla de resultados.
- Declarar el uso de IA de tu parte en la sección "Uso de IA" (ver [07-USO-DE-IA.md](07-USO-DE-IA.md)).

## Convenciones de redacción

- Español, en tercera persona o impersonal ("se ordena", "el algoritmo toma").
- Unidades siempre: km, kg, USD.
- Complejidades con la misma notación en todos lados: `O((V + E) log V)`, `O(n · W)`, `O(V³)`.
- Ids de nodos y paquetes en `código`: `CD`, `P1`.
- Nada de capturas de pantalla para resultados: JSON en bloque de código.

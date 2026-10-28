# Documentación de endpoints

Todos los ejemplos están tomados de la aplicación corriendo contra la red logística del
grupo (ver README). `V` es la cantidad de nodos (10) y `E` la cantidad de rutas (15);
`n` la cantidad de paquetes (8).

Convenciones de error (ningún endpoint devuelve un stack trace):

| Código | Cuándo | Ejemplo de cuerpo |
|---|---|---|
| 400 | parámetro faltante, de tipo inválido o nodo inexistente | `{"estado":"ERROR","detalle":"No existe el nodo con id: ZZZ"}` |
| 422 | estado imposible: destino inalcanzable, grafo no conexo, ciclo negativo | `{"estado":"ERROR","detalle":"Dijkstra: no existe camino de QUI a CD"}` |
| 404 | ruta inexistente | `{"estado":"ERROR","detalle":"No existe el recurso: /api/grafo/nada"}` |
| 500 | falla inesperada | `{"estado":"ERROR","detalle":"Error interno. Revisar el log del servidor."}` |

---

### GET /api/grafo/resumen

Metadatos del grafo cargado. Sirve para verificar la conexión a la base.

**Parámetros:** `dirigido` (opcional, default `true`)

**Ejemplo:** `GET /api/grafo/resumen`

**Respuesta:**
```json
{"ids":["CD","DN","DS","DO","PAL","CAB","SI","AVE","QUI","MOR"],"vertices":10,"dirigido":true,"aristas":15}
```

**Complejidad:** O(V + E), la de cargar el grafo desde la base a la lista de adyacencia.

---

### GET /api/grafo/dfs

Recorrido en profundidad desde un nodo. Devuelve los ids en el orden en que se visitan.

**Parámetros:** `origen` (id del nodo de partida)

**Ejemplo:** `GET /api/grafo/dfs?origen=CD`

**Respuesta:**
```json
["CD","DN","SI","DS","AVE","QUI","DO","MOR","CAB","PAL"]
```
(El orden depende del orden en que la base devuelve las conexiones de cada nodo; lo que
es invariante es que cada nodo aparece una sola vez y que se llega al fondo de una rama
antes de volver: `CD → DN → SI` antes de pasar a `DS`.)

**Complejidad:** O(V + E). Cada vértice entra a la recursión una sola vez gracias al
arreglo de visitados, y la lista de adyacencia de cada uno se recorre una sola vez, lo que
suma E en total. Espacio O(V): visitados más la profundidad de la pila de llamadas.

**Estructura usada:** recursión (pila de llamadas) + `boolean[] visitado` + lista de
adyacencia del `Grafo`.

---

### GET /api/grafo/bfs

Recorrido en anchura desde un nodo: primero los vecinos directos, después los que están a
dos rutas de distancia, etc.

**Parámetros:** `origen` (id del nodo de partida)

**Ejemplo:** `GET /api/grafo/bfs?origen=CD`

**Respuesta:**
```json
["CD","DN","DS","DO","PAL","CAB","SI","AVE","QUI","MOR"]
```
Nivel 0: `CD`. Nivel 1: sus cinco destinos directos. Nivel 2: `SI` (desde DN), `AVE` y
`QUI` (desde DS), `MOR` (desde DO o CAB).

**Complejidad:** O(V + E). Cada vértice se encola y desencola exactamente una vez (se
marca como visitado **al encolar**, no al desencolar, para que no entre repetido) y cada
arista se examina una vez desde su origen. Las operaciones de `ArrayDeque` son O(1).
Espacio O(V).

**Estructura usada:** cola FIFO (`ArrayDeque`) + `boolean[] visitado`.

BFS da el camino con menos rutas, no el más corto: a `DN` llega en 1 ruta (19 km)
cuando Dijkstra encuentra 2 rutas que suman 17 km.

---

### GET /api/grafo/dijkstra

Camino más corto (menor suma de kilómetros) entre dos nodos, con el camino reconstruido.

**Parámetros:** `origen`, `destino` (ids)

**Ejemplo:** `GET /api/grafo/dijkstra?origen=CD&destino=QUI`

**Respuesta:**
```json
{"vertices":["CD","DS","AVE","QUI"],"costoTotal":27.0,"nodosExpandidos":10}
```
Otros verificados a mano: `CD→DN` = 17 km por `[CD,PAL,DN]` (el directo son 19);
`CD→MOR` = 23 km por `[CD,DO,MOR]` (vía CAB serían 24).

**Errores:** 400 si hay una ruta con distancia negativa (el algoritmo no aplica); 422 si el
destino no es alcanzable (`QUI→CD`, porque las rutas tienen sentido).

**Complejidad:** O((V + E) log V). Cada relajación exitosa inserta una entrada en el heap
(a lo sumo E inserciones) y cada vértice se expande una vez (V extracciones útiles, las
entradas obsoletas se descartan al salir). Cada operación del heap cuesta O(log E) =
O(log V) porque E ≤ V². Espacio O(V + E).

**Estructura usada:** `PriorityQueue` (heap binario) de pares (vértice, distancia) con
inserción perezosa, `double[] dist`, `int[] predecesor` para reconstruir el camino
recorriendo hacia atrás desde el destino.

---

### GET /api/grafo/ucs

Uniform Cost Search: igual que Dijkstra pero se detiene apenas el destino sale de la cola.
Mismo resultado, menos nodos expandidos.

**Parámetros:** `origen`, `destino`

**Ejemplo:** `GET /api/grafo/ucs?origen=CD&destino=CAB`

**Respuesta:**
```json
{"vertices":["CD","CAB"],"costoTotal":7.0,"nodosExpandidos":2}
```
Dijkstra sobre la misma consulta devuelve el mismo camino con `nodosExpandidos: 10`:
recorre todo lo alcanzable aunque el destino esté al lado.

**Complejidad:** O((V + E) log V) en el peor caso; en la práctica proporcional a la
porción del grafo con distancia menor o igual a la del destino.

**Estructura usada:** la misma que Dijkstra.

---

## Pendientes

Las fichas de Prim, Kruskal, QuickSort, MergeSort y Greedy (Persona 2) y de Mochila 0/1,
Floyd-Warshall, Backtracking y Branch & Bound (Persona 3) se agregan acá a medida que cada
algoritmo se implementa, con el mismo formato: descripción, parámetros, ejemplo real,
errores, complejidad justificada y estructura de datos. Mientras tanto esos endpoints
responden:

```json
{"estado":"PENDIENTE","detalle":"Todavia no esta implementado: Prim"}
```

con `501 Not Implemented`.

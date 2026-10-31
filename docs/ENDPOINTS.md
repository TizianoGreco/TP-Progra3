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

### GET /api/seleccion/dinamica

Mochila 0/1: elige un subconjunto de paquetes que maximiza el valor declarado en USD sin
superar la capacidad del vehículo en kg. Cada paquete entra entero o no entra.

**Parámetros:** `capacidad` (kg, entero)

**Ejemplo:** `GET /api/seleccion/dinamica?capacidad=10`

**Respuesta:**
```json
{"elegidos":[{"id":"P2","nombre":"Monitor","peso":5.0,"valor":48.0,"nodoId":"CAB"},{"id":"P3","nombre":"Impresora","peso":5.0,"valor":47.0,"nodoId":"SI"}],"valorTotal":95.0,"pesoTotal":10.0,"tecnica":"programacion dinamica"}
```
P2 + P3 = 48 + 47 = 95 USD y 5 + 5 = 10 kg. El greedy por valor/peso, sobre los mismos
paquetes y la misma capacidad, carga {P1, P4, P7} = 60 + 14 + 4 = **78 USD**. Ese es el
contraejemplo del hito 6: el criterio goloso no da el óptimo.

Tabla `dp[i][w]`: fila i = primeros i paquetes (P1…P8), columna w = capacidad en kg. La
celda es el máximo valor en USD.

| | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| — | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| P1 (6 kg, 60) | 0 | 0 | 0 | 0 | 0 | 0 | 60 | 60 | 60 | 60 | 60 |
| P2 (5 kg, 48) | 0 | 0 | 0 | 0 | 0 | 48 | 60 | 60 | 60 | 60 | 60 |
| P3 (5 kg, 47) | 0 | 0 | 0 | 0 | 0 | 48 | 60 | 60 | 60 | 60 | **95** |
| P4 (2 kg, 14) | 0 | 0 | 14 | 14 | 14 | 48 | 60 | 62 | 74 | 74 | 95 |
| P5 (3 kg, 18) | 0 | 0 | 14 | 18 | 18 | 48 | 60 | 62 | 74 | 78 | 95 |
| P6 (8 kg, 40) | 0 | 0 | 14 | 18 | 18 | 48 | 60 | 62 | 74 | 78 | 95 |
| P7 (1 kg, 4) | 0 | 4 | 14 | 18 | 22 | 48 | 60 | 64 | 74 | 78 | 95 |
| P8 (4 kg, 12) | 0 | 4 | 14 | 18 | 22 | 48 | 60 | 64 | 74 | 78 | 95 |

En la columna 10 el salto a 95 aparece al incorporar P3: `dp[2][5] + 47 = 48 + 47`.
Ningún paquete posterior mejora esa celda. El recorrido hacia atrás desde `dp[8][10]`
salta P8, P7, P6, P5 y P4 porque la columna 10 no cambia; toma P3 porque 95 > 60 y la
capacidad baja a 5 kg; ahí toma P2 porque 48 > 0.

**Errores:** 400 si `capacidad` es negativa.

**Complejidad:** O(n · W). Hay una celda por paquete y por kilo de capacidad, y cada una
se resuelve en O(1) mirando la fila anterior. Espacio O(n · W): hace falta la tabla
completa para recuperar los paquetes. Es pseudopolinómica porque W es el valor de la
capacidad, no la cantidad de paquetes.

**Estructura usada:** `double[][] dp` de tamaño `(n+1) × (W+1)`. El peso se indexa con
`Math.ceil` para que la columna sea entera.

---

### GET /api/grafo/floyd

Costo mínimo en km entre todos los pares de nodos. Sirve para armar la tabla de distancias
de la red de una sola vez, en lugar de pedir un camino por vez.

**Parámetros:** ninguno

**Ejemplo:** `GET /api/grafo/floyd`

**Respuesta:**
```json
{"CD->CD":0.0,"CD->DN":17.0,"CD->DS":10.0,"CD->DO":18.0,"CD->PAL":9.0,"CD->CAB":7.0,"CD->SI":29.0,"CD->AVE":15.0,"CD->QUI":27.0,"CD->MOR":23.0,"DN->DN":0.0,"DN->SI":12.0,"DS->DS":0.0,"DS->AVE":5.0,"DS->QUI":17.0,"DO->DS":23.0,"DO->DO":0.0,"DO->CAB":12.0,"DO->AVE":28.0,"DO->QUI":40.0,"DO->MOR":5.0,"PAL->DN":8.0,"PAL->DS":17.0,"PAL->PAL":0.0,"PAL->CAB":6.0,"PAL->SI":20.0,"PAL->AVE":22.0,"PAL->QUI":34.0,"PAL->MOR":23.0,"CAB->DS":11.0,"CAB->CAB":0.0,"CAB->AVE":16.0,"CAB->QUI":28.0,"CAB->MOR":17.0,"SI->SI":0.0,"AVE->AVE":0.0,"AVE->QUI":12.0,"QUI->QUI":0.0,"MOR->MOR":0.0}
```
Solo entran los pares alcanzables. `CD->DN` queda en 17 km (por Palermo) y no en los 19
del tramo directo. `CD->QUI` = 27, `PAL->QUI` = 34, `DO->QUI` = 40. `QUI->CD` no aparece:
desde Quilmes no hay vuelta. La diagonal vale 0.

**Errores:** 422 si hay un ciclo de costo negativo (algún nodo a distancia negativa de sí
mismo). Un peso negativo suelto, sin ciclo, no es un error: el algoritmo igual encuentra
el mínimo.

**Complejidad:** O(V³). Para cada posible nodo intermedio se revisan todos los pares, y
cada revisión es un mínimo en O(1). Espacio O(V²).

**Estructura usada:** matriz `double[][]` copiada de la adyacencia. El bucle del
intermedio `k` va por fuera de `i` y `j`.

---

### GET /api/grafo/floyd/comparacion

Corre Floyd-Warshall y, aparte, Dijkstra desde cada nodo. Los costos tienen que coincidir.
La diferencia es cuánto trabajo hace cada uno.

**Parámetros:** ninguno

**Ejemplo:** `GET /api/grafo/floyd/comparacion`

**Respuesta:**
```json
{"vertices":10,"operacionesFloyd":1000,"nodosExpandidosDijkstraV":39,"relajacionesDijkstraV":40,"costosCoinciden":true}
```
Floyd hace V³ = 10³ = 1000 operaciones. Dijkstra, una vez por nodo, relaja 40 aristas y
expande 39 nodos. En esta red rala Dijkstra × V trabaja menos; Floyd igual llena toda la
tabla.

**Complejidad:** O(V³) por Floyd, más O(V · (V + E) log V) por las V corridas de Dijkstra.

**Estructura usada:** la matriz de Floyd y, para Dijkstra, el mismo heap que el endpoint
de camino mínimo.

---

## Pendientes

Las fichas de Prim, Kruskal, QuickSort, MergeSort y Greedy (Persona 2) y de
Backtracking y Branch & Bound (Persona 3) se agregan acá a medida que cada
algoritmo se implementa, con el mismo formato: descripción, parámetros, ejemplo real,
errores, complejidad justificada y estructura de datos. Mientras tanto esos endpoints
responden:

```json
{"estado":"PENDIENTE","detalle":"Todavia no esta implementado: Prim"}
```

con `501 Not Implemented`.

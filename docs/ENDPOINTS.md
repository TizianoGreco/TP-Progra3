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

### GET /api/grafo/prim

Árbol de recubrimiento mínimo: el conjunto de rutas de menor distancia total que mantiene
conectados todos los nodos de la red (tratada como no dirigida).

**Parámetros:** `origen` (nodo desde el que crece el árbol)

**Ejemplo:** `GET /api/grafo/prim?origen=CD`

**Respuesta:**
```json
{"aristas":[
  {"origen":"CD","destino":"CAB","costo":7.0},{"origen":"CAB","destino":"PAL","costo":6.0},
  {"origen":"PAL","destino":"DN","costo":8.0},{"origen":"CD","destino":"DS","costo":10.0},
  {"origen":"DS","destino":"AVE","costo":5.0},{"origen":"DN","destino":"SI","costo":12.0},
  {"origen":"AVE","destino":"QUI","costo":12.0},{"origen":"CAB","destino":"DO","costo":12.0},
  {"origen":"DO","destino":"MOR","costo":5.0}],
 "costoTotal":77.0}
```
9 aristas = V − 1. Desde cualquier origen el costo es 77 km.

**Errores:** 422 si el grafo no es conexo.

**Complejidad:** O(E log V) con heap. Cada arista se encola a lo sumo dos veces (una por
cada extremo, porque el grafo no dirigido la guarda en las dos listas) y cada operación
cuesta O(log E) = O(log V). Se eligió el heap y no la búsqueda lineal O(V²) porque la red
es rala (E ≈ 1.5 V): 15·log 10 ≈ 50 operaciones contra 100.

**Estructura usada:** `PriorityQueue` de aristas candidatas (origen, destino, costo) +
`boolean[] enArbol`.

---

### GET /api/grafo/kruskal

Mismo árbol de recubrimiento mínimo, construido ordenando todas las rutas por distancia y
agregando los que no cierran ciclo.

**Parámetros:** ninguno

**Ejemplo:** `GET /api/grafo/kruskal`

**Respuesta:**
```json
{"aristas":[
  {"origen":"DS","destino":"AVE","costo":5.0},{"origen":"DO","destino":"MOR","costo":5.0},
  {"origen":"PAL","destino":"CAB","costo":6.0},{"origen":"CD","destino":"CAB","costo":7.0},
  {"origen":"DN","destino":"PAL","costo":8.0},{"origen":"CD","destino":"DS","costo":10.0},
  {"origen":"DN","destino":"SI","costo":12.0},{"origen":"DO","destino":"CAB","costo":12.0},
  {"origen":"AVE","destino":"QUI","costo":12.0}],
 "costoTotal":77.0}
```
Cálculo a mano: rutas ordenadas 5, 5, 6, 7, 8, **9 (CD-PAL, ciclo)**, 10, **11 (CAB-DS,
ciclo)**, 12, 12, 12 → se llega a 9 aristas y se corta.

**Errores:** 422 si el grafo no es conexo (quedan varias componentes).

**Complejidad:** O(E log E), dominada por ordenar las aristas con el MergeSort propio. Las
E operaciones `find`/`union` cuestan O(E · α(V)) con unión por rango y compresión de
caminos, prácticamente lineal. Como E ≤ V², también es O(E log V). Espacio O(V + E).

**Estructura usada:** lista de aristas ordenada con MergeSort + **Union-Find** propio
(`int[] padre`, `int[] rango`) para detectar ciclo con `find(u) == find(v)`.

Prim vs. Kruskal: en grafos ralos rinden parecido y Kruskal es más simple; en grafos densos
(E ≈ V²) Prim con matriz O(V²) le gana a ordenar V² aristas.

---

### GET /api/grafo/floyd

Distancia mínima entre **todos** los pares de nodos. Programación dinámica sobre la matriz
de adyacencia.

**Parámetros:** ninguno

**Ejemplo:** `GET /api/grafo/floyd`

**Respuesta** (recortada; solo se informan los pares alcanzables):
```json
{"CD->CD":0.0,"CD->DN":17.0,"CD->DS":10.0,"CD->DO":18.0,"CD->PAL":9.0,"CD->CAB":7.0,
 "CD->SI":29.0,"CD->AVE":15.0,"CD->QUI":27.0,"CD->MOR":23.0,"DN->SI":12.0,
 "DS->QUI":17.0,"DO->QUI":40.0,"PAL->QUI":34.0, ...}
```

**Errores:** 422 si hay un ciclo de costo negativo (algún `d[i][i] < 0` al terminar).

**Complejidad:** O(V³) tiempo, O(V²) espacio. Recurrencia
`d_k[i][j] = min(d_{k-1}[i][j], d_{k-1}[i][k] + d_{k-1}[k][j])` con `k` en el bucle
externo; como en la iteración `k` la fila y la columna `k` no cambian, alcanza con una sola
matriz en lugar de V.

**Estructura usada:** matriz de adyacencia `double[V][V]` (`Grafo.matrizDeAdyacencia()`).

### GET /api/grafo/floyd/comparacion

Hito 7: compara el trabajo de Floyd contra correr Dijkstra desde cada vértice.

**Ejemplo:** `GET /api/grafo/floyd/comparacion`

**Respuesta:**
```json
{"vertices":10,"operacionesFloyd":1000,"nodosExpandidosDijkstraV":39,"relajacionesDijkstraV":40,"costosCoinciden":true}
```
Floyd hace V³ = 1000 pasos de la recurrencia sin importar la forma del grafo; las 10
corridas de Dijkstra expanden 39 nodos y relajan 40 aristas en total, porque la red es rala
y dirigida (desde QUI o SI no se llega a nadie). Los costos coinciden en los 100 pares.

---

### GET /api/seleccion/greedy

Qué paquetes cargar en un vehículo de capacidad dada, tomando por ratio valor/peso
descendente mientras entren. **Función objetivo:** maximizar la suma de valor sin superar
la capacidad en kg.

**Parámetros:** `capacidad` (kg, decimal)

**Ejemplo:** `GET /api/seleccion/greedy?capacidad=10`

**Respuesta:**
```json
{"elegidos":[
  {"id":"P1","nombre":"Notebook","peso":6.0,"valor":60.0,"nodoId":"PAL"},
  {"id":"P4","nombre":"Libros","peso":2.0,"valor":14.0,"nodoId":"AVE"},
  {"id":"P7","nombre":"Documentacion","peso":1.0,"valor":4.0,"nodoId":"DN"}],
 "valorTotal":78.0,"pesoTotal":9.0,"tecnica":"greedy"}
```

**Caso donde greedy NO da el óptimo** (hito 6): con los paquetes del dominio y capacidad
10, el greedy toma primero P1 (ratio 10, 6 kg) y después ya no le entran P2 ni P3 (5 kg
cada uno); completa con P4 y P7 y llega a **78 USD**. El óptimo es dejar P1 afuera y cargar
P2 + P3 = 10 kg exactos por **95 USD**. El greedy es óptimo para la mochila *fraccional*
(si se pudiera cargar 4/5 de un monitor), no para la 0/1.

**Complejidad:** O(n log n), dominada por el ordenamiento con el MergeSort propio; el
recorrido posterior es O(n). Espacio O(n).

**Estructura usada:** lista ordenada por ratio + acumuladores de peso y valor.

---

### GET /api/seleccion/dinamica

Mochila 0/1 con programación dinámica: la selección óptima de paquetes para la capacidad.

**Parámetros:** `capacidad` (kg, entero)

**Ejemplo:** `GET /api/seleccion/dinamica?capacidad=10`

**Respuesta:**
```json
{"elegidos":[
  {"id":"P2","nombre":"Monitor","peso":5.0,"valor":48.0,"nodoId":"CAB"},
  {"id":"P3","nombre":"Impresora","peso":5.0,"valor":47.0,"nodoId":"SI"}],
 "valorTotal":95.0,"pesoTotal":10.0,"tecnica":"programacion dinamica"}
```

**Tabla dp** del ejemplo (`dp[i][w]` = mejor valor con los primeros `i` paquetes y
capacidad `w`; se muestran las columnas relevantes):

| i (paquete) | w=5 | w=6 | w=8 | w=9 | w=10 |
|---|---|---|---|---|---|
| 0 | 0 | 0 | 0 | 0 | 0 |
| 1 (P1: 6 kg, 60) | 0 | 60 | 60 | 60 | 60 |
| 2 (P2: 5 kg, 48) | 48 | 60 | 60 | 60 | 60 |
| 3 (P3: 5 kg, 47) | 48 | 60 | 60 | 60 | **95** |
| 4 (P4: 2 kg, 14) | 48 | 60 | 74 | 74 | 95 |
| 5 (P5: 3 kg, 18) | 48 | 60 | 74 | 78 | 95 |
| 6 (P6: 8 kg, 40) | 48 | 60 | 74 | 78 | 95 |
| 7 (P7: 1 kg, 4) | 48 | 60 | 74 | 78 | 95 |
| 8 (P8: 4 kg, 12) | 48 | 60 | 74 | 78 | 95 |

Recuperación: desde `dp[8][10]=95` hacia arriba, `dp[i][10]` cambia en i=3 (se toma P3,
w pasa a 5) y en i=2 (`dp[2][5]=48 ≠ dp[1][5]=0`, se toma P2).

**Complejidad:** O(n · W) en tiempo y espacio, con W la capacidad. Es
**pseudopolinómica**: polinómica en el valor numérico de W, pero W ocupa log W bits, así
que respecto del tamaño de la entrada es exponencial. Por eso la tabla no contradice que
mochila 0/1 sea NP-completo.

**Estructura usada:** matriz `double[n+1][W+1]` construida bottom-up; los pesos se
discretizan con `ceil` para indexar.

---

### GET /api/seleccion/quicksort

Ordena los paquetes con QuickSort propio. Devuelve una lista nueva.

**Parámetros:** `criterio` = `valor` | `peso` | `ratio` (default `ratio`), ascendente

**Ejemplo:** `GET /api/seleccion/quicksort?criterio=ratio`

**Respuesta:**
```json
[{"id":"P8",...,"peso":4.0,"valor":12.0},{"id":"P7",...},{"id":"P6",...},{"id":"P5",...},
 {"id":"P4",...},{"id":"P3",...},{"id":"P2",...},{"id":"P1",...,"peso":6.0,"valor":60.0}]
```

**Complejidad:** O(n log n) promedio, O(n²) peor caso. Partición de Lomuto con **pivote en
el elemento central** (intercambiado al final del rango antes de particionar): con pivote
en el último, una entrada ya ordenada, que es lo habitual cuando los datos vienen de una
base, produce particiones de n−1 y 0 en cada nivel y cae en el peor caso
`T(n) = T(n−1) + O(n) = O(n²)`. El peor caso sigue existiendo (por ejemplo, todos los
elementos iguales). Espacio O(log n) de pila en promedio. No es estable.

**Estructura usada:** copia de la lista ordenada in-place, recursión sobre los dos
subrangos.

---

### GET /api/seleccion/mergesort

Ordena los paquetes con MergeSort propio, estable. Devuelve una lista nueva.

**Parámetros:** `criterio` = `valor` | `peso` | `ratio` (default `ratio`), ascendente

**Ejemplo:** `GET /api/seleccion/mergesort?criterio=peso`

**Respuesta:**
```json
[{"id":"P7","peso":1.0,...},{"id":"P4","peso":2.0,...},{"id":"P5","peso":3.0,...},
 {"id":"P8","peso":4.0,...},{"id":"P2","peso":5.0,...},{"id":"P3","peso":5.0,...},
 {"id":"P1","peso":6.0,...},{"id":"P6","peso":8.0,...}]
```
P2 y P3 pesan lo mismo y conservan su orden original: estabilidad (la mezcla toma la mitad
izquierda ante empate).

**Complejidad:** O(n log n) siempre. Recurrencia `T(n) = 2T(n/2) + O(n)`: a=2, b=2, k=1,
`a = b^k` → `T(n) = O(n^k log n) = O(n log n)`. La división es siempre por la mitad, sin
depender de los datos. Espacio O(n) por las listas auxiliares de la mezcla.

**Estructura usada:** recursión sobre `subList` y una lista auxiliar por mezcla.

---

### GET /api/grafo/rutas

Backtracking: todas las rutas simples (sin repetir nodos) entre dos nodos que no superen
una distancia máxima ni una cantidad máxima de rutas.

**Parámetros:** `origen`, `destino`, `costoMaximo` (km, default 1000),
`saltosMaximos` (rutas, default 10)

**Ejemplo:** `GET /api/grafo/rutas?origen=CD&destino=QUI&costoMaximo=37&saltosMaximos=5`

**Respuesta:**
```json
{"rutas":[
  {"vertices":["CD","DS","AVE","QUI"],"costoTotal":27.0},
  {"vertices":["CD","DS","QUI"],"costoTotal":29.0},
  {"vertices":["CD","CAB","DS","AVE","QUI"],"costoTotal":35.0},
  {"vertices":["CD","CAB","DS","QUI"],"costoTotal":37.0}],
 "nodosExploradosConPoda":23,"nodosExploradosSinPoda":30,"ramasPodadas":4}
```

**Nodos explorados con y sin poda** (hito 8), misma consulta CD → QUI:

| Restricción | Rutas | Con poda | Sin poda | Ramas podadas |
|---|---|---|---|---|
| sin restricción | 8 | 30 | 30 | 0 |
| ≤ 37 km, ≤ 5 rutas | 4 | 23 | 30 | 4 |
| ≤ 30 km | 2 | 18 | 30 | 8 |
| ≤ 3 rutas | 3 | 23 | 30 | 5 |

"Sin poda" enumera todos los caminos simples y filtra al final; "con poda" descarta la rama
apenas la distancia acumulada o las rutas superan el límite, sin entrar. En una red rala de 10
nodos la diferencia es chica; en un grafo denso los caminos simples crecen como V! y la poda
es lo que hace tratable el problema.

**Complejidad:** exponencial en el peor caso, O(V!) caminos simples en un grafo completo
(cada permutación de los intermedios es un camino distinto). La poda por viabilidad no
cambia el peor caso pero reduce los nodos explorados en la práctica. Espacio O(V) para el
camino parcial y los visitados, más el tamaño de la salida.

**Estructura usada:** DFS recursivo con `boolean[] visitado` y lista del camino actual;
marcar/agregar al entrar y desmarcar/quitar al volver es el retroceso.

---

### GET /api/seleccion/repartir

Branch & Bound: repartir todos los paquetes entre k vehículos idénticos minimizando la
carga del vehículo más cargado (makespan).

**Parámetros:** `contenedores` (k ≥ 1)

**Ejemplo:** `GET /api/seleccion/repartir?contenedores=3`

**Respuesta:**
```json
{"porContenedor":{
   "contenedor1":[{"id":"P6","peso":8.0,...},{"id":"P5","peso":3.0,...}],
   "contenedor2":[{"id":"P1","peso":6.0,...},{"id":"P8","peso":4.0,...},{"id":"P4","peso":2.0,...}],
   "contenedor3":[{"id":"P2","peso":5.0,...},{"id":"P3","peso":5.0,...},{"id":"P7","peso":1.0,...}]},
 "cargaMaxima":12.0,"cotaInicial":11.333333333333334,"nodosExplorados":38,"nodosPodados":22}
```
Cargas 11, 12 y 11 kg. Como los 34 kg no se dividen en tres partes enteras de 11, 12 es el
óptimo.

**La cota y por qué es optimista** (hito 11). Para un nodo parcial del árbol (los primeros
`i` paquetes ya asignados) se usa como cota inferior del makespan alcanzable:

```
cota = max( carga máxima actual , suma total de pesos / k )
```

Es optimista (nunca supera el makespan real de la mejor hoja bajo ese nodo) porque: (1) la
carga máxima actual solo puede crecer al seguir agregando paquetes, y (2) ningún reparto
puede tener todos los vehículos por debajo del promedio, así que el máximo es al menos
`suma / k`. Como subestima, jamás poda una rama que contenga el óptimo. Si la cota
sobreestimara (fuera pesimista), podaría ramas buenas y el resultado dejaría de ser óptimo
sin avisar. La poda es `cota >= mejor makespan encontrado`.

Además: los paquetes se ramifican de mayor a menor peso (cotas ajustadas temprano), se
arranca con la solución del greedy LPT como primer "mejor" y se corta la simetría (dos
vehículos con la misma carga son intercambiables, se prueba solo uno).

**Nodos podados:** con k=3 se exploran 38 nodos y se podan 22, contra 3⁸ = 6561 hojas del
árbol completo. Con k=4: 14 explorados, 9 podados, makespan 9. Con k=2 el greedy LPT ya da
17 = cota inicial, así que no se ramifica nada.

**Complejidad:** O(k^n) nodos en el peor caso, más O(n log n) del ordenamiento previo. La
cota hace que en la práctica se exploren órdenes de magnitud menos. Espacio O(n + k).

**Estructura usada:** recursión con `double[] carga` por vehículo, `int[] asignacion`
parcial, y el mejor makespan como cota superior global.

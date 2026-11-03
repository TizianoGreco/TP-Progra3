# Informe de complejidades (hito 10)

`V` = vértices, `E` = aristas, `n` = ítems, `W` = capacidad de la mochila, `k` = contenedores.
Sobre el grafo del grupo: V = 10, E = 15, n = 8.

| Algoritmo | Técnica | Recurrencia | Tiempo | Espacio | Estructura que la sostiene |
|---|---|---|---|---|---|
| DFS | recorrido | — | O(V + E) | O(V) | lista de adyacencia + `boolean[]` visitados + pila de llamadas |
| BFS | recorrido | — | O(V + E) | O(V) | lista de adyacencia + cola FIFO (`ArrayDeque`) + visitados |
| Dijkstra | greedy | — | O((V + E) log V) | O(V + E) | heap binario (`PriorityQueue`) con inserción perezosa + `dist[]` + `predecesor[]` |
| UCS | greedy | — | O((V + E) log V) peor caso | O(V + E) | ídem Dijkstra, con corte al expandir el destino |
| Prim | greedy sobre grafos | — | O(E log V) | O(V + E) | heap binario (`PriorityQueue`) de aristas candidatas con inserción perezosa + `boolean[] enArbol` |
| Kruskal | greedy + Union-Find | — | O(E log E) | O(V + E) | lista de aristas ordenada con el MergeSort propio + Union-Find (`int[] padre`, `int[] rango`) |
| QuickSort | divide y vencerás | T(n) = 2T(n/2) + O(n) promedio; T(n) = T(n−1) + O(n) peor | O(n log n) promedio, O(n²) peor | O(log n) pila promedio | copia de la lista ordenada in-place, partición de Lomuto con pivote central |
| MergeSort | divide y vencerás | T(n) = 2T(n/2) + O(n) | O(n log n) | O(n) | recursión sobre `subList` + lista auxiliar por mezcla (estable) |
| Greedy mochila | greedy | — | O(n log n) | O(n) | lista ordenada por ratio valor/peso con el MergeSort propio + acumuladores |
| Mochila 0/1 | programación dinámica | OPT(i, w) = max(OPT(i−1, w), OPT(i−1, w−pesoᵢ) + valorᵢ) | O(n · W) | O(n · W) | tabla `dp[n+1][W+1]` |
| Floyd-Warshall | programación dinámica | d⁽ᵏ⁾(i, j) = min(d⁽ᵏ⁻¹⁾(i, j), d⁽ᵏ⁻¹⁾(i, k) + d⁽ᵏ⁻¹⁾(k, j)) | O(V³) | O(V²) | matriz de adyacencia `double[][]` |
| Backtracking rutas | backtracking | T(v) = O(v · T(v−1)) | O(V!) | O(V) | DFS recursivo + `boolean[]` visitados + camino |
| Branch & Bound reparto | ramificación y poda | T(n) = k · T(n−1) | O(kⁿ) | O(n + k) | DFS recursivo + `double[]` de cargas |

## Notas de justificación

- **Por qué Dijkstra es O((V+E) log V) y no O(V²).** El mínimo se saca del heap en O(log)
  en lugar de barrer `dist[]` en O(V). Con inserción perezosa el heap puede tener hasta E
  entradas, pero log E ≤ 2 log V. Si se implementara con búsqueda lineal del mínimo, la
  complejidad real sería O(V²) y declararla como O((V+E) log V) sería incorrecto.

- **Por qué la mochila 0/1 es O(n · W) y es pseudopolinómica.** Hay una celda por cada
  ítem y por cada kilo de capacidad, y cada celda se resuelve en O(1) leyendo la fila
  anterior. W es el valor de la capacidad, no la cantidad de ítems: si la capacidad pasa
  de 10 kg a 10 000 kg la tabla crece mil veces aunque sigan siendo 8 paquetes.

- **Por qué Floyd-Warshall es O(V³).** El vértice intermedio k recorre los V nodos, y para
  cada uno se revisan los V×V pares. Cada revisión es un mínimo en O(1). La matriz se
  reutiliza, así que el espacio queda en O(V²). En esta red son 10³ = 1000 operaciones;
  correr Dijkstra desde cada nodo relaja 40 aristas.

- **Por qué el backtracking es O(V!).** En cada paso se puede seguir por cualquiera de los
  nodos que todavía no están en el camino, y el camino puede llegar a largo V. Eso es
  V · (V−1) · … · 1. La poda no baja ese techo: corta ramas que ya no pueden servir. De
  Barracas a Quilmes, sin límite se visitan 30 nodos; con tope de 37 km, 23.

- **Por qué la cota del reparto es optimista.** Vale `max(carga máxima actual, suma / k)`.
  El vehículo más cargado no puede bajar de lo que ya lleva, y tampoco se puede dejar a
  todos por debajo del promedio. Si esa cota ya no mejora el mejor reparto encontrado, la
  rama se corta. Con 3 vehículos la cota inicial es 34/3 ≈ 11,33 kg y el óptimo es 12 kg;
  de 217 nodos se podan 122.

- **Por qué Prim con heap y no con búsqueda lineal.** Cada arista se encola a lo sumo dos
  veces (una por extremo) y cada operación del heap cuesta O(log V): O(E log V). Con
  búsqueda lineal del mínimo sería O(V²). La red es rala (E = 15, V = 10), así que el heap
  conviene; en un grafo denso (E ≈ V²) ganaría la versión O(V²).

- **Por qué Kruskal es O(E log E).** Ordenar las aristas domina. Las E operaciones
  `find`/`union` cuestan O(E · α(V)) con unión por rango y compresión de caminos, casi
  lineal. Como E ≤ V², log E ≤ 2 log V, y también vale O(E log V).

- **Por qué MergeSort es O(n log n) siempre.** En `T(n) = 2T(n/2) + O(n)` se tiene a = 2,
  b = 2, k = 1 y a = bᵏ, así que T(n) = O(nᵏ log n) = O(n log n). La división es siempre
  por la mitad, sin depender de los datos.

- **Cuándo QuickSort cae en O(n²).** Cuando el pivote es siempre el mínimo o el máximo del
  rango: T(n) = T(n−1) + O(n). Con pivote en el último elemento eso pasa con una entrada ya
  ordenada; el pivote central lo evita, pero el peor caso sigue existiendo (por ejemplo,
  todos los elementos iguales con la partición de Lomuto).

- **Por qué el greedy no da el óptimo de la mochila 0/1.** Con 10 kg toma primero P1
  (ratio 10, 6 kg) y el hueco de 4 kg ya no admite a P2 ni a P3: llega a 78 USD contra los
  95 de la mochila 0/1. Es óptimo para la versión fraccional, no para la 0/1.

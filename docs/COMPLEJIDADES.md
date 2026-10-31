# Informe de complejidades (hito 10)

`V` = vértices, `E` = aristas, `n` = ítems, `W` = capacidad de la mochila, `k` = contenedores.
Sobre el grafo del grupo: V = 10, E = 15, n = 8.

| Algoritmo | Técnica | Recurrencia | Tiempo | Espacio | Estructura que la sostiene |
|---|---|---|---|---|---|
| DFS | recorrido | — | O(V + E) | O(V) | lista de adyacencia + `boolean[]` visitados + pila de llamadas |
| BFS | recorrido | — | O(V + E) | O(V) | lista de adyacencia + cola FIFO (`ArrayDeque`) + visitados |
| Dijkstra | greedy | — | O((V + E) log V) | O(V + E) | heap binario (`PriorityQueue`) con inserción perezosa + `dist[]` + `predecesor[]` |
| UCS | greedy | — | O((V + E) log V) peor caso | O(V + E) | ídem Dijkstra, con corte al expandir el destino |
| Prim | _pendiente (Persona 2)_ | | | | |
| Kruskal | _pendiente (Persona 2)_ | | | | |
| QuickSort | _pendiente (Persona 2)_ | | | | |
| MergeSort | _pendiente (Persona 2)_ | | | | |
| Greedy mochila | _pendiente (Persona 2)_ | | | | |
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

Las notas de cada algoritmo pendiente las agrega quien lo implementa; Persona 3 consolida el
informe completo.

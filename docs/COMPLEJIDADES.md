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
| Floyd-Warshall | _pendiente (Persona 3)_ | | | | |
| Backtracking rutas | _pendiente (Persona 3)_ | | | | |
| Branch & Bound reparto | _pendiente (Persona 3)_ | | | | |

## Notas de justificación

- **Por qué Dijkstra es O((V+E) log V) y no O(V²).** El mínimo se saca del heap en O(log)
  en lugar de barrer `dist[]` en O(V). Con inserción perezosa el heap puede tener hasta E
  entradas, pero log E ≤ 2 log V. Si se implementara con búsqueda lineal del mínimo, la
  complejidad real sería O(V²) y declararla como O((V+E) log V) sería incorrecto.

- **Por qué la mochila 0/1 es O(n · W) y es pseudopolinómica.** Hay una celda por cada
  ítem y por cada kilo de capacidad, y cada celda se resuelve en O(1) leyendo la fila
  anterior. W es el valor de la capacidad, no la cantidad de ítems: si la capacidad pasa
  de 10 kg a 10 000 kg la tabla crece mil veces aunque sigan siendo 8 paquetes.

Las notas de cada algoritmo pendiente las agrega quien lo implementa; Persona 3 consolida el
informe completo.

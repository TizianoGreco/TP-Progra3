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
| Mochila 0/1 | _pendiente (Persona 3)_ | | | | |
| Floyd-Warshall | _pendiente (Persona 3)_ | | | | |
| Backtracking rutas | _pendiente (Persona 3)_ | | | | |
| Branch & Bound reparto | _pendiente (Persona 3)_ | | | | |

## Notas de justificación

- **Por qué Dijkstra es O((V+E) log V) y no O(V²).** El mínimo se saca del heap en O(log)
  en lugar de barrer `dist[]` en O(V). Con inserción perezosa el heap puede tener hasta E
  entradas, pero log E ≤ 2 log V. Si se implementara con búsqueda lineal del mínimo, la
  complejidad real sería O(V²) y declararla como O((V+E) log V) sería incorrecto.

Las notas de cada algoritmo pendiente las agrega quien lo implementa; Persona 3 consolida el
informe completo.

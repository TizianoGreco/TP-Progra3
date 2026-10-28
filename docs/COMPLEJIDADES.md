# Informe de complejidades (hito 10)

`V` = vértices, `E` = aristas, `n` = ítems, `W` = capacidad de la mochila, `k` = contenedores.
Sobre el grafo del grupo: V = 10, E = 15, n = 8.

| Algoritmo | Técnica | Recurrencia | Tiempo | Espacio | Estructura que la sostiene |
|---|---|---|---|---|---|
| DFS | recorrido | — | O(V + E) | O(V) | lista de adyacencia + `boolean[]` visitados + pila de llamadas |
| BFS | recorrido | — | O(V + E) | O(V) | lista de adyacencia + cola FIFO (`ArrayDeque`) + visitados |
| Dijkstra | greedy | — | O((V + E) log V) | O(V + E) | heap binario (`PriorityQueue`) con inserción perezosa + `dist[]` + `predecesor[]` |
| UCS | greedy | — | O((V + E) log V) peor caso | O(V + E) | ídem Dijkstra, con corte al expandir el destino |
| Prim | greedy | — | O(E log V) | O(V + E) | heap de aristas candidatas + `boolean[] enArbol` |
| Kruskal | greedy | — | O(E log E) = O(E log V) | O(V + E) | aristas ordenadas con MergeSort + Union-Find (rango + compresión), O(α(V)) por operación |
| QuickSort | divide y vencerás | promedio `T(n) = 2T(n/2) + O(n)`; peor `T(n) = T(n−1) + O(n)` | O(n log n) promedio, O(n²) peor | O(log n) pila | arreglo in-place, partición de Lomuto con pivote central |
| MergeSort | divide y vencerás | `T(n) = 2T(n/2) + O(n)`, a = b^k → O(n log n) | O(n log n) siempre | O(n) | listas auxiliares en la mezcla |
| Greedy mochila | greedy | — | O(n log n) | O(n) | lista ordenada por ratio (MergeSort) + acumuladores |
| Mochila 0/1 | programación dinámica | `dp[i][w] = max(dp[i−1][w], dp[i−1][w−p_i] + v_i)` | O(n · W) pseudopolinómica | O(n · W) | matriz `double[n+1][W+1]` bottom-up |
| Floyd-Warshall | programación dinámica | `d_k[i][j] = min(d_{k−1}[i][j], d_{k−1}[i][k] + d_{k−1}[k][j])` | O(V³) | O(V²) | matriz de adyacencia, una sola copia (k externo) |
| Backtracking rutas | backtracking | `T(V) = (V−1) · T(V−1)` en grafo completo | O(V!) peor caso | O(V) + salida | DFS recursivo con visitados y camino parcial, poda por viabilidad |
| Branch & Bound reparto | ramificación y poda | `T(n) = k · T(n−1)` | O(k^n) peor caso | O(n + k) | recursión con cargas por contenedor, cota inferior `max(carga máx, suma/k)`, solución inicial LPT |

## Notas de justificación

- **Por qué Dijkstra es O((V+E) log V) y no O(V²).** El mínimo se saca del heap en O(log)
  en lugar de barrer `dist[]` en O(V). Con inserción perezosa el heap puede tener hasta E
  entradas, pero log E ≤ 2 log V. Si se implementara con búsqueda lineal del mínimo, la
  complejidad real sería O(V²) y declararla como O((V+E) log V) sería incorrecto.
- **Prim con heap vs. matriz.** En la red del grupo E ≈ 1.5 V, así que E log V ≈ 50 contra
  V² = 100. En un grafo denso (E ≈ V²) conviene la versión O(V²) con matriz.
- **Kruskal.** El costo lo pone el ordenamiento; el Union-Find con unión por rango y
  compresión de caminos hace que las E operaciones sumen O(E α(V)), donde α < 5 para
  cualquier V práctico.
- **MergeSort por la regla práctica.** `T(n) = a T(n/b) + O(n^k)` con a = 2, b = 2, k = 1:
  como a = b^k, T(n) = O(n^k log n) = O(n log n).
- **QuickSort peor caso.** Ocurre cuando el pivote es siempre el mínimo o el máximo del
  rango. El pivote central evita ese caso sobre entradas ya ordenadas (lo habitual con datos
  de una base), pero no lo elimina: una lista con todos los elementos iguales lo produce con
  la partición de Lomuto.
- **Mochila 0/1 pseudopolinómica.** O(n·W) parece polinómica, pero W es un número de la
  entrada que ocupa log W bits: duplicar los bits de W multiplica el trabajo por W. Respecto
  del tamaño de la entrada es exponencial, y por eso mochila 0/1 sigue siendo NP-completo.
- **Floyd con k externo.** En la iteración k, la fila k y la columna k no cambian, por eso
  una sola matriz alcanza (O(V²) en vez de O(V³) de espacio). Con k adentro la recurrencia
  usaría intermedios que todavía no fueron "habilitados" y el resultado sería incorrecto.
- **Floyd vs. Dijkstra V veces.** Floyd es V³ = 1000 pasos fijos; V corridas de Dijkstra son
  O(V (V+E) log V) ≈ 10 · 25 · 3.3 ≈ 830 en el peor caso, y en la práctica 39 expansiones y
  40 relajaciones porque el grafo es ralo y dirigido. Floyd gana en grafos densos y cuando
  hay pesos negativos sin ciclos negativos, donde Dijkstra no aplica.
- **Backtracking.** En un grafo completo, cada camino simple de origen a destino es una
  permutación de un subconjunto de los V−2 intermedios: `Σ (V−2)!/(V−2−j)!` ≈ e·(V−2)!,
  de ahí el O(V!). La poda por viabilidad (costo o saltos excedidos) no cambia el peor caso
  pero en la práctica reduce los nodos explorados (30 → 18 con ≤ 30 km en el grafo del
  grupo).
- **Branch & Bound.** El árbol completo tiene k^n hojas (cada ítem en cada contenedor). La
  cota inferior `max(carga máxima actual, suma/k)` es optimista, así que nunca poda el
  óptimo; con k = 3 y n = 8 se exploran 38 nodos y se podan 22 contra 6561 hojas posibles.

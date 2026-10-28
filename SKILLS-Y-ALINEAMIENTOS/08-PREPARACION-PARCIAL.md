# 08 · Preparación del Segundo Parcial

**11/11 y 18/11.** Individual, codificando en vivo, sin asistentes y sin el repositorio.
Se puede pedir **cualquier** algoritmo del trabajo, no solo el que programó cada uno.

Para cada algoritmo hay que poder: escribirlo de memoria, decir su complejidad **y
justificarla**, señalar dónde está la idea central (relajación, poda, retroceso, cota) y
compararlo con otro del trabajo.

---

## BFS

- **Idea:** recorrer por niveles con una cola FIFO.
- **Clave:** marcar visitado **al encolar**, no al desencolar (si no, un nodo entra varias veces).
- **Complejidad:** O(V + E): cada vértice se encola una vez y cada arista se mira una vez.
- **Estructura:** `ArrayDeque` + `boolean[] visitado`.
- **En el dominio:** desde CD → `CD, PAL, DS, CAB, DN, DO, AVE, QUI, MOR, SI`.
- **Pregunta típica:** BFS da el camino con menos rutas, no el más corto en km (a DN: 1
  ruta de 19 km vs. 2 rutas que suman 17).

## DFS

- **Idea:** ir hasta el fondo de una rama antes de volver.
- **Clave:** recursión (la pila de llamadas es la pila); marcar al entrar.
- **Complejidad:** O(V + E); espacio O(V) por la profundidad de la recursión.
- **En el dominio:** desde CD → `CD, PAL, DN, SI, CAB, DS, AVE, QUI, MOR, DO`.
- **Conexión:** es el esqueleto del backtracking.

## Dijkstra

- **Idea:** greedy: fijar siempre el vértice con menor distancia tentativa y relajar sus aristas.
- **Relajación:** `si dist[u] + w < dist[v] → dist[v] = dist[u] + w, pred[v] = u`.
- **Complejidad:** O((V + E) log V) con heap. Con búsqueda lineal del mínimo es O(V²), y
  declararla como log es **incorrecto** (vale 50 %).
- **Estructura:** `PriorityQueue`, `dist[]`, `predecesor[]` (para reconstruir el camino).
- **Pesos negativos:** lo rompen: un vértice ya fijado podría mejorar después.
- **En el dominio:** CD→QUI = 27 por CD→DS→AVE→QUI.
- **UCS:** igual, pero corta cuando el destino sale de la cola (CD→CAB: 2 expandidos vs. 10).

## QuickSort

- **Idea:** elegir pivote, particionar (menores a un lado, mayores al otro), recursión.
- **Complejidad:** O(n log n) promedio, **O(n²) peor caso** (pivote siempre mínimo o máximo:
  `T(n) = T(n−1) + O(n)`). No es estable. Espacio O(log n) de pila.
- **Pregunta típica:** ¿por qué pivote central? Con el último, una lista ya ordenada es el peor caso.

## MergeSort

- **Idea:** partir a la mitad, ordenar cada mitad, mezclar.
- **Recurrencia:** `T(n) = 2T(n/2) + O(n)`; a = 2, b = 2, k = 1 → a = b^k → O(n log n) **siempre**.
- **Estable** (ante empate, toma de la izquierda). Espacio O(n).
- **Por qué lo reutilizamos:** Kruskal, greedy y Branch & Bound necesitan ordenar.

## Prim

- **Idea:** hacer crecer un solo árbol tomando la arista más barata que sale de él.
- **Complejidad:** O(E log V) con heap; O(V²) con arreglos. Heap para grafos ralos, arreglos para densos.
- **Requisito:** grafo **no dirigido** y conexo. En el dominio: 77 km, 9 aristas.

## Kruskal

- **Idea:** ordenar todas las aristas y agregar las que no cierran ciclo.
- **Union-Find:** ciclo ⇔ `find(u) == find(v)`. Con rango y compresión: O(α(V)) por operación.
- **Complejidad:** O(E log E), dominada por el ordenamiento.
- **En el dominio:** descarta CD-PAL (9) y CAB-DS (11).
- **Prim vs. Kruskal:** en grafos densos (E ≈ V²) conviene Prim con arreglos.

## Greedy (mochila)

- **Idea:** ordenar por valor/peso descendente y cargar mientras entre.
- **Complejidad:** O(n log n), por el ordenamiento.
- **No es óptimo en la mochila 0/1:** capacidad 10 → 78 USD (P1, P4, P7) cuando el óptimo
  es 95 (P2, P3). Sí es óptimo en la mochila **fraccional**.

## Mochila 0/1 (programación dinámica)

- **Recurrencia:** `dp[i][w] = max(dp[i−1][w], dp[i−1][w − p_i] + v_i)` si `p_i ≤ w`.
- **Recuperación:** desde `dp[n][W]` hacia arriba; si `dp[i][w] ≠ dp[i−1][w]`, se tomó el ítem i.
- **Complejidad:** O(n · W), **pseudopolinómica**: W ocupa log W bits, así que es
  exponencial en el tamaño de la entrada (por eso la mochila sigue siendo NP-completa).

## Floyd-Warshall

- **Recurrencia:** `d[i][j] = min(d[i][j], d[i][k] + d[k][j])`, con **k en el bucle externo**.
- **Complejidad:** O(V³) tiempo, O(V²) espacio (una sola matriz alcanza porque en la
  iteración k la fila y la columna k no cambian).
- **Ciclos negativos:** algún `d[i][i] < 0` al terminar.
- **vs. Dijkstra × V:** Floyd sirve con pesos negativos (sin ciclos) y en grafos densos;
  Dijkstra × V gana en grafos ralos con pesos positivos.

## Backtracking (rutas simples)

- **Idea:** DFS que construye la ruta paso a paso; al volver, **deshace** (desmarca y quita).
- **Poda por viabilidad:** cortar apenas el costo parcial supera el máximo.
- **Complejidad:** O(V!) en un grafo completo; la poda no cambia el peor caso pero reduce
  los nodos explorados.
- **En el dominio:** 8 rutas CD→QUI; 4 con ≤ 37 km.

## Branch & Bound (reparto)

- **Idea:** backtracking + una **cota** que poda por **optimalidad**.
- **Cota:** `max(carga máxima actual, suma / k)`. Es **optimista** (nunca supera el mejor
  makespan alcanzable desde ese nodo), por eso nunca poda el óptimo. Una cota pesimista
  devolvería una solución peor sin avisar.
- **Complejidad:** O(k^n) peor caso.
- **En el dominio:** k=3 → 12 kg, cota 11.33.

---

## Comparaciones que suelen pedir

| Pregunta | Respuesta corta |
|---|---|
| BFS vs. Dijkstra | BFS minimiza cantidad de aristas; Dijkstra, suma de pesos. Con todos los pesos iguales coinciden. |
| Dijkstra vs. Floyd | uno contra todos vs. todos contra todos; Floyd tolera pesos negativos. |
| Greedy vs. PD | greedy decide una vez y no vuelve atrás; PD considera todas las subsoluciones. |
| Backtracking vs. Branch & Bound | poda por viabilidad (¿es válida?) vs. por optimalidad (¿puede mejorar?). |
| QuickSort vs. MergeSort | Quick: in-place, más rápido en promedio, peor caso n². Merge: n log n siempre, estable, O(n) extra. |
| Prim vs. Kruskal | Prim crece un árbol (mejor en densos); Kruskal une bosques (más simple en ralos). |

## Cómo practicar

1. Hoja en blanco, sin mirar: escribir el método completo en Java.
2. Comparar con el código del repo.
3. Repetir los que fallaron al día siguiente.

La skill `tpo-estudiar-parcial` arma este ciclo con el asistente: te pide el algoritmo,
lo corrige y te pregunta la complejidad.

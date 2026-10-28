# 03 · Reparto y tareas por persona

| Persona | Bloque | Puntos | Algoritmos |
|---|---|---|---|
| 1 — Tiziano Greco | Grafos y configuración | 3 | Spring Boot + Neo4j, carga del grafo, BFS, DFS, Dijkstra (+ UCS opcional) |
| 2 — _(completar)_ | Ordenamiento y expansión mínima | 4 | QuickSort, MergeSort, Prim, Kruskal, Greedy |
| 3 — _(completar)_ | Optimización y búsqueda | 3 | Mochila 0/1, Floyd-Warshall, Backtracking, Branch & Bound + consolidar README y docs |

> Repartirse los algoritmos **para programar** está bien. Repartirse los algoritmos
> **para estudiar**, no: en el Segundo Parcial cada uno codifica cualquiera de los doce.

## Definición de "terminado" (vale para todos)

Un algoritmo está terminado cuando cumple **las seis** condiciones de "Completo" de la
consigna (sección 4.1):

- [ ] Corre sin error por su endpoint.
- [ ] Da los valores de [01-DOMINIO.md](01-DOMINIO.md).
- [ ] Implementación propia, sin librerías ni `sort` de Java.
- [ ] La estructura de datos sostiene la complejidad declarada.
- [ ] Tiene tests en `src/test/java/uade/prog3/tpo/algorithm/` (ver [04-TESTS.md](04-TESTS.md)).
- [ ] Tiene su ficha en `docs/ENDPOINTS.md` y su fila en `docs/COMPLEJIDADES.md` (ver [05-DOCUMENTACION.md](05-DOCUMENTACION.md)).

Y además, para el grupo:

- [ ] `./mvnw test` pasa completo.
- [ ] El estado del endpoint en el README pasa de `501` a `hecho`.
- [ ] Está commiteado **por quien lo hizo**, desde su usuario de GitHub, y mergeado a `main` por PR.

---

## Persona 1 — Grafos y configuración ✅

Hecho y en `main`. Sirve de referencia de estilo para los demás.

| Tarea | Dónde |
|---|---|
| Spring Boot + Neo4j por variables de entorno | `application.properties`, `.env.example` |
| Carga del grafo del dominio (10 nodos, 15 rutas, 8 paquetes) | `seed/CargaInicial.java` |
| BFS (cola, visitado al encolar) y DFS (recursivo) | `algorithm/Recorridos.java` |
| Dijkstra (heap con inserción perezosa, reconstrucción de camino, rechazo de pesos negativos) y UCS | `algorithm/CaminosMinimos.java` |
| Tests | `RecorridosTest` (6), `CaminosMinimosTest` (7), `GrafoTest` (4) |
| Fichas | `docs/ENDPOINTS.md`: resumen, DFS, BFS, Dijkstra, UCS |

Pendiente de Persona 1: verificar contra Neo4j real el orden de DFS/BFS que figura en
`docs/ENDPOINTS.md` (los tests no usan la base).

---

## Persona 2 — Ordenamiento y expansión mínima (4 puntos)

**Orden sugerido:** MergeSort → QuickSort → Kruskal → Prim → Greedy. MergeSort primero
porque Kruskal y Greedy lo usan para ordenar.

### 2.1 QuickSort y MergeSort — `algorithm/Ordenamiento.java` (1 punto)

- `quickSort(List<Item> items, Comparator<Item> criterio)` y
  `mergeSort(List<Item> items, Comparator<Item> criterio)`, ambos **ascendentes**.
- Devolver una **lista nueva**; la de entrada no se toca (copiar y ordenar la copia).
- QuickSort: partición a mano. Documentar la estrategia de pivote y cuándo ocurre el peor
  caso. Recomendado: pivote central (con pivote en el último, una lista ya ordenada es el
  peor caso).
- MergeSort: mezcla a mano y **estable** (ante empate toma de la mitad izquierda). El test
  de estabilidad es P2 antes que P3 al ordenar por peso.
- Valores: ver "Persona 2" en [01-DOMINIO.md](01-DOMINIO.md).

### 2.2 Prim y Kruskal — `algorithm/ArbolRecubrimiento.java` (2 puntos)

- El controller ya les pasa el grafo **no dirigido** (`cargar(false)`).
- `prim(Grafo grafo, String origenId)`: cola de prioridad de aristas candidatas
  (O(E log V)) o arreglos `key[]`/`inMST[]` (O(V²)). Decir cuál y por qué; en esta red rala
  (E ≈ 1.5 V) conviene el heap.
- `kruskal(Grafo grafo)`: juntar las aristas **una sola vez** (en no dirigido cada una
  aparece dos veces en la adyacencia: tomar solo `u < v`), ordenarlas con el MergeSort
  propio y agregar las que no cierran ciclo. **Union-Find a mano** (`find` con compresión de
  caminos, `union` por rango). Cortar al llegar a V−1 aristas.
- Ambos: si el grafo no es conexo, `IllegalStateException` con "no es conexo".
- Devuelven `Mst(List<AristaMst> aristas, double costoTotal)` con ids de negocio.
- Valores: 9 aristas, 77 km, desde cualquier origen.

### 2.3 Greedy — `algorithm/Seleccion.greedy` (1 punto)

- `greedy(List<Item> items, double capacidad)`: ordenar por ratio valor/peso
  **descendente** (con el MergeSort propio) y tomar mientras entre.
- Declarar en el Javadoc la **función objetivo**: maximizar la suma de valor sin superar la
  capacidad.
- `capacidad < 0` → `IllegalArgumentException`.
- Devuelve `Resultado(elegidos, valorTotal, pesoTotal, "greedy")`.
- Valor: capacidad 10 → `{P1, P4, P7}`, 78 USD. **No es óptimo y hay que decirlo** (lo
  compara Persona 3 con la mochila 0/1).

---

## Persona 3 — Optimización y búsqueda (3 puntos)

**Orden sugerido:** Mochila 0/1 → Floyd-Warshall → Backtracking → Branch & Bound →
consolidar docs. Branch & Bound es backtracking más una cota: hacerlo después.

### 3.1 Mochila 0/1 — `algorithm/Seleccion.programacionDinamica` (punto de PD)

- `programacionDinamica(List<Item> items, int capacidad)`: tabla `dp[i][w]` **bottom-up**.
- **Recuperar los ítems elegidos** recorriendo la tabla hacia atrás, no solo el valor.
- Pesos con `Math.ceil` para indexar.
- Devuelve `Resultado(elegidos, valorTotal, pesoTotal, "programacion dinamica")`.
- Valor: capacidad 10 → `{P2, P3}`, 95 USD. Documentar la tabla del ejemplo (hito 6).

### 3.2 Floyd-Warshall — `algorithm/CaminosMinimos.floydWarshall` y `compararConDijkstra`

- Partir de `grafo.matrizDeAdyacencia()`. El bucle de `k` va **por fuera** de `i` y `j`.
- Al terminar, si algún `d[i][i] < 0` → `IllegalStateException` con "ciclo de costo
  negativo".
- Devolver solo los pares alcanzables como `"ORIGEN->DESTINO" → km` (infinito no es JSON
  válido), en un `LinkedHashMap` para que el orden sea estable.
- `compararConDijkstra`: correr Dijkstra desde cada vértice reutilizando el método privado
  `correrDijkstra(grafo, origen, -1)` que ya existe (devuelve `dist[]`, expandidos y
  relajaciones), sumar los contadores y verificar que coinciden con Floyd. Floyd informa V³.
- Valores: CD→QUI 27, CD→DN 17, PAL→QUI 34, `costosCoinciden: true`, 1000 operaciones.

### 3.3 Backtracking — `algorithm/Backtracking.buscar` (1 punto)

- Enumerar **todas** las rutas simples de origen a destino con costo ≤ `costoMaximo` y
  cantidad de rutas ≤ `saltosMaximos`.
- DFS recursivo: marcar visitado y agregar al camino al entrar; **desmarcar y quitar al
  volver** (eso es el retroceso).
- **Podar** apenas el costo parcial supera el máximo o se agotan los saltos, sin esperar a
  completar el camino.
- Contar nodos explorados **con poda** y **sin poda** (misma búsqueda sin cortar, filtrando
  al final) y las ramas podadas: son los campos de `ResultadoRutas` (hito 8).
- Validar: `costoMaximo < 0` o `saltosMaximos < 1` → `IllegalArgumentException`.
- `rutasSimples(...)` devuelve `buscar(...).rutas()`.
- Valores: 8 rutas sin límite, 4 con ≤ 37 km, 1 con ≤ 2 saltos.

### 3.4 Branch & Bound — `algorithm/RamificacionYPoda.repartir` (1 punto)

- Repartir **todos** los paquetes entre `k` vehículos minimizando la carga del más
  cargado (makespan).
- Cota inferior: `max(carga máxima actual, suma total / k)`. Explicar **por qué es
  optimista** (hito 11). Podar si `cota >= mejor encontrado`.
- Mejoras recomendadas: ordenar los paquetes de mayor a menor peso (con el MergeSort
  propio), arrancar con la solución greedy LPT como primer "mejor", romper simetría entre
  vehículos con la misma carga.
- Devuelve `Asignacion(porContenedor, cargaMaxima, cotaInicial, nodosExplorados,
  nodosPodados)`, con claves `contenedor1..k`.
- Valores: k=3 → 12 kg (cota 11.33); k=4 → 9; k=2 → 17.

### 3.5 Consolidar documentación

- `docs/COMPLEJIDADES.md` completo (hito 10) con las doce filas.
- README: tabla de integrantes completa, sección "Uso de IA" con lo de cada persona,
  resultados verificados.
- Correr la skill `tpo-verificar-entrega` antes del tag.

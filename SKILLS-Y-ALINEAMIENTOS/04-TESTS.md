# 04 · Tests

## Reglas

- **JUnit 5**, unitarios, **sin Neo4j** y sin levantar Spring. Corren en milisegundos.
- Un archivo por clase de algoritmo: `src/test/java/uade/prog3/tpo/algorithm/<Clase>Test.java`.
- Los datos salen de `GrafoDePrueba`, que arma lo mismo que la carga inicial:
  - `GrafoDePrueba.redLogistica(true)` → red dirigida (Dijkstra, Floyd, backtracking).
  - `GrafoDePrueba.redLogistica(false)` → red no dirigida (Prim, Kruskal).
  - `GrafoDePrueba.paquetes()` → los 8 paquetes.
  - `GrafoDePrueba.item("X", peso, valor)` → un ítem suelto para casos chicos.
- Las clases con dependencias se instancian a mano: `new Seleccion(new Ordenamiento())`.
- Cada valor esperado sale de [01-DOMINIO.md](01-DOMINIO.md), **calculado a mano**. Nunca
  correr el algoritmo y pegar lo que devuelve: eso no prueba nada.
- El mensaje del assert explica el cálculo: `assertEquals(27.0, c.costoTotal(), "CD->DS->AVE->QUI = 10+5+12")`.
- Nombres en español que describen el comportamiento: `dijkstraEligeElCaminoIndirectoCuandoEsMasBarato`.
- Un test que no puede fallar (sin asserts reales) no cuenta.

## Cómo correrlos

```bash
./mvnw test                          # todos
./mvnw test -Dtest=OrdenamientoTest  # una clase
```

`./mvnw test` en verde es requisito de admisibilidad: **nunca mergear con un test rojo**.

## Casos mínimos por algoritmo

Además del caso feliz contra el dominio, cada algoritmo cubre sus bordes. Usar como modelo
[`CaminosMinimosTest`](../src/test/java/uade/prog3/tpo/algorithm/CaminosMinimosTest.java).

| Algoritmo | Casos que tiene que tener |
|---|---|
| QuickSort | orden por ratio del dominio; lista vacía y de un elemento; **no modifica la entrada**; lista ya ordenada y con todos iguales |
| MergeSort | orden por peso del dominio; **estabilidad** (P2 antes que P3); no modifica la entrada; vacía |
| Prim | 9 aristas y 77 km desde CD; mismo costo desde QUI y MOR; grafo no conexo → `IllegalStateException` |
| Kruskal | mismo árbol que Prim; no incluye CD-PAL ni CAB-DS (ciclo); no conexo → excepción; Union-Find detecta ciclo |
| Greedy | capacidad 10 → 78 con P1, P4, P7; capacidad 0 → nada; capacidad negativa → `IllegalArgumentException`; **no supera la capacidad** |
| Mochila 0/1 | capacidad 10 → 95 con P2, P3; **le gana al greedy en el mismo caso**; capacidad 0; ítem que no entra nunca |
| Floyd | CD→QUI 27, CD→DN 17, PAL→QUI 34; QUI→CD ausente; diagonal en 0; ciclo negativo → excepción; peso negativo sin ciclo funciona |
| Floyd vs. Dijkstra | `costosCoinciden`, 1000 operaciones, relajaciones de Dijkstra < operaciones de Floyd |
| Backtracking | 8 rutas sin límite; 4 con ≤ 37 (límite inclusivo); 1 con ≤ 2 saltos; caminos simples (sin repetidos); con poda explora menos que sin poda; sin camino → vacío; parámetros inválidos |
| Branch & Bound | k=3 → 12; k=4 → 9; k=2 → 17; todos los paquetes asignados exactamente una vez; k < 1 → excepción; podados > 0 con k=3 |

## Antes de pedir el PR

```bash
./mvnw test
```

Pegar en la descripción del PR la línea final (`Tests run: N, Failures: 0, Errors: 0`).

# 01 · Dominio y valores esperados

**Esta es la fuente única de verdad de los números del trabajo.** Los tests, las fichas
de `docs/ENDPOINTS.md` y el README tienen que coincidir con esta página. Si un algoritmo
no da estos valores, el bug está en el algoritmo, no en la tabla: todos están calculados a
mano y verificados contra una implementación.

## El dominio en una frase

Red logística de **depósitos y centros de distribución** del AMBA: los nodos son centros
de distribución y depósitos, las aristas son rutas entre ellos, el peso es la **distancia
en kilómetros** y los ítems son **paquetes con peso (kg) y valor (USD)**.

| Concepto | En el dominio | En el código |
|---|---|---|
| Nodo | centro de distribución (`CENTRO`) o depósito (`DEPOSITO`); `valor` = demanda diaria de paquetes | `model/Nodo` |
| Arista | ruta entre dos nodos, **dirigida** (sale de Barracas hacia la periferia) | relación `CONECTA` / `model/Conexion` |
| Peso | distancia de la ruta en **km** | `Conexion.costo` |
| Ítem | paquete: peso en kg (consume capacidad), valor en USD (se maximiza) | `model/Item` |

La carga está en [`seed/CargaInicial.java`](../src/main/java/uade/prog3/tpo/seed/CargaInicial.java).
Los tests usan exactamente los mismos datos a través de `GrafoDePrueba.redLogistica(...)` y
`GrafoDePrueba.paquetes()`, sin tocar Neo4j.

## Nodos

| Id | Nombre | Tipo |
|---|---|---|
| CD | Centro de Distribución Barracas | CENTRO |
| DN | Centro de Distribución Norte (Vicente López) | CENTRO |
| DS | Centro de Distribución Sur (Lanús) | CENTRO |
| DO | Centro de Distribución Oeste (Ramos Mejía) | CENTRO |
| PAL | Depósito Palermo | DEPOSITO |
| CAB | Depósito Caballito | DEPOSITO |
| SI | Depósito San Isidro | DEPOSITO |
| AVE | Depósito Avellaneda | DEPOSITO |
| QUI | Depósito Quilmes | DEPOSITO |
| MOR | Depósito Morón | DEPOSITO |

## Rutas (15, dirigidas, en km)

El orden de esta lista es el orden de la lista de adyacencia: **define el orden de BFS y
DFS**.

| Ruta | km | Ruta | km | Ruta | km |
|---|---|---|---|---|---|
| CD → PAL | 9 | PAL → DN | 8 | DO → MOR | 5 |
| CD → DS | 10 | PAL → CAB | 6 | DO → CAB | 12 |
| CD → CAB | 7 | DN → SI | 12 | DS → AVE | 5 |
| CD → DN | 19 | CAB → DS | 11 | DS → QUI | 19 |
| CD → DO | 18 | CAB → MOR | 17 | AVE → QUI | 12 |

Propiedades buscadas a propósito:

- El camino más corto **no** es el directo en varios pares (CD→DN: directo 19, vía PAL 17;
  DS→QUI: directo 19, vía AVE 17). Sin eso, Dijkstra no se distingue de mirar la arista.
- Como no dirigido es **conexo**: existe el árbol de recubrimiento mínimo.
- Todos los pesos son positivos: Dijkstra aplica.
- Hay **8 rutas simples** de CD a QUI: material para el backtracking.
- QUI y SI no tienen rutas de salida: sirven para probar "destino inalcanzable".

## Paquetes (8)

| Id | Paquete | Peso (kg) | Valor (USD) | Ratio valor/peso | Nodo destino |
|---|---|---|---|---|---|
| P1 | Notebook | 6 | 60 | 10.0 | PAL |
| P2 | Monitor | 5 | 48 | 9.6 | CAB |
| P3 | Impresora | 5 | 47 | 9.4 | SI |
| P4 | Libros | 2 | 14 | 7.0 | AVE |
| P5 | Ropa | 3 | 18 | 6.0 | QUI |
| P6 | Herramientas | 8 | 40 | 5.0 | MOR |
| P7 | Documentación | 1 | 4 | 4.0 | DN |
| P8 | Repuestos | 4 | 12 | 3.0 | DS |

Peso total: **34 kg**. Valor total: 243 USD.

## Valores esperados por algoritmo

### Persona 1 (implementado, con tests)

| Consulta | Resultado esperado |
|---|---|
| BFS desde CD (dirigido) | `CD, PAL, DS, CAB, DN, DO, AVE, QUI, MOR, SI` |
| DFS desde CD (dirigido) | `CD, PAL, DN, SI, CAB, DS, AVE, QUI, MOR, DO` |
| BFS / DFS desde QUI | `QUI` (no tiene rutas de salida) |
| Dijkstra CD → DN | 17 km, `CD → PAL → DN` (gana al directo de 19) |
| Dijkstra CD → QUI | 27 km, `CD → DS → AVE → QUI` (10 + 5 + 12) |
| Dijkstra CD → MOR | 23 km, `CD → DO → MOR` (gana a CD→CAB→MOR = 24) |
| Dijkstra CD → SI | 29 km, `CD → PAL → DN → SI` |
| Dijkstra QUI → CD | error 422: no existe camino |
| UCS CD → CAB | 7 km, 2 nodos expandidos (Dijkstra expande 10) |

### Persona 2

| Consulta | Resultado esperado |
|---|---|
| QuickSort por `ratio` (ascendente) | `P8, P7, P6, P5, P4, P3, P2, P1` |
| QuickSort / MergeSort por `valor` | `P7, P8, P4, P5, P6, P3, P2, P1` |
| MergeSort por `peso` (estable) | `P7, P4, P5, P8, P2, P3, P1, P6` — P2 y P3 pesan 5 y **conservan el orden original** |
| Prim desde cualquier origen | 9 aristas, **77 km** |
| Kruskal | 9 aristas, **77 km** |
| Aristas del MST | DO-MOR 5, DS-AVE 5, PAL-CAB 6, CD-CAB 7, PAL-DN 8, CD-DS 10, DN-SI 12, DO-CAB 12, AVE-QUI 12 |
| Aristas que Kruskal descarta por ciclo | CD-PAL (9) y CAB-DS (11) |
| Greedy, capacidad 10 kg | `{P1, P4, P7}` = **78 USD**, 9 kg |

El MST es único: aunque hay pesos repetidos (5 y 12), todas las aristas de esos pesos
entran. Kruskal en orden: 5, 5, 6, 7, 8, **9 (ciclo)**, 10, **11 (ciclo)**, 12, 12, 12 → 9
aristas.

### Persona 3

| Consulta | Resultado esperado |
|---|---|
| Mochila 0/1, capacidad 10 kg | `{P2, P3}` = **95 USD**, 10 kg (el greedy da 78: contraejemplo del hito 6) |
| Floyd CD → QUI / CD → DN / PAL → QUI | 27 / 17 / 34 km |
| Floyd DO → QUI | 40 km |
| Floyd QUI → CD | no aparece en el resultado (inalcanzable) |
| Floyd vs. Dijkstra × V | `costosCoinciden: true`, `operacionesFloyd: 1000` (V³) |
| Rutas simples CD → QUI, sin límite | **8** rutas: 27, 29, 35, 37, 43, 45, 58, 60 km |
| Rutas CD → QUI, ≤ 37 km | **4** rutas (27, 29, 35, 37; el límite es inclusivo) |
| Rutas CD → QUI, ≤ 30 km | 2 rutas (27 y 29) |
| Rutas CD → QUI, ≤ 2 rutas de largo | 1 ruta: `CD → DS → QUI`, 29 km |
| Rutas QUI → CD | lista vacía |
| Branch & Bound, 3 vehículos | carga máxima **12 kg**, cota inicial 34/3 = 11.33 |
| Branch & Bound, 4 vehículos | carga máxima **9 kg** |
| Branch & Bound, 2 vehículos | carga máxima **17 kg** (= 34/2, la cota ya es la solución) |

Detalle de las 8 rutas CD → QUI:

```
27  CD DS AVE QUI          29  CD DS QUI
35  CD CAB DS AVE QUI      37  CD CAB DS QUI
43  CD PAL CAB DS AVE QUI  45  CD PAL CAB DS QUI
58  CD DO CAB DS AVE QUI   60  CD DO CAB DS QUI
```

Los conteos de nodos explorados y podados (backtracking, Branch & Bound) dependen de cada
implementación: no hay un valor "correcto", pero **con poda tiene que dar menos que sin
poda** y hay que informarlo en la respuesta.

## Cómo recalcular si cambia el grafo

No cambiar el grafo sin acordarlo con el grupo: rompe los tests de todos. Si hace falta,
se cambia en `CargaInicial`, se recalculan los valores de esta página (a mano o con un
script aparte, nunca copiando la salida del propio algoritmo), se actualizan los tests y
recién ahí se commitea, todo en el mismo PR.

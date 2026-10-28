# TPO Programación III — Red logística de depósitos y centros de distribución (AMBA)

Trabajo Práctico Obligatorio de Programación III (3.4.077), UADE, 2° cuatrimestre 2026.
Sistema Spring Boot + Neo4j que implementa los algoritmos de la materia sobre una
red logística de depósitos y centros de distribución del Área Metropolitana de Buenos Aires.

Partimos del [scaffold de la materia](https://github.com/Mancaceresuade/tpo-scaffold-2026):
modelo, conexión a la base y endpoints ya cableados. Todo lo que hay en `algorithm/`
es implementación propia, sin librerías.

## Integrantes

| Nombre completo | Usuario de GitHub |
|---|---|
| Tiziano Greco | [@TizianoGreco](https://github.com/TizianoGreco) |
| _(completar)_ | _(completar)_ |
| _(completar)_ | _(completar)_ |

## Dominio

**Red logística de depósitos y centros de distribución.** Una empresa de logística mueve
paquetes entre sus centros de distribución y sus depósitos del AMBA. Los camiones circulan
por rutas entre ellos y hay que decidir qué recorrer, qué cargar y cómo repartir la carga.

| Concepto | En el dominio | En el modelo |
|---|---|---|
| **Nodo** | Un centro de distribución (`CENTRO`) o un depósito (`DEPOSITO`). El atributo `valor` es la demanda diaria de paquetes del nodo. | `Nodo` |
| **Arista** | Una ruta entre dos nodos, con sentido de circulación (los recorridos salen del centro de Barracas hacia la periferia). | relación `CONECTA` |
| **Peso de la arista** | **Distancia de la ruta, en kilómetros.** "La ruta de Barracas a Palermo mide 9 km." | `Conexion.costo` |
| **Item** | Un paquete a transportar, con **peso en kg** (lo que consume de la capacidad del vehículo) y **valor declarado en USD** (lo que se busca maximizar). | `Item` |

Las tres preguntas de la consigna:

1. **¿Qué número tiene la arista?** Kilómetros. Habilita Dijkstra (camino más corto),
   Prim y Kruskal (red mínima de rutas que conecta todos los nodos).
2. **¿Tiene sentido el camino más barato pasando por intermedios?** Sí: de Barracas a
   Quilmes conviene pasar por el CD Sur y el depósito Avellaneda (27 km) antes que ir al
   CD Sur y de ahí directo a Quilmes (29 km).
3. **¿Hay algo que seleccionar bajo capacidad?** Sí: qué paquetes cargar en un vehículo de
   N kg maximizando el valor transportado (greedy, mochila 0/1), y cómo repartir todos los
   paquetes entre k vehículos minimizando la carga del más cargado (Branch & Bound).

### El grafo

10 nodos (4 centros de distribución y 6 depósitos) y 15 rutas. Distancias en km.

```
                 12
        SI <------------ DN
                       ^  ^
                     8 |  | 19
                       |  |
                 9    PAL <----- CD -----> DO
       (CD -> PAL) ^   |         |  18   / |
                   |   | 6       |      /  | 5
                   |   v         | 7   / 12|
                   |  CAB <-----+-----/    v
                   |   | \                MOR
                   |   |  \ 17 (CAB -> MOR)
                   |11 |
                   |   v         10
                   |  DS <------ CD
                       | \
                     5 |  \ 19
                       v   \
                      AVE --> QUI
                          12
```

Lista de rutas (origen → destino, km):

| Ruta | km | Ruta | km | Ruta | km |
|---|---|---|---|---|---|
| CD → PAL | 9 | PAL → DN | 8 | DO → MOR | 5 |
| CD → DS | 10 | PAL → CAB | 6 | DO → CAB | 12 |
| CD → CAB | 7 | DN → SI | 12 | DS → AVE | 5 |
| CD → DN | 19 | CAB → DS | 11 | DS → QUI | 19 |
| CD → DO | 18 | CAB → MOR | 17 | AVE → QUI | 12 |

| Id | Nodo | Tipo |
|---|---|---|
| CD | Centro de Distribución Barracas | CENTRO |
| DN | Centro de Distribución Norte (Vicente López) | CENTRO |
| DS | Centro de Distribución Sur (Lanús) | CENTRO |
| DO | Centro de Distribución Oeste (Ramos Mejía) | CENTRO |
| PAL, CAB, SI, AVE, QUI, MOR | Depósitos Palermo, Caballito, San Isidro, Avellaneda, Quilmes, Morón | DEPOSITO |

El grafo está armado a propósito para que el camino más corto no sea el directo en
varios pares (CD→DN: directo 19 km, vía PAL 17; DS→QUI: directo 19, vía AVE 17), para que
como no dirigido sea conexo (MST de 9 rutas y 77 km) y para que haya 8 rutas simples
distintas de CD a QUI para el backtracking.

### Los paquetes

| Id | Paquete | Peso (kg) | Valor (USD) | Ratio |
|---|---|---|---|---|
| P1 | Notebook | 6 | 60 | 10.0 |
| P2 | Monitor | 5 | 48 | 9.6 |
| P3 | Impresora | 5 | 47 | 9.4 |
| P4 | Libros | 2 | 14 | 7.0 |
| P5 | Ropa | 3 | 18 | 6.0 |
| P6 | Herramientas | 8 | 40 | 5.0 |
| P7 | Documentación | 1 | 4 | 4.0 |
| P8 | Repuestos | 4 | 12 | 3.0 |

Con un vehículo de **10 kg**, el greedy por ratio carga {P1, P4, P7} = 78 USD; el óptimo
(mochila 0/1) es {P2, P3} = 95 USD. Es el contraejemplo del hito 6.

## Resultados verificados a mano

| Consulta | Resultado | Cálculo |
|---|---|---|
| Dijkstra CD → DN | 17 km, CD→PAL→DN | 9 + 8, gana al directo (19) |
| Dijkstra CD → QUI | 27 km, CD→DS→AVE→QUI | 10 + 5 + 12 |
| Dijkstra CD → MOR | 23 km, CD→DO→MOR | 18 + 5, gana a CD→CAB→MOR (24) |
| Prim / Kruskal | 9 rutas, 77 km | 5+5+6+7+8+10+12+12+12 |
| Greedy, capacidad 10 | 78 USD | P1 + P4 + P7 |
| Mochila 0/1, capacidad 10 | 95 USD | P2 + P3 |
| Rutas CD → QUI, ≤ 37 km | 4 rutas | 27, 29, 35, 37 |
| Reparto en 3 vehículos | carga máxima 12 kg | {8,4} {6,5,1} {5,3,2}; cota 34/3 = 11.33 |

## Arranque

Requisitos: JDK 17 y una instancia de Neo4j 5 (local o Aura).

```bash
# 1. Credenciales por variables de entorno (el .env esta en .gitignore)
cp .env.example .env          # completar NEO4J_PASSWORD
export $(cat .env | xargs)

# 2. Compilar y ejecutar. La primera vez carga el dominio en la base.
./mvnw spring-boot:run

# 3. Verificar
curl "http://localhost:8080/api/grafo/resumen"
# -> {"vertices":10,"aristas":15,"dirigido":true,"ids":["CD","DN",...]}

# 4. Tests (no necesitan Neo4j)
./mvnw test
```

Si la base ya tiene datos, la carga inicial no se ejecuta. Para recargar el dominio:
`MATCH (n) DETACH DELETE n` en Neo4j y reiniciar la aplicación.

## Endpoints

Cada algoritmo tiene su ficha con ejemplo real, complejidad justificada y estructura de
datos en [docs/ENDPOINTS.md](docs/ENDPOINTS.md). El informe de complejidades completo
(hito 10) está en [docs/COMPLEJIDADES.md](docs/COMPLEJIDADES.md). Las requests listas
para ejecutar están en [docs/api.http](docs/api.http).

| Método | Endpoint | Algoritmo | Puntos |
|---|---|---|---|
| GET | `/api/grafo/resumen` | verificación | — |
| GET | `/api/grafo/dfs?origen=CD` | DFS | 1 |
| GET | `/api/grafo/bfs?origen=CD` | BFS | 1 |
| GET | `/api/grafo/dijkstra?origen=CD&destino=QUI` | Dijkstra | 1 |
| GET | `/api/grafo/prim?origen=CD` | Prim | 1 |
| GET | `/api/grafo/kruskal` | Kruskal | 1 |
| GET | `/api/grafo/floyd` | Floyd-Warshall | 1 (PD) |
| GET | `/api/grafo/floyd/comparacion` | Floyd vs. Dijkstra × V | — |
| GET | `/api/grafo/ucs?origen=CD&destino=QUI` | UCS | opcional |
| GET | `/api/seleccion/greedy?capacidad=10` | Greedy | 1 |
| GET | `/api/seleccion/dinamica?capacidad=10` | Mochila 0/1 con PD | 1 (PD) |
| GET | `/api/seleccion/quicksort?criterio=ratio` | QuickSort | 1 |
| GET | `/api/seleccion/mergesort?criterio=peso` | MergeSort | (con QuickSort) |
| GET | `/api/grafo/rutas?origen=CD&destino=QUI&costoMaximo=37&saltosMaximos=5` | Backtracking | 1 |
| GET | `/api/seleccion/repartir?contenedores=3` | Branch & Bound | 1 |

## Estructura

```
model/          Nodo, Conexion (ruta con distancia en km), Item (paquete)
repository/     acceso a Neo4j (repositorios bloqueantes)
service/        Grafo (lista de adyacencia en memoria) y GrafoService (carga)
algorithm/      TODOS los algoritmos. Sin librerias, sin acceso a la base
controller/     endpoints: cargan el grafo una vez y delegan
config/         manejo de errores: ningun endpoint devuelve un stack trace
seed/           carga del dominio propio
docs/           ENDPOINTS.md, COMPLEJIDADES.md, api.http
src/test/       un test (o mas) por algoritmo, unitarios, sin Neo4j
```

Decisiones de diseño que hacen a la nota:

- Los algoritmos reciben el `Grafo` ya cargado y nunca consultan la base en su bucle.
- El controller solo carga y delega. Prim y Kruskal cargan el grafo como no dirigido.
- Toda excepción se traduce a JSON con `estado` y `detalle`: 400 para parámetros
  inválidos, 422 para estados imposibles (destino inalcanzable, grafo no conexo, ciclo
  negativo), 404 para rutas inexistentes, 500 genérico con el detalle en el log.
- Kruskal, greedy y Branch & Bound reutilizan el MergeSort propio para ordenar.

## Uso de IA

Se usó **Claude Code** (Anthropic) como asistente de programación durante el desarrollo:
para escribir la primera versión de los algoritmos y sus tests a partir de las
especificaciones del scaffold, para redactar la documentación de complejidades y para
verificar los resultados contra el cálculo a mano sobre el grafo del grupo. Todo el código
generado fue leído, ejecutado y revisado por los integrantes, que pueden explicar y
reescribir cada algoritmo; el Segundo Parcial es la verificación de eso.

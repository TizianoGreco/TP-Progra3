# TPO Programación III — Red logística de reparto (AMBA)

Trabajo Práctico Obligatorio de Programación III (3.4.077), UADE, 2° cuatrimestre 2026.
Sistema Spring Boot + Neo4j que implementa los algoritmos de la materia sobre una
red logística de distribución de paquetes en el Área Metropolitana de Buenos Aires.

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

Una empresa de logística reparte paquetes desde un centro de distribución a depósitos
regionales y puntos de entrega del AMBA. Los camiones circulan por tramos de ruta
planificados y hay que decidir qué recorrer, qué cargar y cómo repartir la carga.

| Concepto | En el dominio | En el modelo |
|---|---|---|
| **Nodo** | Un punto de la red: el centro de distribución (`CENTRO`), un depósito regional (`DEPOSITO`) o un punto de entrega (`ENTREGA`). El atributo `valor` es la demanda diaria de paquetes del punto. | `Nodo` |
| **Arista** | Un tramo de ruta planificado entre dos puntos, con sentido de circulación (los recorridos salen del centro hacia la periferia). | relación `CONECTA` |
| **Costo de la arista** | **Tiempo de viaje del tramo, en minutos.** "La conexión entre Barracas y Palermo cuesta 15 minutos." | `Conexion.costo` |
| **Item** | Un paquete a transportar, con **peso en kg** (lo que consume de la capacidad del vehículo) y **valor declarado en USD** (lo que se busca maximizar). | `Item` |

Las tres preguntas de la consigna:

1. **¿Qué número tiene la arista?** Minutos de viaje. Habilita Dijkstra (ruta más rápida),
   Prim y Kruskal (red mínima de tramos que conecta todos los puntos).
2. **¿Tiene sentido el camino más barato pasando por intermedios?** Sí: de Barracas a
   Quilmes conviene pasar por el Depósito Sur y Avellaneda (52 min) antes que ir directo
   al depósito y de ahí a Quilmes (55 min).
3. **¿Hay algo que seleccionar bajo capacidad?** Sí: qué paquetes cargar en un vehículo de
   N kg maximizando el valor transportado (greedy, mochila 0/1), y cómo repartir todos los
   paquetes entre k vehículos minimizando la carga del más cargado (Branch & Bound).

### El grafo

10 nodos y 15 tramos. Costos en minutos.

```
                 18
        SI <------------ DN
                       ^  ^
                    12 |  | 30
                       |  |
                15    PAL <----- CD -----> DO
       (CD -> PAL) ^   |         |  30   / |
                   |   | 14      |      /  | 16
                   |   v         | 22  / 25|
                   |  CAB <-----+-----/    v
                   |   | \                MOR
                   |   |  \ 28 (CAB -> MOR)
                   |18 |
                   |   v         20
                   |  DS <------ CD
                       | \
                    12 |  \ 35
                       v   \
                      AVE --> QUI
                          20
```

Lista de tramos (origen → destino, minutos):

| Tramo | min | Tramo | min | Tramo | min |
|---|---|---|---|---|---|
| CD → PAL | 15 | PAL → DN | 12 | DO → MOR | 16 |
| CD → DS | 20 | PAL → CAB | 14 | DO → CAB | 25 |
| CD → CAB | 22 | DN → SI | 18 | DS → AVE | 12 |
| CD → DN | 30 | CAB → DS | 18 | DS → QUI | 35 |
| CD → DO | 30 | CAB → MOR | 28 | AVE → QUI | 20 |

| Id | Nodo | Tipo |
|---|---|---|
| CD | Centro de Distribución Barracas | CENTRO |
| DN | Depósito Norte (Vicente López) | DEPOSITO |
| DS | Depósito Sur (Lanús) | DEPOSITO |
| DO | Depósito Oeste (Ramos Mejía) | DEPOSITO |
| PAL, CAB, SI, AVE, QUI, MOR | Palermo, Caballito, San Isidro, Avellaneda, Quilmes, Morón | ENTREGA |

El grafo está armado a propósito para que el camino más rápido no sea el directo en
varios pares (CD→DN: directo 30, vía PAL 27; DS→QUI: directo 35, vía AVE 32), para que
como no dirigido sea conexo (MST de 9 tramos y 150 min) y para que haya 8 rutas simples
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
| Dijkstra CD → DN | 27 min, CD→PAL→DN | 15 + 12, gana al directo (30) |
| Dijkstra CD → QUI | 52 min, CD→DS→AVE→QUI | 20 + 12 + 20 |
| Dijkstra CD → MOR | 46 min, CD→DO→MOR | 30 + 16, gana a CD→CAB→MOR (50) |
| Prim / Kruskal | 9 tramos, 150 min | 12+12+14+15+16+18+18+20+25 |
| Greedy, capacidad 10 | 78 USD | P1 + P4 + P7 |
| Mochila 0/1, capacidad 10 | 95 USD | P2 + P3 |
| Rutas CD → QUI, ≤ 75 min | 4 rutas | 52, 55, 72, 75 |
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
| GET | `/api/grafo/rutas?origen=CD&destino=QUI&costoMaximo=75&saltosMaximos=5` | Backtracking | 1 |
| GET | `/api/seleccion/repartir?contenedores=3` | Branch & Bound | 1 |

## Estructura

```
model/          Nodo, Conexion (tramo con costo en minutos), Item (paquete)
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

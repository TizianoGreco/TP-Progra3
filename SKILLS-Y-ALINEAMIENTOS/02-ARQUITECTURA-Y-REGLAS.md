# 02 · Arquitectura y reglas del proyecto

## Stack

- Java 17, Spring Boot 3.5, Spring Data Neo4j, JUnit 5. Build con el wrapper: `./mvnw`.
- Neo4j 5 local (Homebrew) o Aura. Credenciales **solo** por variables de entorno
  (`NEO4J_URI`, `NEO4J_USER`, `NEO4J_PASSWORD`), nunca en el repo.
- Base: el [scaffold de la materia](https://github.com/Mancaceresuade/tpo-scaffold-2026).

## Capas

```
model/          Nodo, Conexion (ruta con distancia en km), Item (paquete)
repository/     acceso a Neo4j
service/        Grafo (lista de adyacencia en memoria) y GrafoService (carga desde Neo4j)
algorithm/      TODOS los algoritmos. Sin librerías, sin acceso a la base
controller/     endpoints: cargan los datos una vez y delegan en algorithm/
config/         ManejadorDeErrores: traduce excepciones a JSON
seed/           CargaInicial: el dominio del grupo
src/test/       tests unitarios, sin Neo4j
```

El flujo de cada request es siempre el mismo:

```
controller → GrafoService.cargar(dirigido) → Grafo en memoria → algorithm.metodo(grafo, ...) → JSON
```

## Para implementar un algoritmo se toca UN archivo

Los endpoints ya están cableados y los métodos ya tienen la firma definitiva. Para que un
endpoint funcione hay que reemplazar el `throw new PendienteDeImplementar(...)` del método
correspondiente en `algorithm/`. **No hace falta tocar controllers, servicios ni modelos.**

| Endpoint | Clase y método |
|---|---|
| `/api/seleccion/quicksort`, `/mergesort` | `Ordenamiento.quickSort`, `Ordenamiento.mergeSort` |
| `/api/grafo/prim`, `/kruskal` | `ArbolRecubrimiento.prim`, `ArbolRecubrimiento.kruskal` |
| `/api/seleccion/greedy`, `/dinamica` | `Seleccion.greedy`, `Seleccion.programacionDinamica` |
| `/api/grafo/floyd`, `/floyd/comparacion` | `CaminosMinimos.floydWarshall`, `CaminosMinimos.compararConDijkstra` |
| `/api/grafo/rutas` | `Backtracking.buscar` (y `rutasSimples`, que delega en `buscar`) |
| `/api/seleccion/repartir` | `RamificacionYPoda.repartir` |

Los **records de respuesta** (`Mst`, `Resultado`, `ResultadoRutas`, `Asignacion`, etc.)
ya están definidos y no se cambian: los controllers y la documentación dependen de ellos.
Sí se pueden agregar records o clases **privadas** de apoyo dentro de la misma clase.

## La API de `Grafo` (lo único que necesitan los algoritmos de grafos)

```java
int n = grafo.cantidadVertices();          // V
int u = grafo.indiceDe("CD");              // id de negocio -> índice 0..V-1 (400 si no existe)
String id = grafo.idDe(u);                 // índice -> id de negocio
for (Grafo.Arista a : grafo.vecinos(u)) {  // lista de adyacencia, en orden de carga
    int v = a.destino();
    double km = a.costo();
}
double[][] m = grafo.matrizDeAdyacencia(); // copia nueva: 0 en la diagonal, INFINITO sin arista
grafo.esDirigido();                        // Prim y Kruskal reciben el grafo NO dirigido
Grafo.INFINITO                             // Double.POSITIVE_INFINITY
```

- Los algoritmos trabajan con **índices `int`**, y convierten a ids recién al armar la
  respuesta. Así los arreglos (`dist[]`, `visitado[]`, `padre[]`) son O(1).
- En el grafo no dirigido cada arista aparece en la lista de los dos extremos.
- `matrizDeAdyacencia()` devuelve una copia: Floyd la puede modificar sin romper nada.

## Reglas que hacen a la nota (no negociables)

| Regla | Por qué | Si se rompe |
|---|---|---|
| **Implementación propia, sin librerías.** Nada de `Collections.sort`, `Arrays.sort`, `List.sort`, `stream().sorted()`, JGraphT. | El punto es implementar el algoritmo | el algoritmo **no cuenta** (0 %) |
| **Para ordenar se usa el MergeSort propio** (`Ordenamiento.mergeSort`). Kruskal, greedy y Branch & Bound lo reciben por constructor (`public Seleccion(Ordenamiento ordenamiento)`, Spring lo inyecta solo). | Misma regla de arriba | ídem |
| **Nada de lógica en el controller.** El controller carga y delega. | Penalización explícita | −0,5 |
| **El algoritmo no consulta la base.** Recibe el `Grafo` o la lista de ítems ya cargados. | La complejidad defendida tiene que ser la del algoritmo | −0,5 por algoritmo |
| **Ningún endpoint devuelve un stack trace.** Se lanzan las excepciones de abajo y el `ManejadorDeErrores` las traduce. | Penalización explícita | −0,5 |
| **La estructura sostiene la complejidad declarada.** Si se declara O((V+E) log V), tiene que haber un heap. | Criterio de "Completo" | 50 % del punto |
| **Cada algoritmo tiene al menos un test y su ficha en `docs/ENDPOINTS.md`.** | Criterio de "Completo" y de admisibilidad | 50 % o entrega devuelta |
| **No se modifica la lista de entrada.** Los ordenamientos devuelven una lista nueva. | Lo pide el scaffold y lo testeamos | test rojo |
| **Credenciales fuera del repo**, también del historial. | Admisibilidad | entrega devuelta sin corregir |

## Errores: qué excepción lanzar

| Situación | Excepción | HTTP |
|---|---|---|
| Parámetro inválido (capacidad negativa, k < 1, costo máximo negativo, nodo inexistente) | `IllegalArgumentException` con mensaje claro | 400 |
| Estado imposible (destino inalcanzable, grafo no conexo, ciclo negativo) | `IllegalStateException` con mensaje claro | 422 |
| Algoritmo todavía no implementado | `PendienteDeImplementar("Nombre")` | 501 |

Los tests verifican el **mensaje** (`e.getMessage().contains(...)`), así que el texto tiene
que decir qué pasó: `"Prim: el grafo no es conexo"`, no `"error"`.

## Estilo de código (para que parezca un solo proyecto)

Mirar [`CaminosMinimos.java`](../src/main/java/uade/prog3/tpo/algorithm/CaminosMinimos.java)
y [`Recorridos.java`](../src/main/java/uade/prog3/tpo/algorithm/Recorridos.java) como modelo.

- Nombres en **español**: `visitado`, `predecesor`, `costoTotal`, `capacidad`.
- Comentarios **sin tildes ni ñ** dentro del código Java (como el resto del repo).
- Cada método público lleva un Javadoc con: qué resuelve, la **estructura de datos**, la
  **complejidad justificada** (no solo el orden: por qué) y, si aplica, la recurrencia.
- El encabezado de la clase dice `UNIDAD`, `PUNTAJE` y `A CARGO DE`. No borrarlo.
- Métodos privados chicos para separar el núcleo del algoritmo del armado de la respuesta
  (ver `correrDijkstra` / `armarCamino`).
- Validar parámetros al principio del método y fallar rápido.
- Sin `System.out.println` en el código de producción.

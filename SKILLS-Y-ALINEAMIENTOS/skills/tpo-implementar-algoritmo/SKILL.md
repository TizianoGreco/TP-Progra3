---
name: tpo-implementar-algoritmo
description: Implementa un algoritmo pendiente del TPO de Programación III (QuickSort, MergeSort, Prim, Kruskal, Greedy, Mochila 0/1, Floyd-Warshall, Backtracking, Branch & Bound) siguiendo las reglas del grupo, con sus tests y su ficha de documentación. Usar cuando alguien del grupo pida implementar, completar o arreglar uno de esos algoritmos, o cuando un endpoint responda 501.
---

# Implementar un algoritmo del TPO

Trabajás en el repo `TP-Progra3` (Spring Boot + Neo4j, Java 17). Antes de escribir código,
leé de `SKILLS-Y-ALINEAMIENTOS/`:

1. `01-DOMINIO.md` — los valores esperados del algoritmo.
2. `02-ARQUITECTURA-Y-REGLAS.md` — qué archivo tocar y qué está prohibido.
3. La sección del algoritmo en `03-REPARTO-Y-TAREAS.md`.

## Pasos

1. **Ubicar el stub.** El método está en `src/main/java/uade/prog3/tpo/algorithm/` y lanza
   `PendienteDeImplementar`. La firma y el record de respuesta **no se cambian**.
2. **Preguntar a la persona si quiere escribirlo ella primero** y que vos lo revises. Si
   prefiere que lo escribas vos, explicá cada decisión mientras lo hacés: en el Segundo
   Parcial lo tiene que escribir sin ayuda.
3. **Implementar** reemplazando solo el cuerpo del método (más métodos o records privados
   de apoyo en la misma clase si hacen falta):
   - Sin `Collections.sort`, `Arrays.sort`, `List.sort`, `stream().sorted()` ni librerías de
     grafos. Para ordenar, `Ordenamiento.mergeSort` inyectado por constructor.
   - Trabajar con índices `int` del `Grafo` y convertir a ids al armar la respuesta.
   - Parámetros inválidos → `IllegalArgumentException`; estado imposible (no conexo,
     inalcanzable, ciclo negativo) → `IllegalStateException`. Mensajes claros.
   - Javadoc con estructura de datos, complejidad **justificada** y recurrencia si aplica.
   - Estilo del repo: nombres en español, comentarios sin tildes, como `CaminosMinimos.java`.
4. **Tests** en `src/test/java/uade/prog3/tpo/algorithm/<Clase>Test.java` con los casos de
   `04-TESTS.md`. Valores de `01-DOMINIO.md`, nunca copiados de la salida del algoritmo.
5. **Correr** `./mvnw test`. Si un test falla, el bug está en el algoritmo: **no cambiar el
   valor esperado**. Si de verdad la tabla está mal, frenar y avisar al grupo.
6. **Documentar** según `05-DOCUMENTACION.md`: ficha en `docs/ENDPOINTS.md` (con respuesta
   real), fila en `docs/COMPLEJIDADES.md`, estado `hecho` en la tabla de endpoints del README.
7. **Commit y PR** según `06-GIT-Y-ENTREGA.md`: rama propia, commits de la persona desde su
   usuario, PR a `main` con merge commit.
8. **Recordarle** declarar el uso de IA en el README (`07-USO-DE-IA.md`) y practicar el
   algoritmo con la skill `tpo-estudiar-parcial`.

## Checklist de salida

- [ ] Da los valores de `01-DOMINIO.md`.
- [ ] Sin librerías ni `sort` de Java; la estructura sostiene la complejidad declarada.
- [ ] Tests nuevos en verde y `./mvnw test` completo en verde.
- [ ] Ficha, fila de complejidades y estado del README actualizados.
- [ ] La persona puede explicar dónde está la idea central (relajación, poda, retroceso, cota).

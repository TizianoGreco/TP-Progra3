package uade.prog3.tpo.algorithm;

import org.junit.jupiter.api.Test;
import uade.prog3.tpo.service.Grafo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecorridosTest {

    private final Recorridos recorridos = new Recorridos();

    @Test
    void bfsVisitaPorNivelesDesdeElCentro() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        List<String> orden = recorridos.bfs(g, "CD");
        // vecinos ordenados por km: CD -> CAB 7, PAL 9, DS 10, DO 18, DN 19
        // nivel 0: CD; nivel 1: CAB PAL DS DO DN;
        // nivel 2: MOR (desde CAB), AVE y QUI (desde DS), SI (desde DN)
        assertEquals(List.of("CD", "CAB", "PAL", "DS", "DO", "DN", "MOR", "AVE", "QUI", "SI"), orden);
    }

    @Test
    void bfsNoRepiteVerticesAunqueHayaVariosCaminos() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        List<String> orden = recorridos.bfs(g, "CD");
        assertEquals(10, orden.size());
        assertEquals(10, orden.stream().distinct().count(), "cada vertice aparece una sola vez");
    }

    @Test
    void bfsDesdeUnaHojaSoloSeVisitaASiMisma() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        assertEquals(List.of("QUI"), recorridos.bfs(g, "QUI"), "QUI no tiene rutas de salida");
    }

    @Test
    void dfsProfundizaAntesDeVolver() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        List<String> orden = recorridos.dfs(g, "CD");
        // CD -> CAB (7) -> DS (11) -> AVE (5) -> QUI (fondo); vuelve a CAB -> MOR;
        // vuelve a CD -> PAL (9) -> DN (8) -> SI; vuelve a CD -> DO (18)
        assertEquals(List.of("CD", "CAB", "DS", "AVE", "QUI", "MOR", "PAL", "DN", "SI", "DO"), orden);
    }

    @Test
    void dfsYBfsVisitanLosMismosVerticesEnDistintoOrden() {
        Grafo g = GrafoDePrueba.redLogistica(false);
        List<String> dfs = recorridos.dfs(g, "MOR");
        List<String> bfs = recorridos.bfs(g, "MOR");
        assertEquals(10, dfs.size());
        assertEquals(10, bfs.size());
        assertTrue(dfs.containsAll(bfs));
        assertNotEquals(dfs, bfs);
    }

    @Test
    void elRecorridoNoDependeDelOrdenEnQueLlegaronLasRutas() {
        // Neo4j no garantiza el orden de las relaciones: el mismo grafo cargado
        // con las rutas al reves tiene que recorrerse igual
        Grafo alReves = new Grafo(true);
        for (String id : GrafoDePrueba.redLogistica(true).ids()) {
            alReves.agregarVertice(id);
        }
        List<String[]> rutas = new java.util.ArrayList<>();
        Grafo original = GrafoDePrueba.redLogistica(true);
        for (int u = 0; u < original.cantidadVertices(); u++) {
            for (Grafo.Arista a : original.vecinos(u)) {
                rutas.add(new String[] {original.idDe(u), original.idDe(a.destino()), String.valueOf(a.costo())});
            }
        }
        for (int i = rutas.size() - 1; i >= 0; i--) {
            alReves.agregarArista(rutas.get(i)[0], rutas.get(i)[1], Double.parseDouble(rutas.get(i)[2]));
        }

        assertEquals(recorridos.bfs(original, "CD"), recorridos.bfs(alReves, "CD"));
        assertEquals(recorridos.dfs(original, "CD"), recorridos.dfs(alReves, "CD"));
    }

    @Test
    void origenInexistenteFallaConMensajeClaro() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        assertThrows(IllegalArgumentException.class, () -> recorridos.dfs(g, "ZZZ"));
        assertThrows(IllegalArgumentException.class, () -> recorridos.bfs(g, "ZZZ"));
    }
}

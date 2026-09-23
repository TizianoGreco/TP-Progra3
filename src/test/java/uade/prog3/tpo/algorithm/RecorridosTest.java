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
        // nivel 0: CD; nivel 1: sus 5 vecinos directos en orden de adyacencia;
        // nivel 2: AVE y QUI (desde DS), MOR (desde CAB), SI (desde DN)
        assertEquals(List.of("CD", "PAL", "DS", "CAB", "DN", "DO", "AVE", "QUI", "MOR", "SI"), orden);
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
        assertEquals(List.of("QUI"), recorridos.bfs(g, "QUI"), "QUI no tiene tramos de salida");
    }

    @Test
    void dfsProfundizaAntesDeVolver() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        List<String> orden = recorridos.dfs(g, "CD");
        // CD -> PAL -> DN -> SI (fondo), vuelve a PAL -> CAB -> DS -> AVE -> QUI, ...
        assertEquals(List.of("CD", "PAL", "DN", "SI", "CAB", "DS", "AVE", "QUI", "MOR", "DO"), orden);
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
    void origenInexistenteFallaConMensajeClaro() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        assertThrows(IllegalArgumentException.class, () -> recorridos.dfs(g, "ZZZ"));
        assertThrows(IllegalArgumentException.class, () -> recorridos.bfs(g, "ZZZ"));
    }
}

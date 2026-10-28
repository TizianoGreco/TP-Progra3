package uade.prog3.tpo.algorithm;

import org.junit.jupiter.api.Test;
import uade.prog3.tpo.service.Grafo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CaminosMinimosTest {

    private final CaminosMinimos caminos = new CaminosMinimos();

    // ---------------- Dijkstra ----------------

    @Test
    void dijkstraEligeElCaminoIndirectoCuandoEsMasBarato() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        CaminosMinimos.Camino c = caminos.dijkstra(g, "CD", "DN");
        assertEquals(17.0, c.costoTotal(), "directo son 19 km, via Palermo 9+8=17");
        assertEquals(List.of("CD", "PAL", "DN"), c.vertices());
    }

    @Test
    void dijkstraReconstruyeElCaminoCompletoAQuilmes() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        CaminosMinimos.Camino c = caminos.dijkstra(g, "CD", "QUI");
        assertEquals(27.0, c.costoTotal(), "CD->DS->AVE->QUI = 10+5+12");
        assertEquals(List.of("CD", "DS", "AVE", "QUI"), c.vertices());
    }

    @Test
    void dijkstraAMoronVaPorElDepositoOeste() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        CaminosMinimos.Camino c = caminos.dijkstra(g, "CD", "MOR");
        assertEquals(23.0, c.costoTotal(), "via DO 18+5=23 contra via CAB 7+17=24");
        assertEquals(List.of("CD", "DO", "MOR"), c.vertices());
    }

    @Test
    void dijkstraOrigenIgualADestinoCuestaCero() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        CaminosMinimos.Camino c = caminos.dijkstra(g, "CD", "CD");
        assertEquals(0.0, c.costoTotal());
        assertEquals(List.of("CD"), c.vertices());
    }

    @Test
    void dijkstraDestinoInalcanzableFalla() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> caminos.dijkstra(g, "QUI", "CD"));
        assertTrue(e.getMessage().contains("no existe camino"));
    }

    @Test
    void dijkstraRechazaPesosNegativos() {
        Grafo g = new Grafo(true);
        g.agregarArista("A", "B", 4);
        g.agregarArista("B", "C", -2);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> caminos.dijkstra(g, "A", "C"));
        assertTrue(e.getMessage().contains("B->C"));
    }

    // ---------------- UCS ----------------

    @Test
    void ucsDaElMismoCostoQueDijkstraExpandiendoMenosNodos() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        CaminosMinimos.Camino dijkstra = caminos.dijkstra(g, "CD", "CAB");
        CaminosMinimos.Camino ucs = caminos.ucs(g, "CD", "CAB");
        assertEquals(dijkstra.costoTotal(), ucs.costoTotal());
        assertEquals(dijkstra.vertices(), ucs.vertices());
        assertEquals(10, dijkstra.nodosExpandidos(), "Dijkstra recorre todo el grafo alcanzable");
        assertEquals(2, ucs.nodosExpandidos(), "UCS corta al expandir CAB, el mas cercano a CD");
    }
}

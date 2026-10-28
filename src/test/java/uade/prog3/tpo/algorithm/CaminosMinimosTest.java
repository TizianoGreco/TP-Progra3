package uade.prog3.tpo.algorithm;

import org.junit.jupiter.api.Test;
import uade.prog3.tpo.service.Grafo;

import java.util.List;
import java.util.Map;

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

    // ---------------- Floyd-Warshall ----------------

    @Test
    void floydCoincideConDijkstraEnTodosLosPares() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        Map<String, Double> d = caminos.floydWarshall(g);
        assertEquals(27.0, d.get("CD->QUI"));
        assertEquals(17.0, d.get("CD->DN"));
        assertEquals(34.0, d.get("PAL->QUI"), "PAL->CAB->DS->AVE->QUI = 6+11+5+12");
        assertEquals(0.0, d.get("SI->SI"));
        assertNull(d.get("QUI->CD"), "los pares inalcanzables no se informan");
    }

    @Test
    void floydDetectaCicloNegativo() {
        Grafo g = new Grafo(true);
        g.agregarArista("A", "B", 2);
        g.agregarArista("B", "C", -5);
        g.agregarArista("C", "A", 1);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> caminos.floydWarshall(g));
        assertTrue(e.getMessage().contains("ciclo de costo negativo"));
    }

    @Test
    void floydAceptaPesosNegativosSinCiclo() {
        Grafo g = new Grafo(true);
        g.agregarArista("A", "B", 5);
        g.agregarArista("B", "C", -3);
        g.agregarArista("A", "C", 4);
        assertEquals(2.0, caminos.floydWarshall(g).get("A->C"), "A->B->C = 5-3 = 2 gana al directo 4");
    }

    @Test
    void comparacionFloydContraDijkstraVVeces() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        CaminosMinimos.ComparacionTodosContraTodos c = caminos.compararConDijkstra(g);
        assertTrue(c.costosCoinciden());
        assertEquals(10, c.vertices());
        assertEquals(1000, c.operacionesFloyd(), "V^3 = 10^3");
        assertTrue(c.relajacionesDijkstraV() < c.operacionesFloyd(),
                "en un grafo ralo, V corridas de Dijkstra hacen menos trabajo que Floyd");
    }
}

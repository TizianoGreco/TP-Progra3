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
    void floydDaLosMismosCaminosMinimosDelDominio() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        Map<String, Double> d = caminos.floydWarshall(g);

        assertEquals(27.0, d.get("CD->QUI"), "CD->DS->AVE->QUI = 10+5+12");
        assertEquals(17.0, d.get("CD->DN"), "via PAL 9+8, mejor que el directo de 19");
        assertEquals(34.0, d.get("PAL->QUI"), "PAL->CAB->DS->AVE->QUI = 6+11+5+12");
        assertEquals(40.0, d.get("DO->QUI"), "DO->CAB->DS->AVE->QUI = 12+11+5+12");
        assertEquals(0.0, d.get("CD->CD"));
        assertEquals(0.0, d.get("QUI->QUI"));
        assertNull(d.get("QUI->CD"), "desde Quilmes no se vuelve a Barracas");
        assertFalse(d.containsValue(Grafo.INFINITO));
    }

    @Test
    void floydDetectaUnCicloDeCostoNegativo() {
        Grafo g = new Grafo(true);
        g.agregarArista("A", "B", 1);
        g.agregarArista("B", "A", -2);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> caminos.floydWarshall(g));
        assertTrue(e.getMessage().contains("ciclo de costo negativo"));
    }

    @Test
    void floydAceptaUnPesoNegativoSiNoHayCiclo() {
        Grafo g = new Grafo(true);
        g.agregarArista("A", "B", 4);
        g.agregarArista("B", "C", -1);
        g.agregarArista("A", "C", 10);

        Map<String, Double> d = caminos.floydWarshall(g);

        assertEquals(3.0, d.get("A->C"), "A->B->C = 4-1, mejor que el directo de 10");
        assertEquals(0.0, d.get("A->A"));
        assertEquals(0.0, d.get("C->C"));
    }

    @Test
    void floydCoincideConDijkstraYCuestaMilOperaciones() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        CaminosMinimos.ComparacionTodosContraTodos c = caminos.compararConDijkstra(g);

        assertTrue(c.costosCoinciden());
        assertEquals(10, c.vertices());
        assertEquals(1000L, c.operacionesFloyd(), "V^3 = 10^3");
        assertTrue(c.relajacionesDijkstraV() < c.operacionesFloyd(),
                "Dijkstra desde cada nodo relaja menos aristas que las V^3 celdas de Floyd");
    }
}

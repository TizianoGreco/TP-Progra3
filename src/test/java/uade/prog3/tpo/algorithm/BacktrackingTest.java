package uade.prog3.tpo.algorithm;

import org.junit.jupiter.api.Test;
import uade.prog3.tpo.service.Grafo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BacktrackingTest {

    private final Backtracking backtracking = new Backtracking();

    /**
     * Rutas simples CD -> QUI en la red dirigida (calculadas a mano):
     *   CD-DS-QUI 29 | CD-DS-AVE-QUI 27 | CD-CAB-DS-QUI 37 | CD-CAB-DS-AVE-QUI 35
     *   CD-PAL-CAB-DS-QUI 45 | CD-PAL-CAB-DS-AVE-QUI 43
     *   CD-DO-CAB-DS-QUI 60 | CD-DO-CAB-DS-AVE-QUI 58
     */
    @Test
    void sinRestriccionesEncuentraLasOchoRutas() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        List<Backtracking.Ruta> rutas = backtracking.rutasSimples(g, "CD", "QUI", 1000, 10);
        assertEquals(8, rutas.size());
        for (Backtracking.Ruta r : rutas) {
            assertEquals(r.vertices().size(), r.vertices().stream().distinct().count(), "camino simple");
            assertEquals("CD", r.vertices().get(0));
            assertEquals("QUI", r.vertices().get(r.vertices().size() - 1));
        }
    }

    @Test
    void laRestriccionDeCostoFiltraRutas() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        List<Backtracking.Ruta> rutas = backtracking.rutasSimples(g, "CD", "QUI", 37, 10);
        assertEquals(4, rutas.size());
        assertTrue(rutas.stream().allMatch(r -> r.costoTotal() <= 37));
        assertTrue(rutas.stream().anyMatch(r -> r.costoTotal() == 37.0), "el limite es inclusivo");
    }

    @Test
    void laRestriccionDeSaltosFiltraRutas() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        List<Backtracking.Ruta> rutas = backtracking.rutasSimples(g, "CD", "QUI", 1000, 2);
        assertEquals(1, rutas.size());
        assertEquals(List.of("CD", "DS", "QUI"), rutas.get(0).vertices());
        assertEquals(29.0, rutas.get(0).costoTotal());
    }

    @Test
    void laPodaExploraMenosNodosYDaElMismoResultado() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        Backtracking.ResultadoRutas r = backtracking.buscar(g, "CD", "QUI", 30, 10);
        assertEquals(2, r.rutas().size(), "solo 29 y 27 entran en 30 km");
        assertTrue(r.nodosExploradosConPoda() < r.nodosExploradosSinPoda(),
                "con poda=" + r.nodosExploradosConPoda() + " sin poda=" + r.nodosExploradosSinPoda());
    }

    @Test
    void sinCaminoDevuelveListaVacia() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        assertTrue(backtracking.rutasSimples(g, "QUI", "CD", 1000, 10).isEmpty());
    }

    @Test
    void elRetrocesoDejaElEstadoLimpioEntreRamas() {
        // grafo con dos caminos que comparten un vertice intermedio: si no se
        // desmarcara el visitado, la segunda rama no lo podria usar
        Grafo g = new Grafo(true);
        g.agregarArista("A", "B", 1);
        g.agregarArista("A", "C", 1);
        g.agregarArista("B", "X", 1);
        g.agregarArista("C", "X", 1);
        g.agregarArista("X", "Z", 1);
        assertEquals(2, backtracking.rutasSimples(g, "A", "Z", 100, 10).size());
    }

    @Test
    void parametrosInvalidos() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        assertThrows(IllegalArgumentException.class, () -> backtracking.rutasSimples(g, "CD", "QUI", -1, 5));
        assertThrows(IllegalArgumentException.class, () -> backtracking.rutasSimples(g, "CD", "QUI", 10, 0));
        assertThrows(IllegalArgumentException.class, () -> backtracking.rutasSimples(g, "CD", "NADA", 10, 5));
    }
}

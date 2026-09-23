package uade.prog3.tpo.algorithm;

import org.junit.jupiter.api.Test;
import uade.prog3.tpo.service.Grafo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BacktrackingTest {

    private final Backtracking backtracking = new Backtracking();

    /**
     * Rutas simples CD -> QUI en la red dirigida (calculadas a mano):
     *   CD-DS-QUI 55 | CD-DS-AVE-QUI 52 | CD-CAB-DS-QUI 75 | CD-CAB-DS-AVE-QUI 72
     *   CD-PAL-CAB-DS-QUI 82 | CD-PAL-CAB-DS-AVE-QUI 79
     *   CD-DO-CAB-DS-QUI 90 | CD-DO-CAB-DS-AVE-QUI 87
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
        List<Backtracking.Ruta> rutas = backtracking.rutasSimples(g, "CD", "QUI", 75, 10);
        assertEquals(4, rutas.size());
        assertTrue(rutas.stream().allMatch(r -> r.costoTotal() <= 75));
        assertTrue(rutas.stream().anyMatch(r -> r.costoTotal() == 75.0), "el limite es inclusivo");
    }

    @Test
    void laRestriccionDeSaltosFiltraRutas() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        List<Backtracking.Ruta> rutas = backtracking.rutasSimples(g, "CD", "QUI", 1000, 2);
        assertEquals(1, rutas.size());
        assertEquals(List.of("CD", "DS", "QUI"), rutas.get(0).vertices());
        assertEquals(55.0, rutas.get(0).costoTotal());
    }

    @Test
    void laPodaExploraMenosNodosYDaElMismoResultado() {
        Grafo g = GrafoDePrueba.redLogistica(true);
        Backtracking.ResultadoRutas r = backtracking.buscar(g, "CD", "QUI", 60, 10);
        assertEquals(2, r.rutas().size(), "solo 55 y 52 entran en 60 minutos");
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

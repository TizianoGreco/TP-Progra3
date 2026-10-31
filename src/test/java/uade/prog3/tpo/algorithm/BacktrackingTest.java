package uade.prog3.tpo.algorithm;

import org.junit.jupiter.api.Test;
import uade.prog3.tpo.service.Grafo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BacktrackingTest {

    private final Backtracking backtracking = new Backtracking();

    @Test
    void deCdAQuilmesHayOchoRutasSimples() {
        Backtracking.ResultadoRutas r = buscar("CD", "QUI", 1000, 10);

        assertEquals(8, r.rutas().size());
        assertEquals(List.of(27.0, 29.0, 35.0, 37.0, 43.0, 45.0, 58.0, 60.0), costos(r));
        assertTrue(r.rutas().stream().anyMatch(ruta ->
                ruta.vertices().equals(List.of("CD", "DS", "AVE", "QUI")) && ruta.costoTotal() == 27.0));
        assertRutasSimples(r);
    }

    @Test
    void con37KmQuedanCuatroRutasYElLimiteEsInclusivo() {
        Backtracking.ResultadoRutas r = buscar("CD", "QUI", 37, 10);

        assertEquals(4, r.rutas().size());
        assertEquals(List.of(27.0, 29.0, 35.0, 37.0), costos(r), "37 km entra; la siguiente vale 43");
        assertTrue(r.nodosExploradosConPoda() < r.nodosExploradosSinPoda());
        assertTrue(r.ramasPodadas() > 0);
    }

    @Test
    void con30KmQuedanLasDosMasCortas() {
        Backtracking.ResultadoRutas r = buscar("CD", "QUI", 30, 10);

        assertEquals(2, r.rutas().size());
        assertEquals(Set.of(27.0, 29.0), new HashSet<>(costos(r)));
    }

    @Test
    void conDosSaltosSoloQuedaLaRutaDirectaPorElSur() {
        List<Backtracking.Ruta> rutas = backtracking.rutasSimples(
                GrafoDePrueba.redLogistica(true), "CD", "QUI", 1000, 2);

        assertEquals(1, rutas.size());
        assertEquals(List.of("CD", "DS", "QUI"), rutas.get(0).vertices());
        assertEquals(29.0, rutas.get(0).costoTotal(), "CD->DS->QUI = 10+19");
    }

    @Test
    void sinCaminoDevuelveListaVacia() {
        Backtracking.ResultadoRutas r = buscar("QUI", "CD", 1000, 10);

        assertTrue(r.rutas().isEmpty());
    }

    @Test
    void parametrosInvalidosFallan() {
        Grafo g = GrafoDePrueba.redLogistica(true);

        IllegalArgumentException costo = assertThrows(IllegalArgumentException.class,
                () -> backtracking.buscar(g, "CD", "QUI", -1, 5));
        assertTrue(costo.getMessage().contains("negativo"));

        IllegalArgumentException saltos = assertThrows(IllegalArgumentException.class,
                () -> backtracking.buscar(g, "CD", "QUI", 37, 0));
        assertTrue(saltos.getMessage().contains("saltos"));
    }

    private Backtracking.ResultadoRutas buscar(String origen, String destino, double costo, int saltos) {
        return backtracking.buscar(GrafoDePrueba.redLogistica(true), origen, destino, costo, saltos);
    }

    private static List<Double> costos(Backtracking.ResultadoRutas r) {
        return r.rutas().stream().map(Backtracking.Ruta::costoTotal).sorted().toList();
    }

    private static void assertRutasSimples(Backtracking.ResultadoRutas r) {
        for (Backtracking.Ruta ruta : r.rutas()) {
            assertEquals(ruta.vertices().size(), new HashSet<>(ruta.vertices()).size(),
                    "un camino simple no repite nodos: " + ruta.vertices());
        }
    }
}

package uade.prog3.tpo.algorithm;

import org.junit.jupiter.api.Test;
import uade.prog3.tpo.model.Item;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SeleccionTest {

    private final Seleccion seleccion = new Seleccion(new Ordenamiento());

    private List<String> ids(List<Item> items) {
        return items.stream().map(Item::getId).toList();
    }

    @Test
    void greedyTomaPorRatioMientrasEntre() {
        Seleccion.Resultado r = seleccion.greedy(GrafoDePrueba.paquetes(), 10);
        // ratios: P1=10, P2=9.6, P3=9.4, P4=7, P5=6, P6=5, P7=4, P8=3
        // toma P1 (6kg); P2 y P3 no entran; P4 (8kg); P5 no; P6 no; P7 (9kg); P8 no
        assertEquals(List.of("P1", "P4", "P7"), ids(r.elegidos()));
        assertEquals(78.0, r.valorTotal());
        assertEquals(9.0, r.pesoTotal());
        assertEquals("greedy", r.tecnica());
    }

    @Test
    void greedyRespetaLaCapacidad() {
        Seleccion.Resultado r = seleccion.greedy(GrafoDePrueba.paquetes(), 34);
        assertEquals(8, r.elegidos().size(), "con capacidad para todo, toma todo");
        assertEquals(34.0, r.pesoTotal());
        assertTrue(seleccion.greedy(GrafoDePrueba.paquetes(), 0).elegidos().isEmpty());
    }

    @Test
    void programacionDinamicaEncuentraElOptimo() {
        Seleccion.Resultado r = seleccion.programacionDinamica(GrafoDePrueba.paquetes(), 10);
        assertEquals(95.0, r.valorTotal(), "P2 + P3 = 48 + 47");
        assertEquals(List.of("P2", "P3"), ids(r.elegidos()));
        assertEquals(10.0, r.pesoTotal());
    }

    @Test
    void greedyNoDaElOptimoYProgramacionDinamicaSi() {
        // Hito 6: juego de datos del dominio donde el greedy pierde
        Seleccion.Resultado greedy = seleccion.greedy(GrafoDePrueba.paquetes(), 10);
        Seleccion.Resultado dp = seleccion.programacionDinamica(GrafoDePrueba.paquetes(), 10);
        assertTrue(greedy.valorTotal() < dp.valorTotal(),
                "greedy=" + greedy.valorTotal() + " debe ser menor que dp=" + dp.valorTotal());
    }

    @Test
    void programacionDinamicaConCapacidadCeroNoEligeNada() {
        Seleccion.Resultado r = seleccion.programacionDinamica(GrafoDePrueba.paquetes(), 0);
        assertTrue(r.elegidos().isEmpty());
        assertEquals(0.0, r.valorTotal());
    }

    @Test
    void programacionDinamicaCasoClasicoConRecuperacion() {
        List<Item> items = List.of(
                GrafoDePrueba.item("a", 1, 1),
                GrafoDePrueba.item("b", 3, 4),
                GrafoDePrueba.item("c", 4, 5),
                GrafoDePrueba.item("d", 5, 7));
        Seleccion.Resultado r = seleccion.programacionDinamica(items, 7);
        assertEquals(9.0, r.valorTotal(), "b + c = 4 + 5 con peso 7");
        assertEquals(List.of("b", "c"), ids(r.elegidos()));
    }

    @Test
    void capacidadNegativaEsInvalida() {
        assertThrows(IllegalArgumentException.class, () -> seleccion.greedy(GrafoDePrueba.paquetes(), -1));
        assertThrows(IllegalArgumentException.class, () -> seleccion.programacionDinamica(GrafoDePrueba.paquetes(), -1));
    }
}

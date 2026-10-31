package uade.prog3.tpo.algorithm;

import org.junit.jupiter.api.Test;
import uade.prog3.tpo.model.Item;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeleccionTest {

    private final Seleccion seleccion = new Seleccion();

    @Test
    void conCapacidad10EligeP2YP3YSuperaAlGreedy() {
        Seleccion.Resultado r = seleccion.programacionDinamica(GrafoDePrueba.paquetes(), 10);

        assertEquals(List.of("P2", "P3"), ids(r));
        assertEquals(95.0, r.valorTotal(), "P2+P3 = 48+47; el greedy por ratio da P1+P4+P7 = 78");
        assertEquals(10.0, r.pesoTotal(), "5+5 kg");
        assertEquals("programacion dinamica", r.tecnica());
        assertTrue(r.valorTotal() > 78.0, "el optimo tiene que ganarle al greedy del mismo caso");
    }

    @Test
    void capacidadCeroNoCargaNada() {
        Seleccion.Resultado r = seleccion.programacionDinamica(GrafoDePrueba.paquetes(), 0);

        assertTrue(r.elegidos().isEmpty());
        assertEquals(0.0, r.valorTotal());
        assertEquals(0.0, r.pesoTotal());
    }

    @Test
    void descartaElItemQueNoEntraNunca() {
        List<Item> items = List.of(
                GrafoDePrueba.item("GRANDE", 20, 100),
                GrafoDePrueba.item("CHICO", 2, 5));

        Seleccion.Resultado r = seleccion.programacionDinamica(items, 10);

        assertEquals(List.of("CHICO"), ids(r), "20 kg no entra en 10; el de 2 kg si");
        assertEquals(5.0, r.valorTotal());
        assertEquals(2.0, r.pesoTotal());
    }

    @Test
    void capacidadNegativaFalla() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> seleccion.programacionDinamica(GrafoDePrueba.paquetes(), -1));
        assertTrue(e.getMessage().contains("negativa"));
    }

    @Test
    void noModificaLaListaDeEntrada() {
        List<Item> items = new ArrayList<>(GrafoDePrueba.paquetes());
        List<Item> antes = List.copyOf(items);

        seleccion.programacionDinamica(items, 10);

        assertEquals(antes, items);
    }

    private static List<String> ids(Seleccion.Resultado r) {
        return r.elegidos().stream().map(Item::getId).toList();
    }
}

package uade.prog3.tpo.algorithm;

import org.junit.jupiter.api.Test;
import uade.prog3.tpo.model.Item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RamificacionYPodaTest {

    private final RamificacionYPoda reparto = new RamificacionYPoda();

    @Test
    void conTresVehiculosLaCargaMaximaEs12() {
        RamificacionYPoda.Asignacion a = reparto.repartir(GrafoDePrueba.paquetes(), 3);

        assertEquals(12.0, a.cargaMaxima(), "8+4, 6+5+1 y 5+3+2");
        assertEquals(34.0 / 3, a.cotaInicial(), 1e-9, "max(8, 34/3) = 11.33");
        assertTrue(a.nodosPodados() > 0);
        assertCadaPaqueteUnaVez(a);
        assertEquals(a.cargaMaxima(), cargaDelMasCargado(a), 1e-9);
    }

    @Test
    void conCuatroVehiculosLaCargaMaximaEs9() {
        RamificacionYPoda.Asignacion a = reparto.repartir(GrafoDePrueba.paquetes(), 4);

        assertEquals(9.0, a.cargaMaxima());
        assertEquals(8.5, a.cotaInicial(), 1e-9, "34/4 = 8.5, por encima del paquete de 8 kg");
        assertCadaPaqueteUnaVez(a);
    }

    @Test
    void conDosVehiculosLaCotaYaEsLaSolucion() {
        RamificacionYPoda.Asignacion a = reparto.repartir(GrafoDePrueba.paquetes(), 2);

        assertEquals(17.0, a.cargaMaxima(), "34/2 = 17");
        assertEquals(17.0, a.cotaInicial(), 1e-9);
        assertCadaPaqueteUnaVez(a);
    }

    @Test
    void menosDeUnContenedorFalla() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> reparto.repartir(GrafoDePrueba.paquetes(), 0));
        assertTrue(e.getMessage().contains("contenedores"));
    }

    @Test
    void noModificaLaListaDeEntrada() {
        List<Item> items = new ArrayList<>(GrafoDePrueba.paquetes());
        List<Item> antes = List.copyOf(items);

        reparto.repartir(items, 3);

        assertEquals(antes, items);
    }

    private static void assertCadaPaqueteUnaVez(RamificacionYPoda.Asignacion a) {
        Set<String> ids = new HashSet<>();
        int total = 0;
        for (List<Item> grupo : a.porContenedor().values()) {
            for (Item item : grupo) {
                assertTrue(ids.add(item.getId()), "paquete repetido: " + item.getId());
                total++;
            }
        }
        assertEquals(8, total);
        assertEquals(8, a.porContenedor().values().stream().mapToInt(List::size).sum());
    }

    private static double cargaDelMasCargado(RamificacionYPoda.Asignacion a) {
        double max = 0;
        for (List<Item> grupo : a.porContenedor().values()) {
            double carga = 0;
            for (Item item : grupo) {
                carga += item.getPeso();
            }
            if (carga > max) {
                max = carga;
            }
        }
        return max;
    }
}

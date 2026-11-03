package uade.prog3.tpo.algorithm;

import org.junit.jupiter.api.Test;
import uade.prog3.tpo.model.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdenamientoTest {

    private final Ordenamiento ordenamiento = new Ordenamiento();
    private static final Comparator<Item> POR_PESO = Comparator.comparingDouble(Item::getPeso);
    private static final Comparator<Item> POR_RATIO = Comparator.comparingDouble(Item::ratioValorPeso);

    private List<String> ids(List<Item> items) {
        List<String> ids = new ArrayList<>();
        for (Item it : items) ids.add(it.getId());
        return ids;
    }

    @Test
    void quickSortOrdenaLosPaquetesPorPeso() {
        List<Item> r = ordenamiento.quickSort(GrafoDePrueba.paquetes(), POR_PESO);
        // pesos: P7=1, P4=2, P5=3, P8=4, P2=5, P3=5, P1=6, P6=8 (P2/P3 pueden intercambiarse)
        assertEquals(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 5.0, 6.0, 8.0), r.stream().map(Item::getPeso).toList());
    }

    @Test
    void quickSortOrdenaPorRatio() {
        List<Item> r = ordenamiento.quickSort(GrafoDePrueba.paquetes(), POR_RATIO);
        assertEquals(List.of("P8", "P7", "P6", "P5", "P4", "P3", "P2", "P1"), ids(r));
    }

    @Test
    void quickSortNoModificaLaEntradaYSoportaEntradaOrdenadaYDuplicados() {
        List<Item> ordenados = List.of(
                GrafoDePrueba.item("A", 1, 1), GrafoDePrueba.item("B", 2, 1),
                GrafoDePrueba.item("C", 2, 1), GrafoDePrueba.item("D", 3, 1), GrafoDePrueba.item("E", 3, 1));
        List<Item> copia = new ArrayList<>(ordenados);
        List<Item> r = ordenamiento.quickSort(ordenados, POR_PESO);
        assertEquals(copia, ordenados, "la lista de entrada queda intacta");
        assertEquals(List.of(1.0, 2.0, 2.0, 3.0, 3.0), r.stream().map(Item::getPeso).toList());
        assertTrue(ordenamiento.quickSort(List.of(), POR_PESO).isEmpty());
    }

    @Test
    void mergeSortOrdenaPorRatio() {
        List<Item> r = ordenamiento.mergeSort(GrafoDePrueba.paquetes(), POR_RATIO);
        assertEquals(List.of("P8", "P7", "P6", "P5", "P4", "P3", "P2", "P1"), ids(r));
    }

    @Test
    void mergeSortEsEstable() {
        // mismo peso, distinto orden de llegada: debe conservarse el orden original
        List<Item> entrada = List.of(
                GrafoDePrueba.item("X1", 5, 1), GrafoDePrueba.item("Y", 2, 1),
                GrafoDePrueba.item("X2", 5, 1), GrafoDePrueba.item("X3", 5, 1), GrafoDePrueba.item("Z", 1, 1));
        List<Item> r = ordenamiento.mergeSort(entrada, POR_PESO);
        assertEquals(List.of("Z", "Y", "X1", "X2", "X3"), ids(r));
    }

    @Test
    void mergeSortNoModificaLaEntrada() {
        List<Item> entrada = GrafoDePrueba.paquetes();
        List<String> antes = ids(entrada);
        ordenamiento.mergeSort(entrada, POR_PESO);
        assertEquals(antes, ids(entrada));
    }

    @Test
    void ambosAlgoritmosDanElMismoOrdenSobreClavesUnicas() {
        assertEquals(ids(ordenamiento.mergeSort(GrafoDePrueba.paquetes(), POR_RATIO)),
                     ids(ordenamiento.quickSort(GrafoDePrueba.paquetes(), POR_RATIO)));
    }
}

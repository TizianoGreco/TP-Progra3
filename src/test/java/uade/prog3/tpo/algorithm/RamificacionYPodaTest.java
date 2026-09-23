package uade.prog3.tpo.algorithm;

import org.junit.jupiter.api.Test;
import uade.prog3.tpo.model.Item;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RamificacionYPodaTest {

    private final RamificacionYPoda bb = new RamificacionYPoda(new Ordenamiento());

    private double cargaDe(List<Item> items) {
        return items.stream().mapToDouble(Item::getPeso).sum();
    }

    @Test
    void repartirEnTresVehiculosMinimizaLaCargaMaxima() {
        // pesos 8,6,5,5,4,3,2,1 = 34; suma/3 = 11.33 -> el optimo es 12: {8,4} {6,5,1} {5,3,2}
        RamificacionYPoda.Asignacion a = bb.repartir(GrafoDePrueba.paquetes(), 3);
        assertEquals(12.0, a.cargaMaxima());
        assertEquals(3, a.porContenedor().size());
        double total = 0;
        for (List<Item> c : a.porContenedor().values()) {
            assertTrue(cargaDe(c) <= 12.0);
            total += cargaDe(c);
        }
        assertEquals(34.0, total, "todos los paquetes quedan asignados");
    }

    @Test
    void laCotaInicialEsOptimistaYNuncaSuperaElResultado() {
        RamificacionYPoda.Asignacion a = bb.repartir(GrafoDePrueba.paquetes(), 3);
        assertEquals(34.0 / 3, a.cotaInicial(), 1e-9, "max(peso mayor 8, 34/3)");
        assertTrue(a.cotaInicial() <= a.cargaMaxima());
    }

    @Test
    void laPodaDescartaNodos() {
        RamificacionYPoda.Asignacion a = bb.repartir(GrafoDePrueba.paquetes(), 3);
        assertTrue(a.nodosExplorados() > 0);
        assertTrue(a.nodosPodados() > 0, "la cota debe podar algo en este caso");
        assertTrue(a.nodosExplorados() < Math.pow(3, 8), "mucho menos que k^n = 6561");
    }

    @Test
    void unSoloContenedorLlevaTodo() {
        RamificacionYPoda.Asignacion a = bb.repartir(GrafoDePrueba.paquetes(), 1);
        assertEquals(34.0, a.cargaMaxima());
        assertEquals(8, a.porContenedor().get("contenedor1").size());
    }

    @Test
    void casoDondeElGreedyLptNoEsOptimo() {
        // LPT: 7,7 | 6,4 | 5 -> {7,6} {7,5} {4}? LPT asigna 7->c1, 7->c2, 6->c3, 5->c3(11)... probemos
        // pesos 3,3,2,2,2 en 2 contenedores: LPT da {3,2,2}=7 vs {3,2}=5 -> makespan 7; optimo 6: {3,3} {2,2,2}
        List<Item> items = List.of(
                GrafoDePrueba.item("a", 3, 0), GrafoDePrueba.item("b", 3, 0),
                GrafoDePrueba.item("c", 2, 0), GrafoDePrueba.item("d", 2, 0), GrafoDePrueba.item("e", 2, 0));
        RamificacionYPoda.Asignacion a = bb.repartir(items, 2);
        assertEquals(6.0, a.cargaMaxima(), "B&B mejora la solucion inicial greedy (7)");
    }

    @Test
    void masContenedoresQueItemsDaElPesoMayor() {
        RamificacionYPoda.Asignacion a = bb.repartir(GrafoDePrueba.paquetes(), 20);
        assertEquals(8.0, a.cargaMaxima(), "cada paquete solo, el mayor pesa 8");
    }

    @Test
    void cantidadInvalidaDeContenedores() {
        assertThrows(IllegalArgumentException.class, () -> bb.repartir(GrafoDePrueba.paquetes(), 0));
    }
}

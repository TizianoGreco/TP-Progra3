package uade.prog3.tpo.algorithm;

import org.junit.jupiter.api.Test;
import uade.prog3.tpo.service.Grafo;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ArbolRecubrimientoTest {

    private final ArbolRecubrimiento mst = new ArbolRecubrimiento();

    /**
     * Calculo a mano (Kruskal sobre la red no dirigida, aristas por costo):
     * DO-MOR 5, DS-AVE 5, PAL-CAB 6, CD-CAB 7, PAL-DN 8, [CD-PAL 9 ciclo],
     * CD-DS 10, [CAB-DS 11 ciclo], DN-SI 12, DO-CAB 12, AVE-QUI 12.
     * 9 aristas, total 77 km.
     */
    private static final Set<String> ARISTAS_ESPERADAS = Set.of(
            "DO-MOR", "DS-AVE", "PAL-CAB", "CD-CAB", "PAL-DN", "CD-DS", "DN-SI", "DO-CAB", "AVE-QUI");

    private Set<String> normalizar(ArbolRecubrimiento.Mst m) {
        Set<String> s = new HashSet<>();
        for (ArbolRecubrimiento.AristaMst a : m.aristas()) {
            // la arista no dirigida se identifica sin importar el sentido
            s.add(ARISTAS_ESPERADAS.contains(a.origen() + "-" + a.destino())
                    ? a.origen() + "-" + a.destino()
                    : a.destino() + "-" + a.origen());
        }
        return s;
    }

    @Test
    void primDaElArbolCalculadoAMano() {
        ArbolRecubrimiento.Mst m = mst.prim(GrafoDePrueba.redLogistica(false), "CD");
        assertEquals(9, m.aristas().size(), "V-1 con V=10");
        assertEquals(77.0, m.costoTotal());
        assertEquals(ARISTAS_ESPERADAS, normalizar(m));
    }

    @Test
    void primDaElMismoCostoDesdeCualquierOrigen() {
        Grafo g = GrafoDePrueba.redLogistica(false);
        assertEquals(77.0, mst.prim(g, "QUI").costoTotal());
        assertEquals(77.0, mst.prim(g, "MOR").costoTotal());
    }

    @Test
    void kruskalDaElArbolCalculadoAMano() {
        ArbolRecubrimiento.Mst m = mst.kruskal(GrafoDePrueba.redLogistica(false));
        assertEquals(9, m.aristas().size());
        assertEquals(77.0, m.costoTotal());
        assertEquals(ARISTAS_ESPERADAS, normalizar(m));
    }

    @Test
    void kruskalSaltaLasAristasQueFormanCiclo() {
        ArbolRecubrimiento.Mst m = mst.kruskal(GrafoDePrueba.redLogistica(false));
        Set<String> aristas = normalizar(m);
        assertFalse(aristas.contains("CD-PAL") || aristas.contains("PAL-CD"), "CD-PAL (9) cierra un ciclo");
        assertFalse(aristas.contains("CAB-DS") || aristas.contains("DS-CAB"), "CAB-DS (11) cierra un ciclo");
    }

    @Test
    void grafoNoConexoFallaConMensajeClaro() {
        Grafo g = new Grafo(false);
        g.agregarArista("A", "B", 1);
        g.agregarArista("C", "D", 1);
        assertTrue(assertThrows(IllegalStateException.class, () -> mst.prim(g, "A")).getMessage().contains("no es conexo"));
        assertTrue(assertThrows(IllegalStateException.class, () -> mst.kruskal(g)).getMessage().contains("no es conexo"));
    }

    @Test
    void unionFindDetectaCiclos() {
        ArbolRecubrimiento.UnionFind uf = new ArbolRecubrimiento.UnionFind(4);
        assertTrue(uf.union(0, 1));
        assertTrue(uf.union(2, 3));
        assertNotEquals(uf.find(0), uf.find(2));
        assertTrue(uf.union(1, 2));
        assertEquals(uf.find(0), uf.find(3));
        assertFalse(uf.union(0, 3), "ya estan en la misma componente: seria un ciclo");
    }
}

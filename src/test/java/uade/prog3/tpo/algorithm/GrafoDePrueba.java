package uade.prog3.tpo.algorithm;

import uade.prog3.tpo.model.Conexion;
import uade.prog3.tpo.model.Item;
import uade.prog3.tpo.model.Nodo;
import uade.prog3.tpo.seed.CargaInicial;
import uade.prog3.tpo.service.Grafo;

import java.util.List;

/**
 * Arma en memoria el mismo grafo que la carga inicial guarda en Neo4j, sin
 * tocar la base. Asi los tests son unitarios y corren en milisegundos.
 */
final class GrafoDePrueba {

    private GrafoDePrueba() { }

    /** La red logistica del grupo (10 nodos, 15 rutas). */
    static Grafo redLogistica(boolean dirigido) {
        Grafo g = new Grafo(dirigido);
        List<Nodo> nodos = CargaInicial.nodos();
        for (Nodo n : nodos) {
            g.agregarVertice(n.getId());
        }
        for (Nodo n : nodos) {
            for (Conexion c : n.getConexiones()) {
                g.agregarArista(n.getId(), c.getDestino().getId(), c.getCosto());
            }
        }
        return g;
    }

    /** Los 8 paquetes del dominio. */
    static List<Item> paquetes() {
        return CargaInicial.paquetes();
    }

    static Item item(String id, double peso, double valor) {
        return new Item(id, id, peso, valor, null);
    }
}

package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.service.Grafo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * UNIDAD: Grafos I (clase 4)          PUNTAJE: 2 puntos
 * A CARGO DE: Persona 1 (grafos y configuracion)
 *
 * Recorridos sobre grafos. DFS es ademas la base estructural del
 * backtracking de rutas (clase Backtracking).
 */
@Component
public class Recorridos {

    /**
     * Recorrido en profundidad (DFS) desde un vertice, recursivo.
     *
     * Se marca el vertice como visitado al ENTRAR en la llamada recursiva y
     * se recorren sus vecinos en el orden de la lista de adyacencia. La pila
     * de llamadas cumple el rol de la pila explicita del algoritmo iterativo.
     *
     * Complejidad: O(V + E). Cada vertice entra a la recursion una sola vez
     * (por el arreglo de visitados) y cada lista de adyacencia se recorre una
     * sola vez, lo que suma E en total (2E si el grafo es no dirigido).
     * Espacio: O(V) por el arreglo de visitados y la profundidad de la pila.
     */
    public List<String> dfs(Grafo grafo, String origenId) {
        int origen = grafo.indiceDe(origenId);
        boolean[] visitado = new boolean[grafo.cantidadVertices()];
        List<String> orden = new ArrayList<>();
        dfsRecursivo(grafo, origen, visitado, orden);
        return orden;
    }

    private void dfsRecursivo(Grafo grafo, int u, boolean[] visitado, List<String> orden) {
        visitado[u] = true;
        orden.add(grafo.idDe(u));
        for (Grafo.Arista a : grafo.vecinos(u)) {
            if (!visitado[a.destino()]) {
                dfsRecursivo(grafo, a.destino(), visitado, orden);
            }
        }
    }

    /**
     * Recorrido en anchura (BFS) desde un vertice, con cola FIFO.
     *
     * El vertice se marca como visitado AL ENCOLAR. Si se marcara al
     * desencolar, un mismo vertice podria entrar varias veces a la cola y
     * el orden de visita dejaria de ser el de BFS.
     *
     * Complejidad: O(V + E). Cada vertice se encola y desencola exactamente
     * una vez (O(1) cada operacion sobre ArrayDeque) y cada arista se examina
     * una vez desde su origen.
     * Espacio: O(V) por la cola y el arreglo de visitados.
     *
     * BFS encuentra el camino con MENOS ARISTAS desde el origen, pero no el de
     * menor costo: no mira el peso de las aristas. Para eso esta Dijkstra.
     */
    public List<String> bfs(Grafo grafo, String origenId) {
        int origen = grafo.indiceDe(origenId);
        boolean[] visitado = new boolean[grafo.cantidadVertices()];
        List<String> orden = new ArrayList<>();
        Deque<Integer> cola = new ArrayDeque<>();

        visitado[origen] = true;
        cola.add(origen);

        while (!cola.isEmpty()) {
            int u = cola.poll();
            orden.add(grafo.idDe(u));
            for (Grafo.Arista a : grafo.vecinos(u)) {
                if (!visitado[a.destino()]) {
                    visitado[a.destino()] = true;
                    cola.add(a.destino());
                }
            }
        }
        return orden;
    }
}

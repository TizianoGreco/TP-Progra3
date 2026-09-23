package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.service.Grafo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * UNIDAD: Grafos II (clase 5)
 * PUNTAJE: forma parte de los 3 puntos de "Dijkstra, Prim, Kruskal"
 *
 * El arbol de recubrimiento minimo solo tiene sentido sobre un grafo NO
 * DIRIGIDO y CONEXO. El controller carga el grafo con cargar(false).
 */
@Component
public class ArbolRecubrimiento {

    /** Una arista del arbol resultante, con ids de negocio. */
    public record AristaMst(String origen, String destino, double costo) { }

    /** Arbol de recubrimiento minimo completo. */
    public record Mst(List<AristaMst> aristas, double costoTotal) { }

    /** Arista candidata en la cola de Prim: llega a 'destino' desde 'origen'. */
    private record Candidata(int origen, int destino, double costo) { }

    /** Arista completa, con ambos extremos, para ordenar en Kruskal. */
    private record AristaCompleta(int u, int v, double costo) { }

    // ------------------------------------------------------------------
    // Prim
    // ------------------------------------------------------------------

    /**
     * Prim con cola de prioridad (heap binario).
     *
     * Hace crecer un unico arbol desde el origen: en cada paso extrae del
     * heap la arista de menor costo y, si su destino aun no esta en el arbol,
     * la incorpora y encola todas las aristas que salen del nuevo vertice.
     * Las entradas cuyo destino ya esta en el arbol se descartan al extraer
     * (insercion perezosa, igual que en Dijkstra).
     *
     * Complejidad: O(E log V). Cada arista se encola a lo sumo dos veces (una
     * por cada extremo, porque el grafo no dirigido la guarda en ambas listas)
     * y cada operacion del heap cuesta O(log E) = O(log V). Se eligio el heap
     * sobre la busqueda lineal O(V^2) porque el grafo del dominio es ralo
     * (E cercano a V), donde E log V es claramente menor que V^2.
     * Espacio: O(V + E).
     *
     * Si el grafo no es conexo, el arbol no alcanza los V vertices y se falla
     * con un mensaje claro.
     */
    public Mst prim(Grafo grafo, String origenId) {
        int n = grafo.cantidadVertices();
        int origen = grafo.indiceDe(origenId);
        boolean[] enArbol = new boolean[n];
        List<AristaMst> aristas = new ArrayList<>();
        double total = 0.0;

        PriorityQueue<Candidata> cola = new PriorityQueue<>(
                (a, b) -> Double.compare(a.costo(), b.costo()));

        enArbol[origen] = true;
        int verticesEnArbol = 1;
        encolarVecinos(grafo, origen, enArbol, cola);

        while (!cola.isEmpty() && verticesEnArbol < n) {
            Candidata c = cola.poll();
            if (enArbol[c.destino()]) {
                continue; // arista obsoleta: ya se llego a ese vertice mas barato
            }
            enArbol[c.destino()] = true;
            verticesEnArbol++;
            aristas.add(new AristaMst(grafo.idDe(c.origen()), grafo.idDe(c.destino()), c.costo()));
            total += c.costo();
            encolarVecinos(grafo, c.destino(), enArbol, cola);
        }

        if (verticesEnArbol < n) {
            throw new IllegalStateException("Prim: el grafo no es conexo, desde " + origenId
                    + " solo se alcanzan " + verticesEnArbol + " de " + n + " vertices");
        }
        return new Mst(aristas, total);
    }

    private void encolarVecinos(Grafo grafo, int u, boolean[] enArbol, PriorityQueue<Candidata> cola) {
        for (Grafo.Arista a : grafo.vecinos(u)) {
            if (!enArbol[a.destino()]) {
                cola.add(new Candidata(u, a.destino(), a.costo()));
            }
        }
    }

    // ------------------------------------------------------------------
    // Kruskal + Union-Find
    // ------------------------------------------------------------------

    /**
     * Kruskal: ordena TODAS las aristas por costo (con el MergeSort propio)
     * y las agrega en ese orden salteando las que formarian ciclo. La
     * deteccion de ciclo la hace el Union-Find: dos vertices ya estan en la
     * misma componente si find(u) == find(v).
     *
     * Complejidad: O(E log E), dominada por el ordenamiento. Las E
     * operaciones de find/union cuestan O(E alfa(V)) con union por rango y
     * compresion de caminos, que es practicamente lineal. Como E <= V^2,
     * log E <= 2 log V y tambien vale O(E log V). Espacio: O(V + E).
     *
     * Kruskal conviene en grafos ralos (pocas aristas para ordenar); Prim con
     * heap rinde parecido, y Prim con matriz O(V^2) gana en grafos densos.
     */
    public Mst kruskal(Grafo grafo) {
        int n = grafo.cantidadVertices();
        List<AristaCompleta> aristas = new ArrayList<>();
        for (int u = 0; u < n; u++) {
            for (Grafo.Arista a : grafo.vecinos(u)) {
                if (u < a.destino()) {           // cada arista no dirigida una sola vez
                    aristas.add(new AristaCompleta(u, a.destino(), a.costo()));
                }
            }
        }
        List<AristaCompleta> ordenadas = mergeSortPorCosto(aristas);

        UnionFind uf = new UnionFind(n);
        List<AristaMst> resultado = new ArrayList<>();
        double total = 0.0;
        for (AristaCompleta a : ordenadas) {
            if (resultado.size() == n - 1) {
                break;
            }
            if (uf.union(a.u(), a.v())) {         // false => formaria ciclo
                resultado.add(new AristaMst(grafo.idDe(a.u()), grafo.idDe(a.v()), a.costo()));
                total += a.costo();
            }
        }

        if (n > 0 && resultado.size() < n - 1) {
            throw new IllegalStateException("Kruskal: el grafo no es conexo, el bosque tiene "
                    + (n - resultado.size()) + " componentes");
        }
        return new Mst(resultado, total);
    }

    /** MergeSort propio sobre aristas, por costo ascendente. O(E log E). */
    private List<AristaCompleta> mergeSortPorCosto(List<AristaCompleta> lista) {
        if (lista.size() <= 1) {
            return new ArrayList<>(lista);
        }
        int medio = lista.size() / 2;
        List<AristaCompleta> izq = mergeSortPorCosto(lista.subList(0, medio));
        List<AristaCompleta> der = mergeSortPorCosto(lista.subList(medio, lista.size()));
        List<AristaCompleta> mezcla = new ArrayList<>(lista.size());
        int i = 0, j = 0;
        while (i < izq.size() && j < der.size()) {
            if (izq.get(i).costo() <= der.get(j).costo()) {
                mezcla.add(izq.get(i++));
            } else {
                mezcla.add(der.get(j++));
            }
        }
        while (i < izq.size()) mezcla.add(izq.get(i++));
        while (j < der.size()) mezcla.add(der.get(j++));
        return mezcla;
    }

    /**
     * Union-Find (conjuntos disjuntos) con union por rango y compresion de
     * caminos. Cada operacion cuesta O(alfa(n)) amortizado, donde alfa es la
     * inversa de Ackermann, menor que 5 para cualquier n practico.
     */
    static final class UnionFind {
        private final int[] padre;
        private final int[] rango;

        UnionFind(int n) {
            padre = new int[n];
            rango = new int[n];
            for (int i = 0; i < n; i++) {
                padre[i] = i;
            }
        }

        int find(int x) {
            if (padre[x] != x) {
                padre[x] = find(padre[x]);    // compresion de caminos
            }
            return padre[x];
        }

        /** Une los conjuntos de a y b. Devuelve false si ya estaban unidos (ciclo). */
        boolean union(int a, int b) {
            int ra = find(a);
            int rb = find(b);
            if (ra == rb) {
                return false;
            }
            if (rango[ra] < rango[rb]) {
                padre[ra] = rb;
            } else if (rango[ra] > rango[rb]) {
                padre[rb] = ra;
            } else {
                padre[rb] = ra;
                rango[ra]++;
            }
            return true;
        }
    }
}

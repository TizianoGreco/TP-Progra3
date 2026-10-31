package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.service.Grafo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * UNIDAD: Grafos II (clase 5) y PD sobre grafos (clase 8)
 * PUNTAJE: forma parte de los 3 puntos de "Dijkstra, Prim, Kruskal"
 *          y de 1 punto de programacion dinamica (Floyd-Warshall).
 * A CARGO DE: Persona 1 (Dijkstra y UCS) y Persona 3 (Floyd-Warshall)
 */
@Component
public class CaminosMinimos {

    /**
     * Resultado de una consulta de camino minimo.
     *
     * @param nodosExpandidos cantidad de vertices que salieron de la cola de
     *                        prioridad y fueron procesados. Sirve para comparar
     *                        Dijkstra completo contra UCS con parada temprana.
     */
    public record Camino(List<String> vertices, double costoTotal, int nodosExpandidos) { }

    /**
     * Comparacion de trabajo entre Floyd-Warshall y correr Dijkstra desde
     * cada vertice (hito 7).
     */
    public record ComparacionTodosContraTodos(int vertices,
                                              long operacionesFloyd,
                                              int nodosExpandidosDijkstraV,
                                              long relajacionesDijkstraV,
                                              boolean costosCoinciden) { }

    /** Entrada de la cola de prioridad: (vertice, distancia tentativa). */
    private record Entrada(int vertice, double distancia) { }

    /** Resultado interno de una corrida de Dijkstra sobre todo el grafo. */
    private record CorridaDijkstra(double[] dist, int[] predecesor, int expandidos, long relajaciones) { }

    // ------------------------------------------------------------------
    // Dijkstra
    // ------------------------------------------------------------------

    /**
     * Dijkstra: camino de costo minimo desde un origen hasta un destino.
     *
     * Estructura: PriorityQueue (heap binario) de pares (vertice, distancia)
     * con insercion perezosa: cuando se mejora la distancia de un vertice se
     * inserta una nueva entrada en lugar de actualizar la existente, y al
     * extraer se descartan las entradas obsoletas (distancia mayor a dist[v]).
     * Arreglo dist[] con las distancias tentativas y predecesor[] para
     * reconstruir el camino al final.
     *
     * Relajacion: para cada arista (u, v, w) se compara dist[u] + w contra
     * dist[v]; si mejora, se actualiza dist[v], predecesor[v] y se encola v.
     *
     * Complejidad: O((V + E) log V). Cada arista puede generar como maximo
     * una insercion en el heap (E inserciones) y cada vertice se expande a lo
     * sumo una vez (V extracciones utiles); cada operacion sobre el heap
     * cuesta O(log V) porque el heap nunca tiene mas de O(E) entradas y
     * log E esta acotado por 2 log V. Espacio: O(V + E).
     *
     * Dijkstra es greedy: en cada paso toma el vertice con menor distancia
     * tentativa y la fija como definitiva. Eso es correcto solo si ningun
     * camino que salga de ese vertice puede volver con menor costo, lo cual
     * requiere pesos no negativos. Con un peso negativo la decision greedy
     * puede ser incorrecta, por eso se valida y se rechaza.
     */
    public Camino dijkstra(Grafo grafo, String origenId, String destinoId) {
        int origen = grafo.indiceDe(origenId);
        int destino = grafo.indiceDe(destinoId);
        validarPesosNoNegativos(grafo);

        CorridaDijkstra corrida = correrDijkstra(grafo, origen, -1);
        return armarCamino(grafo, corrida, origen, destino, "Dijkstra");
    }

    /**
     * UCS (Uniform Cost Search): identico a Dijkstra pero termina apenas el
     * destino sale de la cola de prioridad. En ese momento su distancia ya es
     * definitiva (no hay aristas negativas), asi que el resultado es el mismo
     * pero se expanden menos vertices: solo aquellos con distancia menor o
     * igual a la del destino.
     *
     * Complejidad: O((V + E) log V) en el peor caso (destino lejano); en la
     * practica proporcional a la porcion del grafo mas cercana que el destino.
     */
    public Camino ucs(Grafo grafo, String origenId, String destinoId) {
        int origen = grafo.indiceDe(origenId);
        int destino = grafo.indiceDe(destinoId);
        validarPesosNoNegativos(grafo);

        CorridaDijkstra corrida = correrDijkstra(grafo, origen, destino);
        return armarCamino(grafo, corrida, origen, destino, "UCS");
    }

    /**
     * Nucleo compartido por Dijkstra y UCS.
     *
     * @param detenerEn indice del vertice en el que cortar la busqueda apenas
     *                  se expande, o -1 para recorrer todo el grafo.
     */
    private CorridaDijkstra correrDijkstra(Grafo grafo, int origen, int detenerEn) {
        int n = grafo.cantidadVertices();
        double[] dist = new double[n];
        int[] predecesor = new int[n];
        boolean[] cerrado = new boolean[n];
        Arrays.fill(dist, Grafo.INFINITO);
        Arrays.fill(predecesor, -1);

        PriorityQueue<Entrada> cola = new PriorityQueue<>(
                (a, b) -> Double.compare(a.distancia(), b.distancia()));
        dist[origen] = 0.0;
        cola.add(new Entrada(origen, 0.0));

        int expandidos = 0;
        long relajaciones = 0;

        while (!cola.isEmpty()) {
            Entrada actual = cola.poll();
            int u = actual.vertice();
            // entrada obsoleta: ya se encontro algo mejor para u
            if (cerrado[u] || actual.distancia() > dist[u]) {
                continue;
            }
            cerrado[u] = true;
            expandidos++;

            if (u == detenerEn) {
                break;
            }

            for (Grafo.Arista a : grafo.vecinos(u)) {
                int v = a.destino();
                double candidata = dist[u] + a.costo();
                relajaciones++;
                if (candidata < dist[v]) {          // relajacion de la arista
                    dist[v] = candidata;
                    predecesor[v] = u;
                    cola.add(new Entrada(v, candidata));
                }
            }
        }
        return new CorridaDijkstra(dist, predecesor, expandidos, relajaciones);
    }

    private Camino armarCamino(Grafo grafo, CorridaDijkstra corrida, int origen, int destino, String algoritmo) {
        if (corrida.dist()[destino] == Grafo.INFINITO) {
            throw new IllegalStateException(algoritmo + ": no existe camino de "
                    + grafo.idDe(origen) + " a " + grafo.idDe(destino));
        }
        List<String> camino = new ArrayList<>();
        for (int v = destino; v != -1; v = corrida.predecesor()[v]) {
            camino.add(grafo.idDe(v));
        }
        Collections.reverse(camino);
        return new Camino(camino, corrida.dist()[destino], corrida.expandidos());
    }

    private void validarPesosNoNegativos(Grafo grafo) {
        for (int u = 0; u < grafo.cantidadVertices(); u++) {
            for (Grafo.Arista a : grafo.vecinos(u)) {
                if (a.costo() < 0) {
                    throw new IllegalArgumentException("Dijkstra requiere pesos no negativos: la arista "
                            + grafo.idDe(u) + "->" + grafo.idDe(a.destino()) + " tiene costo " + a.costo());
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Floyd-Warshall (A CARGO DE: Persona 3)
    // ------------------------------------------------------------------

    /**
     * Floyd-Warshall: costo minimo entre todos los pares de vertices.
     *
     * Parte de la matriz de adyacencia. Para cada vertice intermedio k se
     * prueba si i -> k -> j mejora el costo de i -> j:
     * d[i][j] = min( d[i][j] , d[i][k] + d[k][j] ).
     * k va por fuera: cuando se usa k, los caminos que pasan por 0..k-1 ya
     * estan resueltos.
     *
     * Al terminar, si algun d[i][i] es negativo hay un ciclo de costo
     * negativo y el minimo no existe. El mapa solo incluye pares alcanzables
     * ("ORIGEN->DESTINO"), porque el infinito no se puede mandar en JSON.
     *
     * Tiempo O(V^3): los tres bucles recorren todos los vertices. Espacio
     * O(V^2): una sola matriz, que se va pisando.
     */
    public Map<String, Double> floydWarshall(Grafo grafo) {
        double[][] dist = distanciasFloyd(grafo);
        Map<String, Double> costos = new LinkedHashMap<>();
        int n = grafo.cantidadVertices();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (dist[i][j] < Grafo.INFINITO) {
                    costos.put(grafo.idDe(i) + "->" + grafo.idDe(j), dist[i][j]);
                }
            }
        }
        return costos;
    }

    /**
     * Hito 7: Floyd-Warshall contra Dijkstra corrido una vez desde cada
     * vertice. Reutiliza correrDijkstra, que devuelve dist[], expandidos y
     * relajaciones. Las operaciones de Floyd son V^3, una por celda del
     * triple bucle.
     */
    public ComparacionTodosContraTodos compararConDijkstra(Grafo grafo) {
        double[][] dist = distanciasFloyd(grafo);
        int n = grafo.cantidadVertices();
        int expandidos = 0;
        long relajaciones = 0;
        boolean coinciden = true;
        for (int origen = 0; origen < n; origen++) {
            CorridaDijkstra corrida = correrDijkstra(grafo, origen, -1);
            expandidos += corrida.expandidos();
            relajaciones += corrida.relajaciones();
            for (int destino = 0; destino < n; destino++) {
                if (!mismoCosto(dist[origen][destino], corrida.dist()[destino])) {
                    coinciden = false;
                }
            }
        }
        long operacionesFloyd = (long) n * n * n;
        return new ComparacionTodosContraTodos(n, operacionesFloyd, expandidos, relajaciones, coinciden);
    }

    /** Matriz de costos minimos. Lanza si hay un ciclo de costo negativo. */
    private double[][] distanciasFloyd(Grafo grafo) {
        double[][] dist = grafo.matrizDeAdyacencia();
        int n = grafo.cantidadVertices();
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                if (dist[i][k] == Grafo.INFINITO) {
                    continue;
                }
                for (int j = 0; j < n; j++) {
                    if (dist[k][j] == Grafo.INFINITO) {
                        continue;
                    }
                    double porK = dist[i][k] + dist[k][j];
                    if (porK < dist[i][j]) {
                        dist[i][j] = porK;
                    }
                }
            }
        }
        for (int i = 0; i < n; i++) {
            if (dist[i][i] < 0) {
                throw new IllegalStateException("Floyd-Warshall: ciclo de costo negativo");
            }
        }
        return dist;
    }

    private static boolean mismoCosto(double a, double b) {
        if (a == Grafo.INFINITO || b == Grafo.INFINITO) {
            return a == b;
        }
        return Math.abs(a - b) < 1e-9;
    }
}

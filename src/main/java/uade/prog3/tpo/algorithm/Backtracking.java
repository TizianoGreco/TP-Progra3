package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.service.Grafo;

import java.util.ArrayList;
import java.util.List;

/**
 * UNIDAD: Backtracking (clases 9 y 10)      PUNTAJE: 1 punto
 * A CARGO DE: Persona 3 (optimizacion y busqueda)
 *
 * Problema del dominio: enumerar todas las rutas posibles entre dos nodos de
 * la red que no superen una distancia maxima (km) ni una cantidad maxima de
 * rutas, sin pasar dos veces por el mismo nodo.
 *
 * Valor esperado sobre la red del grupo: 8 rutas simples de CD a QUI, 4 de
 * ellas con 37 km o menos (ver README).
 */
@Component
public class Backtracking {

    /** Un camino simple encontrado, con su costo acumulado. */
    public record Ruta(List<String> vertices, double costoTotal) { }

    /**
     * Resultado completo de la busqueda, con la evidencia de la poda.
     *
     * @param nodosExploradosConPoda  llamadas recursivas que hizo la busqueda
     *                                real, podando por costo y saltos
     * @param nodosExploradosSinPoda  llamadas que hubiera hecho enumerando
     *                                todos los caminos simples y filtrando al
     *                                final (misma busqueda sin la poda)
     */
    public record ResultadoRutas(List<Ruta> rutas,
                                 int nodosExploradosConPoda,
                                 int nodosExploradosSinPoda,
                                 int ramasPodadas) { }

    /**
     * Enumerar todos los caminos simples de origen a destino que no superen
     * un costo maximo ni una cantidad maxima de saltos.
     *
     * Un salto es una ruta (una arista). CD -> DS -> QUI tiene 2 saltos.
     */
    public List<Ruta> rutasSimples(Grafo grafo, String origenId, String destinoId,
                                   double costoMaximo, int saltosMaximos) {
        return buscar(grafo, origenId, destinoId, costoMaximo, saltosMaximos).rutas();
    }

    /**
     * Misma busqueda, devolviendo ademas los nodos explorados con y sin poda.
     *
     * DFS recursivo. Al entrar se marca el nodo y se agrega al camino; al
     * volver se desmarca y se quita. Eso es el retroceso: el nodo queda libre
     * para otra rama.
     *
     * Se poda antes de bajar: si la arista siguiente pasa el maximo de km o
     * de saltos, esa rama no se explora. La busqueda sin poda recorre todos
     * los caminos simples y descarta al final los que no cumplen.
     *
     * En el peor caso el arbol tiene un llamado por cada permutacion de
     * nodos, O(V!). La poda no cambia ese techo; en la red del grupo baja
     * los nodos visitados. Espacio O(V): el camino, el arreglo de visitados
     * y la pila de llamadas.
     */
    public ResultadoRutas buscar(Grafo grafo, String origenId, String destinoId,
                                 double costoMaximo, int saltosMaximos) {
        if (costoMaximo < 0) {
            throw new IllegalArgumentException(
                    "El costo maximo no puede ser negativo: " + costoMaximo);
        }
        if (saltosMaximos < 1) {
            throw new IllegalArgumentException(
                    "La cantidad maxima de saltos debe ser al menos 1: " + saltosMaximos);
        }
        int origen = grafo.indiceDe(origenId);
        int destino = grafo.indiceDe(destinoId);

        Busqueda conPoda = new Busqueda();
        explorar(grafo, origen, destino, 0, 0, costoMaximo, saltosMaximos, true,
                new boolean[grafo.cantidadVertices()], new ArrayList<>(), conPoda);

        Busqueda sinPoda = new Busqueda();
        explorar(grafo, origen, destino, 0, 0, costoMaximo, saltosMaximos, false,
                new boolean[grafo.cantidadVertices()], new ArrayList<>(), sinPoda);

        return new ResultadoRutas(conPoda.rutas, conPoda.explorados, sinPoda.explorados, conPoda.podadas);
    }

    private void explorar(Grafo grafo, int actual, int destino, double costo, int saltos,
                          double costoMaximo, int saltosMaximos, boolean podar,
                          boolean[] visitado, List<String> camino, Busqueda busqueda) {
        busqueda.explorados++;
        visitado[actual] = true;
        camino.add(grafo.idDe(actual));

        if (actual == destino) {
            if (costo <= costoMaximo && saltos <= saltosMaximos) {
                busqueda.rutas.add(new Ruta(List.copyOf(camino), costo));
            }
        } else {
            for (Grafo.Arista arista : grafo.vecinos(actual)) {
                if (visitado[arista.destino()]) {
                    continue;
                }
                double nuevoCosto = costo + arista.costo();
                int nuevosSaltos = saltos + 1;
                if (podar && (nuevoCosto > costoMaximo || nuevosSaltos > saltosMaximos)) {
                    busqueda.podadas++;
                    continue;
                }
                explorar(grafo, arista.destino(), destino, nuevoCosto, nuevosSaltos,
                        costoMaximo, saltosMaximos, podar, visitado, camino, busqueda);
            }
        }

        camino.remove(camino.size() - 1);
        visitado[actual] = false;
    }

    /** Contadores de una corrida. La lista de rutas se arma en orden de descubrimiento. */
    private static final class Busqueda {
        private final List<Ruta> rutas = new ArrayList<>();
        private int explorados;
        private int podadas;
    }
}

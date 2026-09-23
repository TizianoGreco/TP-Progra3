package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.service.Grafo;

import java.util.ArrayList;
import java.util.List;

/**
 * UNIDAD: Backtracking (clases 9 y 10)      PUNTAJE: 1 punto
 *
 * Problema del dominio: enumerar todas las rutas de reparto posibles entre
 * dos puntos de la red que no superen un tiempo maximo ni una cantidad
 * maxima de tramos, sin pasar dos veces por el mismo punto.
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
     * Enumerar TODOS los caminos simples de origen a destino que no superen
     * un costo maximo ni una cantidad maxima de saltos.
     *
     * Esquema: se construye el camino de forma incremental (DFS). Al entrar a
     * un vertice se lo marca visitado y se lo agrega al camino; al volver se
     * lo desmarca y se lo quita: ese deshacer es el retroceso. La poda por
     * VIABILIDAD corta la rama apenas el costo parcial supera el maximo o la
     * cantidad de saltos alcanzo el limite sin llegar a destino, sin esperar
     * a completar el camino.
     *
     * Complejidad: exponencial en el peor caso, O(V!) caminos simples en un
     * grafo completo (cada permutacion de los vertices intermedios es un
     * camino distinto). La poda no cambia el peor caso pero reduce
     * drasticamente los nodos explorados en la practica; el resultado reporta
     * ambos conteos para medirlo. Espacio: O(V) para el camino y visitados,
     * mas el tamano de la salida.
     */
    public List<Ruta> rutasSimples(Grafo grafo, String origenId, String destinoId,
                                   double costoMaximo, int saltosMaximos) {
        return buscar(grafo, origenId, destinoId, costoMaximo, saltosMaximos).rutas();
    }

    /** Misma busqueda, devolviendo ademas los nodos explorados con y sin poda. */
    public ResultadoRutas buscar(Grafo grafo, String origenId, String destinoId,
                                 double costoMaximo, int saltosMaximos) {
        int origen = grafo.indiceDe(origenId);
        int destino = grafo.indiceDe(destinoId);
        if (costoMaximo < 0) {
            throw new IllegalArgumentException("El costo maximo no puede ser negativo");
        }
        if (saltosMaximos < 1) {
            throw new IllegalArgumentException("Los saltos maximos deben ser al menos 1");
        }

        Busqueda conPoda = new Busqueda(grafo, destino, costoMaximo, saltosMaximos, true);
        conPoda.explorar(origen, 0.0, 0);

        Busqueda sinPoda = new Busqueda(grafo, destino, costoMaximo, saltosMaximos, false);
        sinPoda.explorar(origen, 0.0, 0);

        return new ResultadoRutas(conPoda.rutas, conPoda.nodosExplorados,
                sinPoda.nodosExplorados, conPoda.ramasPodadas);
    }

    /** Estado de una busqueda: camino parcial, visitados y contadores. */
    private static final class Busqueda {
        private final Grafo grafo;
        private final int destino;
        private final double costoMaximo;
        private final int saltosMaximos;
        private final boolean podar;

        private final boolean[] visitado;
        private final List<String> caminoActual = new ArrayList<>();
        private final List<Ruta> rutas = new ArrayList<>();
        private int nodosExplorados = 0;
        private int ramasPodadas = 0;

        Busqueda(Grafo grafo, int destino, double costoMaximo, int saltosMaximos, boolean podar) {
            this.grafo = grafo;
            this.destino = destino;
            this.costoMaximo = costoMaximo;
            this.saltosMaximos = saltosMaximos;
            this.podar = podar;
            this.visitado = new boolean[grafo.cantidadVertices()];
        }

        void explorar(int u, double costoParcial, int saltos) {
            nodosExplorados++;
            visitado[u] = true;                       // decision: tomar u
            caminoActual.add(grafo.idDe(u));

            if (u == destino) {
                // con poda la condicion ya es cierta; sin poda se filtra aca
                if (costoParcial <= costoMaximo && saltos <= saltosMaximos) {
                    rutas.add(new Ruta(new ArrayList<>(caminoActual), costoParcial));
                }
            } else {
                for (Grafo.Arista a : grafo.vecinos(u)) {
                    if (visitado[a.destino()]) {
                        continue;                      // camino simple: no repetir
                    }
                    double nuevoCosto = costoParcial + a.costo();
                    // PODA por viabilidad: la rama ya no puede producir una
                    // ruta valida, no vale la pena entrar
                    if (podar && (nuevoCosto > costoMaximo || saltos + 1 > saltosMaximos)) {
                        ramasPodadas++;
                        continue;
                    }
                    explorar(a.destino(), nuevoCosto, saltos + 1);
                }
            }

            caminoActual.remove(caminoActual.size() - 1);   // retroceso: deshacer u
            visitado[u] = false;
        }
    }
}

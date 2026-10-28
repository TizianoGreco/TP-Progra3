package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.service.Grafo;

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
     * Enumerar TODOS los caminos simples de origen a destino que no superen
     * un costo maximo ni una cantidad maxima de saltos.
     *
     * Requisitos:
     *   - camino SIMPLE: no repetir vertices. Marcar y desmarcar el visitado
     *     es literalmente el retroceso del backtracking
     *   - podar en cuanto el costo parcial supere el maximo, sin esperar a
     *     completar el camino
     *   - devolver todas las rutas validas, no la primera
     *
     * Complejidad esperada: exponencial en el peor caso. Hay que poder
     * justificar por que, y explicar cuanto ayuda la poda en la practica.
     */
    public List<Ruta> rutasSimples(Grafo grafo, String origenId, String destinoId,
                                   double costoMaximo, int saltosMaximos) {
        throw new PendienteDeImplementar("Backtracking de rutas simples");
    }

    /**
     * Misma busqueda, devolviendo ademas los nodos explorados con y sin poda.
     * Es lo que usa el endpoint: esa comparacion es material de coloquio.
     */
    public ResultadoRutas buscar(Grafo grafo, String origenId, String destinoId,
                                 double costoMaximo, int saltosMaximos) {
        throw new PendienteDeImplementar("Backtracking de rutas simples");
    }
}

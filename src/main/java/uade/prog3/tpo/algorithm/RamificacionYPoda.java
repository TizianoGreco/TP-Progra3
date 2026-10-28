package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.model.Item;

import java.util.List;
import java.util.Map;

/**
 * UNIDAD: Branch and Bound (clase 12)       PUNTAJE: 1 punto
 * A CARGO DE: Persona 3 (optimizacion y busqueda)
 *
 * Problema del dominio: repartir los paquetes entre k vehiculos identicos de
 * modo que el vehiculo mas cargado lleve la menor carga posible (makespan).
 * Es backtracking mas una cota: alli se podaba por VIABILIDAD, aca por
 * OPTIMALIDAD (la rama no puede mejorar lo ya encontrado).
 *
 * Valor esperado con los paquetes del grupo y 3 vehiculos: carga maxima
 * 12 kg, cota inicial 34/3 = 11.33 (ver README).
 */
@Component
public class RamificacionYPoda {

    /**
     * Asignacion de items a contenedores.
     *
     * @param cargaMaxima     makespan de la asignacion optima
     * @param cotaInicial     cota inferior global: max(peso mayor, suma/k)
     * @param nodosExplorados nodos del arbol de busqueda que se visitaron
     * @param nodosPodados    nodos que se descartaron por la cota
     */
    public record Asignacion(Map<String, List<Item>> porContenedor,
                             double cargaMaxima,
                             double cotaInicial,
                             long nodosExplorados,
                             long nodosPodados) { }

    /**
     * Repartir items entre k contenedores identicos minimizando la carga del
     * contenedor mas cargado.
     *
     * Requisitos:
     *   - ramificar: cada item puede ir a cualquiera de los k contenedores
     *   - acotar: definir una cota INFERIOR del makespan alcanzable desde el
     *     nodo actual. Una cota valida y simple es
     *         max( carga maxima actual , suma de todos los pesos / k )
     *   - podar: si la cota es mayor o igual al mejor makespan ya encontrado,
     *     abandonar la rama
     *   - la cota tiene que ser OPTIMISTA. Si subestima el potencial de una
     *     rama se pierden soluciones validas y el resultado deja de ser optimo
     *
     * Complejidad esperada: exponencial, k^n en el peor caso, mucho menor con
     * buena poda.
     */
    public Asignacion repartir(List<Item> items, int cantidadContenedores) {
        throw new PendienteDeImplementar("Branch and Bound de reparto");
    }
}

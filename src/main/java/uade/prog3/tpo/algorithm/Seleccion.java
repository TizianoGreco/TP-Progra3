package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.model.Item;

import java.util.List;

/**
 * UNIDAD: Greedy (clase 3) y Programacion Dinamica (clase 6)
 * PUNTAJE: 1 punto greedy + 1 punto programacion dinamica
 * A CARGO DE: Persona 2 (greedy) y Persona 3 (mochila 0/1)
 *
 * Problema: cargar paquetes en un vehiculo de capacidad limitada (kg)
 * maximizando el valor total transportado. Los dos metodos resuelven EL
 * MISMO problema con tecnicas distintas, y esa comparacion es lo que se
 * evalua: hay que poder mostrar un caso donde greedy NO da el optimo.
 *
 * Valores esperados con los paquetes del grupo y capacidad 10 kg:
 * greedy {P1, P4, P7} = 78 USD, optimo {P2, P3} = 95 USD (ver README).
 */
@Component
public class Seleccion {

    /** Resultado de una seleccion de items bajo restriccion de capacidad. */
    public record Resultado(List<Item> elegidos, double valorTotal, double pesoTotal, String tecnica) { }

    /**
     * GREEDY (Persona 2): seleccionar items maximizando el valor total sin
     * superar la capacidad, ordenando por ratio valor/peso descendente y
     * tomando mientras entren.
     *
     * Requisitos:
     *   - declarar EXPLICITAMENTE cual es la funcion objetivo
     *   - usar el ratio valor/peso como criterio, no el valor a secas
     *   - documentar la complejidad, que esta dominada por el ordenamiento
     *
     * Complejidad esperada: O(n log n).
     */
    public Resultado greedy(List<Item> items, double capacidad) {
        throw new PendienteDeImplementar("Seleccion greedy");
    }

    /**
     * PROGRAMACION DINAMICA (Persona 3): mochila 0/1 con tabla dp.
     *
     * Requisitos:
     *   - construir la tabla dp[i][j] de forma iterativa (bottom-up)
     *   - RECUPERAR EL CAMINO: devolver que items componen la solucion optima,
     *     no solo el valor maximo. Eso se hace recorriendo la tabla hacia atras
     *   - los pesos deben ser enteros o discretizarse para indexar la tabla
     *
     * Complejidad esperada: O(n * W) en tiempo y en espacio.
     *
     * Para el coloquio: explicar por que O(n*W) es pseudopolinomica.
     */
    public Resultado programacionDinamica(List<Item> items, int capacidad) {
        throw new PendienteDeImplementar("Mochila 0/1 con programacion dinamica");
    }
}

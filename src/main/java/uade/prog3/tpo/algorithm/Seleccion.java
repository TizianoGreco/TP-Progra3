package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.model.Item;

import java.util.ArrayList;
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
     * dp[i][w] es el maximo valor usando los primeros i items con capacidad w.
     * Cada celda elige el maximo entre no llevar el item i y llevarlo, si entra:
     * dp[i][w] = max( dp[i-1][w] , dp[i-1][w - peso_i] + valor_i ).
     * Se llena de izquierda a derecha y de arriba hacia abajo (bottom-up):
     * cada celda lee solo la fila anterior, que ya esta resuelta.
     *
     * Los items elegidos se recuperan recorriendo la tabla hacia atras desde
     * dp[n][capacidad]: si la celda mejoro respecto de la fila anterior, ese
     * item entra y la capacidad baja su peso. El orden de la lista sigue el
     * de la entrada.
     *
     * El peso se indexa con Math.ceil para que la columna sea un entero. El
     * peso informado en el resultado es el real, no el redondeado.
     *
     * Tiempo y espacio O(n * W): una celda por item y por kilo de capacidad.
     * Es pseudopolinomica: W es el valor de la capacidad, no la cantidad de
     * bits con que se lo escribe. Con capacidad enorme la tabla no entra en
     * memoria aunque n sea chico.
     */
    public Resultado programacionDinamica(List<Item> items, int capacidad) {
        if (capacidad < 0) {
            throw new IllegalArgumentException(
                    "La capacidad no puede ser negativa: " + capacidad);
        }
        int n = items.size();
        int[] pesoIndice = new int[n];
        for (int i = 0; i < n; i++) {
            double peso = items.get(i).getPeso();
            if (peso < 0) {
                throw new IllegalArgumentException(
                        "El item " + items.get(i).getId() + " tiene peso negativo: " + peso);
            }
            pesoIndice[i] = (int) Math.ceil(peso);
        }

        double[][] dp = new double[n + 1][capacidad + 1];
        for (int i = 1; i <= n; i++) {
            int peso = pesoIndice[i - 1];
            double valor = items.get(i - 1).getValor();
            for (int w = 0; w <= capacidad; w++) {
                dp[i][w] = dp[i - 1][w];
                if (peso <= w) {
                    double llevandolo = dp[i - 1][w - peso] + valor;
                    if (llevandolo > dp[i][w]) {
                        dp[i][w] = llevandolo;
                    }
                }
            }
        }

        List<Item> alReves = new ArrayList<>();
        int w = capacidad;
        for (int i = n; i >= 1; i--) {
            if (dp[i][w] > dp[i - 1][w]) {
                alReves.add(items.get(i - 1));
                w -= pesoIndice[i - 1];
            }
        }
        List<Item> elegidos = new ArrayList<>();
        for (int i = alReves.size() - 1; i >= 0; i--) {
            elegidos.add(alReves.get(i));
        }

        double valorTotal = 0;
        double pesoTotal = 0;
        for (Item item : elegidos) {
            valorTotal += item.getValor();
            pesoTotal += item.getPeso();
        }
        return new Resultado(elegidos, valorTotal, pesoTotal, "programacion dinamica");
    }
}

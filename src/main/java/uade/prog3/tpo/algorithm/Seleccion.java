package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.model.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * UNIDAD: Greedy (clase 3) y Programacion Dinamica (clase 6)
 * PUNTAJE: 1 punto greedy + 1 punto programacion dinamica
 *
 * Problema: cargar paquetes en un vehiculo de capacidad limitada (kg)
 * maximizando el valor total transportado. Los dos metodos lo resuelven con
 * tecnicas distintas; greedy no garantiza el optimo en la version 0/1.
 */
@Component
public class Seleccion {

    /** Resultado de una seleccion de items bajo restriccion de capacidad. */
    public record Resultado(List<Item> elegidos, double valorTotal, double pesoTotal, String tecnica) { }

    private final Ordenamiento ordenamiento;

    public Seleccion(Ordenamiento ordenamiento) {
        this.ordenamiento = ordenamiento;
    }

    /**
     * GREEDY por densidad de valor.
     *
     * Funcion objetivo: maximizar la suma de valor de los items elegidos,
     * sujeto a que la suma de pesos no supere la capacidad.
     *
     * Criterio: ordenar por ratio valor/peso descendente y tomar cada item
     * mientras entre en la capacidad restante. Se usa el ratio y no el valor
     * a secas porque el recurso escaso es el peso: un item de mucho valor y
     * mucho peso puede desplazar a varios que juntos valen mas.
     *
     * Complejidad: O(n log n), dominada por el MergeSort propio; el recorrido
     * posterior es O(n). Espacio: O(n).
     *
     * NO es optimo para mochila 0/1: si el item de mejor ratio deja un hueco
     * que ningun otro puede llenar, una combinacion de items de peor ratio
     * puede valer mas. Ver el contraejemplo del dominio en docs/ENDPOINTS.md.
     * Si es optimo para la version fraccional (donde se puede cargar parte de
     * un item).
     */
    public Resultado greedy(List<Item> items, double capacidad) {
        if (capacidad < 0) {
            throw new IllegalArgumentException("La capacidad no puede ser negativa");
        }
        List<Item> porRatioDesc = ordenamiento.mergeSort(items,
                Comparator.comparingDouble(Item::ratioValorPeso).reversed());

        List<Item> elegidos = new ArrayList<>();
        double peso = 0.0;
        double valor = 0.0;
        for (Item it : porRatioDesc) {
            if (peso + it.getPeso() <= capacidad) {
                elegidos.add(it);
                peso += it.getPeso();
                valor += it.getValor();
            }
        }
        return new Resultado(elegidos, valor, peso, "greedy");
    }

    /**
     * PROGRAMACION DINAMICA: mochila 0/1 con tabla dp bottom-up.
     *
     * dp[i][w] = maximo valor usando solo los primeros i items con capacidad w.
     * Recurrencia:
     *     dp[i][w] = dp[i-1][w]                                  si peso_i > w
     *     dp[i][w] = max( dp[i-1][w], dp[i-1][w - peso_i] + valor_i ) si no
     * Caso base: dp[0][w] = 0.
     *
     * Recuperacion del camino: se recorre la tabla desde dp[n][W] hacia
     * atras; si dp[i][w] != dp[i-1][w] el item i fue tomado y se descuenta su
     * peso.
     *
     * Los pesos se discretizan a enteros (ceil) para indexar la tabla; asi la
     * solucion nunca supera la capacidad real.
     *
     * Complejidad: O(n * W) tiempo y espacio, con W la capacidad. Es
     * PSEUDOPOLINOMICA: es polinomica en el VALOR numerico de W, pero W se
     * representa con log W bits, asi que en funcion del tamano de la entrada
     * es exponencial. Por eso mochila 0/1 sigue siendo NP-completo aunque
     * exista esta tabla. Estructura: matriz double[n+1][W+1].
     */
    public Resultado programacionDinamica(List<Item> items, int capacidad) {
        if (capacidad < 0) {
            throw new IllegalArgumentException("La capacidad no puede ser negativa");
        }
        int n = items.size();
        int[] peso = new int[n];
        for (int i = 0; i < n; i++) {
            if (items.get(i).getPeso() < 0) {
                throw new IllegalArgumentException("El item " + items.get(i).getId() + " tiene peso negativo");
            }
            peso[i] = (int) Math.ceil(items.get(i).getPeso());
        }

        double[][] dp = new double[n + 1][capacidad + 1];
        for (int i = 1; i <= n; i++) {
            double valor = items.get(i - 1).getValor();
            for (int w = 0; w <= capacidad; w++) {
                dp[i][w] = dp[i - 1][w];                       // no tomar el item i
                if (peso[i - 1] <= w) {
                    double tomando = dp[i - 1][w - peso[i - 1]] + valor;
                    if (tomando > dp[i][w]) {
                        dp[i][w] = tomando;                    // tomar el item i
                    }
                }
            }
        }

        // recuperacion de la solucion recorriendo la tabla hacia atras
        List<Item> elegidos = new ArrayList<>();
        double pesoTotal = 0.0;
        int w = capacidad;
        for (int i = n; i >= 1; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                Item it = items.get(i - 1);
                elegidos.add(it);
                pesoTotal += it.getPeso();
                w -= peso[i - 1];
            }
        }
        Collections.reverse(elegidos);
        return new Resultado(elegidos, dp[n][capacidad], pesoTotal, "programacion dinamica");
    }
}

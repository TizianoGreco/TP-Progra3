package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.model.Item;

import java.util.ArrayList;
import java.util.Comparator;
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

    /** Stateless: se instancia aca para no cambiar el constructor que usan los tests. */
    private final Ordenamiento ordenamiento = new Ordenamiento();

    /**
     * GREEDY (Persona 2) por densidad de valor.
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
     * NO es optimo para mochila 0/1: con capacidad 10 toma P1 (ratio 10) y el
     * hueco de 4 kg que deja ya no admite a P2 ni a P3, que juntos valen 95.
     * Si es optimo para la version fraccional (se puede cargar parte de un item).
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

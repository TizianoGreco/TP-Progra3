package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.model.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * UNIDAD: Branch and Bound (clase 12)       PUNTAJE: 1 punto
 *
 * Problema del dominio: repartir los paquetes entre k vehiculos identicos de
 * modo que el vehiculo mas cargado lleve la menor carga posible (makespan).
 * Es backtracking mas una cota: alli se podaba por VIABILIDAD, aca por
 * OPTIMALIDAD (la rama no puede mejorar lo ya encontrado).
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

    private final Ordenamiento ordenamiento;

    public RamificacionYPoda(Ordenamiento ordenamiento) {
        this.ordenamiento = ordenamiento;
    }

    /**
     * Repartir items entre k contenedores identicos minimizando la carga del
     * contenedor mas cargado.
     *
     * Ramificacion: los items se procesan en orden de peso descendente (los
     * pesados primero producen cotas ajustadas temprano); en cada nivel el
     * item actual puede ir a cualquiera de los k contenedores.
     *
     * Cota INFERIOR del makespan alcanzable desde un nodo parcial:
     *     cota = max( carga maxima actual ,  (suma total de pesos) / k )
     * Es OPTIMISTA (nunca supera el makespan real de la mejor hoja bajo el
     * nodo) por dos razones: la carga maxima actual solo puede crecer al
     * seguir agregando items, y ningun reparto puede tener todos los
     * contenedores por debajo del promedio, asi que el maximo es al menos
     * suma/k. Como la cota subestima, nunca poda una rama que contenga la
     * solucion optima; si sobreestimara, se perderian soluciones validas.
     *
     * Poda: si cota >= mejor makespan encontrado, la rama no puede mejorar y
     * se abandona. Ademas se corta la simetria: dos contenedores con la misma
     * carga son intercambiables, asi que el item solo se prueba en uno de
     * ellos (poda de duplicados, no cambia el optimo).
     *
     * Solucion inicial: el greedy LPT (cada item al contenedor menos cargado)
     * da una cota superior buena desde el principio, lo que permite podar
     * mucho mas.
     *
     * Complejidad: O(k^n) nodos en el peor caso (cada item en cada
     * contenedor), con O(n log n) del ordenamiento previo. Con la cota la
     * cantidad real de nodos es ordenes de magnitud menor; el resultado
     * reporta explorados y podados como evidencia. Espacio: O(n + k).
     */
    public Asignacion repartir(List<Item> items, int cantidadContenedores) {
        if (cantidadContenedores < 1) {
            throw new IllegalArgumentException("La cantidad de contenedores debe ser al menos 1");
        }
        for (Item it : items) {
            if (it.getPeso() < 0) {
                throw new IllegalArgumentException("El item " + it.getId() + " tiene peso negativo");
            }
        }
        int k = cantidadContenedores;
        List<Item> ordenados = ordenamiento.mergeSort(items,
                Comparator.comparingDouble(Item::getPeso).reversed());
        int n = ordenados.size();

        double sumaTotal = 0.0;
        double pesoMayor = 0.0;
        for (Item it : ordenados) {
            sumaTotal += it.getPeso();
            pesoMayor = Math.max(pesoMayor, it.getPeso());
        }
        double cotaGlobal = Math.max(pesoMayor, sumaTotal / k);

        Estado estado = new Estado(ordenados, k, sumaTotal / k);

        // solucion inicial greedy (LPT): cota superior para empezar a podar
        estado.mejorMakespan = greedyLpt(ordenados, k, estado.mejorAsignacion);

        if (n > 0 && estado.mejorMakespan > cotaGlobal) {
            estado.ramificar(0, new double[k], new int[n]);
        }

        Map<String, List<Item>> porContenedor = new LinkedHashMap<>();
        for (int c = 0; c < k; c++) {
            porContenedor.put("contenedor" + (c + 1), new ArrayList<>());
        }
        for (int i = 0; i < n; i++) {
            porContenedor.get("contenedor" + (estado.mejorAsignacion[i] + 1)).add(ordenados.get(i));
        }
        return new Asignacion(porContenedor, estado.mejorMakespan, cotaGlobal,
                estado.nodosExplorados, estado.nodosPodados);
    }

    /** Greedy LPT: cada item (ya ordenado por peso desc) al contenedor menos cargado. */
    private double greedyLpt(List<Item> ordenados, int k, int[] asignacion) {
        double[] carga = new double[k];
        for (int i = 0; i < ordenados.size(); i++) {
            int menos = 0;
            for (int c = 1; c < k; c++) {
                if (carga[c] < carga[menos]) {
                    menos = c;
                }
            }
            carga[menos] += ordenados.get(i).getPeso();
            asignacion[i] = menos;
        }
        double max = 0.0;
        for (double c : carga) {
            max = Math.max(max, c);
        }
        return max;
    }

    /** Estado mutable de la busqueda. */
    private static final class Estado {
        private final List<Item> items;
        private final int k;
        private final double promedio;         // suma total / k, parte fija de la cota
        private final int[] mejorAsignacion;
        private double mejorMakespan = Double.POSITIVE_INFINITY;
        private long nodosExplorados = 0;
        private long nodosPodados = 0;

        Estado(List<Item> items, int k, double promedio) {
            this.items = items;
            this.k = k;
            this.promedio = promedio;
            this.mejorAsignacion = new int[items.size()];
        }

        void ramificar(int i, double[] carga, int[] asignacion) {
            nodosExplorados++;

            double cargaMaxima = 0.0;
            for (double c : carga) {
                cargaMaxima = Math.max(cargaMaxima, c);
            }
            double cota = Math.max(cargaMaxima, promedio);   // cota inferior optimista
            if (cota >= mejorMakespan) {                       // PODA por optimalidad
                nodosPodados++;
                return;
            }

            if (i == items.size()) {                           // hoja: solucion completa
                mejorMakespan = cargaMaxima;
                System.arraycopy(asignacion, 0, mejorAsignacion, 0, asignacion.length);
                return;
            }

            double peso = items.get(i).getPeso();
            for (int c = 0; c < k; c++) {
                if (esSimetrico(carga, c)) {
                    continue;   // un contenedor con la misma carga ya fue probado
                }
                carga[c] += peso;                              // decision
                asignacion[i] = c;
                ramificar(i + 1, carga, asignacion);
                carga[c] -= peso;                              // retroceso
            }
        }

        /** true si algun contenedor anterior tiene exactamente la misma carga que c. */
        private boolean esSimetrico(double[] carga, int c) {
            for (int j = 0; j < c; j++) {
                if (carga[j] == carga[c]) {
                    return true;
                }
            }
            return false;
        }
    }
}

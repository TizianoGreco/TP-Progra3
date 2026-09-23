package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.model.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * UNIDAD: Divide y Conquista (clase 2)     PUNTAJE: 1 punto
 *
 * Ambos algoritmos devuelven una lista NUEVA y no modifican la de entrada.
 * No se usa Collections.sort ni Arrays.sort.
 */
@Component
public class Ordenamiento {

    /**
     * QuickSort con particion de Lomuto y pivote en el elemento del medio.
     *
     * Estrategia de pivote: se toma el elemento central del rango y se lo
     * intercambia con el ultimo antes de particionar. Con el pivote en el
     * ultimo elemento, una lista ya ordenada (caso frecuente con datos que
     * vienen de una base) produce particiones de tamano n-1 y 0 en cada
     * nivel, es decir el peor caso O(n^2). El pivote central evita ese caso
     * degenerado sobre entradas ordenadas o casi ordenadas.
     *
     * Recurrencia (promedio): T(n) = 2T(n/2) + O(n) -> O(n log n).
     * Peor caso: T(n) = T(n-1) + O(n) -> O(n^2), cuando el pivote elegido es
     * siempre el minimo o el maximo del rango (por ejemplo, todos los
     * elementos iguales con esta particion).
     * Espacio: O(log n) de pila en promedio (ordena in-place sobre la copia).
     * No es estable.
     */
    public List<Item> quickSort(List<Item> items, Comparator<Item> criterio) {
        List<Item> copia = new ArrayList<>(items);
        quickSort(copia, 0, copia.size() - 1, criterio);
        return copia;
    }

    private void quickSort(List<Item> a, int bajo, int alto, Comparator<Item> c) {
        if (bajo >= alto) {
            return;
        }
        int p = particionar(a, bajo, alto, c);
        quickSort(a, bajo, p - 1, c);
        quickSort(a, p + 1, alto, c);
    }

    /**
     * Particion de Lomuto: deja el pivote en su posicion definitiva, con los
     * menores a la izquierda y los mayores o iguales a la derecha.
     */
    private int particionar(List<Item> a, int bajo, int alto, Comparator<Item> c) {
        int medio = bajo + (alto - bajo) / 2;
        intercambiar(a, medio, alto);
        Item pivote = a.get(alto);
        int i = bajo;                       // frontera de los menores al pivote
        for (int j = bajo; j < alto; j++) {
            if (c.compare(a.get(j), pivote) < 0) {
                intercambiar(a, i, j);
                i++;
            }
        }
        intercambiar(a, i, alto);
        return i;
    }

    private void intercambiar(List<Item> a, int i, int j) {
        if (i != j) {
            Item tmp = a.get(i);
            a.set(i, a.get(j));
            a.set(j, tmp);
        }
    }

    /**
     * MergeSort top-down.
     *
     * Divide la lista en dos mitades, ordena cada una recursivamente y las
     * mezcla. La mezcla es estable porque ante empate toma primero el
     * elemento de la mitad izquierda (comparacion con <= 0).
     *
     * Recurrencia: T(n) = 2T(n/2) + O(n). Con a=2, b=2, k=1 se tiene
     * a = b^k, por lo tanto T(n) = O(n^k log n) = O(n log n), en TODOS los
     * casos: la division siempre es por la mitad, sin depender de los datos.
     * Espacio: O(n) por las listas auxiliares de la mezcla.
     */
    public List<Item> mergeSort(List<Item> items, Comparator<Item> criterio) {
        if (items.size() <= 1) {
            return new ArrayList<>(items);
        }
        int medio = items.size() / 2;
        List<Item> izq = mergeSort(items.subList(0, medio), criterio);
        List<Item> der = mergeSort(items.subList(medio, items.size()), criterio);
        return mezclar(izq, der, criterio);
    }

    private List<Item> mezclar(List<Item> izq, List<Item> der, Comparator<Item> c) {
        List<Item> resultado = new ArrayList<>(izq.size() + der.size());
        int i = 0, j = 0;
        while (i < izq.size() && j < der.size()) {
            if (c.compare(izq.get(i), der.get(j)) <= 0) {   // <= mantiene la estabilidad
                resultado.add(izq.get(i++));
            } else {
                resultado.add(der.get(j++));
            }
        }
        while (i < izq.size()) resultado.add(izq.get(i++));
        while (j < der.size()) resultado.add(der.get(j++));
        return resultado;
    }
}

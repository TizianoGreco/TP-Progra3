package uade.prog3.tpo.algorithm;

import org.springframework.stereotype.Component;
import uade.prog3.tpo.model.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
     * Cada item se prueba en cada contenedor. La cota del nodo es
     * max(carga maxima actual, suma de todos los pesos / k). Es optimista:
     * el makespan final no puede quedar por debajo de lo que ya cargo un
     * vehiculo, ni por debajo del promedio si se reparte parejo. Si la cota
     * es mayor o igual al mejor makespan ya encontrado, esa rama no puede
     * mejorar y se poda.
     *
     * Los vehiculos con la misma carga son indistinguibles: el item solo se
     * prueba en el de menor indice. No cambia el optimo y corta permutaciones.
     *
     * Peor caso O(k^n): n items y k opciones cada uno. Espacio O(n + k),
     * la pila de la recursion y las cargas.
     */
    public Asignacion repartir(List<Item> items, int cantidadContenedores) {
        if (cantidadContenedores < 1) {
            throw new IllegalArgumentException(
                    "La cantidad de contenedores debe ser al menos 1: " + cantidadContenedores);
        }
        double suma = 0;
        double mayor = 0;
        for (Item item : items) {
            suma += item.getPeso();
            if (item.getPeso() > mayor) {
                mayor = item.getPeso();
            }
        }
        double cotaInicial = Math.max(mayor, suma / cantidadContenedores);

        double[] cargas = new double[cantidadContenedores];
        List<List<Item>> grupos = new ArrayList<>();
        for (int j = 0; j < cantidadContenedores; j++) {
            grupos.add(new ArrayList<>());
        }
        Busqueda busqueda = new Busqueda(suma / cantidadContenedores);
        asignar(items, 0, cargas, grupos, busqueda);

        Map<String, List<Item>> porContenedor = new LinkedHashMap<>();
        for (int j = 0; j < cantidadContenedores; j++) {
            porContenedor.put("contenedor" + (j + 1), busqueda.mejorGrupos.get(j));
        }
        return new Asignacion(porContenedor, busqueda.mejor, cotaInicial,
                busqueda.explorados, busqueda.podados);
    }

    private void asignar(List<Item> items, int indice, double[] cargas,
                         List<List<Item>> grupos, Busqueda busqueda) {
        busqueda.explorados++;
        double cargaMaxima = 0;
        for (double carga : cargas) {
            if (carga > cargaMaxima) {
                cargaMaxima = carga;
            }
        }
        double cota = Math.max(cargaMaxima, busqueda.promedio);
        if (cota >= busqueda.mejor) {
            busqueda.podados++;
            return;
        }
        if (indice == items.size()) {
            busqueda.mejor = cargaMaxima;
            busqueda.mejorGrupos = new ArrayList<>();
            for (List<Item> grupo : grupos) {
                busqueda.mejorGrupos.add(new ArrayList<>(grupo));
            }
            return;
        }

        Item item = items.get(indice);
        for (int j = 0; j < cargas.length; j++) {
            if (hayOtroConLaMismaCarga(cargas, j)) {
                continue;
            }
            cargas[j] += item.getPeso();
            grupos.get(j).add(item);
            asignar(items, indice + 1, cargas, grupos, busqueda);
            grupos.get(j).remove(grupos.get(j).size() - 1);
            cargas[j] -= item.getPeso();
        }
    }

    /** Dos vehiculos con la misma carga son el mismo estado. Alcanza con el de menor indice. */
    private static boolean hayOtroConLaMismaCarga(double[] cargas, int j) {
        for (int i = 0; i < j; i++) {
            if (cargas[i] == cargas[j]) {
                return true;
            }
        }
        return false;
    }

    private static final class Busqueda {
        private final double promedio;
        private double mejor = Double.POSITIVE_INFINITY;
        private List<List<Item>> mejorGrupos = List.of();
        private long explorados;
        private long podados;

        private Busqueda(double promedio) {
            this.promedio = promedio;
        }
    }
}

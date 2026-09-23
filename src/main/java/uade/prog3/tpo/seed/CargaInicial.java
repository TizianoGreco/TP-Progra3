package uade.prog3.tpo.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uade.prog3.tpo.model.Item;
import uade.prog3.tpo.model.Nodo;
import uade.prog3.tpo.repository.ItemRepository;
import uade.prog3.tpo.repository.NodoRepository;

import java.util.List;

/**
 * Carga el dominio del grupo: la red logistica de reparto de paquetes en el
 * AMBA. Ver README.md para la descripcion completa y el diagrama.
 *
 * Nodos: un centro de distribucion (CD), tres depositos regionales y seis
 * puntos de entrega. Aristas: tramos de ruta planificados, con sentido de
 * circulacion; el costo es el TIEMPO DE VIAJE EN MINUTOS.
 * Items: paquetes con peso (kg) y valor declarado (USD).
 *
 * El grafo esta armado para que:
 *   - el camino mas rapido NO sea el directo en varios pares
 *     (CD->DN directo 30, via PAL 27; DS->QUI directo 35, via AVE 32)
 *   - sea conexo como no dirigido, para que exista el MST (costo 150)
 *   - todos los costos sean positivos, para que Dijkstra aplique
 *   - haya varias rutas simples entre CD y QUI para el backtracking (8)
 *
 * Los paquetes estan calibrados para que, con capacidad 10 kg, greedy por
 * ratio elija {P1, P4, P7} = 78 USD mientras que el optimo es {P2, P3} = 95.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class CargaInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicial.class);

    private final NodoRepository nodoRepository;
    private final ItemRepository itemRepository;

    public CargaInicial(NodoRepository nodoRepository, ItemRepository itemRepository) {
        this.nodoRepository = nodoRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public void run(String... args) {
        if (nodoRepository.count() > 0) {
            log.info("La base ya tiene datos. No se ejecuta la carga inicial.");
            return;
        }

        List<Nodo> nodos = nodos();
        nodoRepository.saveAll(nodos);
        itemRepository.saveAll(paquetes());

        log.info("Carga inicial completada: {} nodos, 15 tramos, 8 paquetes.", nodos.size());
        log.info("Para desactivarla: app.seed.enabled=false");
    }

    /** Red logistica del grupo. Tambien la usan los tests, por eso es publica y estatica. */
    public static List<Nodo> nodos() {
        Nodo cd  = new Nodo("CD",  "Centro de Distribucion Barracas", "CENTRO",   0);
        Nodo dn  = new Nodo("DN",  "Deposito Norte (Vicente Lopez)",  "DEPOSITO", 120);
        Nodo ds  = new Nodo("DS",  "Deposito Sur (Lanus)",            "DEPOSITO", 150);
        Nodo dO  = new Nodo("DO",  "Deposito Oeste (Ramos Mejia)",    "DEPOSITO", 100);
        Nodo pal = new Nodo("PAL", "Palermo",                         "ENTREGA",  40);
        Nodo cab = new Nodo("CAB", "Caballito",                       "ENTREGA",  35);
        Nodo si  = new Nodo("SI",  "San Isidro",                      "ENTREGA",  25);
        Nodo ave = new Nodo("AVE", "Avellaneda",                      "ENTREGA",  30);
        Nodo qui = new Nodo("QUI", "Quilmes",                         "ENTREGA",  20);
        Nodo mor = new Nodo("MOR", "Moron",                           "ENTREGA",  28);

        // tramo (origen -> destino, minutos)
        cd.conectar(pal, 15);
        cd.conectar(ds, 20);
        cd.conectar(cab, 22);
        cd.conectar(dn, 30);   // directo 30, pero CD->PAL->DN = 27
        cd.conectar(dO, 30);
        pal.conectar(dn, 12);
        pal.conectar(cab, 14);
        dn.conectar(si, 18);
        cab.conectar(ds, 18);
        cab.conectar(mor, 28);
        dO.conectar(mor, 16);
        dO.conectar(cab, 25);
        ds.conectar(ave, 12);
        ds.conectar(qui, 35);  // directo 35, pero DS->AVE->QUI = 32
        ave.conectar(qui, 20);

        return List.of(cd, dn, ds, dO, pal, cab, si, ave, qui, mor);
    }

    /** Paquetes a repartir: peso en kg, valor declarado en USD. */
    public static List<Item> paquetes() {
        return List.of(
                new Item("P1", "Notebook",          6, 60, "PAL"),
                new Item("P2", "Monitor",           5, 48, "CAB"),
                new Item("P3", "Impresora",         5, 47, "SI"),
                new Item("P4", "Libros",            2, 14, "AVE"),
                new Item("P5", "Ropa",              3, 18, "QUI"),
                new Item("P6", "Herramientas",      8, 40, "MOR"),
                new Item("P7", "Documentacion",     1,  4, "DN"),
                new Item("P8", "Repuestos",         4, 12, "DS")
        );
    }
}

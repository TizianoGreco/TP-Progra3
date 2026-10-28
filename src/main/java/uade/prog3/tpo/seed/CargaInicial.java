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
 * Carga el dominio del grupo: la red logistica de depositos y centros de
 * distribucion del AMBA. Ver README.md para la descripcion completa.
 *
 * Nodos: cuatro centros de distribucion (CD central en Barracas y tres
 * regionales) y seis depositos. Aristas: rutas entre ellos, con sentido de
 * circulacion; el costo es la DISTANCIA EN KILOMETROS.
 * Items: paquetes con peso (kg) y valor declarado (USD).
 *
 * El grafo esta armado para que:
 *   - el camino mas corto NO sea el directo en varios pares
 *     (CD->DN directo 19 km, via PAL 17; DS->QUI directo 19, via AVE 17)
 *   - sea conexo como no dirigido, para que exista el MST (77 km)
 *   - todas las distancias sean positivas, para que Dijkstra aplique
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

        log.info("Carga inicial completada: {} nodos, 15 rutas, 8 paquetes.", nodos.size());
        log.info("Para desactivarla: app.seed.enabled=false");
    }

    /** Red logistica del grupo. Tambien la usan los tests, por eso es publica y estatica. */
    public static List<Nodo> nodos() {
        Nodo cd  = new Nodo("CD",  "Centro de Distribucion Barracas",              "CENTRO",   0);
        Nodo dn  = new Nodo("DN",  "Centro de Distribucion Norte (Vicente Lopez)", "CENTRO",   120);
        Nodo ds  = new Nodo("DS",  "Centro de Distribucion Sur (Lanus)",           "CENTRO",   150);
        Nodo dO  = new Nodo("DO",  "Centro de Distribucion Oeste (Ramos Mejia)",   "CENTRO",   100);
        Nodo pal = new Nodo("PAL", "Deposito Palermo",                             "DEPOSITO", 40);
        Nodo cab = new Nodo("CAB", "Deposito Caballito",                           "DEPOSITO", 35);
        Nodo si  = new Nodo("SI",  "Deposito San Isidro",                          "DEPOSITO", 25);
        Nodo ave = new Nodo("AVE", "Deposito Avellaneda",                          "DEPOSITO", 30);
        Nodo qui = new Nodo("QUI", "Deposito Quilmes",                             "DEPOSITO", 20);
        Nodo mor = new Nodo("MOR", "Deposito Moron",                               "DEPOSITO", 28);

        // ruta (origen -> destino, km)
        cd.conectar(pal, 9);
        cd.conectar(ds, 10);
        cd.conectar(cab, 7);
        cd.conectar(dn, 19);   // directo 19, pero CD->PAL->DN = 17
        cd.conectar(dO, 18);
        pal.conectar(dn, 8);
        pal.conectar(cab, 6);
        dn.conectar(si, 12);
        cab.conectar(ds, 11);
        cab.conectar(mor, 17);
        dO.conectar(mor, 5);
        dO.conectar(cab, 12);
        ds.conectar(ave, 5);
        ds.conectar(qui, 19);  // directo 19, pero DS->AVE->QUI = 17
        ave.conectar(qui, 12);

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

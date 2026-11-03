package uade.prog3.tpo.service;

import org.junit.jupiter.api.Test;
import uade.prog3.tpo.repository.NodoRepository;
import uade.prog3.tpo.seed.CargaInicial;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests de la vista de la red que usa la pagina visual.
 * El repositorio se reemplaza por un mock: no hace falta Neo4j.
 */
class GrafoServiceTest {

    private GrafoService.Red redDeLaCargaInicial() {
        NodoRepository repo = mock(NodoRepository.class);
        when(repo.findAll()).thenReturn(CargaInicial.nodos());
        return new GrafoService(repo).red();
    }

    @Test
    void devuelveLosDiezNodosYLasQuinceRutas() {
        GrafoService.Red red = redDeLaCargaInicial();
        assertEquals(10, red.nodos().size());
        assertEquals(15, red.rutas().size());
    }

    @Test
    void cadaRutaConservaSentidoYKilometros() {
        GrafoService.Red red = redDeLaCargaInicial();
        assertTrue(red.rutas().contains(new GrafoService.RutaVista("CD", "DN", 19)));
        assertTrue(red.rutas().contains(new GrafoService.RutaVista("AVE", "QUI", 12)));
        assertFalse(red.rutas().contains(new GrafoService.RutaVista("QUI", "AVE", 12)),
                "las rutas son dirigidas: no se inventa la vuelta");
    }

    @Test
    void cadaNodoTraeNombreYTipo() {
        GrafoService.Red red = redDeLaCargaInicial();
        assertTrue(red.nodos().contains(
                new GrafoService.NodoVista("CD", "Centro de Distribucion Barracas", "CENTRO")));
        assertTrue(red.nodos().contains(
                new GrafoService.NodoVista("QUI", "Deposito Quilmes", "DEPOSITO")));
    }
}

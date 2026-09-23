package uade.prog3.tpo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uade.prog3.tpo.algorithm.PendienteDeImplementar;

import java.util.Map;

/**
 * Traduce excepciones a respuestas HTTP con un cuerpo entendible.
 *
 * Ningun endpoint devuelve un stack trace: los errores de validacion caen en
 * 400, los estados imposibles (grafo no conexo, destino inalcanzable, ciclo
 * negativo) en 422, y cualquier otra falla inesperada en un 500 con mensaje
 * generico y el detalle en el log.
 */
@RestControllerAdvice
public class ManejadorDeErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorDeErrores.class);

    @ExceptionHandler(PendienteDeImplementar.class)
    public ResponseEntity<Map<String, String>> pendiente(PendienteDeImplementar e) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("estado", "PENDIENTE", "detalle", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("estado", "ERROR", "detalle", e.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> parametroFaltante(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("estado", "ERROR", "detalle", "Falta el parametro obligatorio: " + e.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> tipoInvalido(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("estado", "ERROR", "detalle", "El parametro '" + e.getName()
                        + "' tiene un valor invalido: " + e.getValue()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> estadoInvalido(IllegalStateException e) {
        return ResponseEntity.unprocessableEntity()
                .body(Map.of("estado", "ERROR", "detalle", e.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("estado", "ERROR", "detalle", "No existe el recurso: /" + e.getResourcePath()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> inesperado(Exception e) {
        log.error("Error inesperado atendiendo una peticion", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("estado", "ERROR", "detalle", "Error interno. Revisar el log del servidor."));
    }
}

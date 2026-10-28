---
name: tpo-verificar-entrega
description: Verifica que el TPO de Programación III cumpla los requisitos de admisibilidad y de "Completo" antes de poner el tag entrega-final. Usar antes de taggear, antes de mergear el último PR, o cuando pregunten "¿está todo para entregar?" o "¿qué falta?".
---

# Verificar la entrega del TPO

Revisión de solo lectura: **no corrijas nada sin preguntar**. Reportá qué cumple, qué falta
y quién lo tiene que hacer. La referencia es `SKILLS-Y-ALINEAMIENTOS/06-GIT-Y-ENTREGA.md` y
la sección 4 de `CONSIGNA.md`.

## Chequeos

1. **Build y tests.** `./mvnw test` y reportar la línea `Tests run: N, Failures: F, Errors: E`.
   Cualquier falla es bloqueante.
2. **Algoritmos.** Para cada uno de los doce de `01-DOMINIO.md`: ¿el método sigue lanzando
   `PendienteDeImplementar`? (`grep -rn PendienteDeImplementar src/main/java/uade/prog3/tpo/algorithm/`).
   ¿Tiene clase de test? ¿Tiene ficha en `docs/ENDPOINTS.md`? ¿Tiene fila completa en
   `docs/COMPLEJIDADES.md`?
3. **Librerías prohibidas.** `grep -rnE "Collections\.sort|Arrays\.sort|\.sort\(|sorted\(|jgrapht" src/main/java`.
   Cualquier aparición en `algorithm/` es bloqueante.
4. **Lógica en controllers / base en algoritmos.** Revisar que los controllers solo carguen
   y deleguen, y que `algorithm/` no importe nada de `repository`.
5. **Credenciales.** `git log -p --all | grep -inE "password\s*=|NEO4J_PASSWORD=[^$]|secret|token"`
   (ignorar `.env.example` con `cambiame` y `${NEO4J_PASSWORD:}`). Cualquier valor real es
   bloqueante, aunque ya se haya borrado.
6. **README.** Dominio, nodo, arista y unidad del costo declarados; integrantes con
   **nombre completo y usuario de GitHub** (ninguna fila `_(completar)_`); sección "Uso de IA"
   con lo de cada persona; tabla de endpoints sin estados `501`.
7. **Participación.** `git shortlog -sne --all`: tienen que aparecer **todos** los
   integrantes con commits propios. Si alguien no aparece, es bloqueante.
8. **Tag.** `git tag -l entrega-final` y `git ls-remote --tags origin entrega-final`.
9. **Visibilidad.** Si el repo es privado (`gh repo view --json visibility`), el docente
   necesita acceso de lectura.

## Reporte

Tabla con cada chequeo, estado (✅ / ❌ / ⚠️) y qué falta. Al final, la lista de bloqueantes
ordenada por urgencia, con el responsable (Persona 1, 2 o 3) de cada uno. No pongas el tag
vos: lo pone el grupo cuando todo está en verde.

# TP-Progra3 — instrucciones para asistentes de IA

TPO de Programación III (UADE, 2° cuatrimestre 2026): Spring Boot + Neo4j sobre una red
logística de depósitos y centros de distribución, con distancias en km.

Antes de tocar código, leé `SKILLS-Y-ALINEAMIENTOS/README.md` y seguí esa carpeta:

- Valores esperados de todos los algoritmos: `SKILLS-Y-ALINEAMIENTOS/01-DOMINIO.md`.
- Reglas (sin librerías ni `sort` de Java, nada de lógica en controllers, el algoritmo no
  consulta la base, errores manejados): `SKILLS-Y-ALINEAMIENTOS/02-ARQUITECTURA-Y-REGLAS.md`.
- Reparto por persona: `SKILLS-Y-ALINEAMIENTOS/03-REPARTO-Y-TAREAS.md`.
- Skills: `SKILLS-Y-ALINEAMIENTOS/skills/` (implementar un algoritmo, verificar la entrega,
  estudiar para el parcial).

La materia permite usar IA, pero el Segundo Parcial se codifica en vivo sin asistentes:
explicá cada decisión y ofrecé que la persona escriba primero (ver `07-USO-DE-IA.md`).
Nunca cambies un valor esperado de un test para que pase.

Comandos: `./mvnw test` (no necesita Neo4j), `./mvnw spring-boot:run` (necesita las
variables `NEO4J_URI`, `NEO4J_USER`, `NEO4J_PASSWORD`).

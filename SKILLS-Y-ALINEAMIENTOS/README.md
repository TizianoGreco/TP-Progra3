# Skills y alineamientos del TPO

Esta carpeta es el **manual de trabajo del grupo**. Junta en un solo lugar las reglas
del proyecto, el reparto, los valores que tienen que dar los algoritmos y cómo se entrega.
Sirve para dos lectores:

- **Las personas del grupo**, para que los tres trabajemos igual y nadie pierda puntos
  por algo que ya estaba decidido.
- **El asistente de IA que use cada uno** (Claude Code u otro). Las skills de `skills/`
  están escritas para que el asistente las siga al pie de la letra. El `CLAUDE.md` de la
  raíz del repo le indica que lea esta carpeta antes de tocar código.

Si algo de acá contradice a [CONSIGNA.md](../CONSIGNA.md), manda la consigna. Si contradice
al código, manda lo que dice esta carpeta y hay que corregir el código (o avisar al grupo
si la regla está mal).

## Índice

| Archivo | Qué contiene | Quién lo necesita |
|---|---|---|
| [01-DOMINIO.md](01-DOMINIO.md) | El grafo del grupo, los paquetes y **todos los valores esperados** | todos |
| [02-ARQUITECTURA-Y-REGLAS.md](02-ARQUITECTURA-Y-REGLAS.md) | Cómo está armado el proyecto, qué se toca, qué está prohibido | todos, antes de codear |
| [03-REPARTO-Y-TAREAS.md](03-REPARTO-Y-TAREAS.md) | Qué le toca a cada persona, archivo por archivo, con su checklist | cada persona, su sección |
| [04-TESTS.md](04-TESTS.md) | Cómo se escriben los tests y qué casos tiene que cubrir cada algoritmo | todos |
| [05-DOCUMENTACION.md](05-DOCUMENTACION.md) | Formato de la ficha de `docs/ENDPOINTS.md`, del informe de complejidades y del README | todos |
| [06-GIT-Y-ENTREGA.md](06-GIT-Y-ENTREGA.md) | Ramas, commits, PR, tag `entrega-final` y checklist de admisibilidad | todos |
| [07-USO-DE-IA.md](07-USO-DE-IA.md) | Qué permite la materia, cómo declararlo y cómo usar IA sin perder el parcial | todos |
| [08-PREPARACION-PARCIAL.md](08-PREPARACION-PARCIAL.md) | Ficha de estudio de los 12 algoritmos para el Segundo Parcial | todos, **todos los algoritmos** |
| [skills/](skills/) | Skills para el asistente de IA: implementar, verificar la entrega, estudiar | el asistente |

## Orden de lectura

1. `01-DOMINIO.md` y `02-ARQUITECTURA-Y-REGLAS.md`: diez minutos, una sola vez.
2. Tu sección de `03-REPARTO-Y-TAREAS.md`.
3. `04-TESTS.md` y `05-DOCUMENTACION.md` cuando el algoritmo ya funcione.
4. `06-GIT-Y-ENTREGA.md` antes del primer commit.
5. `08-PREPARACION-PARCIAL.md` antes del parcial (11/11 y 18/11).

## Estado al 27/10/2026

| Persona | Bloque | Estado |
|---|---|---|
| 1 — Tiziano Greco | Grafos y configuración | hecho y mergeado en `main` (17 tests) |
| 2 — _(completar)_ | Ordenamiento y expansión mínima | stubs listos, falta implementar |
| 3 — _(completar)_ | Optimización y búsqueda | stubs listos, falta implementar |

**La entrega final es el 28/10** (tag `entrega-final`). Ver
[06-GIT-Y-ENTREGA.md](06-GIT-Y-ENTREGA.md).

## Cómo usar las skills con Claude Code

Las skills viven en `skills/<nombre>/SKILL.md`. Hay dos formas de usarlas:

- **Sin instalar nada:** el `CLAUDE.md` de la raíz ya le dice a Claude Code que lea esta
  carpeta. Alcanza con pedirle, por ejemplo, *"implementá Prim siguiendo la skill
  tpo-implementar-algoritmo"*.
- **Como skills del proyecto** (aparecen como `/tpo-implementar-algoritmo`, etc.): copiarlas
  una vez a `.claude/skills/` desde la raíz del repo.

```bash
mkdir -p .claude/skills && cp -R SKILLS-Y-ALINEAMIENTOS/skills/* .claude/skills/
```

`.claude/` no se commitea: cada uno la arma en su máquina con el comando de arriba.

# 06 · Git, PR y entrega

## Repositorio

- `github.com/TizianoGreco/TP-Progra3`, rama principal `main`.
- Cada integrante tiene que ser **colaborador** del repo (Settings → Collaborators). Si no
  te llegó la invitación, pedila al grupo.

## Flujo de trabajo

```
main ──┬──────────────────────────── merge del PR ──► main
       └── persona-2/prim ── commits ── push ── PR ──┘
```

1. Actualizar `main`: `git checkout main && git pull`.
2. Crear una rama por algoritmo o por bloque: `persona-2/ordenamiento`, `persona-3/floyd`.
3. Implementar + tests + ficha. Commitear a medida que se cierra cada parte.
4. `./mvnw test` en verde.
5. `git push -u origin <rama>` y abrir el PR contra `main`.
6. Merge con **"Create a merge commit"** (no squash): así el historial conserva los commits
   de cada persona, que es la evidencia de participación.

Nunca: `push --force` a `main`, reescribir historia ya pusheada, commitear directo a
`main` código de otro.

## Commits

**Cada uno commitea su propio código desde su usuario de GitHub.** El historial que
muestre participación de todos es requisito de admisibilidad: si una persona sube el
código de otra, para la materia esa otra persona no participó.

Antes del primer commit, verificar la identidad:

```bash
git config user.name    # tu nombre
git config user.email   # el mail asociado a tu cuenta de GitHub
```

Formato del mensaje (inglés, imperativo, minúscula, sin punto final, ≤ 72 caracteres):

```
<tipo>(<alcance>): <qué cambia>

<por qué, si no es obvio>
```

| Tipo | Cuándo |
|---|---|
| `feat` | algoritmo nuevo o funcionalidad nueva |
| `fix` | corrige un resultado incorrecto |
| `test` | agrega o corrige tests sin tocar lógica |
| `docs` | README, ENDPOINTS, COMPLEJIDADES, esta carpeta |
| `refactor` | cambio interno sin cambiar resultados |
| `chore` | build, configuración, dependencias |

Ejemplos del repo:

```
feat(algorithm): implement dijkstra, ucs, floyd-warshall, prim and kruskal
feat(seed): model the network as depots and distribution centers in km
docs: document the work split and what is pending per persona
```

Un commit = un cambio lógico. Mejor tres commits chicos (algoritmo, tests, ficha) que uno
enorme.

## Credenciales

- La contraseña de Neo4j va en `.env` (ignorado por git), nunca en `application.properties`
  ni en el código.
- Antes de commitear: `git diff --staged` y buscar `password`, `NEO4J_PASSWORD=`, tokens.
- Una credencial en **cualquier** commit del historial devuelve la entrega sin corregir,
  aunque después se borre.

## Entrega final — 28/10/2026 (hito 12, obligatorio)

El tag apunta al commit que se corrige; lo posterior no se evalúa.

```bash
git checkout main && git pull
./mvnw test                       # tiene que pasar
git tag -a entrega-final -m "Entrega final TPO Programacion III"
git push origin entrega-final
```

Después, publicar el link del repo en Teams. Si hay que corregir algo después de taggear,
se mueve el tag (`git tag -d entrega-final`, `git push origin :refs/tags/entrega-final` y
se vuelve a crear), siempre antes de la fecha.

## Checklist de admisibilidad (consigna 4.2)

Si falla uno, la entrega se devuelve **sin corregir**.

- [ ] Al menos 9 de los 12 hitos, incluidos el 3, 4, 5 y 12.
- [ ] Compila y `./mvnw test` pasa sin errores.
- [ ] Ninguna credencial en el código, en `application.properties` ni en el historial.
- [ ] El README declara dominio, qué es un nodo, qué es una arista y la unidad del costo.
- [ ] Cada algoritmo declarado tiene su ficha en `docs/ENDPOINTS.md`.
- [ ] El historial de commits muestra participación de **todos** los integrantes.

Más lo de formato (sección 6 de la consigna):

- [ ] README con integrantes: **nombre completo y usuario de GitHub**.
- [ ] Sección "Uso de IA" en el README.
- [ ] Tag `entrega-final` pusheado.
- [ ] Si el repo es privado, el docente tiene acceso de lectura.

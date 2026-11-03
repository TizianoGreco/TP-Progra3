"""Genera la demo estatica que se publica en Vercel.

Vercel no corre Java ni Neo4j, asi que la demo es la misma pagina mas las respuestas reales de la
API guardadas en datos/: todas las consultas que la pagina puede hacer (cada par de nodos, cada
capacidad, cada tope de km). Los resultados los calcula el backend Java; este script solo los copia.

Uso, con la app levantada contra Neo4j:
    python3 scripts/generar_demo.py [http://localhost:8080] [demo]
    vercel deploy --prod demo
"""
import json
import os
import shutil
import sys
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor

BASE = sys.argv[1] if len(sys.argv) > 1 else 'http://localhost:8080'
SALIDA = sys.argv[2] if len(sys.argv) > 2 else 'demo'
CAPACIDAD_MAXIMA = 40   # la pagina deja elegir de 1 a 40 kg
TOPE_MAXIMO = 200       # mas que la suma de todas las rutas: arriba de esto la poda no corta nada


def pedir(ruta):
    try:
        with urllib.request.urlopen(BASE + ruta, timeout=30) as r:
            return {'status': r.status, 'body': json.load(r)}
    except urllib.error.HTTPError as e:
        return {'status': e.code, 'body': json.load(e)}


def guardar(nombre, dato):
    destino = os.path.join(SALIDA, 'datos', nombre + '.json')
    os.makedirs(os.path.dirname(destino), exist_ok=True)
    with open(destino, 'w', encoding='utf-8') as f:
        json.dump(dato, f, ensure_ascii=False, separators=(',', ':'))


def main():
    os.makedirs(SALIDA, exist_ok=True)
    for nombre in os.listdir(SALIDA):  # se limpia todo menos .vercel, que guarda el proyecto vinculado
        if nombre != '.vercel':
            ruta = os.path.join(SALIDA, nombre)
            shutil.rmtree(ruta) if os.path.isdir(ruta) else os.remove(ruta)
    shutil.copy('src/main/resources/static/index.html', os.path.join(SALIDA, 'index.html'))

    red = pedir('/api/grafo/red')
    guardar('red', red)
    ids = [n['id'] for n in red['body']['nodos']]
    guardar('paquetes', pedir('/api/seleccion/mergesort?criterio=peso'))
    guardar('kruskal', pedir('/api/grafo/kruskal'))

    tareas = []
    for o in ids:
        for alg in ('bfs', 'dfs', 'prim'):
            tareas.append((f'{alg}/{o}', f'/api/grafo/{alg}?origen={o}'))
        for d in ids:
            for alg in ('dijkstra', 'ucs'):
                tareas.append((f'{alg}/{o}-{d}', f'/api/grafo/{alg}?origen={o}&destino={d}'))
    for c in range(1, CAPACIDAD_MAXIMA + 1):
        for alg in ('greedy', 'dinamica'):
            tareas.append((f'{alg}/{c}', f'/api/seleccion/{alg}?capacidad={c}'))

    with ThreadPoolExecutor(8) as pool:
        for nombre, dato in zip([t[0] for t in tareas], pool.map(lambda t: pedir(t[1]), tareas)):
            guardar(nombre, dato)

        # Backtracking: una respuesta por cada tope entero, porque la poda cambia con el tope.
        def rutas(par):
            o, d = par
            return f'rutas/{o}-{d}', [pedir(f'/api/grafo/rutas?origen={o}&destino={d}&costoMaximo={t}&saltosMaximos=10')
                                       for t in range(TOPE_MAXIMO + 1)]
        for nombre, lista in pool.map(rutas, [(o, d) for o in ids for d in ids]):
            guardar(nombre, lista)

    print(f'Demo generada en {SALIDA}/ con {len(tareas) + len(ids) ** 2 + 3} archivos de datos.')


if __name__ == '__main__':
    main()

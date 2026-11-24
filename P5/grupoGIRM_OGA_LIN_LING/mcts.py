"""Monte Carlo Tree Search para ajedrez. Grupo YY.

Es la segunda parte de la guía. Cada función con
    raise NotImplementedError
es un hueco. Cuando rellenéis el del paso M1, M2…, comprobadlo con
    python comprobar.py m1
(o, en VS Code, "Comprobar paso…" en Ejecutar y depurar).
"""
import math
import random
import time

import chess

from ajedrez import material
from .minimax import MATE, evaluar

CENTRO = [chess.D4, chess.E4, chess.D5, chess.E5]

class Nodo:
    def __init__(self, tablero, padre=None, jugada=None):
        self.padre = padre
        self.jugada = jugada                          # la jugada que lleva del padre aquí
        self.hijos = []                               # los nodos ya creados debajo
        self.sin_probar = list(tablero.legal_moves)   # las jugadas que todavía no tienen hijo
        random.shuffle(self.sin_probar)
        self.visitas = 0                              # N: cuántas simulaciones han pasado por aquí
        self.valor = 0.0                              # W: la suma de sus recompensas
        self.color = not tablero.turn                 # quien hizo `jugada` (os hará falta en el reto "El rival también elige")

# ---------------------------------------------------------------- paso M1
def recompensa(tablero, color):
    if tablero.is_checkmate():
        return 0.0 if tablero.turn == color else 1.0
    if tablero.is_stalemate() or tablero.is_insufficient_material():
        return 0.5
    return 1 / (1 + math.exp(-material(tablero, color) / 3))


# ---------------------------------------------------------------- paso M2
def ucb1(hijo, c):
    explotar = hijo.valor / hijo.visitas
    explorar = c * math.sqrt(math.log(hijo.padre.visitas) / hijo.visitas)
    return explotar + explorar


def seleccionar(nodo, c):
    return max(nodo.hijos, key=lambda h: ucb1(h, c))


# ---------------------------------------------------------------- paso M3
def expandir(nodo, tablero):
    jugada = nodo.sin_probar.pop()
    tablero.push(jugada)
    hijo = Nodo(tablero, nodo, jugada)
    nodo.hijos.append(hijo)
    return hijo


# ---------------------------------------------------------------- paso M4
def simular(tablero, profundidad, color, recompensa=recompensa):
    for _ in range(profundidad):
        jugadas = list(tablero.legal_moves)
        if not jugadas:
            break
        tablero.push(random.choice(jugadas))
    return recompensa(tablero, color)


# ---------------------------------------------------------------- paso M5
def retropropagar(nodo, r):
    while nodo is not None:
        nodo.visitas += 1
        nodo.valor += r
        nodo = nodo.padre


# ---------------------------------------------------------------- paso M6
class MCTS:
    """El jugador. `c` es la constante de exploración, `profundidad` el corte de la simulación,
    `recompensa` la heurística, e `iteraciones`, si no es None, sustituye al reloj (para las pruebas)."""

    def __init__(self, nombre="grupoGIRM_OGA_LIN_LING", c=1.4, profundidad=10, iteraciones=None, recompensa=recompensa):
        self.nombre = nombre
        self.c = c
        self.profundidad = profundidad
        self.iteraciones = iteraciones
        self.recompensa = recompensa    # la heurística: otra función con la misma forma, para probar la vuestra
        self.estadisticas = []      # (jugada, visitas, valor medio) de la raíz: la ventana las pinta

    # Las cuatro fases, como métodos. Una versión nueva hereda de MCTS y redefine sólo la que cambia:
    #     class MCTSCapturas(MCTS):
    #         def simular(self, tablero): ...
    def seleccionar(self, nodo):
        return seleccionar(nodo, self.c)

    def expandir(self, nodo, tablero):
        return expandir(nodo, tablero)

    def simular(self, tablero):
        return simular(tablero, self.profundidad, self.yo, self.recompensa)

    def retropropagar(self, nodo, r):
        retropropagar(nodo, r)

    def elegir(self, tablero, tiempo):
        fin = time.perf_counter() + tiempo * 0.9    # un margen para no pasarse
        self.yo = tablero.turn                      # el que decide: el árbol entero cuenta desde él
        raiz = Nodo(tablero)
        n = 0
        # el bucle de elegir, sin el raise (allí va con dos sangrías más)
        while True:
            t = tablero.copy(stack=False)
            nodo = raiz
            while not nodo.sin_probar and nodo.hijos:      # 1. selección
                nodo = self.seleccionar(nodo)
                t.push(nodo.jugada)
            if nodo.sin_probar:                              # 2. expansión
                nodo = self.expandir(nodo, t)
            r = self.simular(t)                              # 3. simulación
            self.retropropagar(nodo, r)                      # 4. retropropagación
            n += 1
            if (self.iteraciones and n >= self.iteraciones) or \
            (not self.iteraciones and time.perf_counter() > fin):
                break
        self.estadisticas = sorted(((h.jugada, h.visitas, h.valor / h.visitas) for h in raiz.hijos),
                                   key=lambda e: -e[1])
        self.iteraciones_hechas = n
        return max(raiz.hijos, key=lambda h: h.visitas).jugada   # el más visitado


def centro(tablero, color):
    """Piezas propias en el centro menos las del rival."""
    total = 0
    for casilla in CENTRO:
        pieza = tablero.piece_at(casilla)
        if pieza:
            total += 1 if pieza.color == color else -1
    return total


def evaluar_centro(tablero, color):
    v = evaluar(tablero, color)             # el material, o ±MATE, o 0 en tablas
    if abs(v) == MATE:
        return v
    return v + 0.1 * centro(tablero, color)


# =============================================================================
# RETOS (Práctica 5 - Cuarta parte)
# =============================================================================

class MCTSRival(MCTS):
    """Reto 1: El rival también elige (+2,5 pts · Violeta · MCTS).
    
    En los niveles del árbol en los que mueve el rival (nodo.color == self.yo),
    la selección UCB1 evalúa (1 - media) en vez de la media, asumiendo que el
    rival intentará perjudicarnos en vez de colaborar con nosotros.
    """

    def __init__(self, nombre="grupoGIRM_OGA_LIN_LING-rival", **kwargs):
        super().__init__(nombre=nombre, **kwargs)

    def seleccionar(self, nodo):
        elige_rival = (nodo.color == self.yo)
        log_n = math.log(nodo.visitas)
        c = self.c

        def puntos(hijo):
            media = hijo.valor / hijo.visitas
            if elige_rival:
                media = 1.0 - media
            explorar = c * math.sqrt(log_n / hijo.visitas)
            return media + explorar

        return max(nodo.hijos, key=puntos)


class MCTSCapturas(MCTS):
    """Reto 3: Simulaciones que capturan (+2,5 pts · Violeta · MCTS).
    
    Durante la simulación (rollout), si hay capturas legales disponibles,
    elige al azar entre las capturas en vez de jugar ciegamente. Evita que las
    simulaciones ignoren piezas colgadas y damas regaladas.
    """

    def __init__(self, nombre="grupoGIRM_OGA_LIN_LING-capturas", **kwargs):
        super().__init__(nombre=nombre, **kwargs)

    def simular(self, tablero):
        for _ in range(self.profundidad):
            jugadas = list(tablero.legal_moves)
            if not jugadas:
                break
            capturas = [j for j in jugadas if tablero.is_capture(j)]
            tablero.push(random.choice(capturas if capturas else jugadas))
        return self.recompensa(tablero, self.yo)


class MCTSRivalCapturas(MCTSRival):
    """Combinación de Reto 1 + Reto 3 (MCTS Táctico Completo).
    
    Hereda la selección adversarial de MCTSRival (1 - media para el rival)
    y añade la simulación informada de capturas de MCTSCapturas.
    """

    def __init__(self, nombre="grupoGIRM_OGA_LIN_LING-rival-capturas", **kwargs):
        super().__init__(nombre=nombre, **kwargs)

    def simular(self, tablero):
        for _ in range(self.profundidad):
            jugadas = list(tablero.legal_moves)
            if not jugadas:
                break
            capturas = [j for j in jugadas if tablero.is_capture(j)]
            tablero.push(random.choice(capturas if capturas else jugadas))
        return self.recompensa(tablero, self.yo)


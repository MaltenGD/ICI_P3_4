"""Minimax con poda alfa-beta para ajedrez. Grupo YY.

Es la primera parte de la guía. Cada función con
    raise NotImplementedError
es un hueco. Cuando rellenéis el del paso A1, A2 o A3, comprobadlo con
    python comprobar.py a1
(o, en VS Code, "Comprobar paso…" en Ejecutar y depurar).
"""
import math
import random
import time

import chess

from ajedrez import VALOR, material

MATE = 1000     # más que todo el material junto: un mate vale más que cualquier ventaja


# ---------------------------------------------------------------- paso A1
def evaluar(tablero, color):
    if tablero.is_checkmate():
        return -MATE if tablero.turn == color else MATE
    if tablero.is_stalemate() or tablero.is_insufficient_material():
        return 0
    return material(tablero, color)


# ---------------------------------------------------------------- paso A2
def minimax(tablero, profundidad, color, evaluar=evaluar):
    if profundidad == 0 or tablero.is_game_over():
        return evaluar(tablero, color)
    valores = []
    for jugada in tablero.legal_moves:
        tablero.push(jugada)
        valores.append(minimax(tablero, profundidad - 1, color, evaluar))
        tablero.pop()
    return max(valores) if tablero.turn == color else min(valores)


# ---------------------------------------------------------------- paso A3
def alfabeta(tablero, profundidad, color, alfa=-math.inf, beta=math.inf, evaluar=evaluar):
    if profundidad == 0 or tablero.is_game_over():
        return evaluar(tablero, color)
    if tablero.turn == color:                        # MAX
        mejor = -math.inf
        for jugada in ordenar(tablero, list(tablero.legal_moves)):
            tablero.push(jugada)
            mejor = max(mejor, alfabeta(tablero, profundidad - 1, color, alfa, beta, evaluar))
            tablero.pop()
            alfa = max(alfa, mejor)
            if alfa >= beta:
                break
        return mejor
    mejor = math.inf                                   # MIN
    for jugada in ordenar(tablero, list(tablero.legal_moves)):
        tablero.push(jugada)
        mejor = min(mejor, alfabeta(tablero, profundidad - 1, color, alfa, beta, evaluar))
        tablero.pop()
        beta = min(beta, mejor)
        if alfa >= beta:
            break
    return mejor


# ---------------------------------------------------------------- hecho: el orden y el jugador
def ordenar(tablero, jugadas):
    """Las capturas primero, y entre ellas las de la pieza más valiosa: así la poda corta antes."""
    def valor_capturado(j):
        pieza = tablero.piece_at(j.to_square)
        return VALOR[pieza.piece_type] if pieza else (1 if tablero.is_en_passant(j) else 0)
    return sorted(jugadas, key=valor_capturado, reverse=True)


class Minimax:
    """El jugador. `profundidad` son las medias jugadas que mira, contando la suya; con
    `poda=False` usa minimax a secas (para comparar lo que tardan); `evaluar` es la heurística."""

    def __init__(self, nombre="grupoGIRM_OGA_LIN_LING-ab", profundidad=2, poda=True, evaluar=evaluar):
        self.nombre = nombre
        self.profundidad = profundidad
        self.poda = poda
        self.evaluar = evaluar      # la heurística: otra función con la misma forma, para probar la vuestra

    def valor(self, tablero, color):
        """El valor de la posición después de una jugada: lo que diga la búsqueda que toque."""
        if self.poda:
            return alfabeta(tablero, self.profundidad - 1, color, evaluar=self.evaluar)
        return minimax(tablero, self.profundidad - 1, color, evaluar=self.evaluar)

    def elegir(self, tablero, tiempo):
        legales = list(tablero.legal_moves)
        if len(legales) == 1:
            return legales[0]

        color = tablero.turn
        mejor, mejores = -math.inf, []
        for jugada in ordenar(tablero, legales):
            tablero.push(jugada)
            v = self.valor(tablero, color)
            tablero.pop()
            if v > mejor:
                mejor, mejores = v, [jugada]
            elif v == mejor:
                mejores.append(jugada)
        self.mejor_valor = mejor
        return random.choice(mejores)


class Minimax_Reto1:
    """El jugador. `profundidad` son las medias jugadas que mira, contando la suya; con
    `poda=False` usa minimax a secas (para comparar lo que tardan); `evaluar` es la heurística."""

    def __init__(self, nombre="grupoGIRM_OGA_LIN_LING-ab", profundidad=2, poda=True, evaluar=evaluar):
        self.nombre = nombre
        self.profundidad = profundidad
        self.poda = poda
        self.evaluar = evaluar      # la heurística: otra función con la misma forma, para probar la vuestra

    def valor(self, tablero, color):
        """El valor de la posición después de una jugada: lo que diga la búsqueda que toque."""
        if self.poda:
            return alfabeta(tablero, self.profundidad - 1, color, evaluar=self.evaluar)
        return minimax(tablero, self.profundidad - 1, color, evaluar=self.evaluar)

    def elegir(self, tablero, tiempo):
        """Reto 2: Gestionar el tiempo (+1,75 pts · Azul · MCTS)
                    
        Una sola jugada legal : devolvedla sin pensar.
        """
        legales = list(tablero.legal_moves)
        if len(legales) == 1:
            return legales[0]
        """===================================="""

        color = tablero.turn

        #gestión del tiempo: con pocas piezas, más profundidad
        fin = time.perf_counter() + tiempo * 0.6 # margen para la gestión del tiempo
        prof_original = self.profundidad
        mejor_jugada = random.choice(legales) # si no da tiempo a mirar, devolvemos una legal al azar
        self.mejor_valor = None

        try:
            for prof in range(1, prof_original + 4):    # tope de profundidad
                self.profundidad = prof
                mejor, mejores, completa = -math.inf, [], True
                for jugada in ordenar(tablero, legales):
                    if time.perf_counter() > fin:
                        completa = False
                        break
                    tablero.push(jugada)
                    v = self.valor(tablero, color)
                    tablero.pop()
                    if v > mejor:
                        mejor, mejores = v, [jugada]
                    elif v == mejor:
                        mejores.append(jugada)
                if not completa:
                    break                               # descarto la profundidad a medias
                mejor_jugada = random.choice(mejores)
                self.mejor_valor = mejor
                if abs(mejor) >= MATE:
                    break                               # mate visto: no hace falta más
            return mejor_jugada
        finally:
            self.profundidad = prof_original

#Reto 5
class MinimaxIterativo(Minimax):
    """Busca a profundidad 1, 2, 3... mientras quede tiempo y juega lo que dijo la última búsqueda entera."""

    def __init__(self, nombre="grupoGIRM_OGA_LIN_LING-iterativo", **kwargs):
        super().__init__(nombre=nombre, **kwargs)
        self.profundidad_alcanzada = 0

    def elegir(self, tablero, tiempo):
        legales = list(tablero.legal_moves)
        if len(legales) == 1:
            return legales[0]

        color = tablero.turn
        
        # Margen del 92%
        fin = time.perf_counter() + (tiempo * 0.92) 
        
        orden = ordenar(tablero, legales)
        prof_original = self.profundidad
        profundidad = 1
        
        mejor_jugada_global = random.choice(legales) 
        
        try:
            while True:
                t0 = time.perf_counter()
                self.profundidad = profundidad
                
                resultados = [] 
                completa = True
                
                for jugada in orden:
                    if time.perf_counter() > fin:
                        completa = False
                        break 
                        
                    tablero.push(jugada)
                    v = self.valor(tablero, color)
                    tablero.pop()
                    
                    resultados.append((v, jugada))
                
                if not completa:
                    break 
                
                mejor = max(r[0] for r in resultados)
                mejores = [r[1] for r in resultados if r[0] == mejor]
                
                mejor_jugada_global = random.choice(mejores)
                self.mejor_valor = mejor
                self.profundidad_alcanzada = profundidad
                
                duracion = time.perf_counter() - t0
                
                if abs(mejor) >= MATE:
                    break
                    
                # Factor a 6 para ser más agresivo con el tiempo
                if (fin - time.perf_counter()) < (6 * duracion):
                    break
                    
                # Ordenamos TODAS las jugadas por su valor (las mejores primero)
                resultados.sort(key=lambda x: x[0], reverse=True)
                orden = [r[1] for r in resultados]
                
                profundidad += 1
                
            return mejor_jugada_global
        finally:
            self.profundidad = prof_original
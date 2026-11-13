
package es.ucm.fdi.ici.c2627.practica3.grupoYY.mspacman.actions;

import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Búsqueda hacia delante sobre el laberinto.
 * Evalúa varios movimientos futuros sin modificar el estado del juego.
 */
public final class LookaheadPacman {

    private static final int PROFUNDIDAD = 5;
    private static final double DESCUENTO = 0.65;

    private LookaheadPacman() {
    }

    /**
     * Elige un movimiento evaluando varios pasos futuros.
     *
     * @param game      estado actual
     * @param objetivos nodos objetivo; puede ser null o estar vacío
     * @param huir      true para priorizar la distancia a fantasmas peligrosos
     */
    public static MOVE elegirMovimiento(
            Game game, int[] objetivos, boolean huir) {

        int actual = game.getPacmanCurrentNodeIndex();
        if (actual < 0) {
            return MOVE.NEUTRAL;
        }

        MOVE ultimo = game.getPacmanLastMoveMade();
        MOVE[] opciones = game.getPossibleMoves(actual, ultimo);

        if (opciones == null || opciones.length == 0) {
            return MOVE.NEUTRAL;
        }

        if (opciones.length == 1) {
            return opciones[0];
        }

        MOVE mejorMovimiento = opciones[0];
        double mejorCoste = Double.POSITIVE_INFINITY;

        for (MOVE movimiento : opciones) {
            int siguiente = game.getNeighbour(actual, movimiento);
            if (siguiente < 0) {
                continue;
            }

            double coste = buscar(
                    game, siguiente, movimiento, PROFUNDIDAD - 1,
                    objetivos, huir);

            if (coste < mejorCoste) {
                mejorCoste = coste;
                mejorMovimiento = movimiento;
            }
        }

        return mejorMovimiento;
    }

    private static double buscar(
            Game game,
            int nodo,
            MOVE ultimo,
            int profundidad,
            int[] objetivos,
            boolean huir) {

        double costeLocal = evaluarNodo(game, nodo, objetivos, huir);

        if (profundidad <= 0) {
            return costeLocal;
        }

        MOVE[] opciones = game.getPossibleMoves(nodo, ultimo);

        if (opciones == null || opciones.length == 0) {
            return costeLocal + 100.0;
        }

        double mejorFuturo = Double.POSITIVE_INFINITY;
        boolean encontrado = false;

        for (MOVE movimiento : opciones) {
            int siguiente = game.getNeighbour(nodo, movimiento);
            if (siguiente < 0) {
                continue;
            }

            double coste = buscar(
                    game, siguiente, movimiento, profundidad - 1,
                    objetivos, huir);

            if (coste < mejorFuturo) {
                mejorFuturo = coste;
            }

            encontrado = true;
        }

        if (!encontrado) {
            return costeLocal + 100.0;
        }

        return costeLocal + DESCUENTO * mejorFuturo;
    }

    private static double evaluarNodo(
            Game game, int nodo, int[] objetivos, boolean huir) {

        int grado = 0;
        for (MOVE movimiento : MOVE.values()) {
            if (movimiento == MOVE.NEUTRAL) {
                continue;
            }
            if (game.getNeighbour(nodo, movimiento) >= 0) {
                grado++;
            }
        }

        int distanciaFantasma = Integer.MAX_VALUE;

        for (GHOST fantasma : GHOST.values()) {
            if (game.getGhostLairTime(fantasma) > 0
                    || game.isGhostEdible(fantasma)) {
                continue;
            }

            int nodoFantasma = game.getGhostCurrentNodeIndex(fantasma);
            if (nodoFantasma < 0) {
                continue;
            }

            int distancia = game.getShortestPathDistance(nodo, nodoFantasma);
            if (distancia >= 0 && distancia < distanciaFantasma) {
                distanciaFantasma = distancia;
            }
        }

        double peligro = 0.0;

        if (distanciaFantasma != Integer.MAX_VALUE) {
            if (huir) {
                // Cuanto más cerca esté un fantasma, mayor es el coste.
                peligro = 100.0 / (distanciaFantasma + 1.0);

                if (distanciaFantasma <= 2) {
                    peligro += 100.0;
                } else if (distanciaFantasma <= 4) {
                    peligro += 25.0;
                }
            } else {
                if (distanciaFantasma <= 1) {
                    peligro = 1000.0;
                } else if (distanciaFantasma == 2) {
                    peligro = 150.0;
                } else if (distanciaFantasma == 3) {
                    peligro = 35.0;
                } else if (distanciaFantasma == 4) {
                    peligro = 10.0;
                } else if (distanciaFantasma == 5) {
                    peligro = 3.0;
                }
            }
        }

        double costeObjetivo = 0.0;

        if (!huir && objetivos != null && objetivos.length > 0) {
            int distanciaObjetivo = Integer.MAX_VALUE;

            for (int objetivo : objetivos) {
                int distancia = game.getShortestPathDistance(nodo, objetivo);
                if (distancia >= 0 && distancia < distanciaObjetivo) {
                    distanciaObjetivo = distancia;
                }
            }

            if (distanciaObjetivo == Integer.MAX_VALUE) {
                costeObjetivo = 10000.0;
            } else {
                costeObjetivo = distanciaObjetivo;
            }
        }

        double callejon = 0.0;

        // Evita callejones cuando hay un fantasma peligroso relativamente cerca.
        if (grado <= 1 && distanciaFantasma <= 6) {
            callejon = huir ? 100.0 : 80.0;
        } else if (grado == 2 && distanciaFantasma <= 3) {
            callejon = huir ? 35.0 : 25.0;
        }

        return costeObjetivo + peligro + callejon;
    }
}
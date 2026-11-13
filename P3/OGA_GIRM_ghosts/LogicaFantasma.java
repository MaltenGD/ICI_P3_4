package es.ucm.fdi.ici.c2627.practica3.grupoYY.ghosts.actions;

import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Logica de GhostsV0Me (practica 2) sacada a metodos estaticos SIN estado.
 * Las acciones son envoltorios finos de estos metodos y el GhostsInput usa
 * los mismos calculos para generar los hechos, de modo que regla y accion
 * siempre hablan de lo mismo.
 */
public final class LogicaFantasma {

    public static final double DISTANCIA_MINIMA_FANTASMAS = 5.0;
    public static final int PASOS_PREDICCION = 4;
    public static final int LEJOS = 1000;          // "no hay": ningun fantasma, ninguna pildora

    private LogicaFantasma() { }

    // ------------------------------------------------------------------
    //  Utilidades
    // ------------------------------------------------------------------

    /** Primer movimiento de un camino; tolera que el camino incluya o no el nodo de origen. */
    public static MOVE primerMovimiento(Game game, int origen, int[] camino) {
        if (camino == null || camino.length == 0) return MOVE.NEUTRAL;
        int siguiente;
        if (camino[0] == origen) {
            if (camino.length < 2) return MOVE.NEUTRAL;
            siguiente = camino[1];
        } else {
            siguiente = camino[0];
        }
        MOVE m = game.getMoveToMakeToReachDirectNeighbour(origen, siguiente);
        return m == null ? MOVE.NEUTRAL : m;
    }

    /** Fuera de la carcel y con nodo valido. */
    public static boolean activo(Game game, GHOST g) {
        return game.getGhostLairTime(g) <= 0 && game.getGhostCurrentNodeIndex(g) != -1;
    }

    // ------------------------------------------------------------------
    //  PERSEGUIR (BLINKY, SUE y comportamiento por defecto)
    // ------------------------------------------------------------------
    public static MOVE perseguir(Game game, GHOST f) {
        int nodo = game.getGhostCurrentNodeIndex(f);
        int pacman = game.getPacmanCurrentNodeIndex();
        return primerMovimiento(game, nodo,
                game.getShortestPath(nodo, pacman, game.getGhostLastMoveMade(f)));
    }

    // ------------------------------------------------------------------
    //  HUIR (huirConCriterio)
    // ------------------------------------------------------------------
    public static MOVE huir(Game game, GHOST f) {
        int nodo = game.getGhostCurrentNodeIndex(f);
        MOVE ultimo = game.getGhostLastMoveMade(f);
        int pacman = game.getPacmanCurrentNodeIndex();

        MOVE[] opciones = game.getPossibleMoves(nodo, ultimo);
        if (opciones == null || opciones.length == 0) return MOVE.NEUTRAL;

        MOVE mejor = opciones[0];
        double mejorPuntuacion = Double.NEGATIVE_INFINITY;

        for (MOVE m : opciones) {
            int vecino = game.getNeighbour(nodo, m);
            if (vecino == -1) continue;

            int distancia = game.getShortestPathDistance(vecino, pacman);
            double puntuacion = distancia >= 0 ? distancia * 10.0 : -1000.0;

            if (game.isJunction(vecino)) puntuacion += 8;

            // Evitar las pildoras de poder activas.
            for (int pill : game.getActivePowerPillsIndices()) {
                int d = game.getShortestPathDistance(vecino, pill);
                if (d == 0) puntuacion -= 200;
                else if (d > 0 && d <= 2) puntuacion -= 30;
            }

            if (puntuacion > mejorPuntuacion) {
                mejorPuntuacion = puntuacion;
                mejor = m;
            }
        }
        return mejor;
    }

    // ------------------------------------------------------------------
    //  EMBOSCAR (rol de PINKY)
    //  Port FIEL de GhostsV0Me, incluido su fallo conocido: el bucle usa el nodo
    //  del fantasma ("nodo") y no el de Pac-Man ni "objetivo", asi que apunta al
    //  vecino del fantasma en la direccion de Pac-Man. Se arregla en una version
    //  posterior para poder medir la mejora.
    // ------------------------------------------------------------------
    public static MOVE emboscar(Game game, GHOST f) {
        int nodo = game.getGhostCurrentNodeIndex(f);
        MOVE ultimo = game.getGhostLastMoveMade(f);
        int pacman = game.getPacmanCurrentNodeIndex();

        int objetivo = pacman;
        MOVE direccion = game.getPacmanLastMoveMade();

        for (int i = 0; i < PASOS_PREDICCION; i++) {
            int siguiente = game.getNeighbour(nodo, direccion);
            if (siguiente == -1) break;
            objetivo = siguiente;
            if (game.isJunction(nodo)) break;
        }

        if (objetivo != -1 && objetivo != pacman) {
            int[] camino = game.getShortestPath(nodo, objetivo, ultimo);
            if (camino != null && camino.length >= 2)
                return primerMovimiento(game, nodo, camino);
        }
        return perseguir(game, f);
    }

    // ------------------------------------------------------------------
    //  GUARDAR PPILL (rol de INKY)
    // ------------------------------------------------------------------

    /**
     * La pildora de poder con mayor (distPacman - distFantasma), es decir, donde
     * mas ventaja le saco a Ms. Pac-Man. Devuelve {nodoPildora, ventaja} o null.
     */
    public static int[] mejorPPill(Game game, GHOST f) {
        int nodo = game.getGhostCurrentNodeIndex(f);
        int pacman = game.getPacmanCurrentNodeIndex();

        int mejorPill = -1;
        int mejorVentaja = Integer.MIN_VALUE;
        for (int pill : game.getActivePowerPillsIndices()) {
            int dFantasma = game.getShortestPathDistance(nodo, pill);
            int dPacman = game.getShortestPathDistance(pacman, pill);
            if (dFantasma < 0 || dPacman < 0) continue;
            int ventaja = dPacman - dFantasma;
            if (ventaja > mejorVentaja) {
                mejorVentaja = ventaja;
                mejorPill = pill;
            }
        }
        return mejorPill == -1 ? null : new int[] { mejorPill, mejorVentaja };
    }

    public static MOVE guardarPPill(Game game, GHOST f) {
        int nodo = game.getGhostCurrentNodeIndex(f);
        MOVE ultimo = game.getGhostLastMoveMade(f);

        int[] mejor = mejorPPill(game, f);
        if (mejor != null && mejor[1] >= 0) {
            int[] camino = game.getShortestPath(nodo, mejor[0], ultimo);
            if (camino != null && camino.length >= 2)
                return primerMovimiento(game, nodo, camino);
        }
        return perseguir(game, f);
    }

    // ------------------------------------------------------------------
    //  DISPERSAR
    // ------------------------------------------------------------------

    /** Distancia al otro fantasma activo mas cercano (LEJOS si no hay). */
    public static int distanciaOtro(Game game, GHOST yo) {
        if (!activo(game, yo)) return LEJOS;
        int n1 = game.getGhostCurrentNodeIndex(yo);
        int min = LEJOS;
        for (GHOST otro : GHOST.values()) {
            if (otro == yo || !activo(game, otro)) continue;
            int d = game.getShortestPathDistance(n1, game.getGhostCurrentNodeIndex(otro));
            if (d >= 0) min = Math.min(min, d);
        }
        return min;
    }

    /** necesitanDispersarse de GhostsV0Me: algun par de fantasmas activos a menos de 5. */
    public static boolean necesitanDispersarse(Game game) {
        int activos = 0;
        for (GHOST f : GHOST.values())
            if (activo(game, f)) activos++;
        if (activos < 2) return false;

        for (GHOST f : GHOST.values())
            if (distanciaOtro(game, f) < DISTANCIA_MINIMA_FANTASMAS) return true;
        return false;
    }

    public static MOVE dispersar(Game game, GHOST f) {
        int nodo = game.getGhostCurrentNodeIndex(f);
        MOVE ultimo = game.getGhostLastMoveMade(f);

        MOVE[] opciones = game.getPossibleMoves(nodo, ultimo);
        if (opciones == null || opciones.length == 0) return MOVE.NEUTRAL;

        MOVE mejor = opciones[0];
        double mejorPuntuacion = Double.NEGATIVE_INFINITY;

        for (MOVE m : opciones) {
            int vecino = game.getNeighbour(nodo, m);
            if (vecino == -1) continue;

            double puntuacion = puntuacionDispersion(game, f, vecino);

            // Sesgo propio de cada fantasma (en una version posterior pasara a las reglas)
            switch (f) {
                case BLINKY: if (m == MOVE.LEFT)  puntuacion += 40; break;
                case PINKY:  if (m == MOVE.UP)    puntuacion += 40; break;
                case INKY:   if (m == MOVE.RIGHT) puntuacion += 40; break;
                case SUE:    if (m == MOVE.DOWN)  puntuacion += 40; break;
                default: break;
            }

            if (puntuacion > mejorPuntuacion) {
                mejorPuntuacion = puntuacion;
                mejor = m;
            }
        }
        return mejor;
    }

    private static double puntuacionDispersion(Game game, GHOST fantasma, int candidato) {
        double puntuacion = 0.0;

        // Alejarse de los demas fantasmas.
        for (GHOST otro : GHOST.values()) {
            if (otro == fantasma || !activo(game, otro)) continue;

            int distancia = game.getShortestPathDistance(candidato, game.getGhostCurrentNodeIndex(otro));
            if (distancia < 0) continue;

            puntuacion += Math.min(distancia, 12) * 8.0;
            if (distancia <= 2) puntuacion -= 150;
            else if (distancia <= 4) puntuacion -= 50;
        }

        // Preferir intersecciones.
        if (game.isJunction(candidato)) puntuacion += 20;

        // Evitar las pildoras de poder.
        for (int pill : game.getActivePowerPillsIndices()) {
            int distancia = game.getShortestPathDistance(candidato, pill);
            if (distancia == 0) puntuacion -= 300;
            else if (distancia > 0 && distancia <= 2) puntuacion -= 100;
        }
        return puntuacion;
    }
}

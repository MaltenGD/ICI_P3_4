package es.ucm.fdi.ici.c2627.practica2.GIRM_LC;

import java.util.EnumMap;

import pacman.controllers.GhostController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Constants.DM;
import pacman.game.Game;

public class GhostsV0Me extends GhostController {
	private EnumMap<GHOST, MOVE> moves = new EnumMap<GHOST, MOVE>(GHOST.class);
    public static final double DISTANCIA_MINIMA_FANTASMAS             = 5.0;
    public static final double DISTANCIA_PACMAN_CERCA             = 10.0;
    public static final int PASOS_PREDICCION             = 4;
    
    
//  ESTADOS DE ESTRATEGIA
	private enum FaseG {
		PERSEGUIR,
		EVADIR,
		HUIDA,      
		NORMAL      
	}
	private EnumMap<GHOST, FaseG> estados = new EnumMap<>(GHOST.class);

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        moves.clear();

        boolean dispersar = necesitanDispersarse(game);

        for (GHOST f : GHOST.values()) {

            if (!game.doesGhostRequireAction(f)) continue;

            int nodo = game.getGhostCurrentNodeIndex(f);

            if (nodo == -1 || game.getGhostLairTime(f) > 0) {
                moves.put(f, MOVE.NEUTRAL);
                continue;
            }

            FaseG faseActual = detectarFase(game, f, dispersar);

            actualizarEstados(game, faseActual, f);
        }

        return moves;
    }
    
	private void actualizarEstados(Game game, FaseG estado, GHOST f) {
	
	    switch (estado) {
	
	        case HUIDA:
	            moves.put(f, huirConCriterio(
	                    game,
	                    game.getGhostCurrentNodeIndex(f),
	                    game.getGhostLastMoveMade(f),
	                    game.getPacmanCurrentNodeIndex(),
	                    f));
	            break;
	
	        case EVADIR:
	            moves.put(f, movimientoDispersar(game, f));
	            break;
	
	        case PERSEGUIR:
	        case NORMAL:
	        default:
	            // Cada fantasma utiliza su estrategia propia.
	            moves.put(f, segunSuPapel(game, f));
	            break;
	    }
	}

private MOVE perseguir(Game game, GHOST f) {
    	return primerMovimientoDe(game, 
    			game.getShortestPath(game.getGhostCurrentNodeIndex(f), game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(f)));
    }
    
    private MOVE segunSuPapel(Game game, GHOST f) {
        int  nodo    = game.getGhostCurrentNodeIndex(f);
        MOVE ultimo  = game.getGhostLastMoveMade(f);
        int  pacman  = game.getPacmanCurrentNodeIndex();
        MOVE ultPac  = game.getPacmanLastMoveMade();

        if (game.getGhostEdibleTime(f) > 0) {
            return huirConCriterio(game, nodo, ultimo, pacman, f);
        }

        switch (f) {
        case BLINKY:
            // Persecución directa.
            return primerMovimientoDe(
                    game, game.getShortestPath(nodo, pacman, ultimo));

        case PINKY: {
            // Intentar anticiparse a Pac-Man.
            int objetivo=pacman;
            MOVE direccion = game.getPacmanLastMoveMade();

            for (int i = 0; i < PASOS_PREDICCION; i++) {

                int siguiente = game.getNeighbour(nodo, direccion);

                if (siguiente == -1) break;

                objetivo = siguiente;

                if (game.isJunction(nodo)) break;
            }

            if (objetivo != -1 && objetivo != pacman) {
                int[] camino = game.getShortestPath(
                        nodo, objetivo, ultimo);

                if (camino != null && camino.length >= 2) {
                    return primerMovimientoDe(game, camino);
                }
            }

            return primerMovimientoDe(
                    game, game.getShortestPath(nodo, pacman, ultimo));
        }

        case INKY: {
            // Defender una píldora de poder alcanzable.
            int[] pp = game.getActivePowerPillsIndices();

            int mejorObjetivo = -1;
            double mejorPuntuacion = Double.NEGATIVE_INFINITY;

            for (int pill : pp) {

                int dFantasma =
                        game.getShortestPathDistance(nodo, pill);

                int dPacman =
                        game.getShortestPathDistance(pacman, pill);

                if (dFantasma < 0 || dPacman < 0) continue;

                double puntuacion = dPacman - dFantasma;

                if (puntuacion > mejorPuntuacion) {
                    mejorPuntuacion = puntuacion;
                    mejorObjetivo = pill;
                }
            }

            if (mejorObjetivo != -1 && mejorPuntuacion >= 0) {
                int[] camino = game.getShortestPath(
                        nodo, mejorObjetivo, ultimo);

                if (camino != null && camino.length >= 2) {
                    return primerMovimientoDe(game, camino);
                }
            }

            return primerMovimientoDe(
                    game, game.getShortestPath(nodo, pacman, ultimo));
        }

        case SUE:
        default:
            // Persecución directa, sin movimientos aleatorios.
            return primerMovimientoDe(
                    game, game.getShortestPath(nodo, pacman, ultimo));
        }
    }
    
    private MOVE primerMovimientoDe(Game game, int[] camino) {
        if (camino == null || camino.length < 2) return MOVE.NEUTRAL;
        return game.getMoveToMakeToReachDirectNeighbour(camino[0], camino[1]);
    }
    
    private int proximoCruceDe(Game game, GHOST f) {

        int fantasma = game.getGhostCurrentNodeIndex(f);
        MOVE direccion = game.getPacmanLastMoveMade();

        if (direccion == null || direccion == MOVE.NEUTRAL) {
            return -1;
        }

        int actual = game.getNeighbour(fantasma, direccion);

        if (actual == -1) {
            return -1;
        }

        MOVE movimientoAnterior = direccion;
        int limite = 0;

        while (actual != -1 && !game.isJunction(actual) && limite < 1000) {

            MOVE[] opciones =
                    game.getPossibleMoves(actual, movimientoAnterior);

            if (opciones == null || opciones.length == 0) {
                return -1;
            }

            MOVE siguiente = opciones[0];
            int vecino = game.getNeighbour(actual, siguiente);

            if (vecino == -1) {
                return -1;
            }

            actual = vecino;
            movimientoAnterior = siguiente;
            limite++;
        }

        if (limite >= 1000 || actual == -1) {
            return -1;
        }

        return actual;
    }

private MOVE huirConCriterio(Game game, int nodo, MOVE ultimo, int pacman, GHOST ghostType) {

    MOVE[] opciones = game.getPossibleMoves(nodo, ultimo);

    if (opciones == null || opciones.length == 0) {
        return MOVE.NEUTRAL;
    }

    MOVE mejor = opciones[0];
    double mejorPuntuacion = Double.NEGATIVE_INFINITY;

    for (MOVE m : opciones) {

        int vecino = game.getNeighbour(nodo, m);

        if (vecino == -1) continue;

        int distancia =
                game.getShortestPathDistance(vecino, pacman);

        double puntuacion = distancia >= 0
                ? distancia * 10.0
                : -1000.0;

        if (game.isJunction(vecino)) {
            puntuacion += 8;
        }

        // Evitar las píldoras de poder activas.
        for (int pill : game.getActivePowerPillsIndices()) {

            int d = game.getShortestPathDistance(vecino, pill);

            if (d == 0) {
                puntuacion -= 200;
            } else if (d > 0 && d <= 2) {
                puntuacion -= 30;
            }
        }

        if (puntuacion > mejorPuntuacion) {
            mejorPuntuacion = puntuacion;
            mejor = m;
        }
    }

    return mejor;
}

    
    private boolean necesitanDispersarse(Game game) {
        int activos = 0;

        for (GHOST f : GHOST.values()) {
            if (game.getGhostLairTime(f) > 0) continue;

            int nodo = game.getGhostCurrentNodeIndex(f);

            if (nodo == -1) continue;

            activos++;
        }

        // Con menos de 2 no tiene sentido dispersarse
        if (activos < 2)
            return false;

        for (GHOST f1 : GHOST.values()) {
            if (game.getGhostLairTime(f1) > 0)continue;

            int n1 = game.getGhostCurrentNodeIndex(f1);

            if (n1 == -1)
                continue;

            for (GHOST f2 : GHOST.values()) {
                if (f1 == f2) continue;

                if (game.getGhostLairTime(f2) > 0) continue;

                int n2 = game.getGhostCurrentNodeIndex(f2);

                if (n2 == -1)continue;

                int distancia = game.getShortestPathDistance(n1, n2);

                if (distancia >= 0 &&
                    distancia < DISTANCIA_MINIMA_FANTASMAS) {

                    return true;
                }
            }
        }

        return false;
    }
    private MOVE movimientoDispersar(Game game, GHOST fantasma) {

        int nodo = game.getGhostCurrentNodeIndex(fantasma);

        MOVE ultimo = game.getGhostLastMoveMade(fantasma);

        MOVE[] opciones = game.getPossibleMoves(nodo, ultimo);

        if (opciones == null || opciones.length == 0)
            return MOVE.NEUTRAL;

        MOVE mejor = opciones[0];
        double mejorPuntuacion =
                Double.NEGATIVE_INFINITY;

        for (MOVE m : opciones) {

            int vecino = game.getNeighbour(nodo, m);

            if (vecino == -1) continue;

            double puntuacion = puntuacionDispersión(game,fantasma,vecino);
             
            switch (fantasma) {

                case BLINKY:
                    if (m == MOVE.LEFT)
                        puntuacion += 40;
                    break;

                case PINKY:
                    if (m == MOVE.UP)
                        puntuacion += 40;
                    break;

                case INKY:
                    if (m == MOVE.RIGHT)
                        puntuacion += 40;
                    break;

                case SUE:
                    if (m == MOVE.DOWN)
                        puntuacion += 40;
                    break;
            }

            if (puntuacion > mejorPuntuacion) {
                mejorPuntuacion = puntuacion;
                mejor = m;
            }
        }

        return mejor;
    }

	private double puntuacionDispersión(
	        Game game, GHOST fantasma, int candidato) {
	
	    double puntuacion = 0.0;
	
	    // Alejarse de los demás fantasmas.
	    for (GHOST otro : GHOST.values()) {
	
	        if (otro == fantasma) continue;
	        if (game.getGhostLairTime(otro) > 0) continue;
	
	        int nodoOtro = game.getGhostCurrentNodeIndex(otro);
	
	        if (nodoOtro == -1) continue;
	
	        int distancia =
	                game.getShortestPathDistance(candidato, nodoOtro);
	
	        if (distancia < 0) continue;
	
	        puntuacion += Math.min(distancia, 12) * 8.0;
	
	        if (distancia <= 2) {
	            puntuacion -= 150;
	        } else if (distancia <= 4) {
	            puntuacion -= 50;
	        }
	    }
	
	    // Preferir intersecciones.
	    if (game.isJunction(candidato)) {
	        puntuacion += 20;
	    }
	
	    // Evitar las píldoras de poder.
	    for (int pill : game.getActivePowerPillsIndices()) {
	
	        int distancia =
	                game.getShortestPathDistance(candidato, pill);
	
	        if (distancia == 0) {
	            puntuacion -= 300;
	        } else if (distancia > 0 && distancia <= 2) {
	            puntuacion -= 100;
	        }
	    }
	
	    return puntuacion;
	}
	
    private FaseG detectarFase(
            Game game, GHOST fantasma, boolean dispersar) {

        if (game.isGhostEdible(fantasma)) {
            return FaseG.HUIDA;
        }

        int nodo = game.getGhostCurrentNodeIndex(fantasma);
        int pacman = game.getPacmanCurrentNodeIndex();

        int distancia =
                game.getShortestPathDistance(nodo, pacman);

        // No dispersarse cuando Pac-Man esta cerca.
        if (distancia >= 0
                && distancia <= DISTANCIA_PACMAN_CERCA) {
            return FaseG.PERSEGUIR;
        }

        if (dispersar) {
            return FaseG.EVADIR;
        }

        return FaseG.PERSEGUIR;
    }
    

    public String getName() {
    	return "GhostsV0Me_GIRM_LC";
    }

}

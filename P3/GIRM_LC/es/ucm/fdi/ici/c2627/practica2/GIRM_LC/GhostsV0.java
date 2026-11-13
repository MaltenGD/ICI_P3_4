package es.ucm.fdi.ici.c2627.practica2.GIRM_LC;

import java.util.EnumMap;

import pacman.controllers.GhostController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Constants.DM;
import pacman.game.Game;

public class GhostsV0 extends GhostController {
	private EnumMap<GHOST, MOVE> moves = new EnumMap<GHOST, MOVE>(GHOST.class);
    public static final double DISTANCIA_MINIMA_FANTASMAS             = 5.0;

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {

        moves.clear();

        boolean dispersar = necesitanDispersarse(game);

        for (GHOST f : GHOST.values()) {

            if (!game.doesGhostRequireAction(f)) continue;

//              Si están demasiado juntos, se dispersan.
//              Los fantasmas comestibles siguen huyendo.
             
            if (dispersar && !game.isGhostEdible(f)) {
                moves.put(f, movimientoDispersar(game, f));
            }
            else {
                moves.put(f, segunSuPapel(game, f));
            }
        }

        return moves;
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
            case BLINKY: // ROJO; persigue por el camino más corto
                return primerMovimientoDe(game,
                        game.getShortestPath(nodo, pacman, ultimo));

            case PINKY: { // Rosa: intenta interceptar a Ms. Pac-Man
                int cruce = proximoCruceDe(game);

                if (cruce != -1) {
                    return primerMovimientoDe(
                            game,
                            game.getShortestPath(nodo, cruce, ultimo));
                }

                // Si no encontramos ningún cruce, perseguimos a Pac-Man
                return primerMovimientoDe(
                        game,
                        game.getShortestPath(nodo, pacman, ultimo));
            }

            case INKY: { // AZUL; cubre la píldora de poder activa más cercana a Ms. Pac-Man
                int[] pp = game.getActivePowerPillsIndices();
                if (pp.length > 0) {
                    int guardar = game.getClosestNodeIndexFromNodeIndex(pacman, pp, DM.PATH);
                    return primerMovimientoDe(game, game.getShortestPath(nodo, guardar, ultimo));
                }
                return primerMovimientoDe(game,
                        game.getShortestPath(nodo, pacman, ultimo));
            }

            default: // NARANJA; persigue, pero de vez en cuando se sale del guion
                if (game.getCurrentLevelTime() % 20 == 0) {
                    return movimientoAleatorioLegal(game, nodo, ultimo);
                }
                return primerMovimientoDe(game,
                        game.getShortestPath(nodo, pacman, ultimo));
        }
    }
    
    private MOVE primerMovimientoDe(Game game, int[] camino) {
        if (camino == null || camino.length < 2) return MOVE.NEUTRAL;
        return game.getMoveToMakeToReachDirectNeighbour(camino[0], camino[1]);
    }
    
    private int proximoCruceDe(Game game) {
        int pacman = game.getPacmanCurrentNodeIndex();
        MOVE direccion = game.getPacmanLastMoveMade();

        // Sin dirección conocida, no podemos calcular hacia dónde va
        if (direccion == null || direccion == MOVE.NEUTRAL) {
            return -1;
        }

        // Avanzamos primero en la dirección de Pac-Man
        int actual = game.getNeighbour(pacman, direccion);

        if (actual == -1) {
            return -1;
        }

        MOVE movimientoAnterior = direccion;
        int limite = 0;

        // Buscamos la primera intersección que haya delante
        while (actual == -1 && limite < 1000) {
            MOVE[] opciones =
                    game.getPossibleMoves(actual, movimientoAnterior);

            if (opciones == null || opciones.length == 0) {
                return -1;
            }

            // En un pasillo normalmente solo hay una opción
            MOVE siguiente = opciones[0];
            int vecino = game.getNeighbour(actual, siguiente);

            if (vecino == -1) {
                return -1;
            }

            actual = vecino;
            movimientoAnterior = siguiente;
            limite++;
        }

        if (limite >= 1000) {
            return -1;
        }

        return actual;
    }

    private MOVE huirConCriterio(Game game, int nodo, MOVE ultimo, int pacman, GHOST ghostType) {

        GHOST mejorCompanero = null;
        int menorDistancia = Integer.MAX_VALUE;

        // Buscamos el compañero no comestible más cercano
        // que se encuentre fuera de la guarida.
        for (GHOST otro : GHOST.values()) {

            if (otro == ghostType) {
                continue;
            }

            if (game.getGhostLairTime(otro) > 0) {
                continue;
            }

            if (game.isGhostEdible(otro)) {
                continue;
            }

            int nodoOtro = game.getGhostCurrentNodeIndex(otro);

            if (nodoOtro == -1) {
                continue;
            }

            int distancia =
                    game.getShortestPathDistance(nodo, nodoOtro);

            if (distancia >= 0 && distancia < menorDistancia) {
                menorDistancia = distancia;
                mejorCompanero = otro;
            }
        }

        // Si existe un compañero adecuado, nos acercamos a él
        if (mejorCompanero != null) {
            int objetivo =
                    game.getGhostCurrentNodeIndex(mejorCompanero);

            int[] camino =
                    game.getShortestPath(nodo, objetivo, ultimo);

            return primerMovimientoDe(game, camino);
        }

        // Si no hay compañeros válidos, huimos de Pac-Man
        return game.getApproximateNextMoveAwayFromTarget(
                nodo,
                pacman,
                ultimo,
                DM.PATH);
    }

    private MOVE movimientoAleatorioLegal(Game game, int nodo, MOVE ultimo) {
        MOVE[] ops = game.getPossibleMoves(nodo, ultimo);
        return ops[(int)(Math.random() * ops.length)];
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
            Game game,
            GHOST fantasma,
            int candidato) {

        double puntuacion = 0.0;

        // 1. Alejarse de los demás fantasmas

        for (GHOST otro : GHOST.values()) {

            if (otro == fantasma) continue;

            if (game.getGhostLairTime(otro) > 0) continue;

            int nodoOtro = game.getGhostCurrentNodeIndex(otro);

            if (nodoOtro == -1) continue;

            int distancia = game.getShortestPathDistance(candidato, nodoOtro);

            if (distancia < 0) continue;
            
//              Cuanto más lejos esté el candidato del otro
//              fantasma, mejor.
             
            puntuacion += distancia * 10.0;

//              Penalización fuerte si siguen juntos.
             
            if (distancia <= 2)
                puntuacion -= 500;

            else if (distancia <= 4)
                puntuacion -= 200;

            else if (distancia <= 6)
                puntuacion -= 50;
        }

        // 2. Preferir intersecciones

        if (game.isJunction(candidato)) {
            puntuacion += 30;
        }

        // 3. Evitar que todos se vayan hacia Pac-Man

        int pacman = game.getPacmanCurrentNodeIndex();

        int distanciaPacman =
                game.getShortestPathDistance(
                        candidato,
                        pacman);

        if (distanciaPacman >= 0) {
            puntuacion += distanciaPacman * 2.0;
        }

        return puntuacion;
    }


    public String getName() {
    	return "GhostsV0_GIRM_LC";
    }

}
package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v3Ghosts;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v3Ghosts.Ghosts.Turno;

final class Movimiento implements Action {
    private final String id;
    private final GHOST yo;
    private final Turno plan;

    Movimiento(String id, GHOST yo, Turno plan) {
        this.id = id;
        this.yo = yo;
        this.plan = plan;
    }

    @Override
    public MOVE execute(Game game) {
        int nodo = game.getGhostCurrentNodeIndex(yo);
        MOVE ultimo = game.getGhostLastMoveMade(yo);
        if ("huir".equals(id)) return escapar(game, nodo, ultimo);
        int objetivo = plan.pacman;
        if ("interceptar".equals(id)) objetivo = plan.intercepcion;
        if ("defender".equals(id) && plan.poder >= 0) objetivo = plan.poder;
        return acercar(game, nodo, objetivo, ultimo);
    }

    private MOVE escapar(Game game, int nodo, MOVE ultimo) {
        MOVE[] salidas = game.getPossibleMoves(nodo, ultimo);
        if (salidas.length == 0) return MOVE.NEUTRAL;
        MOVE mejor = salidas[0];
        double maximo = Double.NEGATIVE_INFINITY;
        for (MOVE m : salidas) {
            int futuro = avanzar(game, nodo, m, 9);
            double valor = game.getShortestPathDistance(futuro, plan.pacman);
            for (GHOST g : GHOST.values()) {
                if (g == yo || game.getGhostEdibleTime(g) <= 0) continue;
                int d = game.getShortestPathDistance(futuro, game.getGhostCurrentNodeIndex(g));
                valor -= 0.25 * Math.max(0, 20 - d);
            }
            if (valor > maximo) {
                mejor = m;
                maximo = valor;
            }
        }
        return mejor;
    }

    static int avanzar(Game game, int nodo, MOVE rumbo, int pasos) {
        while (rumbo != MOVE.NEUTRAL && pasos-- > 0) {
            int siguiente = game.getNeighbour(nodo, rumbo);
            if (siguiente < 0) {
                MOVE[] posibles = game.getPossibleMoves(nodo, rumbo);
                if (posibles.length != 1) break;
                rumbo = posibles[0];
                siguiente = game.getNeighbour(nodo, rumbo);
            }
            if (siguiente < 0) break;
            nodo = siguiente;
        }
        return nodo;
    }

    private MOVE acercar(Game game, int nodo, int objetivo, MOVE ultimo) {
        MOVE[] salidas = game.getPossibleMoves(nodo, ultimo);
        if (salidas.length == 0) return MOVE.NEUTRAL;
        MOVE mejor = salidas[0];
        int minima = Integer.MAX_VALUE;
        for (MOVE m : salidas) {
            int vecino = game.getNeighbour(nodo, m);
            int distancia = vecino == objetivo ? 0 : game.getShortestPathDistance(vecino, objetivo, m);
            if (distancia >= 0 && distancia < minima) {
                minima = distancia;
                mejor = m;
            }
        }
        return mejor;
    }

    @Override
    public String getActionId() { return id; }

    @Override
    public String toString() { return yo + " " + id; }
}

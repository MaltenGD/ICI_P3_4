package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.actions;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// huir de pacman: elegir el movimiento que aleje mas al fantasma de pacman
public class FleeFromPacManAction implements Action {

    private final GHOST ghost;

    public FleeFromPacManAction(GHOST ghost) {
        this.ghost = ghost;
    }

    @Override
    public MOVE execute(Game game) {
        int ghostNode  = game.getGhostCurrentNodeIndex(ghost);
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        if (ghostNode < 0 || pacmanNode < 0) return MOVE.NEUTRAL;

        MOVE lastMove = game.getGhostLastMoveMade(ghost);
        MOVE[] possible = game.getPossibleMoves(ghostNode, lastMove);
        if (possible.length == 0) return MOVE.NEUTRAL;

        MOVE best   = possible[0];
        int maxDist = -1;
        for (MOVE m : possible) {
            int neighbor = game.getNeighbour(ghostNode, m);
            if (neighbor < 0) continue;
            int d = game.getShortestPathDistance(neighbor, pacmanNode);
            if (d > maxDist) {
                maxDist = d;
                best    = m;
            }
        }
        return best;
    }

    @Override
    public String getActionId() { return "HUIR_PACMAN"; }
}

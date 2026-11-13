package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.actions;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// huir a rincon: ir al cruce mas alejado de pacman
public class FleeToCornerAction implements Action {

    private final GHOST ghost;

    public FleeToCornerAction(GHOST ghost) {
        this.ghost = ghost;
    }

    @Override
    public MOVE execute(Game game) {
        int ghostNode  = game.getGhostCurrentNodeIndex(ghost);
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        if (ghostNode < 0 || pacmanNode < 0) return MOVE.NEUTRAL;

        int[] junctions = game.getJunctionIndices();
        int farthest = -1;
        int maxDist  = -1;
        for (int j : junctions) {
            int d = game.getShortestPathDistance(j, pacmanNode);
            if (d > maxDist) {
                maxDist  = d;
                farthest = j;
            }
        }

        if (farthest < 0) return MOVE.NEUTRAL;
        return game.getNextMoveTowardsTarget(ghostNode, farthest,
                game.getGhostLastMoveMade(ghost), DM.PATH);
    }

    @Override
    public String getActionId() { return "HUIR_RINCON"; }
}

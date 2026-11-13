package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.actions;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// patrullar: moverse al cruce mas alejado del fantasma actual
// sirve para cubrir el mapa cuando pacman esta lejos
public class PatrolAction implements Action {

    private final GHOST ghost;

    public PatrolAction(GHOST ghost) {
        this.ghost = ghost;
    }

    @Override
    public MOVE execute(Game game) {
        int ghostNode = game.getGhostCurrentNodeIndex(ghost);
        if (ghostNode < 0) return MOVE.NEUTRAL;

        int[] junctions = game.getJunctionIndices();
        int farthest = -1;
        int maxDist  = -1;

        for (int j : junctions) {
            int d = game.getShortestPathDistance(ghostNode, j);
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
    public String getActionId() { return "PATRULLAR"; }
}

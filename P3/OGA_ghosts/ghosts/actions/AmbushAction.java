package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.actions;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// acechar: mantener una distancia media (~25 nodos) de pacman para poder rodearle
// busca el cruce mas cercano a pacman que este a unos 20-30 nodos de el
public class AmbushAction implements Action {

    private final GHOST ghost;
    private static final int TARGET_DIST = 25;
    private static final int TOLERANCE   = 10;

    public AmbushAction(GHOST ghost) {
        this.ghost = ghost;
    }

    @Override
    public MOVE execute(Game game) {
        int ghostNode  = game.getGhostCurrentNodeIndex(ghost);
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        if (ghostNode < 0 || pacmanNode < 0) return MOVE.NEUTRAL;

        int[] junctions = game.getJunctionIndices();
        int bestTarget = -1;
        int bestScore  = Integer.MAX_VALUE;

        for (int j : junctions) {
            int dToPacman = game.getShortestPathDistance(j, pacmanNode);
            // queremos cruces a TARGET_DIST +/- TOLERANCE de pacman
            int diff = Math.abs(dToPacman - TARGET_DIST);
            if (diff < bestScore) {
                bestScore  = diff;
                bestTarget = j;
            }
        }

        if (bestTarget < 0) bestTarget = pacmanNode;
        return game.getNextMoveTowardsTarget(ghostNode, bestTarget,
                game.getGhostLastMoveMade(ghost), DM.PATH);
    }

    @Override
    public String getActionId() { return "ACECHAR"; }
}

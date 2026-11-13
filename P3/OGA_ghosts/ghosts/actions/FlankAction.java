package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.actions;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// flanquear: ir al cruce mas cercano que no este ocupado por un aliado
public class FlankAction implements Action {

    private final GHOST ghost;
    private static final int ALLY_EXCLUSION_DIST = 10; // nodos de exclusion entre aliados

    public FlankAction(GHOST ghost) {
        this.ghost = ghost;
    }

    @Override
    public MOVE execute(Game game) {
        int ghostNode  = game.getGhostCurrentNodeIndex(ghost);
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        if (ghostNode < 0 || pacmanNode < 0) return MOVE.NEUTRAL;

        int[] junctions = game.getJunctionIndices();
        int bestTarget = -1;
        int bestDist   = Integer.MAX_VALUE;

        for (int j : junctions) {
            // solo cruces que esten cerca de pacman (en radio 40 nodos)
            int dToPacman = game.getShortestPathDistance(j, pacmanNode);
            if (dToPacman > 40) continue;

            // ignorar cruces que ya tenga un aliado encima
            if (allyNearby(game, j)) continue;

            int dToGhost = game.getShortestPathDistance(ghostNode, j);
            if (dToGhost < bestDist) {
                bestDist   = dToGhost;
                bestTarget = j;
            }
        }

        if (bestTarget < 0) bestTarget = pacmanNode; // fallback a perseguir
        return game.getNextMoveTowardsTarget(ghostNode, bestTarget,
                game.getGhostLastMoveMade(ghost), DM.PATH);
    }

    private boolean allyNearby(Game game, int node) {
        for (GHOST other : GHOST.values()) {
            if (other == ghost) continue;
            int otherNode = game.getGhostCurrentNodeIndex(other);
            if (otherNode < 0) continue;
            if (game.getShortestPathDistance(node, otherNode) < ALLY_EXCLUSION_DIST) return true;
        }
        return false;
    }

    @Override
    public String getActionId() { return "FLANQUEAR"; }
}

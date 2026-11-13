package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.actions;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// reagrupar: moverse hacia el centroide de todos los fantasmas
// sirve para coordinar cuando los fantasmas se han dispersado demasiado
public class RegroupAction implements Action {

    private final GHOST ghost;

    public RegroupAction(GHOST ghost) {
        this.ghost = ghost;
    }

    @Override
    public MOVE execute(Game game) {
        int ghostNode = game.getGhostCurrentNodeIndex(ghost);
        if (ghostNode < 0) return MOVE.NEUTRAL;

        // calcular centroide de las posiciones de todos los fantasmas
        int sumX = 0, sumY = 0, count = 0;
        for (GHOST g : GHOST.values()) {
            int node = game.getGhostCurrentNodeIndex(g);
            if (node < 0) continue;
            sumX += game.getNodeXCood(node);
            sumY += game.getNodeYCood(node);
            count++;
        }
        if (count == 0) return MOVE.NEUTRAL;

        int cx = sumX / count;
        int cy = sumY / count;

        // cruce mas cercano al centroide
        int[] junctions = game.getJunctionIndices();
        int target = junctions[0];
        int bestDist = Integer.MAX_VALUE;
        for (int j : junctions) {
            int dx = game.getNodeXCood(j) - cx;
            int dy = game.getNodeYCood(j) - cy;
            int d = dx * dx + dy * dy;
            if (d < bestDist) {
                bestDist = d;
                target   = j;
            }
        }

        return game.getNextMoveTowardsTarget(ghostNode, target,
                game.getGhostLastMoveMade(ghost), DM.PATH);
    }

    @Override
    public String getActionId() { return "REAGRUPAR"; }
}

package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.actions;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// guardar respawn: ir a la zona central del mapa (donde reaparecen los fantasmas)
// el nodo de respawn se estima como el cruce mas centrado del mapa
public class GuardRespawnAction implements Action {

    private final GHOST ghost;
    private int respawnNode = -1; // se calcula la primera vez

    public GuardRespawnAction(GHOST ghost) {
        this.ghost = ghost;
    }

    @Override
    public MOVE execute(Game game) {
        int ghostNode = game.getGhostCurrentNodeIndex(ghost);
        if (ghostNode < 0) return MOVE.NEUTRAL;

        // calcular nodo de respawn la primera vez usando el fantasma en su posicion inicial
        if (respawnNode < 0) {
            respawnNode = findCenterJunction(game);
        }

        return game.getNextMoveTowardsTarget(ghostNode, respawnNode,
                game.getGhostLastMoveMade(ghost), DM.PATH);
    }

    // el cruce mas cercano al centro geometrico del mapa
    private int findCenterJunction(Game game) {
        int[] junctions = game.getJunctionIndices();
        // calcular bounding box del mapa y buscar cruce mas cercano al centro
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (int j : junctions) {
            int x = game.getNodeXCood(j);
            int y = game.getNodeYCood(j);
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }
        int cx = (minX + maxX) / 2;
        int cy = (minY + maxY) / 2;

        int best = junctions[0];
        int bestDist = Integer.MAX_VALUE;
        for (int j : junctions) {
            int dx = game.getNodeXCood(j) - cx;
            int dy = game.getNodeYCood(j) - cy;
            int d = dx * dx + dy * dy;
            if (d < bestDist) {
                bestDist = d;
                best = j;
            }
        }
        return best;
    }

    @Override
    public String getActionId() { return "GUARDAR_RESPAWN"; }
}

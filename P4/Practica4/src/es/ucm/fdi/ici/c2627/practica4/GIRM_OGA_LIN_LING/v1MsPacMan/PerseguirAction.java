
package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v1MsPacMan;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class PerseguirAction implements Action {

    @Override
    public MOVE execute(Game game) {

        int pacman = game.getPacmanCurrentNodeIndex();

        int presa = -1;
        double mejorPuntuacion = Double.POSITIVE_INFINITY;

        for (GHOST g : GHOST.values()) {

            if (game.getGhostLairTime(g) > 0)
                continue;

            if (!game.isGhostEdible(g))
                continue;

            int nodo = game.getGhostCurrentNodeIndex(g);

            double distancia =
                game.getShortestPathDistance(pacman, nodo);

            if (distancia < 0)
                continue;

            int tiempo = game.getGhostEdibleTime(g);

            // Aplicar exactamente el mismo criterio que
            // utilizamos en MsPacManInput.
            if (tiempo <= 2 * distancia + 2)
                continue;

            // Priorizar fantasmas cercanos con suficiente
            // margen de tiempo para alcanzarlos.
            double margen = tiempo - (2 * distancia + 2);

            double puntuacion = distancia - 0.05 * margen;

            if (puntuacion < mejorPuntuacion) {
                mejorPuntuacion = puntuacion;
                presa = nodo;
            }
        }

        if (presa == -1)
            return MOVE.NEUTRAL;

        return game.getNextMoveTowardsTarget(
            pacman, presa, DM.PATH
        );
    }

    @Override
    public String getActionId() {
        return "perseguir";
    }

    @Override
    public String toString() {
        return getActionId();
    }
}

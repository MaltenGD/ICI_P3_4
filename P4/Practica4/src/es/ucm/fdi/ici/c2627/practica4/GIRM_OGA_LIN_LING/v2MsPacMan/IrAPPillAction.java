package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v2MsPacMan;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.DM;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Hacia la píldora de poder óptima, teniendo en cuenta aislamiento en fase final
 * y evitando giros bruscos o retrocesos innecesarios.
 */
public class IrAPPillAction implements Action {

    @Override
    public MOVE execute(Game game) {
        int pacman = game.getPacmanCurrentNodeIndex();
        int[] ppills = game.getActivePowerPillsIndices();
        if (ppills.length == 0) return MOVE.NEUTRAL;

        int objetivo = -1;
        double mejorPuntuacion = Double.POSITIVE_INFINITY;

        for (int pill : ppills) {
            double distancia = game.getShortestPathDistance(pacman, pill);
            if (distancia < 0) continue;

            double aislamiento = 0;
            // Si quedan 3 o menos píldoras de poder, priorizar píldoras aisladas
            if (ppills.length <= 3 && ppills.length > 1) {
                double distanciaOtra = Double.POSITIVE_INFINITY;
                for (int otra : ppills) {
                    if (otra == pill) continue;
                    double d = game.getShortestPathDistance(pill, otra);
                    if (d >= 0) distanciaOtra = Math.min(distanciaOtra, d);
                }
                if (distanciaOtra != Double.POSITIVE_INFINITY) {
                    aislamiento = -distanciaOtra * 0.3; // Más aislada -> puntuación menor (mejor)
                }
            }

            double puntuacion = distancia + aislamiento;
            if (puntuacion < mejorPuntuacion) {
                mejorPuntuacion = puntuacion;
                objetivo = pill;
            }
        }

        if (objetivo < 0) return MOVE.NEUTRAL;
        return game.getApproximateNextMoveTowardsTarget(pacman, objetivo, game.getPacmanLastMoveMade(), DM.PATH);
    }

    @Override
    public String getActionId() { return "irAPPill"; }

    @Override
    public String toString() { return getActionId(); }
}

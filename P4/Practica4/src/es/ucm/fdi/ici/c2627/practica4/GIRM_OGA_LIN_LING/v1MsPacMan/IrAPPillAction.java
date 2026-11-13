package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v1MsPacMan;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.DM;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class IrAPPillAction implements Action {

    @Override
    public MOVE execute(Game game) {

        int pacman = game.getPacmanCurrentNodeIndex();

        int[] ppills = game.getActivePowerPillsIndices();

        if (ppills.length == 0)
            return MOVE.NEUTRAL;

        int objetivo = -1;

        double mejorPuntuacion =
            Double.POSITIVE_INFINITY;

        for (int pill : ppills) {

            double distancia = game.getShortestPathDistance(
                pacman, pill
            );

            if (distancia < 0)
                continue;

            double aislamiento = 0;

            // Solo considerar el aislamiento cuando
            // quedan tres o menos Power Pills.
            if (ppills.length <= 3 && ppills.length > 1) {

                double distanciaOtra =
                    Double.POSITIVE_INFINITY;

                for (int otra : ppills) {

                    if (otra == pill)
                        continue;

                    double d = game.getShortestPathDistance(
                        pill, otra
                    );

                    if (d >= 0) {
                        distanciaOtra = Math.min(
                            distanciaOtra, d
                        );
                    }
                }

                if (distanciaOtra !=
                    Double.POSITIVE_INFINITY) {

                    aislamiento = distanciaOtra;
                }
            }

            // Cuanto más aislada esté la píldora,
            // más se reduce su puntuación.
            double puntuacion = distancia;

            if (ppills.length <= 3) {
                puntuacion -= 0.20 * aislamiento;
            }

            if (puntuacion < mejorPuntuacion) {

                mejorPuntuacion = puntuacion;
                objetivo = pill;
            }
        }

        if (objetivo == -1)
            return MOVE.NEUTRAL;

        return game.getNextMoveTowardsTarget(
            pacman, objetivo, DM.PATH
        );
    }


    @Override
    public String getActionId() {
        return "irAPPill";
    }

    @Override
    public String toString() {
        return getActionId();
    }
}

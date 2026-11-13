
package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v1MsPacMan;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class HuirAction implements Action {

    @Override
    public MOVE execute(Game game) {

        int pacman = game.getPacmanCurrentNodeIndex();

        List<Integer> fantasmas = new ArrayList<>();

        for (GHOST g : GHOST.values()) {

            if (game.isGhostEdible(g))
                continue;

            if (game.getGhostLairTime(g) > 0)
                continue;

            int nodo = game.getGhostCurrentNodeIndex(g);

            if (game.getShortestPathDistance(pacman, nodo) >= 0)
                fantasmas.add(nodo);
        }

        if (fantasmas.isEmpty())
            return MOVE.NEUTRAL;

        MOVE mejorMovimiento = MOVE.NEUTRAL;

        double mejorPuntuacion = Double.NEGATIVE_INFINITY;

        MOVE[] movimientos = game.getPossibleMoves(pacman);

        for (MOVE movimiento : movimientos) {

            int siguiente = game.getNeighbour(pacman, movimiento);

            if (siguiente < 0)
                continue;

            double distanciaMinima = Double.POSITIVE_INFINITY;
            double sumaDistancias = 0;

            int evaluados = 0;

            for (int fantasma : fantasmas) {

                double d = game.getShortestPathDistance(
                    siguiente, fantasma
                );

                if (d < 0)
                    continue;

                distanciaMinima = Math.min(distanciaMinima, d);
                sumaDistancias += d;
                evaluados++;
            }

            if (evaluados == 0)
                continue;

            // Seguridad frente a todos los fantasmas.
            double puntuacion =
                3.0 * distanciaMinima + sumaDistancias;

            // Penalizar posiciones con pocas salidas.
            int salidas = game.getPossibleMoves(siguiente).length;

            if (salidas <= 1)
                puntuacion -= 20;
            else if (salidas == 2)
                puntuacion -= 5;

            if (puntuacion > mejorPuntuacion) {

                mejorPuntuacion = puntuacion;
                mejorMovimiento = movimiento;
            }
        }

        return mejorMovimiento;
    }

    @Override
    public String getActionId() {
        return "huir";
    }

    @Override
    public String toString() {
        return getActionId();
    }
}

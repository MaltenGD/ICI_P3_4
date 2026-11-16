package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v0Ghosts;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/** Rol de control de comida: ir a por la píldora normal más cercana. */
public class ControlarPillsAction implements Action {

    private final GHOST yo;

    public ControlarPillsAction(GHOST yo) { this.yo = yo; }

    @Override
    public MOVE execute(Game game) {
        int[] pills = game.getActivePillsIndices();
        if (pills == null || pills.length == 0) {
            // Si no quedan píldoras, perseguir como reserva
            return game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(yo),
                    game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(yo), DM.PATH);
        }
        
        // Buscar la píldora más cercana
        int nodoYo = game.getGhostCurrentNodeIndex(yo);
        int mejorPill = pills[0];
        double minDist = Double.MAX_VALUE;
        
        for (int p : pills) {
            double d = game.getShortestPathDistance(nodoYo, p);
            if (d >= 0 && d < minDist) {
                minDist = d;
                mejorPill = p;
            }
        }
        
        // Ir hacia ella
        return game.getApproximateNextMoveTowardsTarget(nodoYo, mejorPill, 
                game.getGhostLastMoveMade(yo), DM.PATH);
    }

    @Override
    public String getActionId() { return "controlarpills"; }

    @Override
    public String toString() { return yo + " " + getActionId(); }
}
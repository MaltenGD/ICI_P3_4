package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.actions;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// esperar: quedarse parado en el cruce actual
public class WaitAction implements Action {

    public WaitAction(GHOST ghost) {
        // ghost no se usa, la accion es la misma para todos
    }

    @Override
    public MOVE execute(Game game) {
        return MOVE.NEUTRAL;
    }

    @Override
    public String getActionId() { return "ESPERAR"; }
}

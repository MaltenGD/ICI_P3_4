
package es.ucm.fdi.ici.c2627.practica3.grupoYY.mspacman.actions;

import es.ucm.fdi.ici.rules.RulesAction;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/** Huye de los fantasmas peligrosos usando búsqueda hacia delante. */
public class HuirAction implements RulesAction {

    @Override
    public MOVE execute(Game game) {
        int pacman = game.getPacmanCurrentNodeIndex();
        if (pacman < 0) {
            return MOVE.NEUTRAL;
        }

        boolean hayFantasmaPeligroso = false;

        for (GHOST fantasma : GHOST.values()) {
            if (game.getGhostLairTime(fantasma) > 0
                    || game.isGhostEdible(fantasma)) {
                continue;
            }

            if (game.getGhostCurrentNodeIndex(fantasma) >= 0) {
                hayFantasmaPeligroso = true;
                break;
            }
        }

        if (!hayFantasmaPeligroso) {
            return MOVE.NEUTRAL;
        }

        return LookaheadPacman.elegirMovimiento(game, null, true);
    }

    @Override
    public void parseFact(Fact actionFact) {
        // Esta acción no necesita parámetros de Jess.
    }

    @Override
    public String getActionId() {
        return "huir";
    }
}

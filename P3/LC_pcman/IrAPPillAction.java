
package es.ucm.fdi.ici.c2627.practica3.grupoYY.mspacman.actions;

import es.ucm.fdi.ici.rules.RulesAction;
import jess.Fact;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/** Busca píldoras de poder teniendo en cuenta los movimientos futuros. */
public class IrAPPillAction implements RulesAction {

    @Override
    public MOVE execute(Game game) {
        int pacman = game.getPacmanCurrentNodeIndex();
        if (pacman < 0) {
            return MOVE.NEUTRAL;
        }

        int[] objetivos = game.getActivePowerPillsIndices();

        if (objetivos == null || objetivos.length == 0) {
            return MOVE.NEUTRAL;
        }

        return LookaheadPacman.elegirMovimiento(game, objetivos, false);
    }

    @Override
    public void parseFact(Fact actionFact) {
        // Esta acción no necesita parámetros de Jess.
    }

    @Override
    public String getActionId() {
        return "irAPPill";
    }
}

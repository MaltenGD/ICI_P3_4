package es.ucm.fdi.ici.c2627.practica3.grupoYY.ghosts.actions;

import es.ucm.fdi.ici.rules.RulesAction;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/** Rol de INKY: ir a la ppill a la que llega antes que Ms. Pac-Man. */
public class GuardarPPillAction implements RulesAction {

    private final GHOST yo;

    public GuardarPPillAction(GHOST yo) { this.yo = yo; }

    @Override
    public MOVE execute(Game game) {
        return LogicaFantasma.guardarPPill(game, yo);
    }

    @Override
    public void parseFact(Fact actionFact) { }

    @Override
    public String getActionId() { return "guardarPPill"; }   // el mismo id para los cuatro: cada motor tiene su mapa
}

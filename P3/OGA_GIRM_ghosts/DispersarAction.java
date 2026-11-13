package es.ucm.fdi.ici.c2627.practica3.grupoYY.ghosts.actions;

import es.ucm.fdi.ici.rules.RulesAction;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/** Separarse de los demas fantasmas cuando el grupo esta apretado. */
public class DispersarAction implements RulesAction {

    private final GHOST yo;

    public DispersarAction(GHOST yo) { this.yo = yo; }

    @Override
    public MOVE execute(Game game) {
        return LogicaFantasma.dispersar(game, yo);
    }

    @Override
    public void parseFact(Fact actionFact) { }

    @Override
    public String getActionId() { return "dispersar"; }   // el mismo id para los cuatro: cada motor tiene su mapa
}

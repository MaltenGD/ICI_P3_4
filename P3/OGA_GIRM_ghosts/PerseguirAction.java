package es.ucm.fdi.ici.c2627.practica3.grupoYY.ghosts.actions;

import es.ucm.fdi.ici.rules.RulesAction;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/** Camino mas corto hacia Ms. Pac-Man (BLINKY, SUE y por defecto). */
public class PerseguirAction implements RulesAction {

    private final GHOST yo;

    public PerseguirAction(GHOST yo) { this.yo = yo; }

    @Override
    public MOVE execute(Game game) {
        return LogicaFantasma.perseguir(game, yo);
    }

    @Override
    public void parseFact(Fact actionFact) { }

    @Override
    public String getActionId() { return "perseguir"; }   // el mismo id para los cuatro: cada motor tiene su mapa
}

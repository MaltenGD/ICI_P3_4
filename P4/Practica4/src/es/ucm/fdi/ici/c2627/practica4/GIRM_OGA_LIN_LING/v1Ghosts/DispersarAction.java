// DispersarAction.java
package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v1Ghosts;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class DispersarAction implements Action {
    private final GHOST yo;
    public DispersarAction(GHOST yo) { this.yo = yo; }
    @Override
    public MOVE execute(Game game) { return LogicaFantasma.dispersar(game, yo); }
    @Override
    public String getActionId() { return "dispersar"; }
    @Override
    public String toString() { return yo + " " + getActionId(); }
}
// EmboscarAction.java
package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v1Ghosts;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class EmboscarAction implements Action {
    private final GHOST yo;
    public EmboscarAction(GHOST yo) { this.yo = yo; }
    @Override
    public MOVE execute(Game game) { return LogicaFantasma.emboscar(game, yo); }
    @Override
    public String getActionId() { return "emboscar"; }
    @Override
    public String toString() { return yo + " " + getActionId(); }
}
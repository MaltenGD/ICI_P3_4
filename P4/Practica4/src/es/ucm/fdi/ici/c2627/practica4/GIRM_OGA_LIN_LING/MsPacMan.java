package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING;

import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v2MsPacMan.MsPacManv2;

/**
 * Controlador de concurso de Ms. Pac-Man.
 * Por defecto carga la mejor versión desarrollada (v2).
 */
public class MsPacMan extends PacmanController {

    private final PacmanController controlador;

    public MsPacMan() {
        this(new MsPacManv2());
    }

    public MsPacMan(PacmanController version) {
        this.controlador = version;
        setName("MsPacMan GIRM_OGA_LIN_LING");
        setTeam("GIRM_OGA_LIN_LING");
    }

    public MsPacMan(String fcl) {
        this(new MsPacManv2(fcl));
    }

    @Override
    public void preCompute(String rival) {
        controlador.preCompute(rival);
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        return controlador.getMove(game, timeDue);
    }
}

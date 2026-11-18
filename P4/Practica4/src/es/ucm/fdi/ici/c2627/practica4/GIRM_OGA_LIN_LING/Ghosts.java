package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING;

import java.util.EnumMap;
import pacman.controllers.GhostController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Controlador de concurso de Fantasmas.
 * Por defecto carga la mejor versión desarrollada (v4).
 */
public class Ghosts extends GhostController {

    private final GhostController controlador;

    public Ghosts() {
        this("ghosts-v4.fcl");
    }

    public Ghosts(String fcl) {
        this.controlador = new es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.ghosts.Ghosts(fcl);
        setName("Ghosts GIRM_OGA_LIN_LING");
        setTeam("GIRM_OGA_LIN_LING");
    }

    @Override
    public void preCompute(String rival) {
        controlador.preCompute(rival);
    }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        return controlador.getMove(game, timeDue);
    }
}

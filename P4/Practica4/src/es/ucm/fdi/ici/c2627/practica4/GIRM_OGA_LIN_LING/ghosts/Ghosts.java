package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.ghosts;

import java.util.EnumMap;
import java.util.function.Function;
import es.ucm.fdi.ici.fuzzy.FuzzyEngine;
import pacman.controllers.GhostController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// elige la version para las pruebas; por defecto usa la v4
public class Ghosts extends GhostController {
    private final GhostController controlador;
    private final Function<GHOST, FuzzyEngine> motor;

    public Ghosts() { this("ghosts-v4.fcl"); }

    public Ghosts(String fcl) {
        switch (fcl) {
            case "ghosts-v0.fcl": {
                var elegido = new es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v0Ghosts.Ghosts();
                controlador = elegido;
                motor = elegido::getMotor;
                break;
            }
            case "ghosts-v1.fcl": {
                var elegido = new es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v1Ghosts.Ghosts();
                controlador = elegido;
                motor = elegido::getMotor;
                break;
            }
            case "ghosts-v2.fcl": {
                var elegido = new es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v2Ghosts.Ghosts();
                controlador = elegido;
                motor = elegido::getMotor;
                break;
            }
            case "ghosts-v3.fcl": {
                var elegido = new es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v3Ghosts.Ghosts();
                controlador = elegido;
                motor = elegido::getMotor;
                break;
            }
            case "ghosts-v4.fcl": {
                var elegido = new es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v4Ghosts.Ghosts();
                controlador = elegido;
                motor = elegido::getMotor;
                break;
            }
            default: throw new IllegalArgumentException("version desconocida: " + fcl);
        }
        setName("Ghosts GIRM_OGA_LIN_LING");
        setTeam("GIRM_OGA_LIN_LING");
    }

    @Override
    public void preCompute(String rival) { controlador.preCompute(rival); }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        return controlador.getMove(game, timeDue);
    }

    public FuzzyEngine getMotor(GHOST g) { return motor.apply(g); }
}

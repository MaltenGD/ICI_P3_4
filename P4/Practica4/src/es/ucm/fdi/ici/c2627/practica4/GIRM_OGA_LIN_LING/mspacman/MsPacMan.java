package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.mspacman;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.MaxActionSelector;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.mspacman.actions.ComerAction;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.mspacman.actions.HuirAction;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.mspacman.actions.IrAPPillAction;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.mspacman.actions.PerseguirAction;
import es.ucm.fdi.ici.fuzzy.FuzzyEngine;
import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class MsPacMan extends PacmanController {

    // Una ruta del disco, no del classpath: Eclipse copia el .fcl a bin/ y lanza desde la carpeta del proyecto
    public static final String FCL_PATH = "bin/es/ucm/fdi/ici/c2627/practica4/GIRM_OGA_LIN_LING/mspacman/";

    private final FuzzyEngine motor;

    public MsPacMan() {
        this("mspacman-v0.fcl");                 // la versiÃ³n que va al concurso
    }

    /** Cada versiÃ³n, un .fcl distinto, con el mismo Input y las mismas acciones. */
    public MsPacMan(String ficheroFcl) {
        setName("MsPacMan GIRM_OGA_LIN_LING");
        setTeam("GIRM_OGA_LIN_LING");
        Action[] acciones = { new ComerAction(), new HuirAction(), new IrAPPillAction(), new PerseguirAction() };
        motor = new FuzzyEngine("MSPACMAN", FCL_PATH + ficheroFcl, "MsPacMan", new MaxActionSelector(acciones));
    }

    /** Para engancharle un observador desde fuera. */
    public FuzzyEngine getMotor() { return motor; }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        return motor.run(new MsPacManInput(game).getFuzzyValues(), game);
    }
}

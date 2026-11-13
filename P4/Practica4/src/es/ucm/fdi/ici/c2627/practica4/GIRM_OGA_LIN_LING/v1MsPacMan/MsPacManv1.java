package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v1MsPacMan;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.MaxActionSelector;
import es.ucm.fdi.ici.fuzzy.FuzzyEngine;
import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class MsPacManv1 extends PacmanController {

    // Una ruta del disco, no del classpath: Eclipse copia el .fcl a bin/ y lanza desde la carpeta del proyecto
    static final String FCL_PATH = "bin/es/ucm/fdi/ici/c2627/practica4/GIRM_OGA_LIN_LING/v1MsPacMan/";

    private final FuzzyEngine motor;

    public MsPacManv1() {
        this("mspacman-v1.fcl");                 // la versiÃ³n que va al concurso
    }

    /** Cada versiÃ³n, un .fcl distinto, con el mismo Input y las mismas acciones. */
    public MsPacManv1(String ficheroFcl) {
        setName("MsPacMan GIRM_OGA_LIN_LING");
        setTeam("GIRM_OGA_LIN_LING");
        Action[] acciones = { new ComerAction(), new HuirAction(), new IrAPPillAction(), new PerseguirAction() };
        motor = new FuzzyEngine("MSPACMAN", FCL_PATH + ficheroFcl, "MsPacMan", new MaxActionSelector(acciones));
    }

    /** Para engancharle un observador desde fuera. */
    public FuzzyEngine getMotor() { return motor; }


@Override
public MOVE getMove(Game game, long timeDue) {

    MsPacManInputV1 input = new MsPacManInputV1(game);

    java.util.HashMap<String, Double> valores =
        input.getFuzzyValues();

    for (java.util.Map.Entry<String, Double> e : valores.entrySet()) {

        if (e.getValue() == null ||
            !Double.isFinite(e.getValue())) {

            System.err.println(
                "ENTRADA INCORRECTA: " +
                e.getKey() + " = " + e.getValue()
            );
        }
    }

    return motor.run(valores, game);
}

}

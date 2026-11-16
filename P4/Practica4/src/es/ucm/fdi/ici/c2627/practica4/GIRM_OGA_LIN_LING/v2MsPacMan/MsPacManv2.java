package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v2MsPacMan;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.MaxActionSelector;
import es.ucm.fdi.ici.fuzzy.FuzzyEngine;
import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class MsPacManv2 extends PacmanController {

    static final String FCL_PATH = "bin/es/ucm/fdi/ici/c2627/practica4/GIRM_OGA_LIN_LING/v2MsPacMan/";

    private final FuzzyEngine motor;

    public MsPacManv2() {
        this("mspacman-v2.fcl");
    }

    public MsPacManv2(String ficheroFcl) {
        setName("MsPacMan GIRM_OGA_LIN_LING");
        setTeam("GIRM_OGA_LIN_LING");
        Action[] acciones = { new ComerAction(), new HuirAction(), new IrAPPillAction(), new PerseguirAction() };
        motor = new FuzzyEngine("MSPACMAN", FCL_PATH + ficheroFcl, "MsPacMan", new MaxActionSelector(acciones));
    }

    public FuzzyEngine getMotor() { return motor; }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        MsPacManInputV2 input = new MsPacManInputV2(game);
        return motor.run(input.getFuzzyValues(), game);
    }
}

package es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.ejemplo;

import java.util.HashMap;

import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.ejemplo.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.ejemplo.mspacman.actions.ComerAction;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.ejemplo.mspacman.actions.HuirAction;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.ejemplo.mspacman.actions.IrAPPillAction;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.ejemplo.mspacman.actions.PerseguirAction;
import es.ucm.fdi.ici.rules.RuleEngine;
import es.ucm.fdi.ici.rules.RulesAction;
import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class MsPacManEjemplo extends PacmanController {

    private static final String RULES_PATH = "es/ucm/fdi/ici/c2627/practica3/GIRM_OGA_LIN_LING/";

    private final RuleEngine motor;
    private final UltimaAccion ultima = new UltimaAccion();

    public MsPacManEjemplo() {
        this("mspacman-v1.clp");                 // la versión que va al concurso
    }

    /** Cada versión, un .clp distinto, con el mismo Input y las mismas acciones. */
    public MsPacManEjemplo(String ficheroReglas) {
        setName("Pacman YY ejemplo");
        setTeam("Grupo YY");

        HashMap<String, RulesAction> acciones = new HashMap<>();
        for (RulesAction a : new RulesAction[] {
                new ComerAction(), new HuirAction(),
                new IrAPPillAction(), new PerseguirAction() })
            acciones.put(a.getActionId(), a);

        motor = new RuleEngine("MSPACMAN", RULES_PATH + ficheroReglas, acciones);
        motor.addObserver(ultima);
    }

    /** Para engancharle la TrazaReglas desde fuera. */
    public RuleEngine getMotor() { return motor; }

    @Override
    public void preCompute(String rival) {
        ultima.olvidar();
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        MsPacManInput input = new MsPacManInput(game, ultima.get());
        motor.reset();
        motor.assertFacts(input.getFacts());
        return motor.run(game);
    }
}

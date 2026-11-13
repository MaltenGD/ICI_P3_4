package es.ucm.fdi.ici.c2627.practica3.grupoYY;

import java.util.HashMap;

import es.ucm.fdi.ici.c2627.practica3.grupoYY.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica3.grupoYY.mspacman.actions.PerseguirAction;
import es.ucm.fdi.ici.c2627.practica3.grupoYY.mspacman.actions.ComerAction;
import es.ucm.fdi.ici.c2627.practica3.grupoYY.mspacman.actions.HuirAction;
import es.ucm.fdi.ici.c2627.practica3.grupoYY.mspacman.actions.IrAPPillAction;
import es.ucm.fdi.ici.rules.RuleEngine;
import es.ucm.fdi.ici.rules.RulesAction;
import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class MsPacMan extends PacmanController {

    private static final String RULES_PATH =
        "es/ucm/fdi/ici/c2627/practica3/grupoYY/";

    private final RuleEngine motor;
    private final UltimaAccion ultima = new UltimaAccion();

    public MsPacMan() { this("mspacman-v1.clp"); }

    public MsPacMan(String ficheroReglas) {
        setName("MsPacMan GIRM_LC");
        setTeam("GIRM_LC");

        HashMap<String, RulesAction> acciones = new HashMap<>();
        for (RulesAction a : new RulesAction[] {
                new HuirAction(),
                new PerseguirAction(),
                new IrAPPillAction(),
                new ComerAction()})
            acciones.put(a.getActionId(), a);

        motor = new RuleEngine("MSPACMAN", RULES_PATH + ficheroReglas, acciones);
        motor.addObserver(ultima);
    }

    public RuleEngine getMotor() { return motor; }

    @Override
    public void preCompute(String rival) { ultima.olvidar(); }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        MsPacManInput input = new MsPacManInput(game, ultima.get());
        motor.reset();
        motor.assertFacts(input.getFacts());
        return motor.run(game);
    }
}
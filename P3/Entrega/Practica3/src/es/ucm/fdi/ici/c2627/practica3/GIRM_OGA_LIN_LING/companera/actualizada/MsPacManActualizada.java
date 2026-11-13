package es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.companera.actualizada;

import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.ejemplo.UltimaAccion;

import java.util.HashMap;

import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.companera.actualizada.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.companera.actualizada.actions.PerseguirAction;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.companera.actualizada.actions.ComerAction;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.companera.actualizada.actions.HuirAction;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.companera.actualizada.actions.IrAPPillAction;
import es.ucm.fdi.ici.rules.RuleEngine;
import es.ucm.fdi.ici.rules.RulesAction;
import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class MsPacManActualizada extends PacmanController {

    private static final String RULES_PATH =
        "es/ucm/fdi/ici/c2627/practica3/GIRM_OGA_LIN_LING/";

    private final RuleEngine motor;
    private final UltimaAccion ultima = new UltimaAccion();

    public MsPacManActualizada() { this("mspacman-v3.3.clp"); }

    public MsPacManActualizada(String ficheroReglas) {
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
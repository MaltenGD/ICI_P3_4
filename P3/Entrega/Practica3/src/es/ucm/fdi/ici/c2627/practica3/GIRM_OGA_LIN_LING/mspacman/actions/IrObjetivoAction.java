package es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.mspacman.actions;

import es.ucm.fdi.ici.rules.RulesAction;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.mspacman.Caminos;
import jess.Fact;
import jess.JessException;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// varias intenciones pueden usar el mismo movimiento hacia un nodo elegido por las reglas
public class IrObjetivoAction implements RulesAction {
    private final String id;
    private int nodo = -1;
    private MOVE movimiento = MOVE.NEUTRAL;

    public IrObjetivoAction(String id) { this.id = id; }

    @Override public String getActionId() { return id; }

    @Override
    public void parseFact(Fact actionFact) {
        nodo = -1; movimiento = MOVE.NEUTRAL;
        try {
            nodo = actionFact.getSlotValue("nodo").intValue(null);
            movimiento = MOVE.valueOf(actionFact.getSlotValue("movimiento").stringValue(null));
        } catch (JessException | IllegalArgumentException e) {
            // un parametro ausente no debe dejar el objetivo del turno anterior
            nodo = -1; movimiento = MOVE.NEUTRAL;
        }
    }

    @Override
    public MOVE execute(Game game) {
        if (Caminos.legal(game, movimiento)) return movimiento;
        return nodo < 0 ? Caminos.continuar(game) : Caminos.hacia(game, nodo);
    }
}

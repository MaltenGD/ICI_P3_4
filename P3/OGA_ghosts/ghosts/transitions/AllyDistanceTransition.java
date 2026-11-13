package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;

// evalua si un aliado esta demasiado cerca o lejos para evitar solapamientos
public class AllyDistanceTransition implements Transition {

    private final int threshold;
    private final boolean lessThan;

    public AllyDistanceTransition(int threshold, boolean lessThan) {
        this.threshold = threshold;
        this.lessThan = lessThan;
    }

    @Override
    public boolean evaluate(Input in) {
        GhostsInput input = (GhostsInput) in;
        if (input.minAllyDist < 0) return false;
        if (lessThan) {
            return input.minAllyDist < threshold;
        } else {
            return input.minAllyDist > threshold;
        }
    }

    @Override
    public String toString() {
        return "MinAllyDist" + (lessThan ? "<" : ">") + threshold;
    }
}

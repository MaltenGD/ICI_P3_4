package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;

// evalua si el tiempo de comestible restante es bajo (ej. < 30 ticks)
public class EdibleTimeLowTransition implements Transition {

    private final int threshold;

    public EdibleTimeLowTransition(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean evaluate(Input in) {
        GhostsInput input = (GhostsInput) in;
        return input.edibleTime > 0 && input.edibleTime < threshold;
    }

    @Override
    public String toString() {
        return "EdibleTime<" + threshold;
    }
}

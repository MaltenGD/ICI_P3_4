package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;

// evalua si el cruce al que va pacman esta a cierta distancia del fantasma
public class TargetJunctionDistanceTransition implements Transition {

    private final int threshold;
    private final boolean lessThan;

    public TargetJunctionDistanceTransition(int threshold, boolean lessThan) {
        this.threshold = threshold;
        this.lessThan = lessThan;
    }

    @Override
    public boolean evaluate(Input in) {
        GhostsInput input = (GhostsInput) in;
        if (input.distToTargetJunction < 0) return false;
        if (lessThan) {
            return input.distToTargetJunction < threshold;
        } else {
            return input.distToTargetJunction > threshold;
        }
    }

    @Override
    public String toString() {
        return "DistTargetJunction" + (lessThan ? "<" : ">") + threshold;
    }
}

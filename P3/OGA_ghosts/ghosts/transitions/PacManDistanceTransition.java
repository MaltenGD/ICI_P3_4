package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;

// transicion con histeresis basada en la distancia a pacman
public class PacManDistanceTransition implements Transition {

    private final int threshold;
    private final boolean lessThan;

    public PacManDistanceTransition(int threshold, boolean lessThan) {
        this.threshold = threshold;
        this.lessThan = lessThan;
    }

    @Override
    public boolean evaluate(Input in) {
        GhostsInput input = (GhostsInput) in;
        if (input.distToPacman < 0) return false;
        if (lessThan) {
            return input.distToPacman < threshold;
        } else {
            return input.distToPacman > threshold;
        }
    }

    @Override
    public String toString() {
        return "DistPacman" + (lessThan ? "<" : ">") + threshold;
    }
}

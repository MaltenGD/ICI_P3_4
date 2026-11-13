package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;

// se activa cuando el fantasma actual se vuelve comestible (tiempo > 0)
public class GhostEdibleTransition implements Transition {

    @Override
    public boolean evaluate(Input in) {
        GhostsInput input = (GhostsInput) in;
        return input.edibleTime > 0;
    }

    @Override
    public String toString() {
        return "Edible";
    }
}

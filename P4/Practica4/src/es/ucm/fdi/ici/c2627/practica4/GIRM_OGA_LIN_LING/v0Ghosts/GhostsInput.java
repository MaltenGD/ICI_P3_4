package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v0Ghosts;

import java.util.HashMap;
import es.ucm.fdi.ici.fuzzy.FuzzyInput;
import pacman.game.Constants.GHOST;
import pacman.game.Game;

public class GhostsInput extends FuzzyInput {

    public static final double LEJOS = 150;

    private final GHOST yo;
    private double comestible, enCarcel, distPacman, distOtro, restantes, ventajaPPill, soy;

    public GhostsInput(Game game, GHOST yo) {
        super(game);
        this.yo = yo;
        parseInput(); 
    }

    @Override
    public void parseInput() {
        if (yo == null) return;

        //Variables basicas
        int pacman = game.getPacmanCurrentNodeIndex();
        int nodoYo = game.getGhostCurrentNodeIndex(yo);

        enCarcel = (game.getGhostLairTime(yo) > 0) ? 1 : 0;
        comestible = game.getGhostEdibleTime(yo) > 0 ? 1 : 0;

        //Distancias
        distPacman = enCarcel == 1 ? LEJOS
                : Math.min(LEJOS, game.getShortestPathDistance(nodoYo, pacman));

        distOtro = LEJOS;
        for (GHOST otro : GHOST.values()) {
            if (otro == yo || game.getGhostLairTime(otro) > 0) continue;
            int nodoOtro = game.getGhostCurrentNodeIndex(otro);
            if (nodoOtro < 0) continue;
            double d = game.getShortestPathDistance(nodoYo, nodoOtro);
            if (d >= 0) distOtro = Math.min(distOtro, d);
        }

        //Pildoras normales restantes
        restantes = game.getNumberOfActivePills();

        //Ventaja sobre la pildora de poder mas cercana a Pac-Man
        ventajaPPill = LEJOS;
        int[] ppills = game.getActivePowerPillsIndices();
        if (ppills.length > 0) {
            int mejorPill = -1;
            double mejorDistPacman = LEJOS;
            for (int p : ppills) {
                double dPacman = game.getShortestPathDistance(pacman, p);
                if (dPacman >= 0 && dPacman < mejorDistPacman) {
                    mejorDistPacman = dPacman;
                    mejorPill = p;
                }
            }
            if (mejorPill != -1) {
                double dYo = game.getShortestPathDistance(nodoYo, mejorPill);
                if (dYo >= 0) ventajaPPill = mejorDistPacman - dYo;
            }
        }

        //Identidad del fantasma
        switch (yo) {
            case PINKY:  soy = 0; break;
            case INKY:   soy = 1; break;
            case BLINKY: soy = 2; break;
            case SUE:    soy = 3; break;
            default:     soy = 0; break;
        }
    }

    @Override
    public HashMap<String, Double> getFuzzyValues() {
        HashMap<String, Double> vars = new HashMap<>();
        vars.put("comestible", comestible);
        vars.put("enCarcel", enCarcel);
        vars.put("distPacman", distPacman);
        vars.put("distOtro", distOtro);
        vars.put("restantes", restantes);
        vars.put("ventajaPPill", ventajaPPill);
        vars.put("soy", soy);
        return vars;
    }
}
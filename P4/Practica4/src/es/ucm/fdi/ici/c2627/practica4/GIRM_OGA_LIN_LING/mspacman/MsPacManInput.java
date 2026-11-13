package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.mspacman;

import java.util.HashMap;

import es.ucm.fdi.ici.fuzzy.FuzzyInput;
import pacman.game.Constants.GHOST;
import pacman.game.Game;

/**
 * Entradas borrosas que portan la información de la Práctica 3:
 * - peligro: distancia al fantasma peligroso más cercano
 * - comestible: distancia al fantasma comestible más cercano si da tiempo a cazarlo (tiempo > 2*dist + 2); si no da tiempo o no hay, LEJOS
 * - ppill: distancia a la píldora de poder más cercana
 * - ventajaPPill: (distanciaPeligro - distanciaPPill). Si es positiva, Pac-Man llega antes a la píldora que el fantasma a ella
 */
public class MsPacManInput extends FuzzyInput {

    public static final double LEJOS = 150;

    private double peligro, comestible, ppill, ventajaPPill;

    public MsPacManInput(Game game) {
        super(game);                               // llama a parseInput()
    }

    @Override
    public void parseInput() {
        int yo = game.getPacmanCurrentNodeIndex();
        peligro = LEJOS;
        comestible = LEJOS;
        for (GHOST g : GHOST.values()) {
            if (game.getGhostLairTime(g) > 0) continue;
            int nodoFantasma = game.getGhostCurrentNodeIndex(g);
            double d = game.getShortestPathDistance(yo, nodoFantasma);
            if (d < 0) continue;

            if (game.isGhostEdible(g)) {
                int tiempo = game.getGhostEdibleTime(g);
                // Criterio de P3: solo consideramos comestible alcanzable si da tiempo
                if (tiempo > (2 * d + 2)) {
                    comestible = Math.min(comestible, d);
                }
            } else {
                peligro = Math.min(peligro, d);
            }
        }

        ppill = LEJOS;
        for (int p : game.getActivePowerPillsIndices()) {
            double d = game.getShortestPathDistance(yo, p);
            if (d >= 0) ppill = Math.min(ppill, d);
        }

        // Ventaja de Pac-Man hacia la PPill frente al fantasma peligroso (distPeligro - distPPill)
        // Rango aproximado: de -150 a +150
        ventajaPPill = (peligro < LEJOS && ppill < LEJOS) ? (peligro - ppill) : -LEJOS;
    }

    @Override
    public HashMap<String, Double> getFuzzyValues() {
        HashMap<String, Double> vars = new HashMap<>();
        vars.put("peligro", peligro);
        vars.put("comestible", comestible);
        vars.put("ppill", ppill);
        vars.put("ventajaPPill", ventajaPPill);
        return vars;
    }
}
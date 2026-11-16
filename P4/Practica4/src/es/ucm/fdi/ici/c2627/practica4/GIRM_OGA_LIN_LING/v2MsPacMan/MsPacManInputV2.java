package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v2MsPacMan;

import java.util.HashMap;

import es.ucm.fdi.ici.fuzzy.FuzzyInput;
import pacman.game.Constants.GHOST;
import pacman.game.Game;

public class MsPacManInputV2 extends FuzzyInput {

    public static final double LEJOS = 150;
    private static final double RADIO_PERSEGUIDORES = 35;

    private double peligro;
    private double comestible;
    private double ppill;
    private double ventajaPPill;
    private double perseguidoresCerca;
    private double comidaRestante;

    public MsPacManInputV2(Game game) {
        super(game);
    }

    @Override
    public void parseInput() {
        int yo = game.getPacmanCurrentNodeIndex();

        peligro = LEJOS;
        comestible = LEJOS;
        perseguidoresCerca = 0;

        // 1. Distancias a fantasmas y conteo de perseguidores
        for (GHOST g : GHOST.values()) {
            if (game.getGhostLairTime(g) > 0) continue;

            int nodoFantasma = game.getGhostCurrentNodeIndex(g);
            double d = game.getShortestPathDistance(yo, nodoFantasma);
            if (d < 0) continue;

            if (game.isGhostEdible(g)) {
                int tiempo = game.getGhostEdibleTime(g);
                // Criterio estricto de P3: solo cazar si da tiempo de sobra
                if (tiempo > (2 * d + 2)) {
                    comestible = Math.min(comestible, d);
                }
            } else {
                peligro = Math.min(peligro, d);
                if (d < RADIO_PERSEGUIDORES) {
                    perseguidoresCerca++;
                }
            }
        }

        // 2. Power Pills y ventaja
        int[] powerPills = game.getActivePowerPillsIndices();
        ppill = LEJOS;
        for (int p : powerPills) {
            double d = game.getShortestPathDistance(yo, p);
            if (d >= 0) {
                ppill = Math.min(ppill, d);
            }
        }

        if (peligro < LEJOS && ppill < LEJOS) {
            ventajaPPill = peligro - ppill;
        } else {
            ventajaPPill = -LEJOS;
        }

        // 3. Comida normal restante en el laberinto (0 a 220)
        comidaRestante = game.getActivePillsIndices().length;
    }

    @Override
    public HashMap<String, Double> getFuzzyValues() {
        HashMap<String, Double> vars = new HashMap<>();
        vars.put("peligro", peligro);
        vars.put("comestible", comestible);
        vars.put("ppill", ppill);
        vars.put("ventajaPPill", ventajaPPill);
        vars.put("perseguidoresCerca", perseguidoresCerca);
        vars.put("comidaRestante", comidaRestante);
        return vars;
    }
}

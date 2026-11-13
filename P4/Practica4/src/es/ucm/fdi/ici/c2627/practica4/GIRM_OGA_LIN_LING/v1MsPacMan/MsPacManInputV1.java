
package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v1MsPacMan;

import java.util.HashMap;

import es.ucm.fdi.ici.fuzzy.FuzzyInput;
import pacman.game.Constants.GHOST;
import pacman.game.Game;

public class MsPacManInputV1 extends FuzzyInput {

    public static final double LEJOS = 150;

    private static final double RADIO_PERSEGUIDORES = 35;

    private double peligro;
    private double comestible;
    private double ppill;
    private double ventajaPPill;

    private double perseguidoresCerca;
    private double ppillsRestantes;


    public MsPacManInputV1(Game game) {
        super(game);
    }



@Override
public void parseInput() {

    int yo = game.getPacmanCurrentNodeIndex();

    peligro = LEJOS;
    comestible = LEJOS;
    perseguidoresCerca = 0;

    // 1. DISTANCIAS A LOS FANTASMAS
    for (GHOST g : GHOST.values()) {

        if (game.getGhostLairTime(g) > 0) continue;

        int nodoFantasma = game.getGhostCurrentNodeIndex(g);

        double d = game.getShortestPathDistance(yo, nodoFantasma);

        if (d < 0) continue;

        if (game.isGhostEdible(g)) {

            int tiempo = game.getGhostEdibleTime(g);

            // Solo perseguir si da tiempo a alcanzarlo.
            if (tiempo > (2 * d + 2)) {
                comestible = Math.min(comestible, d);
            }

        } else {

            // Fantasma peligroso.
            peligro = Math.min(peligro, d);

            // Contar perseguidores cercanos.
            if (d < RADIO_PERSEGUIDORES) {
                perseguidoresCerca++;
            }
        }
    }

    // 2. POWER PILLS
    int[] powerPills = game.getActivePowerPillsIndices();

    ppillsRestantes = powerPills.length;
    ppill = LEJOS;

    for (int p : powerPills) {

        double d = game.getShortestPathDistance(yo, p);

        if (d >= 0) {
            ppill = Math.min(ppill, d);
        }
    }

    // 3. VENTAJA POWER PILL
    if (peligro < LEJOS && ppill < LEJOS) {
        ventajaPPill = peligro - ppill;
    } else {
        ventajaPPill = -LEJOS;
    }
}


    @Override
    public HashMap<String, Double> getFuzzyValues() {

        HashMap<String, Double> vars = new HashMap<>();

        vars.put("peligro", peligro);
        vars.put("comestible", comestible);
        vars.put("ppill", ppill);
        vars.put("ventajaPPill", ventajaPPill);

        vars.put("perseguidoresCerca", perseguidoresCerca);
        vars.put("ppillsRestantes", ppillsRestantes);

        return vars;
    }
}

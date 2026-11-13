package es.ucm.fdi.ici.c2627.practica3.grupoYY.ghosts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.ucm.fdi.ici.c2627.practica3.grupoYY.ghosts.actions.LogicaFantasma;
import es.ucm.fdi.ici.rules.RulesInput;
import pacman.game.Constants.GHOST;
import pacman.game.Game;

public class GhostsInput extends RulesInput {

    private static final int LEJOS = LogicaFantasma.LEJOS;       // "no hay"
    private static final int SIN_VENTAJA = -1000;                // "no hay ppill alcanzable"

    // Sin inicializar aqui: parseInput() se ejecuta desde el constructor de la superclase
    private List<String> facts;

    public GhostsInput(Game game) { super(game); }

    @Override
    public void parseInput() {
        facts = new ArrayList<>();
        int pacman = game.getPacmanCurrentNodeIndex();

        for (GHOST g : GHOST.values()) {
            int nodo = game.getGhostCurrentNodeIndex(g);
            boolean enCarcel = game.getGhostLairTime(g) > 0 || nodo == -1;

            int distPacman = LEJOS, distOtro = LEJOS, ventaja = SIN_VENTAJA;
            if (!enCarcel) {
                // lo que tarda ESTE fantasma en llegar a ella (no puede dar la vuelta)
                distPacman = game.getShortestPathDistance(nodo, pacman, game.getGhostLastMoveMade(g));
                if (distPacman < 0) distPacman = LEJOS;
                distOtro = LogicaFantasma.distanciaOtro(game, g);
                int[] mejor = LogicaFantasma.mejorPPill(game, g);
                if (mejor != null) ventaja = mejor[1];
            }
            facts.add(String.format(
                    "(FANTASMA (nombre %s) (comestible %s) (enCarcel %s) (distPacman %d) (distOtro %d) (ventajaPPill %d))",
                    g, game.isGhostEdible(g), enCarcel, distPacman, distOtro, ventaja));
        }

        facts.add(String.format("(GRUPO (apretado %s))", LogicaFantasma.necesitanDispersarse(game)));

        int distPPill = LEJOS;
        for (int ppill : game.getActivePowerPillsIndices()) {
            int d = game.getShortestPathDistance(pacman, ppill);
            if (d >= 0) distPPill = Math.min(distPPill, d);
        }
        facts.add(String.format("(MSPACMAN (distPPill %d))", distPPill));
    }

    @Override
    public Collection<String> getFacts() { return facts; }
}

package es.ucm.fdi.ici.c2627.practica3.grupoYY.mspacman.actions;

import es.ucm.fdi.ici.rules.RulesAction;
import jess.Fact;
import jess.JessException;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class PerseguirAction implements RulesAction {

    private GHOST presa = null;

    @Override
    public void parseFact(Fact actionFact) {
        presa = null;
        if (actionFact == null) return;
        try {
            String s = actionFact.getSlotValue("fantasma").stringValue(null);
            if (s != null && !s.isEmpty()) {
                presa = GHOST.valueOf(s.trim().toUpperCase());
            }
        } catch (JessException | IllegalArgumentException e) {
            presa = null;
        }
    }

    @Override
    public MOVE execute(Game game) {
        int pacman = game.getPacmanCurrentNodeIndex();
        if (pacman < 0) return MOVE.NEUTRAL;

        MOVE last = game.getPacmanLastMoveMade();

        // 1) Si la regla dio un fantasma, usarlo solo si sigue siendo válido.
        GHOST objetivo = null;
        if (presa != null
                && game.getGhostLairTime(presa) <= 0
                && game.isGhostEdible(presa)) {
            objetivo = presa;
        }

        // 2) Fallback: el comestible realmente más cercano desde PacMan
        //    (respetando que PacMan no puede dar media vuelta)
        if (objetivo == null) {
            int bestD = Integer.MAX_VALUE;
            for (GHOST g : GHOST.values()) {
                if (game.getGhostLairTime(g) > 0) continue;
                if (!game.isGhostEdible(g)) continue;
                int gn = game.getGhostCurrentNodeIndex(g);
                if (gn < 0) continue;

                int d = game.getShortestPathDistance(pacman, gn, last);
                if (d < 0) continue;

                // No ir a por él si se le acaba el tiempo antes de llegar
                int t = game.getGhostEdibleTime(g);
                if (t < d) continue;

                if (d < bestD) { bestD = d; objetivo = g; }
            }
        }

        if (objetivo == null) return MOVE.NEUTRAL;

        int gn = game.getGhostCurrentNodeIndex(objetivo);
        if (gn < 0) return MOVE.NEUTRAL;

        return game.getNextMoveTowardsTarget(pacman, gn, last, DM.PATH);
    }

    @Override
    public String getActionId() { return "perseguir"; }
}
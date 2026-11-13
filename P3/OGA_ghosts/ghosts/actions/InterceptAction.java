package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.actions;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// interceptar: ir al cruce al que se dirige pacman
public class InterceptAction implements Action {

    private final GHOST ghost;

    public InterceptAction(GHOST ghost) {
        this.ghost = ghost;
    }

    @Override
    public MOVE execute(Game game) {
        int ghostNode  = game.getGhostCurrentNodeIndex(ghost);
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        if (ghostNode < 0 || pacmanNode < 0) return MOVE.NEUTRAL;

        int target = calcPacmanTargetJunction(game, pacmanNode);
        if (target < 0) target = pacmanNode;

        return game.getNextMoveTowardsTarget(ghostNode, target,
                game.getGhostLastMoveMade(ghost), DM.PATH);
    }

    // buscar el primer cruce en la direccion de pacman (max 15 pasos)
    private int calcPacmanTargetJunction(Game game, int pacmanNode) {
        MOVE dir = game.getPacmanLastMoveMade();
        int node = pacmanNode;
        for (int i = 0; i < 15; i++) {
            int next = game.getNeighbour(node, dir);
            if (next < 0) break;
            if (game.isJunction(next)) return next;
            node = next;
        }
        return node;
    }

    @Override
    public String getActionId() { return "INTERCEPTAR"; }
}

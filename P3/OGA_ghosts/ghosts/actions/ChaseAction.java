package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.actions;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// perseguir: ir al nodo de pacman por el camino mas corto
public class ChaseAction implements Action {

    private final GHOST ghost;

    public ChaseAction(GHOST ghost) {
        this.ghost = ghost;
    }

    @Override
    public MOVE execute(Game game) {
        int ghostNode  = game.getGhostCurrentNodeIndex(ghost);
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        if (ghostNode < 0 || pacmanNode < 0) return MOVE.NEUTRAL;
        return game.getNextMoveTowardsTarget(ghostNode, pacmanNode,
                game.getGhostLastMoveMade(ghost), DM.PATH);
    }

    @Override
    public String getActionId() { return "PERSEGUIR"; }
}

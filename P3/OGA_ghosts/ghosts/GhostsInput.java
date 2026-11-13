package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts;

import es.ucm.fdi.ici.Input;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// variables precalculadas para que transiciones y acciones no repitan calculos
public class GhostsInput extends Input {

    public GHOST ghost;
    public int pacmanNode;
    public int ghostNode;
    public int distToPacman;
    public int edibleTime;
    public int pacmanTargetJunction; // cruce al que se dirige pacman
    public int distToTargetJunction;
    public int minAllyDist;          // distancia al fantasma aliado mas cercano
    public MOVE pacmanLastMove;

    public GhostsInput(Game game) {
        super(game); // llama parseInput() con ghost=null, no hace nada
    }

    @Override
    public void parseInput() {
        // se llama desde super() antes de que ghost este asignado, ignorar
        if (ghost == null) return;
        doParseInput();
    }

    // llamar esto desde getMove() para cada fantasma antes de run()
    public void parseFor(GHOST g) {
        this.ghost = g;
        doParseInput();
    }

    private void doParseInput() {
        pacmanNode    = game.getPacmanCurrentNodeIndex();
        pacmanLastMove = game.getPacmanLastMoveMade();
        ghostNode     = game.getGhostCurrentNodeIndex(ghost);
        if (ghostNode < 0) return;

        distToPacman = game.getShortestPathDistance(ghostNode, pacmanNode);
        edibleTime   = game.getGhostEdibleTime(ghost);

        pacmanTargetJunction = calcPacmanTargetJunction();
        distToTargetJunction = (pacmanTargetJunction >= 0)
            ? game.getShortestPathDistance(ghostNode, pacmanTargetJunction)
            : Integer.MAX_VALUE;

        // distancia al aliado mas cercano
        minAllyDist = Integer.MAX_VALUE;
        for (GHOST other : GHOST.values()) {
            if (other == ghost) continue;
            int otherNode = game.getGhostCurrentNodeIndex(other);
            if (otherNode < 0) continue;
            int d = game.getShortestPathDistance(ghostNode, otherNode);
            if (d < minAllyDist) minAllyDist = d;
        }
    }

    // caminar desde pacman en su direccion hasta encontrar un cruce (max 15 nodos)
    private int calcPacmanTargetJunction() {
        if (pacmanNode < 0) return -1;
        int node = pacmanNode;
        MOVE dir = pacmanLastMove;
        for (int i = 0; i < 15; i++) {
            int next = game.getNeighbour(node, dir);
            if (next < 0) break;
            if (game.isJunction(next)) return next;
            node = next;
        }
        return node;
    }
}

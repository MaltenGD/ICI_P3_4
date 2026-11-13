package es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.mspacman;

import java.util.*;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// aqui solo hay medidas de caminos; la decision entre comer, huir y cazar va en clips
public final class Caminos {
    public static final int LEJOS = 10000;
    private Caminos() { }

    public static MOVE ultimo(Game game) {
        MOVE m = game.getPacmanLastMoveMade();
        return m == null ? MOVE.NEUTRAL : m;
    }

    public static boolean fuera(Game game, GHOST g) {
        return game.getGhostLairTime(g) == 0 && game.getGhostCurrentNodeIndex(g) >= 0;
    }

    public static int distancia(Game game, int a, int b, MOVE entrada) {
        if (a < 0 || b < 0) return LEJOS;
        if (a == b) return 0;
        int d = game.getShortestPathDistance(a, b, entrada == null ? MOVE.NEUTRAL : entrada);
        return d < 0 ? LEJOS : d;
    }

    public static int[] ruta(Game game, int destino) {
        int origen = game.getPacmanCurrentNodeIndex();
        if (origen < 0 || destino < 0 || origen == destino) return new int[0];
        int[] ruta = game.getShortestPath(origen, destino, ultimo(game));
        if (ruta == null || ruta.length == 0 || ruta[ruta.length - 1] != destino) return new int[0];
        return ruta[0] == origen ? Arrays.copyOfRange(ruta, 1, ruta.length) : ruta;
    }

    public static MOVE hacia(Game game, int destino) {
        int[] ruta = ruta(game, destino);
        if (ruta.length > 0) {
            for (MOVE m : game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), ultimo(game)))
                if (game.getNeighbour(game.getPacmanCurrentNodeIndex(), m) == ruta[0]) return m;
        }
        return continuar(game);
    }

    public static MOVE continuar(Game game) {
        MOVE[] salidas = game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), ultimo(game));
        for (MOVE m : salidas) if (m == ultimo(game)) return m;
        return salidas.length == 0 ? MOVE.NEUTRAL : salidas[0];
    }

    public static boolean legal(Game game, MOVE movimiento) {
        for (MOVE m : game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), ultimo(game)))
            if (m == movimiento) return true;
        return false;
    }

    public static int[] pasillo(Game game, MOVE salida) {
        List<Integer> nodos = new ArrayList<Integer>();
        int nodo = game.getNeighbour(game.getPacmanCurrentNodeIndex(), salida);
        MOVE entrada = salida;
        // limite de prediccion: evita recorrer un circuito completo sin cruces
        while (nodo >= 0 && nodos.size() < 40) {
            nodos.add(nodo);
            MOVE[] posibles = game.getPossibleMoves(nodo, entrada);
            if (game.isJunction(nodo) || posibles.length != 1) break;
            entrada = posibles[0]; nodo = game.getNeighbour(nodo, entrada);
        }
        int[] ruta = new int[nodos.size()];
        for (int i = 0; i < ruta.length; i++) ruta[i] = nodos.get(i);
        return ruta;
    }

    public static int margen(Game game, int[] ruta) {
        int minimo = LEJOS;
        // solo predecimos cuarenta pasos; no es una simulacion de los fantasmas
        for (int i = 0; i < Math.min(40, ruta.length); i++) {
            for (GHOST g : GHOST.values()) {
                if (!fuera(game, g)) continue;
                int origen = game.getGhostCurrentNodeIndex(g);
                int cota = game.getShortestPathDistance(origen, ruta[i]);
                if (cota < 0) continue;
                int minimoPosible = Math.max(game.getGhostEdibleTime(g), Math.max(0, cota - Constants.EAT_DISTANCE));
                if (minimoPosible - i - 1 > 3) {
                    minimo = Math.min(minimo, minimoPosible - i - 1);
                    continue;
                }
                int d = distancia(game, origen, ruta[i], game.getGhostLastMoveMade(g));
                int llegada = Math.max(game.getGhostEdibleTime(g), Math.max(0, d - Constants.EAT_DISTANCE));
                minimo = Math.min(minimo, llegada - i - 1);
            }
        }
        return minimo;
    }

    public static int siguienteCruce(Game game, int nodo, MOVE entrada) {
        for (int i = 0; i < 40; i++) {
            MOVE[] salidas = game.getPossibleMoves(nodo, entrada == null ? MOVE.NEUTRAL : entrada);
            if (game.isJunction(nodo) || salidas.length != 1) return nodo;
            entrada = salidas[0]; nodo = game.getNeighbour(nodo, entrada);
            if (nodo < 0) return -1;
        }
        return nodo;
    }
}

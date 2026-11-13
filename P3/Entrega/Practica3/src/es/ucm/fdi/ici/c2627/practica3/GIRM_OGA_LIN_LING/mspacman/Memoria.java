package es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.mspacman;

import java.util.Arrays;
import pacman.game.Game;

public class Memoria {
    private int[] visitas;
    private int comida = -1, ultimoNodo = -1, sinComer;

    public void olvidar() { visitas = null; comida = -1; ultimoNodo = -1; sinComer = 0; }

    public void actualizar(Game game) {
        int quedan = game.getActivePillsIndices().length + game.getActivePowerPillsIndices().length;
        if (visitas == null || visitas.length != game.getNumberOfNodes())
            visitas = new int[game.getNumberOfNodes()];
        if (comida != quedan) {
            Arrays.fill(visitas, 0);
            sinComer = 0;
        } else sinComer++;
        comida = quedan;
        int nodo = game.getPacmanCurrentNodeIndex();
        if (nodo >= 0 && nodo != ultimoNodo) visitas[nodo]++;
        ultimoNodo = nodo;
    }

    public int sinComer() { return sinComer; }
    public int visitas(int nodo) { return visitas == null || nodo < 0 ? 0 : visitas[nodo]; }
}

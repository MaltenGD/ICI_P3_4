package es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.companera.actions;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

/**
 * Lógica de MsPacManV0Me (mapa de costes + lookahead por intersecciones).
 * Ya no decide la fase: la fase la elige el motor de reglas y llega como parámetro.
 */
public class MapaCostes {

    public enum Fase { CAZA, POWER, HUIDA, NORMAL }

    //  PARÁMETROS (CERCA y HOLGURA deben coincidir con los umbrales del .clp)
    public static final int CERCA                 = 30;
    public static final int HOLGURA               = 6;
    public static final int PROFUNDIDAD_SEGURA    = 5;
    public static final boolean DEBUG             = false;

    public static final double PELIGRO_BASE       = 500.0;
    public static final double APETITO_BASE       = 100.0;
    public static final double COSTE_INALCANZABLE = 100_000.0;
    public static final double PESO_DISTANCIA     = 1.0;
    public static final double PESO_PELIGRO       = 1.0;
    public static final double PESO_CALLEJON      = 80.0;

    //  ESTADO
    private int      nNodos;
    private double[] dist, peligro, apetito, coste, callejon;
    private int[]    grado;
    private Fase     fase = Fase.NORMAL;

    public MOVE decidir(Game game, Fase fase) {
        this.fase = fase;
        int nodo = game.getPacmanCurrentNodeIndex();
        MOVE last = game.getPacmanLastMoveMade();
        MOVE[] opciones = game.getPossibleMoves(nodo, last);

        // con una sola salida el resultado no depende del mapa
        if (opciones.length == 1) return opciones[0];

        asegurarBuffers(game);
        calcularDistanciasPildoras(game);
        calcularPeligroYApetito(game);
        calcularCallejones();
        combinarCoste(fase);

        if (DEBUG) dibujarCostes(game);

        return decidirConLookahead(game, nodo, opciones);
    }

    private void dibujarCostes(Game game) {
        double max = 0.0;
        for (int i = 0; i < nNodos; i++) {
            double v = coste[i];
            if (Double.isFinite(v) && v < COSTE_INALCANZABLE) max = Math.max(max, Math.max(0.0, v));
        }
        double escala = Math.log1p(max);
        for (int i = 0; i < nNodos; i++) {
            double v = coste[i];
            if (!Double.isFinite(v) || v >= COSTE_INALCANZABLE) continue;
            double t = escala > 0.0 ? Math.log1p(Math.max(0.0, v)) / escala : 0.0;
            t = Math.max(0.0, Math.min(1.0, t));
            GameView.addPoints(game, new Color((int) (255 * t), (int) (255 * (1.0 - t)), 0), i);
        }
        GameView.addPoints(game, Color.BLUE, game.getPacmanCurrentNodeIndex());
    }

    private void asegurarBuffers(Game game) {
        int n = game.getNumberOfNodes();
        if (dist == null || nNodos != n) {
            nNodos   = n;
            dist     = new double[n];
            peligro  = new double[n];
            apetito  = new double[n];
            coste    = new double[n];
            callejon = new double[n];
            grado    = new int[n];
            for (int i = 0; i < n; i++) {
                int g = 0;
                for (MOVE m : MOVE.values()) if (game.getNeighbour(i, m) != -1) g++;
                grado[i] = g;
            }
        }
    }

    //  DISTANCIAS A PÍLDORAS
    private void calcularDistanciasPildoras(Game game) {
        Arrays.fill(dist, COSTE_INALCANZABLE);
        Deque<Integer> cola = new ArrayDeque<>();
        for (int p : game.getActivePillsIndices())      { dist[p] = 0; cola.add(p); }
        for (int p : game.getActivePowerPillsIndices()) { dist[p] = 0; cola.add(p); }

        if (game.getActivePillsIndices().length == 0) { Arrays.fill(dist, 0); return; }

        while (!cola.isEmpty()) {
            int u = cola.poll();
            double du = dist[u];
            for (MOVE m : MOVE.values()) {
                int v = game.getNeighbour(u, m);
                if (v == -1) continue;
                if (du + 1 < dist[v]) { dist[v] = du + 1; cola.add(v); }
            }
        }
    }

    private boolean esInterseccion(int nodo) { return grado[nodo] >= 3; }

    private double costeInterseccion(Game game, int interseccion) {
        if (!esInterseccion(interseccion)) return 0.0;
        int nodoPacman = game.getPacmanCurrentNodeIndex();
        double p = 0.0;
        for (GHOST g : GHOST.values()) {
            if (game.getGhostLairTime(g) > 0 || game.isGhostEdible(g)) continue;
            int nodoFantasma = game.getGhostCurrentNodeIndex(g);
            if (nodoFantasma == -1) continue;

            int tPacman   = game.getShortestPathDistance(nodoPacman, interseccion, game.getPacmanLastMoveMade());
            int tFantasma = game.getShortestPathDistance(nodoFantasma, interseccion, game.getGhostLastMoveMade(g));
            if (tPacman < 0 || tFantasma < 0) continue;

            int margen = tFantasma - tPacman;
            if (margen <= HOLGURA) {
                if (margen <= 0) p += PELIGRO_BASE * 5.0;                    // llega antes o a la vez
                else             p += PELIGRO_BASE * (HOLGURA + 1 - margen); // llega después, poco margen
            }
        }
        return p;
    }

    //  PELIGRO Y APETITO
    private void calcularPeligroYApetito(Game game) {
        Arrays.fill(peligro, 0.0);
        Arrays.fill(apetito, 0.0);

        for (GHOST g : GHOST.values()) {
            if (game.getGhostLairTime(g) > 0) continue;
            int ng = game.getGhostCurrentNodeIndex(g);
            if (ng == -1) continue;
            boolean comestible = game.isGhostEdible(g);

            for (int nodo = 0; nodo < nNodos; nodo++) {
                int d = game.getShortestPathDistance(nodo, ng);
                if (d < 0 || d > CERCA) continue;

                if (comestible) {
                    double t = game.getGhostEdibleTime(g);
                    if (d < t) {                                  // solo si da tiempo a comerlo
                        double factorTiempo = Math.min(1.0, t / 40.0);
                        apetito[nodo] += 2.0 * APETITO_BASE * factorTiempo / (d + 1);
                    }
                } else {
                    double mult = 1.0;
                    MOVE dirG = game.getGhostLastMoveMade(g);
                    if (dirG != null && game.getNeighbour(ng, dirG) == nodo) mult = 1.5;
                    peligro[nodo] += PELIGRO_BASE * mult / ((d + 1) * (d + 1));
                }
            }
        }
        for (int nodo = 0; nodo < nNodos; nodo++)
            if (esInterseccion(nodo)) peligro[nodo] += costeInterseccion(game, nodo);
    }

    //  CALLEJONES SIN SALIDA
    private void calcularCallejones() {
        Arrays.fill(callejon, 0.0);
        for (int i = 0; i < nNodos; i++) {
            if (grado[i] > 2 || peligro[i] <= 0) continue;
            callejon[i] = PESO_CALLEJON * peligro[i] * (3 - grado[i]) / PELIGRO_BASE;
        }
    }

    //  COSTE COMBINADO SEGÚN FASE
    private void combinarCoste(Fase fase) {
        double kPeligro = 1.0, kApetito = 1.0;
        switch (fase) {
            case CAZA:  kApetito = 2.0; break;
            case POWER: kPeligro = 1.2; break;
            case HUIDA: kPeligro = 2.0; break;
            default:    break;
        }
        for (int i = 0; i < nNodos; i++)
            coste[i] = PESO_DISTANCIA * dist[i]
                     + PESO_PELIGRO * peligro[i] * kPeligro
                     - apetito[i] * kApetito
                     + callejon[i];
    }

    //  DECISIÓN CON LOOKAHEAD
    private MOVE decidirConLookahead(Game game, int nodo, MOVE[] opciones) {
        MOVE mejor = opciones[0];
        double mejorCoste = Double.MAX_VALUE;
        for (MOVE m : opciones) {
            int v = game.getNeighbour(nodo, m);
            if (v == -1) continue;
            double c = costeDe(game, v, m, PROFUNDIDAD_SEGURA);
            if (c < mejorCoste) { mejorCoste = c; mejor = m; }
        }
        return mejor;
    }

    private double costeDe(Game game, int nodo, MOVE ultimo, int prof) {
        if (prof <= 0) return 0.0;
        MOVE[] ops = game.getPossibleMoves(nodo, ultimo);
        if (ops == null || ops.length == 0) return 0.0;

        double mejorFuturo = Double.POSITIVE_INFINITY;
        for (MOVE m : ops) {
            int v = game.getNeighbour(nodo, m);
            if (v == -1) continue;

            double costeTramo = 0.0;
            int actual = v;
            MOVE direccion = m;
            boolean[] visitados = new boolean[grado.length];

            while (actual != -1 && !visitados[actual]) {
                visitados[actual] = true;
                costeTramo += coste[actual];
                if (esInterseccion(actual)) break;

                MOVE[] sig = game.getPossibleMoves(actual, direccion);
                if (sig == null || sig.length == 0) break;
                int siguiente = game.getNeighbour(actual, sig[0]);
                if (siguiente == -1 || visitados[siguiente]) break;
                direccion = sig[0];
                actual = siguiente;
            }

            double futuro = 0.0;
            if (actual != -1 && esInterseccion(actual) && prof > 1)
                futuro = costeDe(game, actual, direccion, prof - 1);

            mejorFuturo = Math.min(mejorFuturo, costeTramo + futuro);
        }

        double pesoLocal = (fase == Fase.HUIDA) ? 0.7 : 0.5;   // en HUIDA, corto plazo
        return mejorFuturo == Double.POSITIVE_INFINITY ? 0.0 : mejorFuturo * pesoLocal;
    }
}

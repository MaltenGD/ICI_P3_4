package es.ucm.fdi.ici.c2627.practica2.GIRM_LC;

import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Constants.GHOST;
import pacman.game.Game;
import pacman.game.GameView;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.text.*;

public class MsPacManV1Me extends PacmanController {

  
    //  PARÁMETROS
    public static final int CERCA                       = 30;
    public static final int HOLGURA                     = 6;
    public static final int PROFUNDIDAD_SEGURA          = 5;

    public static final boolean DEBUG                   = false;

    // Pesos base
    public static final double PELIGRO_BASE             = 500.0;
    public static final double APETITO_BASE             = 100.0;
    public static final double COSTE_INALCANZABLE       = 100_000.0;
    public static final double PESO_DISTANCIA           = 1.0;
    public static final double PESO_PELIGRO             = 1.0;
    public static final double PESO_CALLEJON            = 80.0;
    public static final double PESO_POWER_PILL          = 1.5;

  
    //  ESTADO
    private int      nNodos;
    private double[] dist;          // distancia a la píldora más cercana
    private double[] peligro;       // peligro acumulado de fantasmas no comestibles
    private double[] apetito;       // atractivo de fantasmas comestibles
    private double[] coste;         // coste combinado final
    private double[] callejon;      // penalización por callejones sin salida
    private int[]    grado;         // grado de cada nodo (nº vecinos)

    //  ESTADOS DE ESTRATEGIA
    private enum Fase {
        CAZA,       // hay fantasmas comestibles -> perseguirlos
        POWER,      // power pill disponible y fantasmas no comestibles cerca -> ir a por ella
        HUIDA,      // fantasmas no comestibles muy cerca -> sobrevivir
        NORMAL      // comer píldoras normales
    }
    private Fase faseActual = Fase.NORMAL;
    
    @Override
    public MOVE getMove(Game game, long timeDue) {

        int nodo = game.getPacmanCurrentNodeIndex();
        MOVE last = game.getPacmanLastMoveMade();
        MOVE[] opciones = game.getPossibleMoves(nodo, last);

        asegurarBuffers(game);

        faseActual = detectarFase(game); //Detectar fase
        
        calcularDistanciasPildoras(game);
        calcularPeligroYApetito(game);
        calcularCallejones(game);
        combinarCoste(faseActual);

        // Dibujar todos los nodos en cada turno.
        if (DEBUG) {
            dibujarCostes(game);
        }

        // Si solo hay un movimiento, continuar por él.
        if (opciones.length == 1) {
            return opciones[0];
        }

        return decidirConLookahead(game, nodo, last, opciones);
    }
 
    private void dibujarCostes(Game game) {
    	double max = 0.0;

    	// Buscar el máximo coste alcanzable.
    	for (int i = 0; i < nNodos; i++) {
    	    double v = coste[i];

    	    if (Double.isFinite(v)
    	            && v < COSTE_INALCANZABLE) {
    	        max = Math.max(max, Math.max(0.0, v));
    	    }
    	}

    	// Evitar divisiones problemáticas.
    	double escala = Math.log1p(max);

    	for (int i = 0; i < nNodos; i++) {

    	    double v = coste[i];

    	    if (!Double.isFinite(v)
    	            || v >= COSTE_INALCANZABLE) {
    	        continue;
    	    }

    	    // Normalización logarítmica.
    	    double t = escala > 0.0
    	            ? Math.log1p(Math.max(0.0, v)) / escala
    	            : 0.0;

    	    t = Math.max(0.0, Math.min(1.0, t));

    	    // Verde = coste bajo; rojo = coste alto.
    	    Color color = new Color(
    	            (int) (255 * t),
    	            (int) (255 * (1.0 - t)),
    	            0
    	    );

    	    GameView.addPoints(game, color, i);
    	}

    	// Dibujar Pac-Man en azul.
    	GameView.addPoints(
    	        game,
    	        Color.BLUE,
    	        game.getPacmanCurrentNodeIndex()
    	);        
    }


//  DEBUG DE COSTES
    private void asegurarBuffers(Game game) {
        int n = game.getNumberOfNodes();
        if (dist == null || nNodos != n) {
            nNodos    = n;
            dist      = new double[n];
            peligro   = new double[n];
            apetito   = new double[n];
            coste     = new double[n];
            callejon  = new double[n];
            grado     = new int[n];
            precomputarGrado(game);
        }
    }
    // Calcula cuántos vecinos tiene cada nodo para las direcciones para calcular las intersecciones
    private void precomputarGrado(Game game) {
        for (int i = 0; i < nNodos; i++) {
            int g = 0;
            for (MOVE m : MOVE.values()) {
                if (game.getNeighbour(i, m) != -1) g++;
            }
            grado[i] = g;
        }
    }

    // =========================================================
    //  DISTANCIAS A PÍLDORAS
    // =========================================================
    private void calcularDistanciasPildoras(Game game) {
        Arrays.fill(dist, COSTE_INALCANZABLE);

        Deque<Integer> cola = new ArrayDeque<>();
        
        for (int p : game.getActivePillsIndices()) {
            dist[p] = 0;
            cola.add(p);
        }
        for (int p : game.getActivePowerPillsIndices()) {
            dist[p] = 0;
            cola.add(p);
        }

        if (cola.isEmpty()) { Arrays.fill(dist, 0); return; }

        recorrerAnchura(game, cola, dist);
    }

    private void recorrerAnchura(Game game, Deque<Integer> cola, double[] d) {
        while (!cola.isEmpty()) {
            int u = cola.poll();
            double du = d[u];
            for (MOVE m : MOVE.values()) {
                int v = game.getNeighbour(u, m);
                if (v == -1) continue;
                if (du + 1 < d[v]) {
                    d[v] = du + 1;
                    cola.add(v);
                }
            }
        }
    }
    
private boolean esInterseccion(int nodo) {
    return grado[nodo] >= 3;
}

private double costeInterseccion(Game game, int interseccion) {

    if (!esInterseccion(interseccion)) return 0.0;
    
    int nodoPacman = game.getPacmanCurrentNodeIndex();

    double peligroInterseccion = 0.0;

    for (GHOST g : GHOST.values()) {

        if (game.getGhostLairTime(g) > 0) continue;

        if (game.isGhostEdible(g)) continue;

        int nodoFantasma = game.getGhostCurrentNodeIndex(g);

        if (nodoFantasma == -1) continue;

        int tiempoPacman =
                game.getShortestPathDistance( nodoPacman, interseccion, game.getPacmanLastMoveMade());

        int tiempoFantasma =
                game.getShortestPathDistance(nodoFantasma, interseccion, game.getGhostLastMoveMade(g));

        if (tiempoPacman < 0 || tiempoFantasma < 0) continue;

        int margen = tiempoFantasma - tiempoPacman;

        if (margen <= HOLGURA) {
            if (margen <= 0) {
            	 //Si se llega antes o no
            	peligroInterseccion += PELIGRO_BASE * 5.0;
            } else {
            	// Llega después, pero con poco margen
                peligroInterseccion += PELIGRO_BASE * (HOLGURA + 1 - margen);
            }
        }
    }

    return peligroInterseccion;
}

    // =========================================================
    //  PELIGRO Y APETITO
    // =========================================================
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
                    if (d < t) // Solo si da tiempo a comer al fantasma
                    {
	                    double factorTiempo = Math.min(1.0, t / 40.0);
	                    // Atrae hacia el fantasma, especialmente en fase CAZA
	                    double peso = 2.0;
	                    apetito[nodo] += peso * APETITO_BASE * factorTiempo / (d + 1);
                    }
                } else {
                    double multiplicador = 1.0;
                    MOVE dirG = game.getGhostLastMoveMade(g);
                    if (dirG != null) {
                        int sig = game.getNeighbour(ng, dirG);
                        if (sig == nodo) multiplicador = 1.5;
                    }
                    peligro[nodo] += PELIGRO_BASE * multiplicador / ((d + 1) * (d + 1));
                }
            }
        }
     // Seguridad de intersecciones
     for (int nodo = 0; nodo < nNodos; nodo++) {

         if (!esInterseccion(nodo))
             continue;

         peligro[nodo] += costeInterseccion(game, nodo);
         }
    }

    //  CALLEJONES SIN SALIDA
    private void calcularCallejones(Game game) {
        Arrays.fill(callejon, 0.0);
        // Penaliza nodos de grado 1 ó 2 que además estén cerca de un fantasma no comestible.
        for (int i = 0; i < nNodos; i++) {
            if (grado[i] > 2) continue;
            double p = peligro[i];
            if (p <= 0) continue;
            // Cuanto más "encerrado" y más peligroso, más penalización
            callejon[i] = PESO_CALLEJON * p * (3 - grado[i]) / PELIGRO_BASE;
        }
    }
    
//  COSTE COMBINADO SEGÚN FASE (Maquina de estados)
    private void combinarCoste(Fase fase) {
        switch (fase) {
        case CAZA:
        	for (int i = 0; i < nNodos; i++) {
	            coste[i] = PESO_DISTANCIA * dist[i]
	                     + PESO_PELIGRO * peligro[i]
	                     - apetito[i] * 2.0f
	                     + callejon[i];
	        }
        	break;
        
        case POWER:
        	for (int i = 0; i < nNodos; i++) {
	            coste[i] = PESO_DISTANCIA * dist[i]
	                     + PESO_PELIGRO * peligro[i] * 1.2f
	                     - apetito[i]
	                     + callejon[i];
	        }
        	break;
        case HUIDA:
        	for (int i = 0; i < nNodos; i++) {
	            coste[i] = PESO_DISTANCIA * dist[i]
	                     + PESO_PELIGRO * peligro[i] * 2.0f
	                     - apetito[i]
	                     + callejon[i];
	        }
        	break;
        case NORMAL:
        	default: for (int i = 0; i < nNodos; i++) {
	            coste[i] = PESO_DISTANCIA * dist[i]
	                     + PESO_PELIGRO * peligro[i]
	                     - apetito[i]
	                     + callejon[i];
	        }
        	break;
        }
    }

    //  DECISIÓN CON LOOKAHEAD
    private MOVE decidirConLookahead(Game game, int nodo, MOVE ultimo, MOVE[] opciones) {
        MOVE mejor = opciones[0];
        double mejorCoste = Double.MAX_VALUE;
        for (MOVE m : opciones) {
            int v = game.getNeighbour(nodo, m);
            if (v == -1) continue;
            double c = costeDe(game, v, m, PROFUNDIDAD_SEGURA);
            if (c < mejorCoste) {
                mejorCoste = c;
                mejor = m;
            }
        }
        return mejor;
    }

    private double costeDe(Game game, int nodo, MOVE ultimo, int prof) {
        if (prof <= 0) {
            return 0.0;
        }

        MOVE[] ops = game.getPossibleMoves(nodo, ultimo);

        if (ops == null || ops.length == 0) {
            return 0.0;
        }

        double mejorFuturo = Double.POSITIVE_INFINITY;

        for (MOVE m : ops) {
            int v = game.getNeighbour(nodo, m);
            if (v == -1) {
                continue;
            }

            double costeTramo = 0.0;
            int actual = v;
            MOVE direccion = m;

            // Evita bucles dentro de un mismo pasillo.
            boolean[] visitados = new boolean[grado.length];

            while (actual != -1 && !visitados[actual]) {
                visitados[actual] = true;
                costeTramo += coste[actual];

                // El tramo termina al llegar a un cruce.
                if (esInterseccion(actual)) {
                    break;
                }

                MOVE[] siguientes = game.getPossibleMoves(actual, direccion);

                if (siguientes == null || siguientes.length == 0) {
                    // Final del pasillo o callejón.
                    break;
                }

                int siguiente = game.getNeighbour(actual, siguientes[0]);

                if (siguiente == -1 || visitados[siguiente]) {
                    break;
                }

                direccion = siguientes[0];
                actual = siguiente;
            }

            double futuro = 0.0;

            if (actual != -1
                    && esInterseccion(actual)
                    && prof > 1) {
                futuro = costeDe(
                    game, actual, direccion, prof - 1
                );
            }

            double total = costeTramo + futuro;

            if (total < mejorFuturo) {
                mejorFuturo = total;
            }
        }

        //En HUIDA, priorizar el corto plazo
        double pesoLocal = 0.5f;
        if (faseActual == Fase.HUIDA)
        {
        	pesoLocal = 0.7f;
        }
        
        return mejorFuturo == Double.POSITIVE_INFINITY
            ? 0.0
            : mejorFuturo * pesoLocal;
    }

    
    private Fase detectarFase(Game game) {

        int nodoPacman = game.getPacmanCurrentNodeIndex();

        boolean hayComestible = false;
        int     fantasmasCercaNoComestibles = 0;
        int     comestiblesCerca = 0;

        for (GHOST g : GHOST.values()) {
            if (game.getGhostLairTime(g) > 0) continue;
            int ng = game.getGhostCurrentNodeIndex(g);
            if (ng == -1) continue;

            int d = game.getShortestPathDistance(nodoPacman, ng);
            boolean comestible = game.isGhostEdible(g);

            if (comestible) {
                hayComestible = true;
                if (d >= 0 && d <= CERCA) comestiblesCerca++;
            } else {
                if (d >= 0 && d <= HOLGURA) fantasmasCercaNoComestibles++;
            }
        }

        // 1) Si hay fantasmas no comestibles pegados -> HUIDA
        if (fantasmasCercaNoComestibles > 0) return Fase.HUIDA;

        // 2) Si hay fantasmas comestibles y están razonablemente cerca -> CAZA
        if (hayComestible && comestiblesCerca > 0) return Fase.CAZA;


        // 3) Si quedan power pills y hay varios fantasmas rondando -> POWER
        int powerPillsRestantes = game.getActivePowerPillsIndices().length;
        int c=0;
        for (GHOST g : GHOST.values()) {
            if (game.getGhostLairTime(g) > 0) c++;
        }
        
        if (powerPillsRestantes > 0 && c >= GHOST.values().length)
            return Fase.POWER;

        return Fase.NORMAL;
    }
    
    @Override
    public String getName() { return "MsPacManV1Me_GIRM_LC"; }
}
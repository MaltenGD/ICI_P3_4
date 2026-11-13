package es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.mspacman;

import java.util.*;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.UltimaAccion;
import es.ucm.fdi.ici.rules.RulesInput;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class MsPacManInput extends RulesInput {
    private List<String> hechos;
    private int distFantasma;

    public MsPacManInput(Game game, UltimaAccion ultima, Memoria memoria) {
        super(game);
        // super ya ha llamado a parseInput; no inicializamos hechos en la declaracion
        prepararObjetivos(ultima, memoria);
    }

    @Override
    public void parseInput() {
        hechos = new ArrayList<String>();
        distFantasma = Caminos.LEJOS;
        int pacman = game.getPacmanCurrentNodeIndex();
        if (pacman < 0) return;
        for (GHOST g : GHOST.values()) {
            if (!Caminos.fuera(game, g)) continue;
            int nodo = game.getGhostCurrentNodeIndex(g);
            boolean comestible = game.isGhostEdible(g);
            int d = comestible ? Caminos.distancia(game, pacman, nodo, Caminos.ultimo(game))
                    : Caminos.distancia(game, nodo, pacman, game.getGhostLastMoveMade(g));
            if (!comestible) distFantasma = Math.min(distFantasma, d);
            // el margen solo interviene al evaluar presas que aun tienen tiempo
            int margen = comestible && 2 * d + 8 < game.getGhostEdibleTime(g)
                    ? Caminos.margen(game, Caminos.ruta(game, nodo)) : -Caminos.LEJOS;
            hechos.add(String.format(Locale.ROOT,
                    "(FANTASMA (nombre %s) (indice %d) (comestible %s) (distancia %d)"
                    + " (tiempo %d) (margen %d) (nodo %d))",
                    g, g.ordinal(), comestible, d, game.getGhostEdibleTime(g), margen, nodo));
        }
    }

    private void prepararObjetivos(UltimaAccion ultima, Memoria memoria) {
        int origen = game.getPacmanCurrentNodeIndex();
        if (origen < 0) return;
        int[] comida = game.getActivePillsIndices();
        int[] poder = game.getActivePowerPillsIndices();
        int[] normales = comida.length == 0 ? poder : comida;
        int cercana = masCercano(normales);
        int distanciaComida = Caminos.distancia(game, origen, cercana, Caminos.ultimo(game));
        int nodoPoder = masCercano(poder);
        int distanciaPoder = Caminos.distancia(game, origen, nodoPoder, Caminos.ultimo(game));
        hechos.add(String.format(Locale.ROOT,
                "(MSPACMAN (distFantasma %d) (distPPill %d) (ultima %s) (presaAnterior %s)"
                + " (objetivoAnterior %d) (pildoras %d) (poderes %d) (comida %d)"
                + " (distComida %d) (sinComer %d))",
                distFantasma, distanciaPoder, ultima.get(), ultima.fantasma(), ultima.nodo(),
                comida.length, poder.length, cercana, distanciaComida, memoria.sinComer()));

        objetivo("comida", mejorSeguro(normales, 6), 0, "ninguno", 0);
        objetivo("poder", mejorSeguro(poder, poder.length), 0, "ninguno", 0);
        // comprobamos si el objetivo anterior sigue siendo un poder activo
        for (int p : poder) {
            if (p == ultima.nodo()) objetivo("anterior", p, 0, "ninguno", 0);
        }

        int zona = -1, cantidadZona = 0;
        for (int cruce : cercanos(game.getJunctionIndices(), 6)) {
            if (cruce == origen) continue;
            int cantidad = 0;
            for (int p : comida) {
                int d = game.getShortestPathDistance(cruce, p);
                if (d >= 0 && d <= 12) cantidad++;
            }
            if (cantidad > cantidadZona && Caminos.margen(game, Caminos.ruta(game, cruce)) > 3) {
                zona = cruce; cantidadZona = cantidad;
            }
        }
        objetivo("zona", zona, cantidadZona, "ninguno", 0);

        GHOST interceptado = null;
        int intercepcion = -1, mejorTiempo = Caminos.LEJOS, ventaja = 0;
        for (GHOST g : GHOST.values()) {
            if (!Caminos.fuera(game, g) || !game.isGhostEdible(g)) continue;
            int nodo = game.getGhostCurrentNodeIndex(g);
            int cruce = Caminos.siguienteCruce(game, nodo, game.getGhostLastMoveMade(g));
            if (cruce < 0 || cruce == nodo || !game.isJunction(cruce)) continue;
            int yo = Caminos.distancia(game, origen, cruce, Caminos.ultimo(game));
            int el = 2 * Caminos.distancia(game, nodo, cruce, game.getGhostLastMoveMade(g));
            int encuentro = Math.max(yo, el);
            int directo = 2 * Caminos.distancia(game, origen, nodo, Caminos.ultimo(game));
            if (encuentro < mejorTiempo && encuentro + 8 < game.getGhostEdibleTime(g)
                    && Caminos.margen(game, Caminos.ruta(game, cruce)) > 3) {
                mejorTiempo = encuentro; intercepcion = cruce;
                interceptado = g; ventaja = directo - encuentro;
            }
        }
        if (interceptado != null) objetivo("interceptar", intercepcion, 0, interceptado.name(), ventaja);

        for (MOVE m : game.getPossibleMoves(origen, Caminos.ultimo(game))) {
            int[] ruta = Caminos.pasillo(game, m);
            if (ruta.length == 0) continue;
            int finalRuta = ruta[ruta.length - 1], visitas = 0, distancia = Caminos.LEJOS;
            for (int n : ruta) visitas += memoria.visitas(n);
            for (int p : normales) {
                int d = game.getShortestPathDistance(finalRuta, p);
                if (d >= 0) distancia = Math.min(distancia, d);
            }
            hechos.add(String.format(Locale.ROOT,
                    "(SALIDA (indice %d) (movimiento %s) (nodo %d) (margen %d)"
                    + " (visitas %d) (distComida %d))",
                    m.ordinal(), m, finalRuta, Caminos.margen(game, ruta), visitas, distancia));
        }
    }

    private List<Integer> cercanos(int[] fuentes, int limite) {
        List<Integer> nodos = new ArrayList<Integer>();
        for (int n : fuentes) if (n >= 0) nodos.add(n);
        int origen = game.getPacmanCurrentNodeIndex();
        nodos.sort((a, b) -> {
            int da = game.getShortestPathDistance(origen, a), db = game.getShortestPathDistance(origen, b);
            if (da < 0) da = Caminos.LEJOS;
            if (db < 0) db = Caminos.LEJOS;
            return da == db ? Integer.compare(a, b) : Integer.compare(da, db);
        });
        return nodos.subList(0, Math.min(limite, nodos.size()));
    }

    private int masCercano(int[] fuentes) {
        int mejor = -1, distancia = Caminos.LEJOS;
        for (int n : fuentes) {
            int d = Caminos.distancia(game, game.getPacmanCurrentNodeIndex(), n, Caminos.ultimo(game));
            if (d < distancia || (d == distancia && n < mejor)) { distancia = d; mejor = n; }
        }
        return mejor;
    }

    private int mejorSeguro(int[] fuentes, int limite) {
        int mejor = -1, distancia = Caminos.LEJOS;
        for (int n : cercanos(fuentes, limite)) {
            int[] ruta = Caminos.ruta(game, n);
            int d = Caminos.distancia(game, game.getPacmanCurrentNodeIndex(), n, Caminos.ultimo(game));
            if (ruta.length > 0 && d < distancia && Caminos.margen(game, ruta) > 3) {
                mejor = n; distancia = d;
            }
        }
        return mejor;
    }

    private void objetivo(String tipo, int nodo, int cantidad, String fantasma, int ventaja) {
        if (nodo < 0) return;
        int[] ruta = Caminos.ruta(game, nodo);
        if (ruta.length == 0) return;
        hechos.add(String.format(Locale.ROOT,
                "(OBJETIVO (tipo %s) (nodo %d) (distancia %d) (margen %d)"
                + " (cantidad %d) (fantasma %s) (ventaja %d) (movimiento %s))",
                tipo, nodo, ruta.length, Caminos.margen(game, ruta), cantidad, fantasma, ventaja,
                Caminos.hacia(game, nodo)));
    }

    @Override public Collection<String> getFacts() { return hechos; }
}

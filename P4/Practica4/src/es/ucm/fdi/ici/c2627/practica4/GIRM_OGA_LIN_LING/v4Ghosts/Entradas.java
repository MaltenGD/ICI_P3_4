package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v4Ghosts;

import java.util.HashMap;
import es.ucm.fdi.ici.fuzzy.FuzzyInput;
import pacman.game.Constants.GHOST;
import pacman.game.Game;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v4Ghosts.Ghosts.Turno;

final class Entradas extends FuzzyInput {
    private static final int LEJOS = 150;
    private final GHOST yo;
    private final Turno plan;
    private final int version, comidos;
    private final HashMap<String, Double> valores = new HashMap<>();

    Entradas(Game game, GHOST yo, Turno plan, int version, int comidos) {
        super(game);
        this.yo = yo;
        this.plan = plan;
        this.version = version;
        this.comidos = comidos;
        parseInput();
    }

    @Override
    public void parseInput() {
        if (yo == null) return;
        int origen = game.getGhostCurrentNodeIndex(yo);
        int tiempo = game.getGhostEdibleTime(yo);
        int distancia = game.getShortestPathDistance(origen, plan.pacman);
        int aliado = LEJOS;
        for (GHOST g : GHOST.values()) {
            if (g == yo || game.getGhostLairTime(g) > 0) continue;
            int d = game.getShortestPathDistance(origen, game.getGhostCurrentNodeIndex(g));
            if (d >= 0) aliado = Math.min(aliado, d);
        }
        poner("soy", yo.ordinal());
        poner("comestible", tiempo > 0 ? 1 : 0);
        poner("tiempo", tiempo);
        poner("distPacman", Math.min(LEJOS, distancia));
        poner("distAliado", aliado);
        if (version >= 3) {
            int desvio = game.getShortestPathDistance(origen, plan.intercepcion) - distancia;
            poner("desvio", desvio);
        }
        if (version >= 4) {
            poner("asignado", plan.poder < 0 ? 0 : 1);
            poner("distPoder", plan.poder < 0 ? LEJOS : game.getShortestPathDistance(plan.pacman, plan.poder));
            poner("comidos", comidos);
        }
    }

    private void poner(String variable, int valor) {
        valores.put(variable, (double) valor);
    }

    @Override
    public HashMap<String, Double> getFuzzyValues() { return valores; }
}

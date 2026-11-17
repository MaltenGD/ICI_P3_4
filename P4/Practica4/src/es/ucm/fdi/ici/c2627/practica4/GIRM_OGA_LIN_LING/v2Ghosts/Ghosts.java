package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v2Ghosts;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.fuzzy.FuzzyEngine;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.MaxActionSelector;
import pacman.controllers.GhostController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public final class Ghosts extends GhostController {
    private static final String FCL_PATH = "bin/es/ucm/fdi/ici/c2627/practica4/GIRM_OGA_LIN_LING/v2Ghosts/";
    private final EnumMap<GHOST, FuzzyEngine> motores = new EnumMap<>(GHOST.class);
    private final EnumMap<GHOST, Turno> planes = new EnumMap<>(GHOST.class);
    private final int version;
    private int comidos, turnoAnterior = -1;

    public Ghosts() { this("ghosts-v2.fcl"); }

    public Ghosts(String fcl) {
        version = Integer.parseInt(fcl.substring("ghosts-v".length(), fcl.indexOf('.')));
        setName("Ghosts GIRM_OGA_LIN_LING");
        setTeam("GIRM_OGA_LIN_LING");
        Path ruta = Path.of(FCL_PATH, fcl);
        if (!Files.isRegularFile(ruta)) ruta = Path.of("Practica4").resolve(ruta);
        if (!Files.isRegularFile(ruta)) throw new IllegalArgumentException("no se encuentra " + ruta);
        for (GHOST g : GHOST.values()) {
            Turno plan = new Turno();
            planes.put(g, plan);
            Action[] acciones = {
                new Movimiento("perseguir", g, plan),
                new Movimiento("huir", g, plan),
                new Movimiento("interceptar", g, plan),
                new Movimiento("defender", g, plan)
            };
            motores.put(g, new FuzzyEngine(g.name(), ruta.toString(), "Ghost", new MaxActionSelector(acciones)));
        }
    }

    public FuzzyEngine getMotor(GHOST g) { return motores.get(g); }

    @Override
    public void preCompute(String rival) {
        super.preCompute(rival);
        comidos = 0;
        turnoAnterior = -1;
    }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        EnumMap<GHOST, MOVE> movimientos = new EnumMap<>(GHOST.class);
        int turno = game.getTotalTime();
        if (turno < turnoAnterior) comidos = 0;
        if (turno != turnoAnterior) comidos += game.getNumGhostsEaten();
        turnoAnterior = turno;

        int pacman = game.getPacmanCurrentNodeIndex();
        int futuro = version >= 3 ? Movimiento.avanzar(game, pacman, game.getPacmanLastMoveMade(), 18) : pacman;
        int[] objetivos = version >= 4 ? repartirPoder(game) : null;
        for (GHOST g : GHOST.values()) {
            if (!game.doesGhostRequireAction(g)) continue;
            Turno plan = planes.get(g);
            plan.pacman = pacman;
            plan.intercepcion = futuro;
            plan.poder = objetivos == null ? -1 : objetivos[g.ordinal()];
            var datos = new Entradas(game, g, plan, version, comidos);
            movimientos.put(g, motores.get(g).run(datos.getFuzzyValues(), game));
        }
        return movimientos;
    }

    private int[] repartirPoder(Game game) {
        int[] poder = game.getActivePowerPillsIndices();
        int[] destinos = new int[GHOST.values().length];
        Arrays.fill(destinos, -1);
        // cada pildora se asigna una sola vez. blinky mantiene la persecucion.
        GHOST[] apoyo = {GHOST.SUE, GHOST.INKY, GHOST.PINKY};
        for (int ronda = 0; ronda < 3; ronda++) {
            GHOST elegido = null;
            int mejor = Integer.MAX_VALUE, indice = -1;
            for (GHOST g : apoyo) {
                if (destinos[g.ordinal()] >= 0 || game.getGhostLairTime(g) > 0 || game.getGhostEdibleTime(g) > 0)
                    continue;
                int nodo = game.getGhostCurrentNodeIndex(g);
                for (int i = 0; i < poder.length; i++) {
                    if (poder[i] < 0) continue;
                    int distancia = game.getShortestPathDistance(nodo, poder[i]);
                    if (distancia >= 0 && distancia < mejor) {
                        elegido = g;
                        mejor = distancia;
                        indice = i;
                    }
                }
            }
            if (elegido == null) break;
            destinos[elegido.ordinal()] = poder[indice];
            poder[indice] = -1;
        }
        return destinos;
    }

    static final class Turno {
        int pacman, intercepcion, poder;
    }
}

package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.depurar;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.ghosts.Ghosts;
import es.ucm.fdi.ici.fuzzy.FuzzyEngine;
import es.ucm.fdi.ici.fuzzy.FuzzyEngineObserver;
import es.ucm.fdi.ici.rivales.MsPacManAvanzado;
import es.ucm.fdi.ici.rivales.MsPacManBasico;
import es.ucm.fdi.ici.rivales.MsPacManIntermedio;
import net.sourceforge.jFuzzyLogic.rule.Rule;
import pacman.controllers.PacmanController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public final class ComprobarTrazaGhosts {
    private static final class Resumen {
        final Map<Integer, Integer> activaciones = new TreeMap<>();
        final Map<String, Integer> acciones = new TreeMap<>();
        boolean muestraEscrita;
        int tick;
        int legales, totalDecisiones;
    }

    private static final class Observador implements FuzzyEngineObserver {
        final Resumen resumen;
        final int version;
        final List<Rule> reglas;

        Observador(FuzzyEngine motor, Resumen resumen, int version) {
            this.resumen = resumen;
            this.version = version;
            reglas = TrazaBorrosa.reglasDe(motor);
            for (Rule r : reglas) resumen.activaciones.putIfAbsent(Integer.parseInt(r.getName()), 0);
        }

        @Override
        public String getObservedRules() { return null; }

        @Override
        public void actionFired(String accion, HashMap<String, Double> entradas,
                HashMap<String, Double> salidas, Collection<String> ignorado) {
            for (Rule r : reglas)
                if (r.getDegreeOfSupport() > 0) resumen.activaciones.merge(Integer.parseInt(r.getName()), 1, Integer::sum);
            resumen.acciones.merge(accion, 1, Integer::sum);

            boolean ejemplo = version == 4 ? accion.endsWith("defender")
                    : version == 3 ? accion.endsWith("interceptar") : accion.endsWith("huir");
            if (ejemplo && !resumen.muestraEscrita) {
                System.out.printf("v%d, turno %d: %s; entradas %s; salidas %s%n", version, resumen.tick,
                        accion, new TreeMap<>(entradas), new TreeMap<>(salidas));
                for (Rule r : reglas)
                    if (r.getDegreeOfSupport() > 0) System.out.println("  " + r);
                resumen.muestraEscrita = true;
            }
        }
    }

    public static void main(String[] args) {
        int desde = args.length == 0 ? 2 : Integer.parseInt(args[0]);
        int cantidad = args.length < 2 ? 24 : Integer.parseInt(args[1]);
        for (int v = desde; v <= 4; v++) {
            Resumen resumen = new Resumen();
            for (PacmanController rival : List.of(new MsPacManBasico(), new MsPacManIntermedio(), new MsPacManAvanzado())) {
                Ghosts equipo = new Ghosts("ghosts-v" + v + ".fcl");
                for (GHOST g : GHOST.values()) {
                    FuzzyEngine motor = equipo.getMotor(g);
                    motor.addObserver(new Observador(motor, resumen, v));
                }
                for (int i = 0; i < cantidad; i++) {
                    Game game = new Game(5001 + i);
                    rival.preCompute(equipo.getClass().getCanonicalName());
                    equipo.preCompute(rival.getClass().getCanonicalName());
                    while (!game.gameOver() && game.getTotalTime() <= 4000) {
                        resumen.tick = game.getTotalTime();
                        long limite = System.currentTimeMillis() + 40;
                        MOVE pc = rival.getMove(game.copy(), limite);
                        var movimientos = equipo.getMove(game.copy(), limite);
                        if (v >= 2) {
                            for (var e : movimientos.entrySet()) {
                                GHOST g = e.getKey();
                                int nodo = game.getGhostCurrentNodeIndex(g);
                                MOVE ultimo = game.getGhostLastMoveMade(g);
                                for (MOVE m : game.getPossibleMoves(nodo, ultimo))
                                    if (m == e.getValue()) resumen.legales++;
                                resumen.totalDecisiones++;
                            }
                        }
                        game.advanceGame(pc, movimientos);
                    }
                    rival.postCompute();
                    equipo.postCompute();
                }
            }
            System.out.println("v" + v + ": " + (cantidad * 3) + " partidas (" + cantidad + " por rival)");
            System.out.println("activaciones por regla: " + resumen.activaciones);
            System.out.println("acciones ganadoras: " + resumen.acciones);
            System.out.printf("movimientos validos: %d/%d%n", resumen.legales, resumen.totalDecisiones);
            if (resumen.legales != resumen.totalDecisiones) throw new IllegalStateException("salida no permitida");
            System.out.println("reglas sin activar: " + resumen.activaciones.entrySet().stream()
                    .filter(e -> e.getValue() == 0).map(Map.Entry::getKey)
                    .collect(java.util.stream.Collectors.toList()));
        }
    }
}

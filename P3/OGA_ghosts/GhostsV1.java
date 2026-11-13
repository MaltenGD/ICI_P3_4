package es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA;

import java.util.EnumMap;
import es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.GhostsInput;
import es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.actions.*;
import es.ucm.fdi.ici.c2627.practica2.grupoPr2CY_OGA.ghosts.transitions.*;
import es.ucm.fdi.ici.fsm.CompoundState;
import es.ucm.fdi.ici.fsm.FSM;
import es.ucm.fdi.ici.fsm.SimpleState;
import pacman.controllers.GhostController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class GhostsV1 extends GhostController {

    private static final boolean DEBUG = false;
    private EnumMap<GHOST, FSM> machines;

    public GhostsV1() {
        buildMachines();
    }

    @Override
    public void preCompute(String opponent) {
        buildMachines();
    }

    private void buildMachines() {
        machines = new EnumMap<GHOST, FSM>(GHOST.class);
        for (GHOST g : GHOST.values()) {
            machines.put(g, buildFSMForGhost(g));
        }
    }

    private FSM buildFSMForGhost(GHOST g) {
        // --- 10 Estados Simples con sus Acciones ---
        SimpleState perseguir    = new SimpleState("PERSEGUIR",   new ChaseAction(g));
        SimpleState interceptar  = new SimpleState("INTERCEPTAR", new InterceptAction(g));
        SimpleState flanquear    = new SimpleState("FLANQUEAR",   new FlankAction(g));
        SimpleState acechar      = new SimpleState("ACECHAR",     new AmbushAction(g));
        SimpleState patrullar    = new SimpleState("PATRULLAR",   new PatrolAction(g));
        SimpleState huirPacman   = new SimpleState("HUIR_PACMAN", new FleeFromPacManAction(g));
        SimpleState huirRincon   = new SimpleState("HUIR_RINCON", new FleeToCornerAction(g));
        SimpleState guardar      = new SimpleState("GUARDAR",     new GuardRespawnAction(g));
        SimpleState reagrupar    = new SimpleState("REAGRUPAR",   new RegroupAction(g));
        SimpleState esperar      = new SimpleState("ESPERAR",     new WaitAction(g));

        // --- Submáquina ACTIVO (compuesto) ---
        FSM activoFSM = new FSM("activo-v1-" + g);

        // MEJORA V1: Mayor agresividad y persistencia en la persecución
        // Intercepta si el cruce al que va Pac-Man está a tiro (< 25 nodos)
        activoFSM.add(perseguir, new TargetJunctionDistanceTransition(25, true), interceptar);
        activoFSM.add(interceptar, new TargetJunctionDistanceTransition(35, false), perseguir);

        // Flanqueo coordinado si dos aliados coinciden a menos de 7 nodos
        activoFSM.add(perseguir, new AllyDistanceTransition(7, true), flanquear);
        activoFSM.add(flanquear, new AllyDistanceTransition(14, false), perseguir);

        // Acecho solo si Pac-Man está muy controlado (distancia 20..30 con histeresis)
        activoFSM.add(perseguir, new PacManDistanceTransition(25, true), acechar);
        activoFSM.add(acechar, new PacManDistanceTransition(12, true), perseguir);
        activoFSM.add(acechar, new PacManDistanceTransition(38, false), perseguir);

        // Patrullar solo si Pac-Man está extremadamente lejos (> 90 nodos) en lugar de 65
        activoFSM.add(perseguir, new PacManDistanceTransition(90, false), patrullar);
        activoFSM.add(patrullar, new PacManDistanceTransition(50, true), perseguir);

        activoFSM.add(patrullar, new AllyDistanceTransition(5, true), reagrupar);
        activoFSM.add(reagrupar, new AllyDistanceTransition(15, false), patrullar);

        activoFSM.ready(perseguir);

        // --- Submáquina COMESTIBLE (compuesto) ---
        FSM comestibleFSM = new FSM("comestible-v1-" + g);

        // MEJORA V1: Huida mucho más temprana hacia un rincón seguro (cruce lejano)
        // en lugar de esperar a que Pac-Man esté encima (< 15)
        comestibleFSM.add(huirPacman, new PacManDistanceTransition(35, true), huirRincon);
        comestibleFSM.add(huirRincon, new PacManDistanceTransition(55, false), huirPacman);

        // Guardar respawn si el tiempo comestible es crítico (< 35 ticks)
        comestibleFSM.add(huirPacman, new EdibleTimeLowTransition(35), guardar);
        comestibleFSM.add(huirRincon, new EdibleTimeLowTransition(35), guardar);

        comestibleFSM.ready(huirRincon); // Estado inicial seguro: dispersarse a rincones

        // --- Estados Compuestos ---
        CompoundState activo     = new CompoundState("ACTIVO", activoFSM);
        CompoundState comestible = new CompoundState("COMESTIBLE", comestibleFSM);

        // --- Máquina Principal ---
        FSM mainFSM = new FSM("ghosts-main-v1-" + g);
        mainFSM.add(activo, new GhostEdibleTransition(), comestible);
        mainFSM.add(comestible, new GhostNotEdibleTransition(), activo);
        mainFSM.ready(activo);

        return mainFSM;
    }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        EnumMap<GHOST, MOVE> moves = new EnumMap<GHOST, MOVE>(GHOST.class);
        GhostsInput input = new GhostsInput(game);

        for (GHOST g : GHOST.values()) {
            if (!game.doesGhostRequireAction(g)) {
                continue;
            }
            input.parseFor(g);
            MOVE move = machines.get(g).run(input);
            moves.put(g, move);
        }

        return moves;
    }

    @Override
    public String getName() {
        return "GhostsV1_Pr2CY_OGA";
    }
}

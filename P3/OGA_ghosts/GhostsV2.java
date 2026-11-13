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

public class GhostsV2 extends GhostController {

    private static final boolean DEBUG = false;
    private EnumMap<GHOST, FSM> machines;

    public GhostsV2() {
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
        FSM activoFSM = new FSM("activo-v2-" + g);

        // MEJORA V2: Ataque coordinado en tenaza
        // Intercepta si Pacman va hacia un cruce a distancia razonable (< 22 nodos)
        activoFSM.add(perseguir, new TargetJunctionDistanceTransition(22, true), interceptar);
        activoFSM.add(interceptar, new TargetJunctionDistanceTransition(32, false), perseguir);

        // Flanqueo activo: Si otro fantasma está persiguiendo muy cerca (< 10 nodos),
        // este fantasma toma un camino alternativo para no bloquearse
        activoFSM.add(perseguir, new AllyDistanceTransition(10, true), flanquear);
        activoFSM.add(flanquear, new AllyDistanceTransition(18, false), perseguir);

        // Acecho solo si Pacman está cerca (< 22 nodos)
        activoFSM.add(perseguir, new PacManDistanceTransition(22, true), acechar);
        activoFSM.add(acechar, new PacManDistanceTransition(10, true), perseguir);
        activoFSM.add(acechar, new PacManDistanceTransition(35, false), perseguir);

        // Solo patrullar en los raros casos donde Pacman se pierde de vista (> 75 nodos)
        activoFSM.add(perseguir, new PacManDistanceTransition(75, false), patrullar);
        activoFSM.add(patrullar, new PacManDistanceTransition(40, true), perseguir);

        activoFSM.add(patrullar, new AllyDistanceTransition(6, true), reagrupar);
        activoFSM.add(reagrupar, new AllyDistanceTransition(16, false), patrullar);

        activoFSM.ready(perseguir);

        // --- Submáquina COMESTIBLE (compuesto) ---
        FSM comestibleFSM = new FSM("comestible-v2-" + g);

        // MEJORA V2: La huida principal es dinámica y móvil (HUIR_PACMAN) para no quedar
        // encerrados en un rincón prefijado.
        // Solo si Pacman está pegado (< 12 nodos), intentamos una maniobra extrema a rincón
        comestibleFSM.add(huirPacman, new PacManDistanceTransition(12, true), huirRincon);
        comestibleFSM.add(huirRincon, new PacManDistanceTransition(28, false), huirPacman);

        // Si el tiempo comestible casi se agota (< 20 ticks), retornar hacia casa para contraatacar
        comestibleFSM.add(huirPacman, new EdibleTimeLowTransition(20), guardar);
        comestibleFSM.add(huirRincon, new EdibleTimeLowTransition(20), guardar);

        comestibleFSM.ready(huirPacman); // Estado inicial: huida dinámica y dispersión

        // --- Estados Compuestos ---
        CompoundState activo     = new CompoundState("ACTIVO", activoFSM);
        CompoundState comestible = new CompoundState("COMESTIBLE", comestibleFSM);

        // --- Máquina Principal ---
        FSM mainFSM = new FSM("ghosts-main-v2-" + g);
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
        return "GhostsV2_Pr2CY_OGA";
    }
}

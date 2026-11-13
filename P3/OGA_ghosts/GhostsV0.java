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

public class GhostsV0 extends GhostController {

    private static final boolean DEBUG = false;
    private EnumMap<GHOST, FSM> machines;

    public GhostsV0() {
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
        FSM activoFSM = new FSM("activo-" + g);

        // De PERSEGUIR a INTERCEPTAR si el cruce al que va Pacman esta cerca (< 20 nodos)
        activoFSM.add(perseguir, new TargetJunctionDistanceTransition(20, true), interceptar);
        // De INTERCEPTAR a PERSEGUIR si el cruce queda lejos (> 30 nodos)
        activoFSM.add(interceptar, new TargetJunctionDistanceTransition(30, false), perseguir);

        // De PERSEGUIR a FLANQUEAR si hay un aliado demasiado cerca (< 8 nodos)
        activoFSM.add(perseguir, new AllyDistanceTransition(8, true), flanquear);
        // De FLANQUEAR a PERSEGUIR cuando el aliado ya no estorba (> 15 nodos)
        activoFSM.add(flanquear, new AllyDistanceTransition(15, false), perseguir);

        // De PERSEGUIR a ACECHAR si Pacman esta a distancia media (< 35 nodos)
        activoFSM.add(perseguir, new PacManDistanceTransition(35, true), acechar);
        // De ACECHAR a PERSEGUIR si Pacman se acerca mucho (< 15) o se aleja (> 45)
        activoFSM.add(acechar, new PacManDistanceTransition(15, true), perseguir);
        activoFSM.add(acechar, new PacManDistanceTransition(45, false), perseguir);

        // De PERSEGUIR a PATRULLAR si Pacman esta muy lejos (> 65 nodos)
        activoFSM.add(perseguir, new PacManDistanceTransition(65, false), patrullar);
        // De PATRULLAR a PERSEGUIR si Pacman entra en rango (< 45 nodos)
        activoFSM.add(patrullar, new PacManDistanceTransition(45, true), perseguir);

        // Estados simples adicionales conectados en la red
        activoFSM.add(patrullar, new AllyDistanceTransition(6, true), reagrupar);
        activoFSM.add(reagrupar, new AllyDistanceTransition(16, false), patrullar);

        activoFSM.ready(perseguir);

        // --- Submáquina COMESTIBLE (compuesto) ---
        FSM comestibleFSM = new FSM("comestible-" + g);

        // De HUIR_PACMAN a HUIR_RINCON si Pacman esta muy cerca (< 15 nodos)
        comestibleFSM.add(huirPacman, new PacManDistanceTransition(15, true), huirRincon);
        // De HUIR_RINCON a HUIR_PACMAN si Pacman se aleja (> 25 nodos)
        comestibleFSM.add(huirRincon, new PacManDistanceTransition(25, false), huirPacman);

        // De HUIR_PACMAN a GUARDAR_RESPAWN si queda poco tiempo (< 25 ticks)
        comestibleFSM.add(huirPacman, new EdibleTimeLowTransition(25), guardar);

        comestibleFSM.ready(huirPacman);

        // --- Estados Compuestos ---
        CompoundState activo     = new CompoundState("ACTIVO", activoFSM);
        CompoundState comestible = new CompoundState("COMESTIBLE", comestibleFSM);

        // --- Máquina Principal ---
        FSM mainFSM = new FSM("ghosts-main-" + g);
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
        return "GhostsV0_Pr2CY_OGA";
    }
}

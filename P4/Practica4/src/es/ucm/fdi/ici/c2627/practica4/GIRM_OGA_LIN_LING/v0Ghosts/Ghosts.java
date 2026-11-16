package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.v0Ghosts;

import java.util.EnumMap;
import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.MaxActionSelector;
import es.ucm.fdi.ici.fuzzy.FuzzyEngine;
import pacman.controllers.GhostController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Ghosts extends GhostController {

	static final String FCL_PATH = "bin/es/ucm/fdi/ici/c2627/practica4/GIRM_OGA_LIN_LING/v0Ghosts/";
	
    private final EnumMap<GHOST, FuzzyEngine> motores = new EnumMap<>(GHOST.class);

    public Ghosts() {
        this("ghosts-v0.fcl"); // nombre del fichero que contiene las reglas traducidas
    }

    public Ghosts(String ficheroFcl) {
        setName("Ghosts GIRM_OGA_LIN_LING");
        setTeam("GIRM_OGA_LIN_LING");
        for (GHOST g : GHOST.values()) {
            Action[] acciones = {
                new PerseguirAction(g),
                new HuirAction(g),
                new EmboscarAction(g),
                new DispersarAction(g),
                new GuardarPPillAction(g)
            };
            motores.put(g, new FuzzyEngine(g.name(), FCL_PATH + ficheroFcl, "Ghost",
                    new MaxActionSelector(acciones)));
        }
    }

    public FuzzyEngine getMotor(GHOST g) { return motores.get(g); }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        EnumMap<GHOST, MOVE> movimientos = new EnumMap<>(GHOST.class);
        for (GHOST g : GHOST.values()) {
            if (!game.doesGhostRequireAction(g)) continue;
            movimientos.put(g, motores.get(g).run(new GhostsInput(game, g).getFuzzyValues(), game));
        }
        return movimientos;
    }
}
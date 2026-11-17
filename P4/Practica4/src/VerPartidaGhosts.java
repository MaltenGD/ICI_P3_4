import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.depurar.TrazaBorrosa;
import es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.ghosts.Ghosts;
import es.ucm.fdi.ici.rivales.MsPacManAvanzado;
import pacman.Executor;
import pacman.game.Constants.GHOST;

/** una partida con los fantasmas elegidos contra el rival avanzado */
public class VerPartidaGhosts {
    public static void main(String[] args) {
        Ghosts fantasmas = args.length == 0 ? new Ghosts() : new Ghosts("ghosts-" + args[0] + ".fcl");
        TrazaBorrosa traza = args.length > 1
                ? new TrazaBorrosa("sue", fantasmas.getMotor(GHOST.SUE), true) : null;

        Executor executor = new Executor.Builder()
                .setTickLimit(4000)
                .setVisual(true)
                .setScaleFactor(2.5)
                .build();
        executor.runGame(new MsPacManAvanzado(), fantasmas, 40);
        if (traza != null) traza.resumen();
    }
}

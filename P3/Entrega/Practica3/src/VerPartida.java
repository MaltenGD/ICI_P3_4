import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.MsPacMan;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.depurar.TrazaReglas;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.Ghosts;
import pacman.Executor;

/** Una partida en pantalla, con la traza de las reglas de Ms. Pac-Man en la consola. */
public class VerPartida {

    public static void main(String[] args) {
        Executor executor = new Executor.Builder()
                .setTickLimit(4000)
                .setVisual(true)
                .setScaleFactor(3.0)
                .build();

        MsPacMan pacman = new MsPacMan();
        Ghosts ghosts = new Ghosts();
        TrazaReglas traza = new TrazaReglas("MSPACMAN", pacman.getMotor(), true);   // true: sólo los cambios

        executor.runGame(pacman, ghosts, 40);   // 40 ms por turno: más, para verlo despacio
        traza.resumen();
    }
}

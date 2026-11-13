package es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.depurar;

import java.util.*;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.MsPacMan;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.mspacman.Caminos;
import pacman.controllers.GhostController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

// comprueba movimientos y reglas en partidas reales; no se conecta al controlador normal
public class ComprobarFusion {
    public static void main(String[] args) throws Exception {
        String[] nombres={"GhostsBasico","GhostsIntermedio","GhostsAvanzado"};
        Map<String,Long> disparos=new TreeMap<>();int movimientos=0;
        int partidas=args.length==0 ? 10 : Integer.parseInt(args[0]);
        for(String nombre:nombres) {
            MsPacMan pacman=new MsPacMan("mspacman-v4.clp");
            CoberturaReglas cobertura=new CoberturaReglas(pacman.getMotor());
            GhostController ghosts=(GhostController)Class.forName("es.ucm.fdi.ici.rivales."+nombre).getDeclaredConstructor().newInstance();
            for(int i=0;i<partidas;i++) {
                Game game=new Game(8001+i);pacman.preCompute(nombre);ghosts.preCompute(pacman.getClass().getName());
                while(!game.gameOver() && game.getTotalTime()<=4000) {
                    MOVE m=pacman.getMove(game.copy(),System.currentTimeMillis()+40);
                    if(!Caminos.legal(game,m))throw new IllegalStateException("movimiento no permitido: "+m);
                    movimientos++;
                    game.advanceGame(m,ghosts.getMove(game.copy(),System.currentTimeMillis()+40));
                }
                pacman.postCompute();ghosts.postCompute();
            }
            for(Map.Entry<String,Long> e:cobertura.get().entrySet())disparos.merge(e.getKey(),e.getValue(),Long::sum);
        }
        for(Map.Entry<String,Long> e:disparos.entrySet())System.out.println(e.getKey()+": "+e.getValue());
        System.out.println("movimientos legales: "+movimientos+" | partidas: "+(3*partidas));
        int sinActivar=0;for(long n:disparos.values())if(n==0)sinActivar++;
        System.out.println("reglas sin activar: "+sinActivar);
        if(disparos.size()!=16 || sinActivar!=0)throw new IllegalStateException("falta cubrir alguna regla de v4");
    }
}

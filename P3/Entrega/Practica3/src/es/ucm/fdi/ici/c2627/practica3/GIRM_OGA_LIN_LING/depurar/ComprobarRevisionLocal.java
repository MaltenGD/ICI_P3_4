package es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.depurar;

import java.util.*;
import pacman.game.Game;
import pacman.game.Constants.MOVE;
import pacman.controllers.GhostController;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.MsPacMan;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.UltimaAccion;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.companera.actions.MapaCostes;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.fusion.BusquedaCorta;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.mspacman.Caminos;
import es.ucm.fdi.ici.rules.RuleEngineObserver;

public class ComprobarRevisionLocal implements RuleEngineObserver {
    private String accion="";
    private int prioridad;
    public void actionFired(String action, Collection<String> facts) {
        accion=UltimaAccion.slot(action,"id","");
        prioridad=Integer.parseInt(UltimaAccion.slot(action,"priority","0"));
    }
    public void ruleEngineReset() {}
    public static void main(String[] args)throws Exception {
        int cambios=0, revisiones=0, pasos=0;
        for(String nombre:Arrays.asList("Basico","Intermedio","Avanzado")) {
            MsPacMan pacman=new MsPacMan("mspacman-v4.clp");
            ComprobarRevisionLocal observador=new ComprobarRevisionLocal();pacman.getMotor().addObserver(observador);
            MapaCostes mapa=new MapaCostes();
            GhostController ghosts=(GhostController)Class.forName("es.ucm.fdi.ici.rivales.Ghosts"+nombre).getDeclaredConstructor().newInstance();
            for(int i=0;i<10;i++) {
                Game game=new Game(9001+i);pacman.preCompute(nombre);ghosts.preCompute(pacman.getClass().getName());
                while(!game.gameOver() && game.getTotalTime()<=4000) {
                    MOVE elegida=pacman.getMove(game.copy(),System.currentTimeMillis()+40);
                    if(!Caminos.legal(game,elegida))throw new IllegalStateException("movimiento ilegal");
                    if(observador.accion.equals("huir") || observador.accion.equals("comer") && observador.prioridad!=70) {
                        boolean huir=observador.accion.equals("huir");
                        MOVE prevista=mapa.decidir(game,huir?MapaCostes.Fase.HUIDA:MapaCostes.Fase.NORMAL);
                        int[] comida=game.getActivePillsIndices();if(comida.length==0)comida=game.getActivePowerPillsIndices();
                        MOVE revisada=BusquedaCorta.revisar(game,prevista,huir?null:comida,huir);
                        if(revisada!=elegida)throw new IllegalStateException("la revision no coincide con la accion");
                        revisiones++;if(revisada!=prevista)cambios++;
                    }
                    pasos++;game.advanceGame(elegida,ghosts.getMove(game.copy(),System.currentTimeMillis()+40));
                }
            }
        }
        System.out.println("movimientos legales: "+pasos+"; revisiones: "+revisiones+"; cambios por busqueda corta: "+cambios);
        if(cambios==0)throw new IllegalStateException("no se ha observado ninguna correccion local");
    }
}

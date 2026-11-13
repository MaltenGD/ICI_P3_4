package es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.fusion;

import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.companera.actions.MapaCostes;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.mspacman.actions.IrObjetivoAction;
import jess.Fact;
import jess.JessException;
import pacman.game.Game;
import pacman.game.Constants.MOVE;

// las reglas eligen la intencion; el mapa ayuda a recorrer los pasillos
public class AccionFusion extends IrObjetivoAction {
    // debe coincidir con la prioridad de terminarNivel en el clp
    private static final int PRIORIDAD_TERMINAR = 70;
    private final String id;
    private final MapaCostes mapa;
    private int prioridad;

    public AccionFusion(String id, MapaCostes mapa) {
        super(id); this.id = id; this.mapa = mapa;
    }

    @Override
    public void parseFact(Fact hecho) {
        super.parseFact(hecho);
        try { prioridad = hecho.getSlotValue("priority").intValue(null); }
        catch (JessException e) { prioridad = 0; }
    }

    @Override
    public MOVE execute(Game game) {
        // estos objetivos necesitan seguir la ruta elegida, no una atraccion general
        if (id.equals("irAPPill") || id.equals("perseguir") || id.equals("interceptar")
                || id.equals("irZona") || (id.equals("comer") && prioridad == PRIORIDAD_TERMINAR))
            return super.execute(game);
        boolean huir = id.equals("huir");
        MOVE previsto = mapa.decidir(game, huir ? MapaCostes.Fase.HUIDA : MapaCostes.Fase.NORMAL);
        int[] comida = game.getActivePillsIndices();
        if (comida.length == 0) comida = game.getActivePowerPillsIndices();
        return BusquedaCorta.revisar(game, previsto, huir ? null : comida, huir);
    }
}

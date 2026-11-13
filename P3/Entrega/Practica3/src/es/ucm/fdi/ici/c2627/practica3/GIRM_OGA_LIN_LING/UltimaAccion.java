package es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import es.ucm.fdi.ici.rules.RuleEngineObserver;

// recordamos lo que gano, no un estado con transiciones escondidas
public class UltimaAccion implements RuleEngineObserver {
    private String id = "nada", fantasma = "ninguno";
    private int nodo = -1;

    public String get() { return id; }
    public String fantasma() { return fantasma; }
    public int nodo() { return nodo; }

    public void olvidar() { id = "nada"; fantasma = "ninguno"; nodo = -1; }

    @Override
    public void actionFired(String action, Collection<String> facts) {
        id = slot(action, "id", "nada");
        fantasma = slot(action, "fantasma", "ninguno");
        try { nodo = Integer.parseInt(slot(action, "nodo", "-1")); }
        catch (NumberFormatException e) { nodo = -1; }
    }

    public static String slot(String hecho, String nombre, String defecto) {
        Matcher m = Pattern.compile("\\(" + Pattern.quote(nombre) + "\\s+([^\\s)]+)\\)").matcher(hecho);
        return m.find() ? m.group(1) : defecto;
    }

    @Override public void ruleEngineReset() { }
}

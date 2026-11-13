package es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.depurar;

import java.lang.reflect.Field;
import java.util.*;
import es.ucm.fdi.ici.rules.RuleEngine;
import jess.*;

// solo se conecta desde las herramientas de pruebas, nunca desde el controlador de entrega
public class CoberturaReglas implements JessListener {
    private final Map<String, Long> veces = new TreeMap<String, Long>();

    public CoberturaReglas(Rete rete) {
        for (Iterator<?> it = rete.listDefrules(); it.hasNext();) {
            Defrule regla = (Defrule) it.next();
            veces.put(nombre(regla.getName()), 0L);
        }
        rete.addJessListener(this);
        rete.setEventMask(rete.getEventMask() | JessEvent.DEFRULE_FIRED);
    }

    public CoberturaReglas(RuleEngine motor) { this(reteDe(motor)); }

    public static Rete reteDe(RuleEngine motor) {
        try {
            Field campo = RuleEngine.class.getDeclaredField("jess");
            campo.setAccessible(true);
            return (Rete) campo.get(motor);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("no se puede conectar la cobertura de reglas", e);
        }
    }

    private String nombre(String regla) { return regla.replace("MAIN::", ""); }

    @Override
    public void eventHappened(JessEvent evento) throws JessException {
        if (evento.getType() != JessEvent.DEFRULE_FIRED) return;
        String regla = nombre(((Activation) evento.getObject()).getRule().getName());
        veces.put(regla, veces.getOrDefault(regla, 0L) + 1);
    }

    public Map<String, Long> get() { return new TreeMap<String, Long>(veces); }
}

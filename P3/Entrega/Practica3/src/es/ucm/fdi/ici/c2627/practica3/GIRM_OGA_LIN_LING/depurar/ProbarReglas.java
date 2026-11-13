package es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.depurar;

import java.util.*;
import es.ucm.fdi.ici.c2627.practica3.GIRM_OGA_LIN_LING.MsPacMan;
import jess.*;

public class ProbarReglas {
    private int casos, fallos;

    public static void main(String[] args) throws JessException {
        new ProbarReglas().ejecutar();
    }

    private void ejecutar() throws JessException {
        // probamos las dos ampliaciones con los casos que corresponden a sus reglas
        for (String version : new String[] {"v2", "v2.1", "v4"}) {
            int v = version.equals("v2") ? 4 : 10;
            Rete rete = new Rete();
            rete.batch(MsPacMan.RULES_PATH + "mspacman-" + version + ".clp");
            CoberturaReglas cobertura = new CoberturaReglas(rete);
            probar(rete, version + " comida por defecto", "r1:", "comer",
                    ms(80, "comer", "(comida 10)"));
            probar(rete, version + " peligro", "r4:", "huir", ms(15,"comer",""), salida(0,20,0,20));
            probar(rete, version + " mantener peligro", "r4:", "huir", ms(25,"huir",""), salida(0,20,0,20));
            probar(rete, version + " umbral entrada exacto", "r1:", "comer", ms(20,"comer",""));
            probar(rete, version + " umbral salida exacto", "r1:", "comer", ms(30,"huir",""));
            probar(rete, version + " poder antes del peligro", "r5:", "irAPPill",
                    ms(15,"comer",""), salida(0,20,0,20), objetivo("poder",5,10,""));
            probar(rete, version + " cazar con tiempo", "r6:", "perseguir",
                    ms(80,"comer",""), fantasma("PINKY",2,true,30,100,10));
            probar(rete, version + " peligro se deduce antes que cazar", "r4:", "huir",
                    ms(15,"comer",""), salida(0,20,0,20), fantasma("PINKY",2,true,30,100,10));
            probar(rete, version + " rematar con margen", "r7:", "perseguir",
                    ms(15,"huir",""), salida(0,20,0,20), fantasma("PINKY",2,true,5,100,10));
            probar(rete, version + " tiempo justo no basta", "r1:", "comer",
                    ms(80,"comer",""), fantasma("PINKY",2,true,10,28,10));
            probar(rete, version + " presa cercana insegura no tapa otra", "r6:", "perseguir",
                    ms(80,"comer",""), fantasma("BLINKY",0,true,5,100,0),
                    fantasma("PINKY",2,true,12,100,10));
            if (v >= 3) {
                probar(rete, version + " dos perseguidores sin poder", "r1:", "comer",
                        ms(25,"comer",""), fantasma("BLINKY",0,false,25,0,0),
                        fantasma("SUE",3,false,26,0,0));
                if (cobertura.get().get("variosPerseguidores") == 0) fallos++;
            }
            if (v >= 4) probar(rete, version + " poder con varios perseguidores", "r9:", "irAPPill",
                    ms(25,"comer",""), fantasma("BLINKY",0,false,25,0,0),
                    fantasma("SUE",3,false,26,0,0), objetivo("poder",50,10,""));
            if (v >= 5) probar(rete, version + " mantener poder", "r10:", "irAPPill",
                    ms(80,"irAPPill","(objetivoAnterior 200)"), objetivo("anterior",50,10,""));
            if (v >= 6) probar(rete, version + " terminar nivel", "r11:", "comer",
                    ms(80,"comer","(pildoras 0) (poderes 1)"), objetivo("comida",50,10,""));
            if (v >= 7) probar(rete, version + " interceptar", "r12:", "interceptar",
                    ms(80,"comer",""), fantasma("PINKY",2,true,40,180,10),
                    objetivo("interceptar",20,10,"(fantasma PINKY) (ventaja 12)"));
            if (v >= 8) probar(rete, version + " comer con presa lejana", "r13:", "comer",
                    ms(80,"comer",""), fantasma("PINKY",2,true,80,100,10), objetivo("comida",5,10,""));
            if (v >= 9) probar(rete, version + " zona con comida", "r14:", "irZona",
                    ms(80,"comer","(distComida 20)"), objetivo("zona",20,10,"(cantidad 5)"));
            if (v >= 10) probar(rete, version + " circuito", "r15:", "irZona",
                    ms(80,"comer","(sinComer 130)"), salida(0,10,3,50), salida(1,8,0,20));
            if (version.equals("v4")) {
                probar(rete, "v4 poder oportuno", "r16:", "irAPPill", ms(25,"comer",""),
                        fantasma("BLINKY",0,false,25,0,0), objetivo("poder",20,4,""));
                probar(rete, "v4 poder a veinticinco", "r16:", "irAPPill", ms(30,"comer",""),
                        fantasma("BLINKY",0,false,30,0,0), objetivo("poder",25,4,""));
                probar(rete, "v4 poder a veintiseis", "r1:", "comer", ms(30,"comer",""),
                        fantasma("BLINKY",0,false,30,0,0), objetivo("poder",26,4,""));
                probar(rete, "v4 margen tres no basta", "r1:", "comer", ms(25,"comer",""),
                        fantasma("BLINKY",0,false,25,0,0), objetivo("poder",20,3,""));
                probar(rete, "v4 sin peligro cercano", "r1:", "comer", ms(31,"comer",""),
                        fantasma("BLINKY",0,false,31,0,0), objetivo("poder",20,4,""));
            }
            for (Map.Entry<String,Long> regla : cobertura.get().entrySet()) {
                if (regla.getValue() == 0) {
                    System.out.println("sin caso para " + regla.getKey() + " en " + version); fallos++;
                }
            }
        }
        System.out.println("casos: " + casos + " | fallos: " + fallos);
        if (fallos != 0) throw new IllegalStateException("hay casos de reglas que fallan");
    }

    private String ms(int peligro, String ultima, String extra) {
        return "(MSPACMAN (distFantasma " + peligro + ") (ultima " + ultima + ") " + extra + ")";
    }
    private String fantasma(String nombre, int indice, boolean edible, int d, int t, int margen) {
        return "(FANTASMA (nombre " + nombre + ") (indice " + indice + ") (comestible " + edible
                + ") (distancia " + d + ") (tiempo " + t + ") (margen " + margen + ") (nodo 300))";
    }
    private String objetivo(String tipo, int d, int margen, String extra) {
        return "(OBJETIVO (tipo " + tipo + ") (nodo 200) (distancia " + d + ") (margen " + margen
                + ") (movimiento UP) " + extra + ")";
    }
    private String salida(int indice, int margen, int visitas, int distancia) {
        return "(SALIDA (indice " + indice + ") (movimiento " + (indice == 0 ? "UP" : "RIGHT")
                + ") (nodo 400) (margen " + margen + ") (visitas " + visitas
                + ") (distComida " + distancia + "))";
    }

    private void probar(Rete rete, String nombre, String prefijo, String id, String... hechos) throws JessException {
        casos++; rete.reset();
        for (String hecho : hechos) rete.assertString(hecho);
        rete.run();
        Fact gana = null; int prioridad = Integer.MIN_VALUE;
        for (Iterator<?> it = rete.listFacts(); it.hasNext();) {
            Fact f = (Fact) it.next();
            if (!f.getName().equals("MAIN::ACTION")) continue;
            int p = f.getSlotValue("priority").intValue(null);
            if (p > prioridad) { gana = f; prioridad = p; }
        }
        boolean bien = gana != null && gana.getSlotValue("id").stringValue(null).equals(id)
                && gana.getSlotValue("info").stringValue(null).startsWith(prefijo);
        System.out.println((bien ? "BIEN " : "MAL ") + nombre);
        if (!bien) { fallos++; System.out.println("gana: " + gana + " | esperaba " + prefijo + " " + id); }
    }
}

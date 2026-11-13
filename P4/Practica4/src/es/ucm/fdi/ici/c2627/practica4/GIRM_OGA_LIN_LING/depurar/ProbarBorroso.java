package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.depurar;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Rule;
import net.sourceforge.jFuzzyLogic.rule.Variable;

/**
 * Prueba el .fcl de la v0 (port de la Práctica 3) sin jugar.
 */
public class ProbarBorroso {

    static final String FCL = "bin/es/ucm/fdi/ici/c2627/practica4/GIRM_OGA_LIN_LING/v1MsPacMan/mspacman-v1.fcl";
    static final String BLOQUE = "MsPacMan";
    static int fallos = 0;

    public static void main(String[] args) {
        FIS fis = FIS.load(FCL, true);
        if (fis == null) { System.out.println("No encuentro " + FCL); return; }
        FunctionBlock fb = fis.getFunctionBlock(BLOQUE);

        //   nombre del caso                                               gana esperada   peligro comestible ppill ventaja
        caso(fb, "1. Nada cerca: come tranquilamente",
        	     "comer", 150, 150, 150, -150, 0, 4);

        	caso(fb, "2. Peligro extremo: huir",
        	     "huir", 5, 150, 150, -150, 0, 4);

        	caso(fb, "3. Varios fantasmas cerca",
        	     "huir", 20, 150, 150, -150, 3, 4);

        	caso(fb, "4. Power Pill como escape",
        	     "irAPPill", 5, 150, 10, -5, 1, 4);

        	caso(fb, "5. Ultimas Power Pills",
        	     "irAPPill", 80, 150, 10, -70, 0, 2);
        	
        // Curva de control según distancia al peligro
        System.out.println("\npeligro  salidas -> gana");
        for (int d = 0; d <= 100; d += 5) {
            evaluar(fb, d, 150, 150, -150, 1, 4);
            System.out.printf("%5d   %s -> %s%n", d, salidas(fb), gana(fb));
        }

        System.out.println(fallos == 0 ? "\nTodos los casos bien." : "\n" + fallos + " casos MAL.");
    }

    static void caso(FunctionBlock fb, String nombre, String esperada,
                     double peligro, double comestible, double ppill, double ventaja, double perseguidores, double
                     restantes) {
        evaluar(fb, peligro, comestible, ppill, ventaja, perseguidores, restantes);
        String gana = gana(fb);
        StringBuilder activas = new StringBuilder();
        for (Rule r : fb.getRuleBlocks().values().iterator().next().getRules())
            if (r.getDegreeOfSupport() > 0) activas.append(String.format(" R%s:%.2f", r.getName(), r.getDegreeOfSupport()));

        boolean bien = gana.equals(esperada);
        if (!bien) fallos++;
        System.out.printf("%s  %-48s gana %-9s (esperaba %s)%n      salidas: %s%n      reglas: %s%n",
                bien ? "BIEN" : "MAL ", nombre, gana, esperada, salidas(fb), activas);
    }

    static void evaluar(
            FunctionBlock fb,
            double peligro,
            double comestible,
            double ppill,
            double ventaja,
            double perseguidores,
            double restantes) {

        fb.setVariable("peligro", peligro);
        fb.setVariable("comestible", comestible);
        fb.setVariable("ppill", ppill);
        fb.setVariable("ventajaPPill", ventaja);

        fb.setVariable("perseguidoresCerca", perseguidores);
        fb.setVariable("ppillsRestantes", restantes);

        fb.evaluate();
    }

    static String gana(FunctionBlock fb) {
        String gana = "(ninguna)";
        double max = Double.NEGATIVE_INFINITY;
        for (Variable v : fb.getVariables().values())
            if (v.isOutput() && v.getLatestDefuzzifiedValue() > max) {
                max = v.getLatestDefuzzifiedValue();
                gana = v.getName();
            }
        return gana;
    }

    static String salidas(FunctionBlock fb) {
        StringBuilder sb = new StringBuilder();
        for (Variable v : new java.util.TreeMap<>(fb.getVariables()).values())
            if (v.isOutput()) sb.append(String.format("%s=%.1f ", v.getName(), v.getLatestDefuzzifiedValue()));
        return sb.toString().trim();
    }
}
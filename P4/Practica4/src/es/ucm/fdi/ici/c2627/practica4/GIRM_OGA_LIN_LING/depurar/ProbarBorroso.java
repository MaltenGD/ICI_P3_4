package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.depurar;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Rule;
import net.sourceforge.jFuzzyLogic.rule.Variable;

/**
 * Prueba el .fcl de la v2 (corrección táctica de Power Pills y comida restante) sin jugar.
 */
public class ProbarBorroso {

    static final String FCL_V2 = "bin/es/ucm/fdi/ici/c2627/practica4/GIRM_OGA_LIN_LING/v2MsPacMan/mspacman-v2.fcl";
    static final String BLOQUE = "MsPacMan";
    static int fallos = 0;

    public static void main(String[] args) {
        FIS fis = FIS.load(FCL_V2, true);
        if (fis == null) { System.out.println("No encuentro " + FCL_V2); return; }
        FunctionBlock fb = fis.getFunctionBlock(BLOQUE);

        System.out.println("=== Pruebas Unitarias FCL v2 ===");
        // caso: (fb, nombre, esperada, peligro, comestible, ppill, ventaja, perseguidores, comidaRestante)

        // 1. Sin peligro y comida normal: come
        caso(fb, "1. Sin peligro: come tranquilamente",
             "comer", 150, 150, 150, -150, 0, 150);

        // 2. Peligro extremo (fantasma a distancia 5): huir
        caso(fb, "2. Peligro extremo: huir",
             "huir", 5, 150, 150, -150, 1, 150);

        // 3. Varios perseguidores cerca y sin ventaja a ppill: huir
        caso(fb, "3. Varios fantasmas cerca sin escape: huir",
             "huir", 20, 150, 150, -150, 3, 120);

        // 4. Peligro con ventaja a Power Pill: irAPPill (salvavidas)
        caso(fb, "4. Peligro pero llego antes a PPill: irAPPill",
             "irAPPill", 25, 150, 20, 25, 1, 120);

        // 5. Múltiples perseguidores pero con ventaja a PPill: irAPPill
        caso(fb, "5. Múltiples perseguidores con ventaja a PPill",
             "irAPPill", 20, 150, 15, 15, 2, 80);

        // 6. Fantasmas lejos y PPill cerca: NO comerla! Debe seguir comiendo (no malgastar PPill)
        caso(fb, "6. Fantasmas lejos y PPill cerca: NO comerla (comer)",
             "comer", 120, 150, 10, -50, 0, 100);

        // 7. Fantasma comestible cerca: perseguir
        caso(fb, "7. Fantasma comestible a tiro: perseguir",
             "perseguir", 150, 20, 150, -150, 0, 100);

        // 8. Poca comida restante en el laberinto y sin peligro: comer prioritario
        caso(fb, "8. Pocas píldoras restantes en laberinto: comer",
             "comer", 100, 150, 80, -30, 0, 15);

        // Curva de control según distancia al peligro
        System.out.println("\npeligro  salidas -> gana");
        for (int d = 0; d <= 100; d += 10) {
            evaluar(fb, d, 150, 150, -150, 1, 100);
            System.out.printf("%5d   %s -> %s%n", d, salidas(fb), gana(fb));
        }

        System.out.println(fallos == 0 ? "\nTodos los casos bien." : "\n" + fallos + " casos MAL.");
    }

    static void caso(FunctionBlock fb, String nombre, String esperada,
                     double peligro, double comestible, double ppill, double ventaja,
                     double perseguidores, double comidaRestante) {
        evaluar(fb, peligro, comestible, ppill, ventaja, perseguidores, comidaRestante);
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
            double comidaRestante) {

        fb.setVariable("peligro", peligro);
        fb.setVariable("comestible", comestible);
        fb.setVariable("ppill", ppill);
        fb.setVariable("ventajaPPill", ventaja);
        fb.setVariable("perseguidoresCerca", perseguidores);
        fb.setVariable("comidaRestante", comidaRestante);

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
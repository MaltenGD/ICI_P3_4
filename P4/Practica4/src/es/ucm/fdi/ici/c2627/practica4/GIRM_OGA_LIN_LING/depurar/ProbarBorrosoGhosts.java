package es.ucm.fdi.ici.c2627.practica4.GIRM_OGA_LIN_LING.depurar;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;

public class ProbarBorrosoGhosts {
    private static final String RUTA = "es/ucm/fdi/ici/c2627/practica4/GIRM_OGA_LIN_LING/";

    private static FunctionBlock cargar(int v) {
        Path ruta = Path.of("bin", RUTA, "v" + v + "Ghosts", "ghosts-v" + v + ".fcl");
        if (!Files.isRegularFile(ruta)) ruta = Path.of("Practica4").resolve(ruta);
        FIS fis = FIS.load(ruta.toString(), false);
        if (fis == null) throw new IllegalStateException("no se encuentra " + ruta);
        return fis.getFunctionBlock("Ghost");
    }

    static void poner(FunctionBlock fb, String nombre, int valor) {
        if (fb.getVariable(nombre) != null) fb.setVariable(nombre, valor);
    }

    private static class Caso {
        int soy, tiempo, distancia = 45, aliado = 8, desvio = 0, asignado, poder = 40, comidos;
    }

    private static String evaluar(FunctionBlock fb, Caso c) {
        poner(fb, "soy", c.soy);
        poner(fb, "comestible", c.tiempo > 0 ? 1 : 0);
        poner(fb, "tiempo", c.tiempo);
        poner(fb, "distPacman", c.distancia);
        poner(fb, "distAliado", c.aliado);
        poner(fb, "desvio", c.desvio);
        poner(fb, "asignado", c.asignado);
        poner(fb, "distPoder", c.poder);
        poner(fb, "comidos", c.comidos);
        fb.evaluate();

        String gana = "";
        double primero = -1, segundo = -1;
        for (String id : new String[]{"perseguir", "huir", "interceptar", "defender"}) {
            if (fb.getVariable(id) == null) continue;
            double grado = fb.getVariable(id).getValue();
            if (!Double.isFinite(grado)) throw new IllegalStateException("salida no valida " + id);
            if (grado > primero) {
                segundo = primero;
                primero = grado;
                gana = id;
            } else {
                segundo = Math.max(segundo, grado);
            }
        }
        if (primero <= 0 || primero - segundo < 1e-6)
            throw new IllegalStateException("hueco o empate: papel " + c.soy + ", distancia " + c.distancia
                    + ", desvio " + c.desvio + ", comidos " + c.comidos + ", pildora " + c.poder);
        return gana;
    }

    private static void caso(FunctionBlock fb, Caso c, String esperado, String descripcion) {
        String gana = evaluar(fb, c);
        if (!esperado.equals(gana))
            throw new IllegalStateException(descripcion + ": esperamos " + esperado + " y sale " + gana);
        System.out.println("BIEN: " + descripcion + " -> " + gana);
    }

    public static void main(String[] args) {
        for (int v = 2; v <= 4; v++) {
            FunctionBlock fb = cargar(v);
            System.out.println("\nfantasmas v" + v);
            Caso c = new Caso();
            c.tiempo = 13;
            caso(fb, c, "huir", "fantasma comestible");
            c.tiempo = 0;
            caso(fb, c, "perseguir", "blinky mantiene la presion");
            c.soy = 1;
            caso(fb, c, v >= 3 ? "interceptar" : "perseguir", "pinky tiene un desvio corto");
            c.desvio = 35;
            caso(fb, c, "perseguir", "el desvio es demasiado largo");
            c.soy = 2;
            c.desvio = 2;
            caso(fb, c, v >= 3 ? "interceptar" : "perseguir", "inky tiene un desvio corto");
            c.distancia = 8;
            caso(fb, c, "perseguir", "inky ya tiene a pacman cerca");
            if (v == 4) {
                c.distancia = 45;
                c.asignado = 1;
                c.comidos = 2;
                caso(fb, c, "interceptar", "poder asignado pero pacman no ha cazado bastante");
                c.comidos = 3;
                caso(fb, c, "defender", "poder asignado y pacman ya ha cazado a tres");
                c.distancia = 7;
                caso(fb, c, "perseguir", "pacman esta encima y se abandona la guardia");
                c.distancia = 15;
                c.poder = 20;
                caso(fb, c, "interceptar", "disputa cercana: inky deja la guardia y vuelve al corte");
                c.distancia = 45;
                c.asignado = 0;
                caso(fb, c, "interceptar", "sin poder asignado");
                c.asignado = 1;
                c.tiempo = 40;
                caso(fb, c, "huir", "comestible aunque haya poder asignado");
            }

            int puntos = 0;
            for (int rol = 0; rol < 4; rol++) {
                c.soy = rol;
                for (int distancia = 0; distancia <= 150; distancia++) {
                    c.distancia = distancia;
                    for (int tiempo : new int[]{1, 8, 20, 50, 120}) {
                        c.tiempo = tiempo;
                        if (!"huir".equals(evaluar(fb, c))) throw new IllegalStateException("no huye");
                        puntos++;
                    }
                    c.tiempo = 0;
                    for (int desvio : new int[]{-16, 0, 7, 9, 10, 22, 40}) {
                        c.desvio = desvio;
                        c.asignado = 0;
                        evaluar(fb, c);
                        puntos++;
                    }
                    if (v != 4 || rol == 0) continue;
                    c.asignado = 1;
                    for (int poder : new int[]{7, 15, 23, 24, 25, 50}) {
                        c.poder = poder;
                        for (int comidos = 0; comidos <= 5; comidos++) {
                            c.comidos = comidos;
                            evaluar(fb, c);
                            puntos++;
                        }
                    }
                }
            }
            System.out.println(puntos + " puntos sin huecos ni empates ganadores");
            if (v >= 3) {
                c = new Caso();
                c.soy = 1;
                System.out.println("desvio | perseguir | interceptar | gana");
                for (c.desvio = 0; c.desvio <= 16; c.desvio++) {
                    String gana = evaluar(fb, c);
                    System.out.printf(Locale.ROOT, "%d | %.3f | %.3f | %s%n", c.desvio,
                            fb.getVariable("perseguir").getValue(), fb.getVariable("interceptar").getValue(), gana);
                }
            }
        }
    }
}

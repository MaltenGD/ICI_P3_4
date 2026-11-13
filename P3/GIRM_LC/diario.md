Nombres de los integrantes : Guillermo Isaac Ramos Medina y Liling Chen

# **Diario de la Práctica 2**

Medimos siempre 50 partidas contra:

- R1: rival-basico
- R2: rival-intermedio
- R3: rival-avanzado
  
Ejecutor:

     private static void imprimirResultados( String nombre, Stats[] resultados) {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("       " + nombre);
        System.out.println("==============================================");

        for (Stats stats : resultados) {

            System.out.println("----------------------------------------------");

            System.out.println("Métrica: " + stats.getDescription());

            System.out.println("Media:   " + stats.getAverage());

            System.out.println("Mínimo:  " + stats.getMin());

            System.out.println("Máximo:  " + stats.getMax());

            System.out.println("Desv.:   " + stats.getStandardDeviation());

            System.out.println("N:       " + stats.getN());
        }

        System.out.println("==============================================");
    } 


## **MS PACMAN**
### Versiones

#### **1) MsPacManV0Me v.1.0** – Sábado 17 de octubre del 2026 (línea base)

Codigo : [MsPacManV0Me](https://github.com/GuillermoY/ICI_repo/blob/8117dca11b6d916295149dd04698fef361a7db90/Li/P1Viejo/MsPacManEntrega.java)

# falta poner bien el archivo 

Contenido/Cambios:
Implementamos una maquina de estados base con los siguientes superestados: Con poder y sin poder. Esta máquina de estados incluye modificaciones en "combinarCoste" para que los costes de los nodos tengan prioridad según su estado. 
<br>Dentro de cada superestado se tienen los siguientes estados principales:

* Con poder:
  * <strong>Normal</strong>: Si no hay fantasmas comestibles a 30 nodos o menos de distancia se comerá el mayor número de píldoras posible.
  * <strong>Huir</strong>: Si hay algún fantasma sin vulnerabilidad a 30 nodos o menos de distancia. Se incrementará el coste de los caminos seguros con intersecciones.
  * <strong>Caza</strong>: Se comerán fantasmas a 30 nodos o menos de distancia antes de su tiempo de vulnerabilidad. Se incrementará el coste de los fantasmas comestibles.
* Sin poder:
    * <strong>Normal</strong>: Si no hay fantasmas a 30 nodos o menos de distancia se comerá el mayor número de píldoras posible.
    * <strong>Huir</strong>: Si hay más de 2 fantasmas persiguiendo activamente al PacMan. Se incrementará el coste de los caminos seguros con intersecciones.

Apoyo de IA: No

Partida MsPacManV4:

| Rival | Puntos | Media de puntos | Desviación |
| :---: | :---: | :---: | :---: |
| R1 | 12080.0 ± 1260.0 | 6376.8 | 2609.611308925808 |
| R2 | 15680.0 ± 1460.0 |  6186.6  | 3274.732336779759 |
| R3 | 6430.0 ± 1100.0 | 2797.8 | 1055.9632221486752 |

Partida MsPacManV0Me:

| Rival | Puntos | Media de puntos | Desviación |
| :---: | :---: | :---: | :---: |
| R1 | 12630.0  ± 1250.0 | 7137.4 | 3160.00375225817 |
| R2 | 18570.0 ± 1400.0 | 7501.0 | 4271.055525993832 |
| R3 |  7230.0 ± 1190.0  | 3150.4 |  1390.6846940868181 |

Tiempo de partida MsPacManV4:

| Rival | Tiempo | Media de tiempo | Desviación |
| :---: | :---: | :---: | :---: |
| R1 | 4001.0 ± 97.0 | 2182.08 | 1303.3143118816565 |
| R2 | 4001.0 ± 80.0  | 2092.88 | 1474.1445619028532 |
| R3 | 4001.0 ± 175.0 | 1300.0 | 744.452243503331 |

Tiempo de partida MsPacManV0Me:

| Rival | Tiempo | Media de tiempo | Desviación |
| :---: | :---: | :---: | :---: |
| R1 | 3447.0 ± 64.0 | 1207.94 | 790.1250588175446 |
| R2 | 4001.0 ± 181.0 | 2037.76 | 1322.622912925645 |
| R3 | 2315.0 ± 189.0 | 1306.38 | 504.3864282270009 |

---

**USANDO DE RIVAL R3**

===== MsPacManV4 =====
|Pacman|Media|Min|Max|Desv|Partidas|
|:---:|:---:|:---:|:---:|:---:|:---:|
MsPacManV4 | Media: 2797.8 | Min: 1100.0 | Max: 6430.0 | Desv.: 1055.9632221486752 | N: 50
MsPacManV4 Ticks | Media: 1300.0 | Min: 175.0 | Max: 4001.0 | Desv.: 744.452243503331 | N: 50

===== MsPacManV0Me =====
|Pacman|Media|Min|Max|Desv|Partidas|
|:---:|:---:|:---:|:---:|:---:|:---:|
MsPacManV0Me | Media: 3150.4 | Min: 1190.0 | Max: 7230.0 | Desv.: 1390.6846940868181 | N: 50
MsPacManV0Me Ticks | Media: 1306.38 | Min: 189.0 | Max: 2315.0 | Desv.: 504.3864282270009 | N: 50

**Comparación con la MsPacManV4 y MsPacManV0Me del pacman**

D      = 3150,4 − 2797,8 = +352,6

ET_D   = √(196,67² + 149,33²) ≈ 246,9

2·ET_D = 493,9

|D| = 352,6 < 493,9 → no se distingue de ruido

#### **1) MsPacManV1Me v.1.0** – Sábado 17 de octubre del 2026

El proposito de este controlador es testear con las fases pero nos dimos cuenta de que no hay mucha diferencia si la fase de CAZA va antes que HUIDA o al reves:

Partida MsPacManV0Me:

| Rival | Puntos | Media de puntos | Desviación |
| :---: | :---: | :---: | :---: |
| GhostEntrega | 12470.0  ± 1290.0 | 5996.8 | 3106.9122673057077 |

Partida MsPacManV1Me:

| Rival | Puntos | Media de puntos | Desviación |
| :---: | :---: | :---: | :---: |
| GhostEntrega | 13990.0  ± 1290.0 | 5540.4 | 2911.622003948581 |


**Comparación con la MsPacManV0Me y MsPacManV1Me del pacman**

Margen V0Me = 12470,0 − 5996,8 = 6473,2

Margen V1Me = 13990,0 − 5540,4 = 8449,6

D      = 8449,6 − 6473,2 = +1976,4

ET_D   = √(1824,3² + 4257,7²) ≈ 4632,1

2·ET_D = 9264,3

|D| < 2·ET_D → no se distingue de ruido

## **GHOST**
### Versiones

#### **1) GhostV0 v.1.0** – Sábado 17 de octubre del 2026 (línea base)

Codigo : [GhostV0](https://github.com/GuillermoY/ICI_repo/blob/8117dca11b6d916295149dd04698fef361a7db90/Li/P1Viejo/GhostV0ME.java)

Contenido/Cambios: Este controlador se extiende de GhostEntrega, dentro de este implementamos las máquinas de estados y cambiamos dos métodos principales entre estos:

* **segunSuPapel**: 
  * En el papel de PINKY, ahora calcula el próximo cruce al que se dirige Pac-Man en vez de perseguir al Pac-Man como BLINKY.


        case PINKY: { // Rosa: intenta interceptar a Ms. Pac-Man
                int cruce = proximoCruceDe(game);

                if (cruce != -1) {
                    return primerMovimientoDe(
                            game,
                            game.getShortestPath(nodo, cruce, ultimo));
                }

                // Si no encontramos ningún cruce, perseguimos a Pac-Man
                return primerMovimientoDe(
                        game,
                        game.getShortestPath(nodo, pacman, ultimo));
        }



* **huirConCriterio**:
  * Cambiamos la lógica para que se excluya de si mismo en el bucle, diferencie entre si es un fantasma comestible o no y si se trata del fantasma no comestible más cercano a él entonces se dirije hacia ese fantasma, si no hay fantasmas no comestibles entonces huye de Pac-Man.


        private MOVE huirConCriterio(Game game, int nodo, MOVE ultimo, int pacman, GHOST ghostType) {

            GHOST mejorCompanero = null;
            int menorDistancia = Integer.MAX_VALUE;

            // Buscamos el compañero no comestible más cercano
            // que se encuentre fuera de la guarida.
            for (GHOST otro : GHOST.values()) {

                if (otro == ghostType) {
                    continue;
                }

                if (game.getGhostLairTime(otro) > 0) {
                    continue;
                }

                if (game.isGhostEdible(otro)) {
                    continue;
                }

                int nodoOtro = game.getGhostCurrentNodeIndex(otro);

                if (nodoOtro == -1) {
                    continue;
                }

                int distancia =
                        game.getShortestPathDistance(nodo, nodoOtro);

                if (distancia >= 0 && distancia < menorDistancia) {
                    menorDistancia = distancia;
                    mejorCompanero = otro;
                }
            }

            // Si existe un compañero adecuado, nos acercamos a él
            if (mejorCompanero != null) {
                int objetivo =
                        game.getGhostCurrentNodeIndex(mejorCompanero);

                int[] camino =
                        game.getShortestPath(nodo, objetivo, ultimo);

                return primerMovimientoDe(game, camino);
            }

            // Si no hay compañeros válidos, huimos de Pac-Man
            return game.getApproximateNextMoveAwayFromTarget(
                    nodo,
                    pacman,
                    ultimo,
                    DM.PATH);
        }

Además implementamos otro método auxiliar:
* **proximoCruceDe**: este método toma la última dirección de Pac-Man para calcular el índice del nodo de la proxima intersección que tiene delante el Pac-Man. De esta forma se sabrá con antelación dondde se tiene que tomar una decisión de giro los fantasmas.
* 
        private int proximoCruceDe(Game game) {
            int pacman = game.getPacmanCurrentNodeIndex();
            MOVE direccion = game.getPacmanLastMoveMade();

            // Sin dirección conocida, no podemos calcular hacia dónde va
            if (direccion == null || direccion == MOVE.NEUTRAL) {
                return -1;
            }

            // Avanzamos primero en la dirección de Pac-Man
            int actual = game.getNeighbour(pacman, direccion);

            if (actual == -1) {
                return -1;
            }

            MOVE movimientoAnterior = direccion;
            int limite = 0;

            // Buscamos la primera intersección que haya delante
            while (actual == -1 && limite < 1000) {
                MOVE[] opciones =
                        game.getPossibleMoves(actual, movimientoAnterior);

                if (opciones == null || opciones.length == 0) {
                    return -1;
                }

                // En un pasillo normalmente solo hay una opción
                MOVE siguiente = opciones[0];
                int vecino = game.getNeighbour(actual, siguiente);

                if (vecino == -1) {
                    return -1;
                }

                actual = vecino;
                movimientoAnterior = siguiente;
                limite++;
            }

            if (limite >= 1000) {
                return -1;
            }

            return actual;
        }

Apoyo de la IA: No 

Partida GhostV0:

| Rival | Puntos | Media de puntos | Desviación |
| :---: | :---: | :---: | :---: |
| MsPacManV0ME | 12900.0  ± 1570.0 | 6428.0 | 2984.4488090562973 |

Tiempo de partida GhostV0:

| Rival | Tiempo | Media de tiempo | Desviación |
| :---: | :---: | :---: | :---: |
| MsPacManV0ME | 4001.0 ± 51.0 | 1719.76 | 1333.1056047700752 |



**Sábado 17 de Octubre del 2026**

Editamos unas condiciones del bucle while del GhostV0ME ya que actual nunca iria a ser actual==-1. (actual = game.getNeighbour(pacman, direccion);).
Lo cambiamos a while(actual != -1 && !game.isJunction(actual) && limite <1000).

#### **2) GhostV0ME v.1.0** – Sábado 17 de octubre del 2026 (línea base)
Código: [GhostV0ME](https://github.com/GuillermoY/ICI_repo/blob/8117dca11b6d916295149dd04698fef361a7db90/Li/P1Viejo/GhostV0ME.java)

Contenido/Cambios: Implementamos la máquina de estados teniendo las siguientes fases:
* PERSEGUIR
* EVADIR
* HUIDA
* NORMAL

Para cada fantasma tendrá un estado diferente que se da en el momento a la hora de que pida un movimiento.

En este controlador hemos cambiado:
1) getMove:
    Miramos si el fantasma esta en la guarida y si no entonces calculamos su fase actual con ***detectarFase*** con la fase que devuelve el método anterior lo usamos en ***actualizarEstados*** para que realize dicha acción dependiendo de su fase con un switch siendo este:
    
        private void actualizarEstados(Game game, FaseG estado, GHOST f) {
            switch (estado) {
        
                case HUIDA:
                    moves.put(f, huirConCriterio(
                            game,
                            game.getGhostCurrentNodeIndex(f),
                            game.getGhostLastMoveMade(f),
                            game.getPacmanCurrentNodeIndex(),
                            f));
                    break;
        
                case EVADIR:
                    moves.put(f, movimientoDispersar(game, f));
                    break;
        
                case PERSEGUIR:
                case NORMAL:
                default:
                    // Cada fantasma utiliza su estrategia propia.
                    moves.put(f, segunSuPapel(game, f));
                    break;
            }
	    }

        private FaseG detectarFase(Game game, GHOST fantasma, boolean dispersar) {

            if (game.isGhostEdible(fantasma)) {
                return FaseG.HUIDA;
            }

            int nodo = game.getGhostCurrentNodeIndex(fantasma);
            int pacman = game.getPacmanCurrentNodeIndex();

            int distancia =
                    game.getShortestPathDistance(nodo, pacman);

            // No dispersarse cuando Pac-Man esta cerca.
            if (distancia >= 0
                    && distancia <= DISTANCIA_PACMAN_CERCA) {
                return FaseG.PERSEGUIR;
            }

            if (dispersar) {
                return FaseG.EVADIR;
            }

            return FaseG.PERSEGUIR;
        }
    
2) segunSuPapel:
* **BLINKY**: mantiene su comportamiendo
* **PINKY**: Se tiene una variable constante de `PASOS_PREDICCION` donde también realiza la acción de predecir el movimiento de pacman
* **INKY**: Ahora puntua caa púldona de poder que está activada con `dPacman - dFantasma` y elige la de mayor puntuación (la que está más cerca del Pac-Man), si no hay pildoras de poder activadas entonces persigue al pacman.
* **SUE**: Realiza una persecución directa sin aleatoriedad.

3) huirConCriterio:
   
   Ahora en vez de ir hacia el fantasma no comestible, se calcula el movimiento que va a realizar dependiendo de una puntuación donde interfiere: la posición del Pac-Man, si se trata de un cruce y si hay alguna pildora de poder cercano al fantasma para evadirlo. 

4) puntuacionDispersión: se realiza unos ajustes ligeros

Partida GhostV0Me:

| Rival | Puntos | Media de puntos | Desviación |
| :---: | :---: | :---: | :---: |
| MsPacManV0ME | 13520.0 ± 960.0 | 5763.4 | 3216.857770926055 |

Tiempo de partida GhostV0Me:

| Rival | Tiempo | Media de tiempo | Desviación |
| :---: | :---: | :---: | :---: |
| MsPacManV0ME | 4001.0 ± 19.0 | 1557.8 | 1246.6714867672106 |


**Conclusión**:  

**Comparación con la GhostV0Me y GhostV0 del fantasma**

Margen V0Me = 13520,0 − 5763,4 = 7756,6

Margen V0   = 12900,0 − 6428,0 = 6472,0

D      = 7756,6 − 6472,0 = +1284,6

ET_D   = √(1840,3² + 4387,7²) ≈ 4757,7

2·ET_D = 9515,4

|D| < 2·ET_D → no se distingue de ruido
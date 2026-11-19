# Diario · Práctica 5 · Grupo GIRM_OGA_LIN_LING

**Integrantes:** Guillermo Isaac Ramos Medina, Óliver García Aguado, Lin Ling, Liling Chen  
**Grupo:** GIRM_OGA_LIN_LING  
**Controladores a concurso:** 
- **Ms. Pac-Man:** Versión elegida **v2** (mspacman-v2.fcl)
- **Ghosts:** Versión elegida **v4** (ghosts-v4.fcl)  
**Rivales de referencia:** 
- Para Ms. Pac-Man: 5 rivales de referencia (Random, Aggressive, Basico, Intermedio, Avanzado).
- Para Ghosts: 3 rivales de referencia (Basico, Intermedio, Avanzado).  
**Partidas por rival:** 300  
**Métricas:** Puntos, turnos por vida, fantasmas comidos  

---

# CONTROLADOR MS. PAC-MAN


## Sesión 1 (Jueves 19 de noviembre) · Base de controlador de ajedrez

Empezamos la práctica mirando la wiki y lo que venía en el proyecto de partida. De la carpeta zip `ICI-Practica5.zip` lo descomprimimos los y probamos con él. Seguido de ello pasamos por los siguientes apartados:

* A1 : Evaluar una posición
* A2 : Minimax
* A3 : Alfa-beta
* M1 : La recompensa
* M2 : Selección: UCB1
* M3 : Expansión
* M4 : Simulación
* M5 : Retropropagación
* M6 : El bucle

```
                          Aleatorio                 Voraz             AlfaBeta2         TOTAL
mcts               100% ±0 (16-0-0)      88% ±14 (13-2-1)        0% ±0 (0-0-16)       62% ±14
mcts-centro        100% ±0 (16-0-0)       94% ±9 (14-2-0)       9% ±14 (1-1-14)       68% ±13
ab2                 97% ±6 (15-1-0)       94% ±9 (14-2-0)       59% ±21 (7-5-4)        83% ±9
```
### Uso de IA y Prompts (Sesión 1)
- No se uso IA para estos apartados

---

## Sesión 2 (Jueves 19 de noviembre) · Retos realizados

Terminamos de ajustar las 9 reglas de la v0 en `mspacman-v0.fcl`. La idea era mantener la lógica que ya nos funcionaba bien en la P3: huida a tope si el peligro está encima ($d \le 10$), ir a por la Power Pill solo si llegamos antes que el fantasma (`ventajaPPill IS ventaja`), perseguir comestibles si da tiempo a cazarlos y comer por defecto cuando no hay peligro cerca.

Para comprobar que todo funcionaba antes de ponernos a jugar partidas, probamos el FCL con `ProbarBorroso` sobre 7 situaciones típicas (peligro encima, peligro a distancia media, píldora con ventaja y sin ventaja, caza de comestibles). En todos los casos ganó la acción que esperábamos y la curva mostró una transición limpia hacia huir por debajo de 35 pasos. También miramos un par de partidas en `VerPartida` con la traza conectada para comprobar que se movía con sentido antes de lanzar las 300 partidas oficiales de `Comparar.java`.

### Resultados de la línea base (MsPacMan v0 - Porteo P3)

Ejecutamos 300 partidas por rival frente a los cinco fantasmas de referencia (1500 partidas cada MsPacMan)

#### Puntos obtenidos
| rival | v0 ± error típico |
| :--- | ---: |
| Random | 4016.87 ± 113.39 |
| Aggressive | 3104.83 ± 112.44 |
| Basico | 2220.17 ± 68.43 |
| Intermedio | 2119.33 ± 70.92 |
| Avanzado | 1716.47 ± 48.06 |
| **MEDIA (5)** | **2635.53 ± 38.74** |

#### Turnos por vida
| rival | v0 ± error típico |
| :--- | ---: |
| Random | 668.08 ± 26.54 |
| Aggressive | 796.03 ± 72.28 |
| Basico | 286.83 ± 7.36 |
| Intermedio | 230.03 ± 7.27 |
| Avanzado | 392.41 ± 39.26 |
| **MEDIA (5)** | **474.68 ± 17.41** |

#### Fantasmas comidos
| rival | v0 ± error típico |
| :--- | ---: |
| Random | 4.38 ± 0.15 |
| Aggressive | 3.50 ± 0.15 |
| Basico | 2.33 ± 0.11 |
| Intermedio | 2.18 ± 0.11 |
| Avanzado | 1.52 ± 0.09 |
| **MEDIA (5)** | **2.78 ± 0.06** |

## Sesión 2 (Viernes 13 de noviembre) · Realización de un nuevo controlador (MsPacMan v1 - MsPacMan v0)

Elegimos crear una versión v1 porque En la v0 la variable ventajaPPil cambia muy rapido cuando pacman gira una esquina porque "desventaja" se solapa con "ventaja" en valores muy cercanos a 0, por lo que la accion de irAPPil y huir terminan estando muy juntas.
Tambien, en el v0 no se estaba teniendo en cuenta el número de PowerPills que quedan, puede venir bien para que al final se prioricen las ultimas pildoras aisladas. Además, estábamos en duda de que se tuviera en cuenta la cantidad de fantasmas cerca del pacman, porque antes era al más cercano, provocando que MsPacMan pudiera huir hacia la direccion por la que le viene otro.
Para ello, sería conveniente añadir a MsPacManInput una variable tipo perseguidoresCerca(), relajando la exigencia de la ventaja en la power pill para buscarla como salvaguardia en caso de huir a la desesperada.

## Problemas de la integración de este v1

1. **Variables inexistentes en el FCL:** al añadir `perseguidoresCerca` y `ppillsRestantes` a Java, el motor devolvía `No such variable`. Las entradas también tenían que estar declaradas en el archivo FCL correspondiente.
2. **Valores nulos:** detectamos que algunas entradas llegaban como `null`, provocando excepciones al evaluar la lógica borrosa en estos primeros casos.
3. **Ruta de los archivos FCL:** comprobamos que los archivos se cargaban correctamente desde `bin/`. Para reutilizar la misma ruta entre Ms. Pac-Man y los fantasmas, compartimos la constante `FCL_PATH` de `MsPacMan`.
4. **Error de carga del motor:** al utilizar una ruta incorrecta, `FIS.load()` devolvía `null`, provocando posteriormente un `NullPointerException`.
5. **Controlador de fantasmas:** adaptamos también su constructor para utilizar la ruta compartida y cargar correctamente sus archivos FCL.

### Verificación unitaria con `ProbarBorroso`

Realizamos cinco pruebas unitarias con diferentes situaciones para comprobar el comportamiento de las nuevas reglas:

1. Nada cerca: gana `comer` (8,7 frente a 1,3). Se activan R26 y R27. [BIEN]
2. Peligro extremo: gana `huir` (8,7 frente a 1,3). Se activan R1, R2 y R3. [BIEN]
3. Varios fantasmas cerca: gana `huir` (8,7 frente a 1,3). Se activan R8, R9 y R10. [BIEN]
4. Power Pill como escape: gana `irAPPill` (8,7), por delante de huir (5,0). Se activan R4, R5, R6 y R7. [BIEN]
5. Últimas Power Pills: gana `irAPPill` (8,6), por delante de comer (1,4) y huir (1,4). Se activan R22, R35 y R27. [BIEN]

Todos los casos dieron el resultado esperado.

### Ampliación de reglas en mspacman-v1.fcl

Pasamos de las 9 reglas a 35 reglas borrosas, organizadas en varios bloques:

- **Supervivencia ante peligro extremo (R1-R7)**: Misma lógica que v0.
- **Múltiples fantasmas peligrosos (R8-R15):** Incorporamos el número de perseguidores cercanos. Si hay muchos fantasmas, la huida será de prioridad máxima y se reduce la intención de comer y perseguir.
- **Power Pills como vía de escape (R16-R21)**: Metemos decisiones basadas en la ventaja calculada. Si Pac-Man puede alcanzar una Power Pill antes que los fantasmas, aumenta la prioridad de dirigirse hacia ella. Si la ventaja no está clara y hay varios perseguidores, se prioriza huir.
- **Administración de Power Pills (R22-R24)**: Tenemos en cuenta cuántas píldoras quedan en el mapa. Cuando quedan pocas, aumenta la prioridad de recogerlas, siempre que el nivel de peligro lo permita.
- **Comportamiento de comida (R25-R28)**: Se mantiene la prioridad de comer cuando no existe peligro importante, reduciendo esta acción cuando hay fantasmas comestibles que perseguir.
- **Persecución de fantasmas comestibles (R29-R35)**: Ampliamos la lógica de caza. Se persigue con intensidad cuando no hay peligro cercano, y se reduce progresivamente la intención de perseguir cuando hay varios fantasmas peligrosos alrededor.


Uso de IA: Se utilizó IA como apoyo para diseñar y revisar los problemas que fueron surgiendo con la nueva versión para comprobarlo con el motor borroso. Para la incorporación de perseguidoresCerca para considerar varios fantasmas peligrosos y ppillsRestantes para conocer cuántas Power Pills quedan en el mapa. Ayuda a construir el mspacman-v1.fcl que incorpora los nuevos criterios de supervivencia, gestión de Power Pills y persecución.

### Resultados de la línea base (MsPacMan v1 - MsPacMan v0)

Ejecutamos 300 partidas por rival frente a los cinco fantasmas de referencia (1500 partidas cada MsPacMan)

#### Puntos obtenidos
rival                        v0               v1         D   2·ET_D
Random               3866 ± 110       4348 ± 109      +482      309  mejor
Aggressive           3233 ± 106       3422 ± 116      +188      315  ruido
Basico                2124 ± 67        2805 ± 73      +680      198  mejor
Intermedio            2176 ± 71        2555 ± 79      +379      212  mejor
Avanzado              1789 ± 49        1960 ± 52      +171      143  mejor
------------------------------------------------------------------------
MEDIA (5)             2638 ± 37        3018 ± 40      +380      109  mejor
v1 es mejor contra 4 rivales, peor contra 0 y no se distingue contra 1.

#### Turnos por vida
rival                        v0               v1         D   2·ET_D
Random                 671 ± 27         731 ± 29       +60       79  ruido
Aggressive             797 ± 71         989 ± 77      +192      210  ruido
Basico                  278 ± 7          394 ± 9      +116       23  mejor
Intermedio              241 ± 7         309 ± 20       +68       43  mejor
Avanzado               362 ± 34         333 ± 24       -29       84  ruido
------------------------------------------------------------------------
MEDIA (5)              470 ± 17         551 ± 18       +81       49  mejor
v1 es mejor contra 2 rivales, peor contra 0 y no se distingue contra 3.

#### Fantasmas comidos
rival                        v0               v1         D   2·ET_D
Random              4,07 ± 0,15      4,82 ± 0,15     +0,75     0,42  mejor
Aggressive          3,73 ± 0,14      4,11 ± 0,16     +0,39     0,43  ruido
Basico              2,18 ± 0,11      3,18 ± 0,12     +0,99     0,33  mejor
Intermedio          2,27 ± 0,11      2,71 ± 0,12     +0,45     0,33  mejor
Avanzado            1,65 ± 0,09      1,94 ± 0,09     +0,30     0,26  mejor
------------------------------------------------------------------------
MEDIA (5)           2,78 ± 0,06      3,35 ± 0,06     +0,57     0,16  mejor
v1 es mejor contra 4 rivales, peor contra 0 y no se distingue contra 1.

## Conclusión de la sesión
Los resultados de esta primera ampliación son positivos:
La puntuación media aumenta de 2638 a 3018 puntos (+380).
Los turnos por vida aumentan de 470 a 551 (+81).
Los fantasmas comidos aumentan de 2,78 a 3,35 (+0,57).
Las pruebas unitarias terminan con los cinco escenarios correctos.
La mejora que más se nota es que el controlador consigue combinar mejor la supervivencia con la caza de fantasmas comestibles, sin limitarse a huir constantemente ante cualquier situación de peligro. Decidiremos conservar esta versión como nuestra entrega y nuevo modelo para futuras versiones.

---

## Sesión 3 (Lunes 16 de noviembre) · Versión v2: Corrección táctica de Power Pills y barrido final de laberinto

Mirando las partidas y el FCL de la v1 vimos que aunque los números subían, Pac-Man hacía una cosa muy rara: se iba directo a comerse las Power Pills aunque los fantasmas estuvieran en la otra punta del mapa. Al mirar el código vimos el fallo: en `MsPacManInputV1` se metió `ppillsRestantes` contando las 4 Power Pills en vez de las bolitas normales. Las reglas R22 y R35 se activaban y Pac-Man quemaba su mejor salvavidas aunque no hubiera peligro cerca. Además, el FCL se había ido a 35 micro-reglas bastante difíciles de seguir.

Para la **v2** cambiamos `ppillsRestantes` por `comidaRestante` con las píldoras normales ($0 \dots 220$) y dejamos el FCL en solo 22 reglas. La idea era no tocar las Power Pills si no hay moros en la costa, usarlas para emboscar cuando se acercan a media distancia, acelerar a por la píldora si tenemos un fantasma cerca al que le pudieramos ganar en carrera, y usarlas para limpiar el laberinto cuando quedan pocas pills normales. En Java ajustamos `IrAPPillAction` con la heurística de píldoras aisladas y el movimiento aproximado para que no baile en los cruces.
### Resultados comparativos (300 partidas por rival, semillas 1 a 300)

Ejecutamos `Comparar.java` con las 300 partidas oficiales frente a los 5 rivales de referencia (1500 partidas por versión):

#### Puntos obtenidos (v2 frente a v1)
```
rival                        v1               v2         D   2·ET_D
Random               4408 ± 126       4686 ± 137      +279      372  ruido
Aggressive           3438 ± 106       3570 ± 119      +132      319  ruido
Basico                2577 ± 71        2674 ± 76       +98      208  ruido
Intermedio            2454 ± 75        2206 ± 70      -249      206  peor
Avanzado              1953 ± 53        2027 ± 56       +74      155  ruido
------------------------------------------------------------------------
MEDIA (5)             2966 ± 40        3033 ± 43       +67      118  ruido
v2 es mejor contra 0 rivales, peor contra 1 y no se distingue contra 4.
```

#### Turnos por vida (v2 frente a v1)
```
rival                        v1               v2         D   2·ET_D
Random                 761 ± 31         727 ± 31       -34       89  ruido
Aggressive            1004 ± 77         948 ± 76       -56      217  ruido
Basico                 371 ± 10         366 ± 16        -5       37  ruido
Intermedio             321 ± 22         275 ± 10       -45       49  ruido
Avanzado               412 ± 37         320 ± 20       -92       84  peor
------------------------------------------------------------------------
MEDIA (5)              573 ± 19         527 ± 17       -46       51  ruido
v2 es mejor contra 0 rivales, peor contra 1 y no se distingue contra 4.
```

#### Fantasmas comidos (v2 frente a v1)
```
rival                        v1               v2         D   2·ET_D
Random              4.90 ± 0.17      5.99 ± 0.20     +1.09     0.52  mejor
Aggressive          4.19 ± 0.15      4.22 ± 0.16     +0.03     0.44  ruido
Basico              2.82 ± 0.12      3.17 ± 0.13     +0.35     0.35  mejor
Intermedio          2.59 ± 0.11      2.52 ± 0.12     -0.07     0.34  ruido
Avanzado            1.96 ± 0.10      2.25 ± 0.11     +0.29     0.29  ruido
------------------------------------------------------------------------
MEDIA (5)           3.29 ± 0.06      3.63 ± 0.07     +0.34     0.18  mejor
v2 es mejor contra 2 rivales, peor contra 0 y no se distingue contra 3.
```

### Uso de IA y Prompts (Sesión 3)
- **Prompt:** *"Tengo un controlador borroso en FCL con 35 reglas donde el agente come Power Pills innecesariamente cuando los fantasmas están lejos. Refactorizalo a un conjunto modular de mas o menos 20 reglas en jFuzzyLogic que sustituya ppillsRestantes por comidaRestante (0 a 220), preserve las Power Pills para emboscadas a media distancia o escape ante acoso, y dé prioridad a limpiar las últimas pills del mapa."*
- **Qué cambiamos nosotros:** La IA propuso una regla de escape que exigía `perseguidoresCerca IS ninguno`, lo que era contradictorio porque si un fantasma está cerca ya cuenta como 1 perseguidor (`pocos`), provocando que la regla nunca se disparase en partidas reales. La modificamos manualmente para evaluar `perseguidoresCerca IS pocos` y añadimos la modulación `huir IS poco` en las carreras ganables para evitar empates por centro de gravedad en intersecciones estrechas.

### Conclusión de la sesión
El cambio ha merecido la pena: al no malgastar las píldoras a lo tonto, Pac-Man sobrevive mejor y caza mucho más. Los fantasmas comidos suben a 3.63 de media (+0.34 sobre v1 y casi un fantasma más que en v0), comiéndose casi 6 fantasmas por partida contra Random y más de 3.1 contra Básico. En puntos sube a 3033 de media y el código queda mucho más limpio con 22 reglas en vez de las 35 anteriores.

---

# CONTROLADOR GHOSTS

## Sesión 1 (Sábado 14 de noviembre) · Porteo de Ghosts de la P3 a reglas borrosas (v0)

Miramos la wiki y descargamos la base de la practica 4. El archivo `ghosts-v0.fcl` solo contenía 2 reglas y no se diferenciaba por tipo de fantasma y por ende realizamos un port directo del Ghosts que teníamos en la Práctica 3 a lógica borrosa.

Para trasladarlo a Java adaptamos las acciones para que implementen `Action` directa (`DispersarAction`, `HuirAction`, `EmboscarAction`, `GuardarPPillAction`, `PerseguirAction`) y en `GhostsInput.java` calculamos los valores (`comestible`, `enCarcel`, `distPacman`, `distOtro` , `restantes`, `ventajaPPill` y `soy`) para pasarlos al motor borroso por cada fantasma siendo los valores que devuelven estos:

* `comestible`: Devuelve 1 si el fantasma está comestible (huyendo de Pac-Man) y 0 si está en modo normal.
* `enCarcel`: Devuelve 1 si el fantasma está en la cárcel (muerto) y 0 si está jugando.
* `distPacman`: La distancia en pasos desde el fantasma hasta Ms. Pac-Man (si está en la cárcel, vale 150).
* `distOtro`: La distancia al fantasma aliado más cercano (sirve para saber si el grupo está "apretado" y deben dispersarse).
* `restantes`: La cantidad de píldoras normales que quedan en el mapa (para saber si es momento de controlar la comida).
* `ventajaPPill`: La diferencia de distancia entre Ms. Pac-Man y el fantasma para llegar a la píldora de poder más cercana. Si es positiva, el fantasma llega antes; si es negativa, llega antes Pac-Man.
* `soy`: Un número identificativo del fantasma (0=Pinky, 1=Inky, 2=Blinky, 3=Sue) para que las reglas del FCL sepan de quién se trata y respeten su rol.

Al escribir el FCL intentamos acercarnos a las reglas que se plantearon en la clp de la prática anterior y vimos que era necesario separar las acciones en reglas independientes, eliminar algunas de ellas o editarlas ya que veíamos que: añadía más variables de `DEFUZZIFY` para una sola cosa que complicaría su condición y no la veíamos viable, adaptar PACMAN_CERCA que fue un hecho booleano de distPacman<=10 a la lógica borrosa donde ahora se usa repetidamente `distPacman IS cerca/lejos` (lo mismo que con GRUPO apretado a `distOtro IS apretado`) o que tuvimos que repetir reglas pero para diferentes fantasmas por su número identificativo.

### Uso de IA y Prompts (Sesión 1)
- **Prompt:** *"Traduce la lógica de reglas de Jess de este archivo a un bloque FCL de jFuzzyLogic para Ghosts, con estas entradas (comestible, enCarcel, distPacman, distOtro, restantes, ventajaPPill, soy) + (los usos de estas variables anteriormente explicadas) y 5 salidas (guardarPPill, huir, perseguir, dispersar, emboscar, controlarPills) usando COG."*

- **Qué cambiamos nosotros:** La IA intentó meter varios consecuentes en un solo `THEN` (`IF comestible IS si THEN huir IS mucho, controlarPills IS poco, emboscar IS poco, dispersar IS poco, guardarPPill IS poco, perseguir IS poco;`), lo que hacía fallar el parser de FCL además de tener algunos de los DEFUZZIFY con mayúsculas. Tuvimos que separar esas acciones en varias en caso de que los fantasmas si fueran comestibles terminado con 6 reglas:

``` 
   RULE 1 : IF comestible IS si THEN huir IS mucho;
   RULE 2 : IF comestible IS si THEN controlarpills IS poco;
   RULE 3 : IF comestible IS si THEN emboscar IS poco;
   RULE 4 : IF comestible IS si THEN dispersar IS poco;
   RULE 5 : IF comestible IS si THEN guardarppill IS poco;
   RULE 6 : IF comestible IS si THEN perseguir IS poco;
```

Entre otras más y cambiar esos DEFUZZIFY que contenían mayúsculas ya que la nomenclatura no lo permitía y por tanto también el retuen de los identificadores de las acciones guardarPPill y controlarPills.

---

## Sesión 2 (Domingo 15 de noviembre) · Crear ControlarPillsAction y adaptar los ejecutores

Vimos que existía en la anterior práctica una salida que al final ,al parecer, se destartó pero lo integramos para esta, su objetivo es ir hacia la píldora más cercana y si no quedan entonces persiguen a Ms.Pac-Man, sabemos que es una acción bastante arriesgada pero queríamos probrar con ella.

Adaptamos los ejecutores de Comparar y verPartida para ver sus trazas además de ProbarBorroso para los casos.

### Resultados de la línea base (v0Ghosts - Porteo P3)

```
300 partidas por versión y rival, semillas 1 a 300, comparando fantasmas.

== puntos: v0 frente a anterior (menos es mejor) ==
rival                  anterior               v0         D   2·ET_D
Basico               4218 ± 130        2208 ± 59     -2010      286  mejor
Intermedio           9192 ± 148       4343 ± 151     -4849      423  mejor
Avanzado            10443 ± 157       8297 ± 192     -2145      496  mejor
------------------------------------------------------------------------
MEDIA (3)             7951 ± 84        4949 ± 84     -3002      237  mejor
v0 es mejor contra 3 rivales, peor contra 0 y no se distingue contra 0.

== turnos por vida: v0 frente a anterior (menos es mejor) ==
rival                  anterior               v0         D   2·ET_D
Basico                 737 ± 37        1742 ± 93     +1005      199  peor
Intermedio            2556 ± 76        2780 ± 83      +224      224  peor
Avanzado              3673 ± 47        3081 ± 71      -592      170  mejor
------------------------------------------------------------------------
MEDIA (3)             2322 ± 32        2535 ± 48      +212      115  peor
v0 es mejor contra 1 rivales, peor contra 2 y no se distingue contra 0.

== fantasmas comidos: v0 frente a anterior (menos es mejor) ==
rival                  anterior               v0         D   2·ET_D
Basico              4,12 ± 0,17      1,86 ± 0,09     -2,26     0,39  mejor
Intermedio         10,86 ± 0,22      4,46 ± 0,20     -6,40     0,59  mejor
Avanzado           12,57 ± 0,21     10,87 ± 0,25     -1,70     0,65  mejor
------------------------------------------------------------------------
MEDIA (3)           9,18 ± 0,11      5,73 ± 0,11     -3,45     0,32  mejor
v0 es mejor contra 3 rivales, peor contra 0 y no se distingue contra 0.
```

## Sesión 3 (Lunes 16 de noviembre) · v1: Arreglo de la fcl (38 reglas)
 
En la fcl anterior, que se tomo de base las reglas de la practic anterior nos dimos cuenta que era necesario realisar unos cambios ya que en las reglas de control de comina (desde R7 hasta R21) no se comprobaban a qué fantasma pertenecía y al no tener `soy IS ...`, todos los fantasmas activaban las reglas de control de píldoras y por eso PINKY, BLINKY y el resto ganaban con controlarpills aunque no fuera su rol.

El FCL de 38 reglas se conserva tal como venía en el proyecto. Al probarlo, se vio que el problema no era solo escribir `soy IS`: algunos términos de la identidad se solapaban. Con identidad 0, Pinky también activaba el término de Inky. Además, en el controlador original la salida `controlarpills` no tenía una acción registrada en el selector. Dejamos v1 así para comparar con lo que había y corregimos las dos cosas en la siguiente versión.


---

## Sesión 4 (Lunes 16 de noviembre) · v2: ordenar los papeles (38 reglas)

Partimos del v1 de 38 reglas y rehacemos el bloque de prioridades. Separamos las reglas de huir, perseguir, emboscar, dispersarse y guardar poder. Los términos de identidad son triángulos centrados en el número de cada fantasma, así Pinky ya no activa los papeles de los demás. Se quita la salida de controlar comida, que nunca podía ganar en el selector.

Blinky aumenta la presión cuando tiene a Pac-Man cerca. Pinky intenta cortar el paso si puede llegar al cruce previsto; si no puede, persigue. Inky guarda poder según su ventaja de llegada. Sue se separa si el grupo está junto y Pac-Man está lejos. Para huir, las reglas distinguen si Pac-Man está cerca y cuánto tiempo queda comestible. Las salidas usan varios grados: por ejemplo, una ventaja ajustada no pesa igual que una ventaja amplia.

Se probó que Pinky persigue si no llega al cruce, que Inky persigue si no hay poder que pueda defender y que Sue se dispersa cuando corresponde. La curva pasó por 46508 puntos sin huecos ni empates ganadores. Las 38 reglas se activaron en la traza.

### Resultados de v2 frente a v1

**Semillas 1 a 300**

```text
300 partidas por versión y rival, semillas 1 a 300, comparando fantasmas.

== puntos: v2 frente a v1 (menos es mejor) ==
rival                        v1               v2         D   2·ET_D
Basico                2094 ± 53        2095 ± 56        +1      154  ruido
Intermedio           5009 ± 169       2827 ± 127     -2182      423  mejor
Avanzado             8514 ± 196      10473 ± 202     +1959      562  peor
------------------------------------------------------------------------
MEDIA (3)             5206 ± 88        5132 ± 82       -74      240  ruido
v2 es mejor contra 1 rivales, peor contra 1 y no se distingue contra 1.

== turnos por vida: v2 frente a v1 (menos es mejor) ==
rival                        v1               v2         D   2·ET_D
Basico                1652 ± 94         758 ± 68      -894      232  mejor
Intermedio            2909 ± 79        3598 ± 55      +689      194  peor
Avanzado              3042 ± 72        2530 ± 78      -512      212  mejor
------------------------------------------------------------------------
MEDIA (3)             2534 ± 47        2295 ± 39      -239      123  mejor
v2 es mejor contra 2 rivales, peor contra 1 y no se distingue contra 0.

== fantasmas comidos: v2 frente a v1 (menos es mejor) ==
rival                        v1               v2         D   2·ET_D
Basico              1.66 ± 0.09      2.12 ± 0.10     +0.46     0.27  peor
Intermedio          5.40 ± 0.22      2.42 ± 0.18     -2.97     0.57  mejor
Avanzado           11.17 ± 0.26     13.96 ± 0.26     +2.79     0.74  peor
------------------------------------------------------------------------
MEDIA (3)           6.08 ± 0.12      6.17 ± 0.11     +0.09     0.32  ruido
v2 es mejor contra 1 rivales, peor contra 2 y no se distingue contra 0.
```

**Semillas 1001 a 1300**

```text
300 partidas por versión y rival, semillas 1001 a 1300, comparando fantasmas.

== puntos: v2 frente a v1 (menos es mejor) ==
rival                        v1               v2         D   2·ET_D
Basico                2236 ± 62        2171 ± 60       -65      172  ruido
Intermedio           4375 ± 155       2624 ± 112     -1751      382  mejor
Avanzado             8290 ± 199       9881 ± 199     +1590      563  peor
------------------------------------------------------------------------
MEDIA (3)             4967 ± 87        4892 ± 79       -75      234  ruido
v2 es mejor contra 1 rivales, peor contra 1 y no se distingue contra 1.

== turnos por vida: v2 frente a v1 (menos es mejor) ==
rival                        v1               v2         D   2·ET_D
Basico                1880 ± 98         808 ± 71     -1073      242  mejor
Intermedio            2906 ± 79        3634 ± 54      +728      191  peor
Avanzado              3037 ± 71        2505 ± 82      -532      217  mejor
------------------------------------------------------------------------
MEDIA (3)             2608 ± 48        2316 ± 40      -292      126  mejor
v2 es mejor contra 2 rivales, peor contra 1 y no se distingue contra 0.

== fantasmas comidos: v2 frente a v1 (menos es mejor) ==
rival                        v1               v2         D   2·ET_D
Basico              1.98 ± 0.11      2.19 ± 0.10     +0.22     0.30  ruido
Intermedio          4.52 ± 0.21      2.28 ± 0.16     -2.24     0.53  mejor
Avanzado           10.88 ± 0.26     13.06 ± 0.25     +2.19     0.72  peor
------------------------------------------------------------------------
MEDIA (3)           5.79 ± 0.12      5.85 ± 0.11     +0.05     0.31  ruido
v2 es mejor contra 1 rivales, peor contra 1 y no se distingue contra 1.
```

- Semillas 1 a 300: la media de puntos pasa de 5206 a 5132 (-74 puntos, umbral 240). La diferencia queda dentro del ruido.
- Semillas 1001 a 1300: la media de puntos pasa de 4967 a 4892 (-75 puntos, umbral 234). La diferencia queda dentro del ruido.

### prompts IA

1. crea un fcl v2 aparte. corrige la identidad de los fantasmas con terminos triangulares que no coincidan en los valores 0, 1, 2 y 3. organiza las reglas por huida, persecucion, emboscada, dispersion y defensa de poder. deja varias intensidades por salida para que una ventaja pequeña no tenga el mismo peso que una grande.

## Sesión 5 (Martes 17 de noviembre) · v3: defender dos píldoras (44 reglas)

Inky podía defender una píldora, pero quedaban otras entradas por las que Pac-Man podía conseguir poder. Añadimos una sola táctica: si Inky tiene un objetivo defendible, Sue puede cubrir otra píldora activa. Busca la que le da más ventaja de llegada, excluye la de Inky y pasa esa misma ventaja al FCL.

Las seis reglas nuevas distinguen entre ventaja amplia, margen justo, falta de opción, Pac-Man demasiado cerca y Sue comestible. No se cambia la persecución de Blinky, la emboscada de Pinky ni la búsqueda de poder de Inky. En las 90 partidas de traza de v3, Sue eligió cubrir 2345 veces, y se activaron las 44 reglas. Esta versión ayuda enseguida, aunque Pac-Man aún no haya comido fantasmas.

### Resultados de v3 frente a v2

**Semillas 1 a 300**

```text
300 partidas por versión y rival, semillas 1 a 300, comparando fantasmas.

== puntos: v3 frente a v2 (menos es mejor) ==
rival                        v2               v3         D   2·ET_D
Basico                2095 ± 56        1994 ± 52      -101      152  ruido
Intermedio           2827 ± 127       3000 ± 129      +173      363  ruido
Avanzado            10473 ± 202       9534 ± 174      -939      533  mejor
------------------------------------------------------------------------
MEDIA (3)             5132 ± 82        4843 ± 74      -289      221  mejor
v3 es mejor contra 1 rivales, peor contra 0 y no se distingue contra 2.

== turnos por vida: v3 frente a v2 (menos es mejor) ==
rival                        v2               v3         D   2·ET_D
Basico                 758 ± 68        1130 ± 86      +372      220  peor
Intermedio            3598 ± 55        3557 ± 60       -40      163  ruido
Avanzado              2530 ± 78        2586 ± 80       +56      225  ruido
------------------------------------------------------------------------
MEDIA (3)             2295 ± 39        2424 ± 44      +129      118  peor
v3 es mejor contra 0 rivales, peor contra 1 y no se distingue contra 2.

== fantasmas comidos: v3 frente a v2 (menos es mejor) ==
rival                        v2               v3         D   2·ET_D
Basico              2.12 ± 0.10      1.94 ± 0.10     -0.18     0.28  ruido
Intermedio          2.42 ± 0.18      2.73 ± 0.18     +0.31     0.50  ruido
Avanzado           13.96 ± 0.26     12.77 ± 0.23     -1.19     0.70  mejor
------------------------------------------------------------------------
MEDIA (3)           6.17 ± 0.11      5.82 ± 0.10     -0.35     0.30  mejor
v3 es mejor contra 1 rivales, peor contra 0 y no se distingue contra 2.
```

**Semillas 1001 a 1300**

```text
300 partidas por versión y rival, semillas 1001 a 1300, comparando fantasmas.

== puntos: v3 frente a v2 (menos es mejor) ==
rival                        v2               v3         D   2·ET_D
Basico                2171 ± 60        2056 ± 51      -115      157  ruido
Intermedio           2624 ± 112       3009 ± 142      +385      361  peor
Avanzado             9881 ± 199       9675 ± 178      -206      534  ruido
------------------------------------------------------------------------
MEDIA (3)             4892 ± 79        4913 ± 78       +21      221  ruido
v3 es mejor contra 0 rivales, peor contra 1 y no se distingue contra 2.

== turnos por vida: v3 frente a v2 (menos es mejor) ==
rival                        v2               v3         D   2·ET_D
Basico                 808 ± 71        1183 ± 88      +376      226  peor
Intermedio            3634 ± 54        3566 ± 58       -68      159  ruido
Avanzado              2505 ± 82        2697 ± 77      +192      225  ruido
------------------------------------------------------------------------
MEDIA (3)             2316 ± 40        2482 ± 44      +167      119  peor
v3 es mejor contra 0 rivales, peor contra 1 y no se distingue contra 2.

== fantasmas comidos: v3 frente a v2 (menos es mejor) ==
rival                        v2               v3         D   2·ET_D
Basico              2.19 ± 0.10      2.01 ± 0.10     -0.18     0.29  ruido
Intermedio          2.28 ± 0.16      2.82 ± 0.19     +0.54     0.51  peor
Avanzado           13.06 ± 0.25     12.90 ± 0.23     -0.16     0.68  ruido
------------------------------------------------------------------------
MEDIA (3)           5.85 ± 0.11      5.91 ± 0.11     +0.07     0.30  ruido
v3 es mejor contra 0 rivales, peor contra 1 y no se distingue contra 2.
```

- Semillas 1 a 300: la media de puntos pasa de 5132 a 4843 (-289 puntos, umbral 221). La diferencia es una mejora según el comparador.
- Semillas 1001 a 1300: la media de puntos pasa de 4892 a 4913 (+21 puntos, umbral 221). La diferencia queda dentro del ruido.

### prompts IA

1. haz que v3 conserve las reglas de v2 y añade la segunda defensa de poder. busca primero el objetivo que tendria inky. al recorrer las pildoras para sue, descarta esa misma y calcula la diferencia entre la distancia de pacman y la de sue. calcula el camino de sue con su ultima direccion. si no puede defender otra pildora con ventaja, usa las salidas que ya tenia v2.

## Sesión 6 (Martes 17 de noviembre) · v4: reservar la ayuda de Sue (45 reglas)

Con v3, Sue podía dejar la persecución en muchos turnos en los que Pac-Man todavía no estaba aprovechando el poder. En v4 conservamos la segunda defensa, pero pasamos a las reglas los fantasmas que Pac-Man se ha comido durante toda la partida. El motor solo devuelve los que se han comido en el último turno, así que acumulamos el dato una vez por turno y lo reiniciamos al comenzar la siguiente partida.

La defensa gana peso a partir de tres fantasmas comidos y llega al máximo con cinco. Una regla nueva rebaja la cobertura cuando todavía se han comido pocos. En el caso de prueba, con el resto de las entradas fijas, Sue pasó de perseguir a cubrir entre el cuarto y el quinto fantasma comido. La curva de v4 pasó por 116270 puntos sin huecos ni empates ganadores.

En la traza se activaron las 45 reglas. Sue eligió cubrir 476 veces en 90 partidas. Por ejemplo, en el turno 587 ya se habían comido cinco fantasmas y Sue tenía 18 pasos de ventaja: cubrir obtuvo 8,77 y perseguir 7,03. La ayuda se usa menos a menudo que en v3.

### Resultados de v4 frente a v3

**Semillas 1 a 300**

```text
300 partidas por versión y rival, semillas 1 a 300, comparando fantasmas.

== puntos: v4 frente a v3 (menos es mejor) ==
rival                        v3               v4         D   2·ET_D
Basico                1994 ± 52        2072 ± 55       +78      152  ruido
Intermedio           3000 ± 129       2539 ± 107      -462      336  mejor
Avanzado             9534 ± 174       9820 ± 179      +286      499  ruido
------------------------------------------------------------------------
MEDIA (3)             4843 ± 74        4810 ± 72       -32      207  ruido
v4 es mejor contra 1 rivales, peor contra 0 y no se distingue contra 2.

== turnos por vida: v4 frente a v3 (menos es mejor) ==
rival                        v3               v4         D   2·ET_D
Basico                1130 ± 86         905 ± 78      -225      232  ruido
Intermedio            3557 ± 60        3728 ± 47      +171      152  peor
Avanzado              2586 ± 80        2497 ± 78       -89      224  ruido
------------------------------------------------------------------------
MEDIA (3)             2424 ± 44        2377 ± 40       -48      119  ruido
v4 es mejor contra 0 rivales, peor contra 1 y no se distingue contra 2.

== fantasmas comidos: v4 frente a v3 (menos es mejor) ==
rival                        v3               v4         D   2·ET_D
Basico              1.94 ± 0.10      2.06 ± 0.10     +0.11     0.27  ruido
Intermedio          2.73 ± 0.18      2.17 ± 0.16     -0.56     0.48  mejor
Avanzado           12.77 ± 0.23     12.98 ± 0.23     +0.20     0.65  ruido
------------------------------------------------------------------------
MEDIA (3)           5.82 ± 0.10      5.74 ± 0.10     -0.08     0.28  ruido
v4 es mejor contra 1 rivales, peor contra 0 y no se distingue contra 2.
```

**Semillas 1001 a 1300**

```text
300 partidas por versión y rival, semillas 1001 a 1300, comparando fantasmas.

== puntos: v4 frente a v3 (menos es mejor) ==
rival                        v3               v4         D   2·ET_D
Basico                2056 ± 51        2131 ± 59       +75      156  ruido
Intermedio           3009 ± 142       2555 ± 115      -454      365  mejor
Avanzado             9675 ± 178       9597 ± 185       -78      514  ruido
------------------------------------------------------------------------
MEDIA (3)             4913 ± 78        4761 ± 75      -153      217  ruido
v4 es mejor contra 1 rivales, peor contra 0 y no se distingue contra 2.

== turnos por vida: v4 frente a v3 (menos es mejor) ==
rival                        v3               v4         D   2·ET_D
Basico                1183 ± 88         797 ± 72      -387      227  mejor
Intermedio            3566 ± 58        3685 ± 52      +119      157  ruido
Avanzado              2697 ± 77        2633 ± 80       -64      222  ruido
------------------------------------------------------------------------
MEDIA (3)             2482 ± 44        2372 ± 40      -111      118  ruido
v4 es mejor contra 1 rivales, peor contra 0 y no se distingue contra 2.

== fantasmas comidos: v4 frente a v3 (menos es mejor) ==
rival                        v3               v4         D   2·ET_D
Basico              2.01 ± 0.10      2.19 ± 0.10     +0.18     0.28  ruido
Intermedio          2.82 ± 0.19      2.17 ± 0.17     -0.65     0.52  mejor
Avanzado           12.90 ± 0.23     12.69 ± 0.24     -0.21     0.67  ruido
------------------------------------------------------------------------
MEDIA (3)           5.91 ± 0.11      5.68 ± 0.10     -0.23     0.30  ruido
v4 es mejor contra 1 rivales, peor contra 0 y no se distingue contra 2.
```

- Semillas 1 a 300: la media de puntos pasa de 4843 a 4810 (-32 puntos, umbral 207). La diferencia queda dentro del ruido.
- Semillas 1001 a 1300: la media de puntos pasa de 4913 a 4761 (-153 puntos, umbral 217). La diferencia queda dentro del ruido.

### Resultados de v4 frente a v2

**Semillas 1 a 300**

```text
300 partidas por versión y rival, semillas 1 a 300, comparando fantasmas.

== puntos: v4 frente a v2 (menos es mejor) ==
rival                        v2               v4         D   2·ET_D
Basico                2095 ± 56        2072 ± 55       -23      157  ruido
Intermedio           2827 ± 127       2539 ± 107      -288      333  ruido
Avanzado            10473 ± 202       9820 ± 179      -653      539  mejor
------------------------------------------------------------------------
MEDIA (3)             5132 ± 82        4810 ± 72      -321      217  mejor
v4 es mejor contra 1 rivales, peor contra 0 y no se distingue contra 2.

== turnos por vida: v4 frente a v2 (menos es mejor) ==
rival                        v2               v4         D   2·ET_D
Basico                 758 ± 68         905 ± 78      +147      207  ruido
Intermedio            3598 ± 55        3728 ± 47      +131      145  ruido
Avanzado              2530 ± 78        2497 ± 78       -33      221  ruido
------------------------------------------------------------------------
MEDIA (3)             2295 ± 39        2377 ± 40       +82      112  ruido
v4 es mejor contra 0 rivales, peor contra 0 y no se distingue contra 3.

== fantasmas comidos: v4 frente a v2 (menos es mejor) ==
rival                        v2               v4         D   2·ET_D
Basico              2.12 ± 0.10      2.06 ± 0.10     -0.07     0.28  ruido
Intermedio          2.42 ± 0.18      2.17 ± 0.16     -0.25     0.49  ruido
Avanzado           13.96 ± 0.26     12.98 ± 0.23     -0.99     0.70  mejor
------------------------------------------------------------------------
MEDIA (3)           6.17 ± 0.11      5.74 ± 0.10     -0.43     0.30  mejor
v4 es mejor contra 1 rivales, peor contra 0 y no se distingue contra 2.
```

**Semillas 1001 a 1300**

```text
300 partidas por versión y rival, semillas 1001 a 1300, comparando fantasmas.

== puntos: v4 frente a v2 (menos es mejor) ==
rival                        v2               v4         D   2·ET_D
Basico                2171 ± 60        2131 ± 59       -40      168  ruido
Intermedio           2624 ± 112       2555 ± 115       -69      320  ruido
Avanzado             9881 ± 199       9597 ± 185      -284      543  ruido
------------------------------------------------------------------------
MEDIA (3)             4892 ± 79        4761 ± 75      -131      218  ruido
v4 es mejor contra 0 rivales, peor contra 0 y no se distingue contra 3.

== turnos por vida: v4 frente a v2 (menos es mejor) ==
rival                        v2               v4         D   2·ET_D
Basico                 808 ± 71         797 ± 72       -11      202  ruido
Intermedio            3634 ± 54        3685 ± 52       +51      150  ruido
Avanzado              2505 ± 82        2633 ± 80      +128      229  ruido
------------------------------------------------------------------------
MEDIA (3)             2316 ± 40        2372 ± 40       +56      113  ruido
v4 es mejor contra 0 rivales, peor contra 0 y no se distingue contra 3.

== fantasmas comidos: v4 frente a v2 (menos es mejor) ==
rival                        v2               v4         D   2·ET_D
Basico              2.19 ± 0.10      2.19 ± 0.10     -0.00     0.29  ruido
Intermedio          2.28 ± 0.16      2.17 ± 0.17     -0.11     0.47  ruido
Avanzado           13.06 ± 0.25     12.69 ± 0.24     -0.37     0.69  ruido
------------------------------------------------------------------------
MEDIA (3)           5.85 ± 0.11      5.68 ± 0.10     -0.16     0.29  ruido
v4 es mejor contra 0 rivales, peor contra 0 y no se distingue contra 3.
```

- Semillas 1 a 300: la media de puntos pasa de 5132 a 4810 (-321 puntos, umbral 217). La diferencia es una mejora según el comparador.
- Semillas 1001 a 1300: la media de puntos pasa de 4892 a 4761 (-131 puntos, umbral 218). La diferencia queda dentro del ruido.


### prompts IA

1. cambia en v4 solo cuando ayuda sue. en el controlador suma los fantasmas comidos del ultimo turno una vez por tick. reinicia el contador en `preCompute`. añade al fcl un termino que suba desde dos hasta cinco fantasmas comidos. si falta ventaja, pacman esta encima o sue es comestible, deja ganar a las otras acciones.

### Elección para el concurso

Se elige **v4**.

---

# Elección final para el concurso

- **Ms. Pac-Man:** Se envía **v2** (mspacman-v2.fcl) porque supera a v0 (+1.487 pts) y a v1 (+853 pts) al gestionar el acoso múltiple y racionar las Power Pills.
- **Ghosts:** Se envía **v4** (ghosts-v4.fcl) porque minimiza los puntos concedidos a Pac-Man (4.810 pts, -321 frente a v2) y reduce los fantasmas comidos a 5,68.


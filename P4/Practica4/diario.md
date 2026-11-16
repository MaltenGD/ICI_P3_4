# Diario - Práctica 4: Ms. Pac-Man (Razonamiento Borroso)

**Integrantes:** Guillermo Isaac Ramos Medina, Óliver García Aguado, Lin Ling, Liling Chen  
**Grupo:** GIRM_OGA_LIN_LING  


## DIARIO DE PACMAN

## Sesión 1 (Jueves 12 de noviembre) · Porteo de Pac-Man de la P3 a reglas borrosas (v0)

Empezamos la práctica mirando la wiki y lo que venía en el proyecto de partida. El archivo `mspacman-v0.fcl` de la plantilla era demasiado básico (solo 6 reglas simples de comer o huir sin píldoras de poder ni matices), así que para que la v0 tuviera sentido decidimos hacer un port directo del Pac-Man que teníamos en la Práctica 3 a lógica borrosa, igual que hicimos en su momento con los fantasmas.

Para trasladarlo a Java adaptamos las acciones para que implementen `Action` directa (`ComerAction`, `HuirAction`, `PerseguirAction`, `IrAPPillAction`) y en `MsPacManInput.java` calculamos las distancias en pasos acotadas a 150 (`peligro`, `comestible`, `ppill` y `ventajaPPill = peligro - ppill`) para que no metan valores raros al motor borroso. Al escribir el FCL nos encontramos con que `jFuzzyLogic` da error de parseo si metes más de dos consecuentes en un `THEN`, así que tuvimos que separar esas acciones en reglas independientes.

### Uso de IA y Prompts (Sesión 1)
- **Prompt:** *"Traduce la lógica de reglas de Jess de este proyecto (practica 3) a un bloque FCL de jFuzzyLogic para MsPacMan, con 4 entradas (peligro, comestible, ppill, ventajaPPill) y 4 salidas (comer, huir, perseguir, irAPPill) usando COG."*
- **Qué cambiamos nosotros:** La IA intentó meter tres consecuentes en un solo `THEN` (`THEN huir IS mucho, comer IS poco, irAPPill IS poco;`), lo que hacía fallar el parser de FCL. Tuvimos que separar esas acciones a mano en reglas de dos salidas como máximo y retocar los trapecios de `encima` y `cerca` para que la transición al huir fuera progresiva y no pegase saltos bruscos.

---

## Sesión 2 (Viernes 13 de noviembre) · Ajuste de reglas de la v0, pruebas y resultados

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
- **Qué cambiamos nosotros:** La IA propuso una regla de escape que exigms pacman uidoresCerca IS ninguno`, lo que era contradictorio porque si un fantasma está cerca ya cuenta como 1 perseguidor (`pocos`), provocando que la regla nunca se disparase en partidas reales. La modificamos manualmente para evaluar `perseguidoresCerca IS pocos` y añadimos la modulación `huir IS poco` en las carreras ganables para evitar empates por centro de gravedad en intersecciones estrechas.

### Conclusión de la sesión
El cambio ha merecido la pena: al no malgastar las píldoras a lo tonto, Pac-Man sobrevive mejor y caza mucho más. Los fantasmas comidos suben a 3.63 de media (+0.34 sobre v1 y casi un fantasma más que en v0), comiéndose casi 6 fantasmas por partida contra Random y más de 3.1 contra Básico. En puntos sube a 3033 de media y el código queda mucho más limpio con 22 reglas en vez de las 35 anteriores.
mereci'pó, as csecome mucho mas



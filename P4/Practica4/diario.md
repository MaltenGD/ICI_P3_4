# Diario - Práctica 4: Ms. Pac-Man (Razonamiento Borroso)

**Integrantes:** Guillermo Isaac Ramos Medina, Óliver García Aguado, Lin Ling, Liling Chen  
**Grupo:** GIRM_OGA_LIN_LING  
**Controlador:** Ms. Pac-Man (Lógica Borrosa)  
**Rivales de referencia:** 5 fantasmas (Random, Aggressive, Basico, Intermedio, Avanzado)  
**Partidas por rival:** 300  
**Métricas fijadas:** Puntos, turnos por vida, fantasmas comidos  

---

## Sesión 1 (Jueves 12 de noviembre) porteo del pacman de la P3


Al revisar el proyecto de partida vimos que el `mspacman-v0.fcl` es una plantilla muy simplona (solo 6 reglas de huir o comer sin píldoras de poder ni matices).
Decidimos que nuestra `v0`  iba a ser un porteo fiel de uno de los primeros pacman de la Práctica 3 y no una copia directa de la plantilla, este controlador agrega a la plantilla lo siguiente:
1. **Peligro escalonado:** En la P3 el pacman diferencia entre peligro cercano general ($d < 25$) y peligro crítico o desesperado ($d \le 6$) donde huir era la mayor prioridad.
2. **Píldoras de poder con ventaja:** Comprobábamos si Pac-Man llegaba a la píldora antes que el fantasma. Si el fantasma estaba demasiado encima, no se arriesgaba y huía.
3. **Criterio de caza estricto:** Solo perseguir a un fantasma comestible si daba tiempo a alcanzarlo antes de que volviese a ser peligroso ($tiempo > 2 \cdot dist + 2$).
4. **Rematar:** Si un comestible estaba muy cerca, valía la pena rematarlo aunque hubiese peligro a distancia media.

Para trasladar esto a la arquitectura de `jFuzzyLogic`:
- En `MsPacManInput.java` calculamos en Java las distancias en pasos de camino acotadas a `LEJOS = 150` (evitando devolver `-1` para no tener problemas con los conjuntos borrosos): `peligro`, `comestible` (solo si da tiempo a cazarlo; si no, vale `LEJOS`), `ppill` y una nueva variable `ventajaPPill = peligro - ppill`.
- Las acciones (`ComerAction`, `HuirAction`, `PerseguirAction`, `IrAPPillAction`) ahora implementan `Action` directa, eliminando `RulesAction` y `parseFact` de Jess.
- En `mspacman-v0.fcl` definimos las 4 entradas, las 4 salidas (`comer`, `huir`, `perseguir`, `irAPPill`) con defuzzificación por Centro de Gravedad (`COG`) y particiones borrosas con términos trapezoidales y triangulares (`encima`, `cerca`, `media`, `lejos`).

Tuvimos que lidiar con un par de detalles de sintaxis de FCL con jFuzzyLogic: en una regla (`THEN`), jFuzzyLogic solo admite asignar a lo sumo dos salidas separadas por coma en una misma línea; si se ponen tres da error de parseo (`mismatched input ',' expecting SEMICOLON`). Lo solucionamos separandolo en una regla aparte.

---

## Sesión 2 (Viernes 13 de noviembre) · Caza, verificación de casos y resultados de la v0

Terminamos de ajustar las 9 reglas borrosas de la `v0` que completan el porteo de la P3:
- **R1 y R2 (Huida desesperada):** Si `peligro IS encima`, `huir IS mucho`, `comer IS poco` e `irAPPill IS poco`.
- **R3 (Huida estándar):** Si `peligro IS cerca`, `huir IS mucho`, `comer IS poco`.
- **R4 y R5 (Comida y retirada de huida):** Con peligro a distancia `media`, `comer IS algo`; con peligro `lejos`, `comer IS mucho` y `huir IS poco`.
- **R6 (Poder con ventaja):** Si `peligro IS cerca AND ppill IS cerca AND ventajaPPill IS ventaja`, `irAPPill IS mucho, huir IS poco`.
- **R7 (Caza estándar):** Si `comestible IS cerca AND peligro IS NOT cerca AND peligro IS NOT encima`, `perseguir IS mucho, comer IS poco`. La supresión de `comer` es obligatoria para romper el empate contra la regla de comer en ausencia de peligro.
- **R8 (Rematar antes de huir):** Si `comestible IS cerca AND peligro IS media`, `perseguir IS mucho, comer IS poco`.
- **R9 (Comer por defecto):** Si no hay peligro encima ni cerca y no hay comestibles que cazar, `comer IS mucho`.

### Verificación unitaria con `ProbarBorroso`

Comprobamos el comportamiento del motor borroso en frío con `ProbarBorroso`, diseñando 7 situaciones que replican las condiciones de la P3:
1. **Nada cerca:** entradas (150, 150, 150, -150) -> gana `comer` (8.7 vs 1.3). [BIEN]
2. **Peligro encima a 5 pasos (desesperado):** entradas (5, 150, 150, -150) -> gana `huir` (8.7 vs 1.3). [BIEN]
3. **Peligroso a 20 pasos sin píldora:** entradas (20, 150, 150, -150) -> gana `huir` (8.7 vs 1.3). [BIEN]
4. **Peligroso a 45 pasos (media):** entradas (45, 150, 150, -150) -> gana `comer` (6.5 vs 0.0). [BIEN]
5. **Comestible alcanzable a 10 pasos con peligro lejos:** entradas (150, 10, 150, -150) -> gana `perseguir` (8.7 vs 5.0). [BIEN]
6. **Peligro a 25 pasos y PPill a 8 pasos (ventaja +17):** entradas (25, 150, 8, +17) -> gana `irAPPill` (8.6 vs 5.0). [BIEN]
7. **Peligro encima a 5 pasos y PPill a 8 pasos (desesperado sin ventaja):** entradas (5, 150, 8, -3) -> gana `huir` (8.7 vs 1.3). [BIEN]

La curva de control de peligro muestra transiciones suaves:
- De 0 a 30 pasos gana `huir` con holgura.
- A partir de 35 pasos `comer` toma la delantera suavemente (6.9 a 35 pasos, subiendo hasta 8.7 a los 75 pasos).
- No hay escalones muertos ni empates no deseados.

Probamos también `VerPartida` con `TrazaBorrosa` conectada frente a los fantasmas agresivos para comprobar que las reglas se activan y que las 4 acciones se ejecutan cuando toca.

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

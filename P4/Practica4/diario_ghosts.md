# Diario - Práctica 4: Ghosts (Razonamiento Borroso)

**Integrantes:** Guillermo Isaac Ramos Medina, Óliver García Aguado, Lin Ling, Liling Chen  
**Grupo:** GIRM_OGA_LIN_LING  


# DIARIO DE GHOSTS

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

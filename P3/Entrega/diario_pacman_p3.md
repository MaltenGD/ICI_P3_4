# diario - practica 3, pacman
## 25 de octubre de 2026 - v2
se ha ampliado la plantilla de siete reglas. en esta version se tienen nueve reglas.

primero se ha revisado la informacion que reciben las reglas. en MsPacManInput.java se recogen los datos del juego y se preparan las distancias, las salidas disponibles y los objetivos de comida y poder. tambien se calcula un margen para estimar si un fantasma puede alcanzar a pacman durante el recorrido.

para huir se comparan las salidas que puede utilizar segun su ultima direccion. para cazar se comprueba que quede tiempo suficiente antes de que el fantasma deje de ser comestible y que el camino tenga margen frente a los otros fantasmas.

se han añadido dos reglas para cuando lo persiguen varios fantasmas. una comprueba si hay al menos dos fantasmas peligrosos a menos de treinta pasos y crea el hecho ACOSO. la otra utiliza ese hecho para ir a por una pildora de poder a ochenta pasos o menos, siempre que el camino tenga margen suficiente. asi no se descarta el poder solo porque los perseguidores esten mas cerca que la pildora.

### ayuda de ia en esta version
se ha pedido ayuda a la ia para ampliar las siete reglas de la plantilla y programar en Java los calculos de caminos, los hechos y las acciones. 

### prompts
1. amplia MsPacManInput para preparar la informacion que necesitan las reglas. recorre los fantasmas y descarta los que siguen en la carcel. guarda la distancia, si son comestibles y cuanto tiempo les queda. prepara tambien las pildoras disponibles y las salidas permitidas segun la ultima direccion de pacman.

2. crea Caminos.java para reunir los calculos que se van a repetir. necesito buscar rutas con los metodos del motor y comprobar si un fantasma puede alcanzar a pacman durante el recorrido. ten en cuenta la distancia de contacto del juego y el tiempo que le queda comestible. limita la comprobacion a cuarenta nodos y no permitas girar 180 grados.

3. modifica la accion de huir para comparar las salidas disponibles. calcula el margen frente a los fantasmas en cada recorrido y pasa esa informacion como hechos SALIDA. deja que la regla de huida elija la salida con mayor margen y que la accion ejecute el movimiento indicado.

4.  añade una regla para detectar cuando lo persiguen varios fantasmas. comprueba que haya dos fantasmas distintos, peligrosos y a menos de treinta pasos. cuando se cumpla, crea el hecho ACOSO. añade otra regla que use ese hecho para buscar una pildora de poder a ochenta pasos o menos si su camino tiene margen suficiente.

5. revisa las reglas de perseguir y rematar. no basta con que el fantasma sea comestible: comprueba que su tiempo restante supere el doble de la distancia mas ocho pasos y que la ruta tenga margen frente a los otros fantasmas. guarda la accion ganadora y sus parametros en UltimaAccion para consultarlos en el siguiente turno.

### resultados de v2 frente a v1
se ha probado v2 frente a la plantilla v1 con trescientas partidas por rival y version. 

#### puntos
| rival | v1 ± error tipico | v2 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 4342 ± 128 | 5929 ± 165 | +1587 | 418 | mejor |
| Aggressive | 3639 ± 142 | 4155 ± 154 | +516 | 419 | mejor |
| basico | 2349 ± 80 | 4250 ± 119 | +1901 | 287 | mejor |
| intermedio | 2374 ± 92 | 3371 ± 113 | +997 | 292 | mejor |
| avanzado | 1829 ± 51 | 2551 ± 57 | +722 | 153 | mejor |
| media de los cinco rivales | 2907 ± 47 | 4051 ± 57 | +1144 | 147 | mejor |

la media de puntos sube de 2907 a 4051, aproximadamente un 39.4 %. la v2 mejora frente a los cinco rivales segun el criterio del comparador.

#### turnos por vida
| rival | v1 ± error tipico | v2 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 551 ± 22 | 936 ± 47 | +385 | 105 | mejor |
| Aggressive | 722 ± 70 | 2521 ± 95 | +1799 | 235 | mejor |
| basico | 264 ± 7 | 605 ± 29 | +341 | 59 | mejor |
| intermedio | 254 ± 18 | 426 ± 34 | +172 | 77 | mejor |
| avanzado | 375 ± 40 | 350 ± 14 | -25 | 86 | ruido |
| media de los cinco rivales | 433 ± 17 | 967 ± 23 | +534 | 58 | mejor |

la media de turnos por vida pasa de 433 a 967, aproximadamente un 123.3 % mas. mejora frente a cuatro rivales.

#### fantasmas comidos
| rival | v1 ± error tipico | v2 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 5.60 ± 0.19 | 7.14 ± 0.22 | +1.53 | 0.57 | mejor |
| Aggressive | 4.04 ± 0.18 | 5.01 ± 0.21 | +0.97 | 0.55 | mejor |
| basico | 2.76 ± 0.13 | 5.31 ± 0.17 | +2.55 | 0.43 | mejor |
| intermedio | 2.58 ± 0.13 | 4.01 ± 0.16 | +1.44 | 0.42 | mejor |
| avanzado | 2.04 ± 0.10 | 3.16 ± 0.11 | +1.12 | 0.29 | mejor |
| media de los cinco rivales | 3.40 ± 0.07 | 4.92 ± 0.08 | +1.52 | 0.21 | mejor |

la media de fantasmas comidos pasa de 3.40 a 4.92 por partida, aproximadamente un 44.7 % mas. mejora frente a los cinco rivales.

#### valoracion de los cambios
en esta prueba, v2 mejora las medias generales de puntos, turnos por vida y fantasmas comidos respecto a v1. no se detecta un empeoramiento claro frente a ningun rival.

esta version ya puede buscar poder cuando hay varios perseguidores, pero todavia no tiene reglas para mantener ese objetivo, aprovechar zonas con comida o salir de recorridos repetidos. esto se mejora en la siguiente version.

## 27 de octubre de 2026 - v2.1
esta version parte de la anterior y añade seis reglas, hasta llegar a quince. se mantienen los calculos de caminos y las comprobaciones de seguridad, y se amplian las decisiones del archivo de reglas.

se ha añadido una regla para mantener la pildora de poder elegida mientras siga activa y el camino tenga margen suficiente. asi pacman no cambia de objetivo sin motivo en cada turno.

tambien se da prioridad a recoger comida cuando quedan diez pildoras normales o menos. cuando hay fantasmas comestibles pero ninguno se puede alcanzar a tiempo, sigue buscando comida en vez de perseguir una presa que queda demasiado lejos.

se ha añadido la posibilidad de interceptar a un fantasma. se calcula el siguiente cruce al que se dirige y se compara con la persecucion directa. la regla elige interceptar si supone ahorrar al menos cuatro pasos, queda tiempo comestible suficiente y el recorrido tiene margen.

para recoger comida se permite buscar una zona con al menos tres pildoras cuando la comida mas cercana queda a mas de diez pasos. de esta forma puede ir hacia una zona con mas comida.

se ha añadido una regla para cuando lleva ciento veinte turnos sin comer. Memoria.java guarda las visitas a los nodos y el tiempo sin recoger comida. con esos datos se elige una salida menos visitada. los datos se borran cuando recoge una pildora normal o de poder.

### ayuda de ia en esta version
se ha usado la ia para programar las reglas de mantener poder, terminar de recoger comida, interceptar, comer cuando no hay una presa alcanzable, buscar zonas y evitar recorridos sin comida. tambien ha ayudado con la memoria, las comprobaciones y la comparacion de resultados.

### prompts
1. añade una regla para mantener la pildora de poder elegida. utiliza el objetivo guardado en UltimaAccion y comprueba que siga activo, este a noventa pasos o menos y conserve un margen mayor que tres. dale prioridad sobre la busqueda normal de poder para que no cambie de objetivo sin motivo.

2. añade una regla para terminar de recoger comida cuando queden diez pildoras normales o menos. utiliza un objetivo de comida con camino seguro y dale prioridad sobre una persecucion normal. si ya no quedan pildoras normales, permite que ese objetivo sea una de poder.

3. prepara un objetivo de intercepcion para cada fantasma comestible. sigue su direccion hasta el siguiente cruce y calcula cuanto tarda pacman en llegar. añade una regla que elija interceptar cuando ahorre al menos cuatro pasos frente a perseguir directamente, quede tiempo comestible suficiente y el camino tenga margen.

4. añade una regla para seguir comiendo cuando haya fantasmas comestibles pero ninguno sea alcanzable a tiempo. comprueba que no haya peligro y que exista un objetivo de comida con margen suficiente. no quiero que pierda turnos persiguiendo a un fantasma al que no puede llegar.

5. añade una regla para buscar una zona con varias pildoras. en MsPacManInput revisa los cruces cercanos y cuenta la comida que tienen alrededor. si la comida mas cercana esta a mas de diez pasos, no hay peligro y existe una zona segura con al menos tres pildoras, permite elegirla como objetivo.

6. amplia Memoria para contar las visitas a los nodos y los turnos sin comer. cuenta una visita solo cuando cambie de nodo y borra los datos cuando recoja comida. añade una regla que, despues de ciento veinte turnos sin comer, elija una salida con margen mayor que tres y menos visitas. si empatan, prefiere la que deje la comida mas cerca. reinicia tambien la memoria al morir, cambiar de nivel o empezar otra partida.

### resultados de v2.1 frente a v2
se han comparado las dos versiones con trescientas partidas contra cada uno de los cinco rivales

#### puntos
| rival | v2 ± error tipico | v2.1 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 5764 ± 162 | 6031 ± 160 | +267 | 456 | ruido |
| Aggressive | 4629 ± 169 | 7969 ± 312 | +3341 | 710 | mejor |
| basico | 4170 ± 130 | 4992 ± 155 | +822 | 404 | mejor |
| intermedio | 3415 ± 118 | 3770 ± 132 | +356 | 353 | mejor |
| avanzado | 2507 ± 62 | 2538 ± 61 | +31 | 174 | ruido |
| media de los cinco rivales | 4097 ± 60 | 5060 ± 82 | +963 | 203 | mejor |

la media de puntos sube de 4097 a 5060, aproximadamente un 23.5 %.

#### turnos por vida
| rival | v2 ± error tipico | v2.1 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 876 ± 39 | 983 ± 48 | +107 | 124 | ruido |
| Aggressive | 2534 ± 94 | 1931 ± 89 | -603 | 259 | peor |
| basico | 562 ± 23 | 596 ± 22 | +34 | 65 | ruido |
| intermedio | 383 ± 27 | 437 ± 31 | +55 | 82 | ruido |
| avanzado | 376 ± 22 | 345 ± 14 | -30 | 53 | ruido |
| media de los cinco rivales | 946 ± 22 | 859 ± 22 | -87 | 62 | peor |

la media de turnos por vida baja de 946 a 859, aproximadamente un 9.2 %. 

#### fantasmas comidos
| rival | v2 ± error tipico | v2.1 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 6.97 ± 0.23 | 7.40 ± 0.22 | +0.43 | 0.63 | ruido |
| Aggressive | 5.65 ± 0.22 | 9.37 ± 0.37 | +3.72 | 0.87 | mejor |
| basico | 5.28 ± 0.19 | 6.38 ± 0.21 | +1.11 | 0.56 | mejor |
| intermedio | 4.04 ± 0.17 | 4.65 ± 0.18 | +0.61 | 0.49 | mejor |
| avanzado | 2.99 ± 0.11 | 3.18 ± 0.12 | +0.19 | 0.32 | ruido |
| media de los cinco rivales | 4.99 ± 0.08 | 6.20 ± 0.10 | +1.21 | 0.27 | mejor |

la media de fantasmas comidos pasa de 4.99 a 6.20 por partida, aproximadamente un 24.2 % mas.

#### valoracion de los cambios
la v2.1 consigue mas puntos y come mas fantasmas, pero baja en turnos por vida.

## 25 de octubre de 2026 - v3.1
se ha creado otra version a partir de la plantilla del profesor a la par que v2.1 para probar ideas distintas y despues unirlas con v2.1.

se ha añadido AcecharAction. esta accion busca un cruce a unos veinticinco nodos del fantasma para intentar cortarle el paso. si el fantasma no es comestible y queda a menos de seis nodos, pacman intenta huir.

tambien se han cambiado las acciones de comer, huir, ir a por poder y perseguir.

### resultados historicos de la autora
#### puntos
| rival | v0 del ensayo ± error tipico | v1 del ensayo ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 1991 ± 74 | 2139 ± 87 | +148 | 228 | ruido |
| Aggressive | 1598 ± 118 | 1442 ± 92 | -156 | 299 | ruido |
| Basico | 1275 ± 61 | 1156 ± 54 | -119 | 164 | ruido |
| Intermedio | 1236 ± 71 | 1419 ± 93 | +183 | 233 | ruido |
| Avanzado | 1127 ± 55 | 1252 ± 57 | +125 | 159 | ruido |
| MEDIA (5) | 1445 ± 35 | 1482 ± 35 | +36 | 100 | ruido |

el cambio del ensayo es mejor contra 0 rivales, peor contra 0 y no se distingue contra 5.

#### turnos por vida
| rival | v0 del ensayo ± error tipico | v1 del ensayo ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 536 ± 24 | 632 ± 47 | +96 | 105 | ruido |
| Aggressive | 890 ± 139 | 576 ± 108 | -314 | 352 | ruido |
| Basico | 254 ± 14 | 232 ± 10 | -22 | 34 | ruido |
| Intermedio | 240 ± 39 | 198 ± 8 | -42 | 79 | ruido |
| Avanzado | 224 ± 20 | 255 ± 26 | +30 | 66 | ruido |
| MEDIA (5) | 429 ± 30 | 379 ± 24 | -50 | 77 | ruido |

el cambio del ensayo es mejor contra 0 rivales, peor contra 0 y no se distingue contra 5.

#### fantasmas comidos
| rival | v0 del ensayo ± error tipico | v1 del ensayo ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 1.43 ± 0.13 | 1.84 ± 0.17 | +0.41 | 0.43 | ruido |
| Aggressive | 1.50 ± 0.18 | 1.13 ± 0.15 | -0.37 | 0.46 | ruido |
| Basico | 0.67 ± 0.11 | 0.50 ± 0.09 | -0.17 | 0.28 | ruido |
| Intermedio | 0.79 ± 0.12 | 1.07 ± 0.13 | +0.28 | 0.36 | ruido |
| Avanzado | 0.54 ± 0.09 | 0.72 ± 0.09 | +0.18 | 0.26 | ruido |
| MEDIA (5) | 0.99 ± 0.06 | 1.05 ± 0.06 | +0.07 | 0.16 | ruido |

el cambio del ensayo es mejor contra 0 rivales, peor contra 0 y no se distingue contra 5.

## 26 de octubre de 2026 - v3.2
se ha quitado AcecharAction porque, en la prueba realizada, la duracion media de las partidas bajaba de 1074 a 744 turnos. se ha conservado el codigo en el repositorio como referencia.

## 27 de octubre de 2026 - v3.3
se ha añadido una busqueda que mira cinco movimientos por adelantado para las acciones de huir, comer e ir a por una pildora de poder.

- huir: compara las salidas para alejarse de los fantasmas peligrosos y evita los callejones cuando puede.

- comer: busca pildoras teniendo en cuenta el peligro del camino.

- ir a por poder: utiliza tambien la busqueda de cinco movimientos.

### resultados historicos de la autora
se han jugado cien partidas por version y rival, con semillas del 1 al 100.

#### puntos
| rival | v0 del ensayo ± error tipico | v1 del ensayo ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 4148 ± 203 | 4511 ± 198 | +363 | 568 | ruido |
| Aggressive | 2073 ± 114 | 2332 ± 100 | +260 | 302 | ruido |
| Basico | 1820 ± 75 | 1822 ± 65 | +2 | 199 | ruido |
| Intermedio | 1743 ± 78 | 1672 ± 83 | -71 | 228 | ruido |
| Avanzado | 1403 ± 53 | 1358 ± 50 | -45 | 145 | ruido |
| MEDIA (5) | 2238 ± 52 | 2339 ± 50 | +102 | 145 | ruido |

el cambio del ensayo es mejor contra 0 rivales, peor contra 0 y no se distingue contra 5.

#### turnos por vida
| rival | v0 del ensayo ± error tipico | v1 del ensayo ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 959 ± 88 | 1153 ± 98 | +195 | 262 | ruido |
| Aggressive | 852 ± 123 | 1548 ± 162 | +696 | 407 | mejor |
| Basico | 384 ± 17 | 409 ± 19 | +26 | 52 | ruido |
| Intermedio | 319 ± 39 | 327 ± 54 | +8 | 132 | ruido |
| Avanzado | 504 ± 86 | 524 ± 94 | +20 | 255 | ruido |
| MEDIA (5) | 603 ± 36 | 792 ± 44 | +189 | 113 | mejor |

el cambio del ensayo es mejor contra 1 rival, peor contra 0 y no se distingue contra 4.

#### fantasmas comidos
| rival | v0 del ensayo ± error tipico | v1 del ensayo ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 3.21 ± 0.23 | 3.23 ± 0.22 | +0.02 | 0.64 | ruido |
| Aggressive | 2.02 ± 0.18 | 2.36 ± 0.17 | +0.34 | 0.50 | ruido |
| Basico | 0.75 ± 0.09 | 0.70 ± 0.10 | -0.05 | 0.27 | ruido |
| Intermedio | 1.01 ± 0.13 | 1.14 ± 0.12 | +0.13 | 0.36 | ruido |
| Avanzado | 0.24 ± 0.05 | 0.59 ± 0.08 | +0.35 | 0.19 | mejor |
| MEDIA (5) | 1.45 ± 0.07 | 1.60 ± 0.07 | +0.16 | 0.19 | ruido |

el cambio del ensayo es mejor contra 1 rival, peor contra 0 y no se distingue contra 4.

### ayuda de ia en esta version

segun el diario de la autora, no se ha usado ia para este cambio. la busqueda se ha tomado del codigo de la practica 2 de GRIM_LC, MsPacManV0ME.java.

### resultados de v3.3 frente a v2.1
se han medido trescientas partidas por rival y version, con semillas del 1001 al 1300. estas tablas pertenecen a otra tanda de pruebas. no se mezclan con las cien partidas por rival que aparecen arriba. los valores despues de ± son errores tipicos.

#### puntos
| rival | v2.1 ± error tipico | v3.3 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 6261.67 ± 166.49 | 4174.07 ± 110.34 | -2087.60 | 399.47 | peor |
| Aggressive | 8051.50 ± 300.28 | 2253.07 ± 67.17 | -5798.43 | 615.41 | peor |
| Basico | 4757.30 ± 144.76 | 1845.73 ± 41.29 | -2911.57 | 301.07 | peor |
| Intermedio | 3835.80 ± 133.07 | 1889.33 ± 54.52 | -1946.47 | 287.60 | peor |
| Avanzado | 2574.97 ± 61.60 | 1528.60 ± 33.80 | -1046.37 | 140.52 | peor |
| MEDIA | 5096.25 ± 80.09 | 2338.16 ± 30.00 | -2758.09 | 171.04 | peor |

#### turnos por vida
| rival | v2.1 ± error tipico | v3.3 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 1085.65 ± 54.96 | 1054.28 ± 55.84 | -31.37 | 156.69 | ruido |
| Aggressive | 2025.39 ± 90.78 | 1080.72 ± 81.23 | -944.67 | 243.63 | peor |
| Basico | 568.17 ± 17.75 | 381.67 ± 10.84 | -186.50 | 41.60 | peor |
| Intermedio | 461.36 ± 31.84 | 276.24 ± 13.57 | -185.11 | 69.22 | peor |
| Avanzado | 356.79 ± 14.45 | 511.50 ± 48.46 | +154.71 | 101.14 | mejor |
| MEDIA | 899.47 ± 22.63 | 660.88 ± 22.24 | -238.59 | 63.45 | peor |

#### fantasmas comidos
| rival | v2.1 ± error tipico | v3.3 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 7.66 ± 0.23 | 3.08 ± 0.12 | -4.59 | 0.52 | peor |
| Aggressive | 9.53 ± 0.36 | 2.34 ± 0.10 | -7.19 | 0.75 | peor |
| Basico | 6.13 ± 0.20 | 0.89 ± 0.06 | -5.24 | 0.42 | peor |
| Intermedio | 4.72 ± 0.18 | 1.39 ± 0.08 | -3.32 | 0.40 | peor |
| Avanzado | 3.27 ± 0.11 | 0.77 ± 0.06 | -2.50 | 0.25 | peor |
| MEDIA | 6.26 ± 0.10 | 1.69 ± 0.04 | -4.57 | 0.22 | peor |

## 27 de octubre de 2026 - v4

se han unido las ideas de v2.1 y v3.3 en una sola version. de v2.1 se mantienen las reglas para elegir que hacer, los objetivos concretos y la memoria para evitar dar vueltas sin comer. de v3.3 se aprovecha la busqueda que mira cinco movimientos por adelantado.

tambien se utiliza MapaCostes.java para valorar los nodos del laberinto segun la comida y el peligro. el mapa ayuda a elegir el movimiento al comer y al huir, y tiene en cuenta los cruces que hay mas adelante. las reglas siguen decidiendo si pacman debe comer, huir, buscar poder o cazar.

se ha creado AccionFusion.java para conectar estas partes. cuando toca comer de forma normal o huir, primero se consulta el mapa. despues, BusquedaCorta.java revisa la salida elegida si hay varias salidas permitidas y un fantasma peligroso a doce pasos o menos.

esta segunda comprobacion mira el margen frente a los fantasmas durante un maximo de cinco nodos del pasillo. solo cambia el movimiento si el camino elegido tiene un margen de tres o menos y hay otra salida con margen positivo y mayor. ademas, la valoracion de esa alternativa debe mejorar al menos veinte unidades. asi no se cambia una salida solo por una diferencia pequeña.

para ir a por poder, perseguir, interceptar, buscar una zona con comida o terminar el nivel, se mantiene el camino hacia el objetivo concreto. estas acciones ya utilizan las comprobaciones de v2.1. todos los movimientos respetan la ultima direccion de pacman y no permiten girar 180 grados.

v4 tiene dieciseis reglas. conserva las quince de v2.1 y añade una para aprovechar una pildora de poder cercana cuando el camino tiene margen suficiente.

### ayuda de ia en esta version

se ha pedido ayuda a la ia para unir las dos formas de jugar: mantener las reglas, los objetivos y la memoria de v2.1, y aprovechar la busqueda de cinco movimientos de v3.3. tambien ha ayudado a conectar el mapa con las acciones, comprobar los movimientos y comparar los resultados.

### prompts

1. crea v4 uniendo v2.1 y v3.3. conserva las quince reglas de v2.1, los hechos que necesitan y la memoria de visitas y turnos sin comer. manten las decisiones de mantener poder, terminar el nivel, interceptar, comer sin una presa alcanzable, buscar zonas y salir de recorridos repetidos. añade una regla para aprovechar una pildora de poder cercana si el camino tiene margen suficiente.

2. utiliza MapaCostes.java para elegir el movimiento cuando pacman come de forma normal o huye. ten en cuenta la comida, los fantasmas peligrosos y los cruces que hay mas adelante. para buscar poder, perseguir, interceptar, ir a una zona con comida o terminar el nivel, conserva el camino hacia el objetivo elegido por las reglas.

3. crea BusquedaCorta.java a partir de LookaheadPacman de v3.3. manten la busqueda de cinco movimientos y el descuento de 0.65, para que los pasos mas lejanos cuenten menos. conserva tambien la valoracion del peligro, la distancia al objetivo y los callejones. añade un metodo que reciba la salida propuesta por el mapa y la compare con la que encuentra esta busqueda.

4. aplica esa comprobacion solo si hay varias salidas permitidas y un fantasma peligroso a doce pasos o menos. revisa el margen de hasta cinco nodos de cada pasillo. cambia la salida si la elegida tiene un margen de tres o menos y la alternativa tiene margen positivo y mayor. exige tambien una mejora de al menos veinte unidades en su valoracion y no permitas girar 180 grados.

5. crea AccionFusion.java para llamar al mapa y a la busqueda corta cuando la regla elija comer de forma normal o huir. para las demas acciones conserva IrObjetivoAction. comprueba en partidas que no se devuelvan movimientos ilegales, que la busqueda corta llegue a corregir alguna salida y que se activen las dieciseis reglas.

6. compara v2.1, v3.3 y v4 con trescientas partidas por rival, el mismo limite de turnos y las mismas semillas. muestra los puntos, los turnos por vida y los fantasmas comidos con sus errores tipicos. deja los resultados en la consola de Comparar.java y utiliza dos hilos. prepara las tablas del diario con los resultados medidos.

### comprobaciones

se ha compilado el codigo con el motor real y se han pasado cincuenta y seis casos de reglas sin fallos. en treinta partidas de comprobacion se han revisado 68030 movimientos permitidos y se han activado las dieciseis reglas. en otra comprobacion de treinta partidas, la busqueda corta ha corregido trece salidas. estas partidas de comprobacion no se incluyen en las tablas de resultados.

el margen de una ruta es una estimacion. ayuda a comparar caminos, pero no garantiza que pacman sobreviva a todos los movimientos futuros de los fantasmas.

### resultados de v4 frente a v3.3

se muestran trescientas partidas por version y rival, con semillas del 1001 al 1300 y un limite de cuatro mil turnos. las tres versiones de estas tablas son v2.1, v3.3 y v4: en total, 4500 partidas. se utilizan los resultados finales de cada una.

los valores despues de ± son errores tipicos, no desviaciones tipicas. D indica cuanto cambia la media. el comparador marca una diferencia como mejor o peor cuando el valor absoluto de D supera 2 · ET_D. si no lo supera, aparece como ruido y no se puede afirmar que haya un cambio claro.

#### puntos
| rival | v3.3 ± error tipico | v4 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 4174.07 ± 110.34 | 8653.80 ± 115.91 | +4479.73 | 320.06 | mejor |
| Aggressive | 2253.07 ± 67.17 | 16949.30 ± 307.61 | +14696.23 | 629.71 | mejor |
| Basico | 1845.73 ± 41.29 | 9683.40 ± 213.98 | +7837.67 | 435.86 | mejor |
| Intermedio | 1889.33 ± 54.52 | 9008.70 ± 234.63 | +7119.37 | 481.75 | mejor |
| Avanzado | 1528.60 ± 33.80 | 4035.67 ± 70.73 | +2507.07 | 156.79 | mejor |
| MEDIA | 2338.16 ± 30.00 | 9666.17 ± 92.50 | +7328.01 | 194.49 | mejor |

#### turnos por vida
| rival | v3.3 ± error tipico | v4 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 1054.28 ± 55.84 | 2772.28 ± 73.05 | +1717.99 | 183.89 | mejor |
| Aggressive | 1080.72 ± 81.23 | 2623.28 ± 76.54 | +1542.56 | 223.22 | mejor |
| Basico | 381.67 ± 10.84 | 1620.52 ± 67.43 | +1238.86 | 136.59 | mejor |
| Intermedio | 276.24 ± 13.57 | 1077.56 ± 53.50 | +801.32 | 110.39 | mejor |
| Avanzado | 511.50 ± 48.46 | 566.33 ± 15.76 | +54.83 | 101.93 | ruido |
| MEDIA | 660.88 ± 22.24 | 1732.00 ± 27.46 | +1071.11 | 70.68 | mejor |

#### fantasmas comidos
| rival | v3.3 ± error tipico | v4 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 3.08 ± 0.12 | 11.27 ± 0.17 | +8.19 | 0.42 | mejor |
| Aggressive | 2.34 ± 0.10 | 20.79 ± 0.36 | +18.45 | 0.76 | mejor |
| Basico | 0.89 ± 0.06 | 12.86 ± 0.28 | +11.97 | 0.57 | mejor |
| Intermedio | 1.39 ± 0.08 | 11.83 ± 0.29 | +10.43 | 0.60 | mejor |
| Avanzado | 0.77 ± 0.06 | 5.77 ± 0.12 | +5.00 | 0.26 | mejor |
| MEDIA | 1.69 ± 0.04 | 12.50 ± 0.12 | +10.81 | 0.24 | mejor |

### puntos frente a v2.1
| rival | v2.1 ± error tipico | v4 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Random | 6261.67 ± 166.49 | 8653.80 ± 115.91 | +2392.13 | 405.73 | mejor |
| Aggressive | 8051.50 ± 300.28 | 16949.30 ± 307.61 | +8897.80 | 859.75 | mejor |
| Basico | 4757.30 ± 144.76 | 9683.40 ± 213.98 | +4926.10 | 516.70 | mejor |
| Intermedio | 3835.80 ± 133.07 | 9008.70 ± 234.63 | +5172.90 | 539.46 | mejor |
| Avanzado | 2574.97 ± 61.60 | 4035.67 ± 70.73 | +1460.70 | 187.59 | mejor |
| MEDIA | 5096.25 ± 80.09 | 9666.17 ± 92.50 | +4569.93 | 244.70 | mejor |

### valoracion de los cambios
frente a v3.3, la media de puntos pasa de 2338.16 a 9666.17, un 313.4 % mas. v4 consigue mas puntos y come mas fantasmas frente a los cinco rivales. tambien mejora los turnos por vida frente a cuatro rivales; frente a Avanzado no se ve una diferencia clara.

frente a v2.1, la media de puntos pasa de 5096.25 a 9666.17, un 89.7 % mas. la mejora de puntos se observa frente a los cinco rivales segun el criterio del comparador. en esta tanda, la fusion consigue mas puntos que las dos versiones usadas como referencia.

---

## Elección de versión para Fantasmas a concurso

Elegimos **mspacman-v4** como nuestro controlador a concurso

---

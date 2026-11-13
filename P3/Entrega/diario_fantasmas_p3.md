# Diario - Práctica 3: Fantasmas

**Integrantes:** Óliver García Aguado y Guillermo Isaac Ramos Medina  
**Grupo:** OGA_GIRM  
**Controlador:** Fantasmas  

---

## Sesión 1 (Jueves 29) · Primer contacto con Jess y port de la Pracitca 2 a reglas (version v0)

Empezamos la práctica leyendo la wiki de la asignatura y mirando qué traía el zip de partida. Al principio el tema de las reglas con Jess nos chocó un poco, sobre todo al ver cómo se integraba Java con CLIPS: las clases RuleEngine, RulesInput y RulesAction, cómo funciona la agenda de Rete y cómo se resuelve qué acción ejecutar con el campo "priority" del hecho "ACTION".

Estuvimos un buen rato decidiendo qué estrategia seguir. Vimos claro que ponernos a inventar una lógica de cero en un lenguaje nuevo era una mala idea e iba a ser complicado. Como el controlador de la práctica anterior (GhostsV0Me) funciona bien, decidimos que la "v0" de esta práctica fuera un port directo de la máquina de estados a reglas.

Antes de programar tuvimos en cuenta que:
- CLIPS tiene que usarse para evaluar condiciones y decidir prioridades, sin meter logica pesada ahí dentro.
- Toda la matemática pesada la dejariamos en metodos estáticos en alguna clase auxiliar (esta terminó siendo LogicaFantasma.java.)
- Las acciones de la FSM anterior las adaptamos a clases que heredan de "RulesAction": "PerseguirAction", "HuirAction", "EmboscarAction", "GuardarPPillAction" y "DispersarAction".
- En "GhostsInput.java" preparamos los hechos que necesita leer el motor: el hecho "FANTASMA" con las distancias calculadas y si está en la cárcel o comestible, el hecho "MSPACMAN" con la distancia a la píldora de poder más cercana, y el hecho "GRUPO" con el booleano "apretado" para saber si hay compañeros a menos de 5 nodos.

Al principio dudamos si crear cuatro archivos ".clp" separados (uno para cada fantasma), pero era una locura mantener cuatro ficheros casi iguales. Lo solucionamos metiendo el hecho "(YO (nombre ?yo))" que se aserta antes de cada turno en Java, y así usamos un único archivo "ghosts-v0.clp" para los cuatro.

Tuvimos un par de problemas al arrancar con Jess (por ejemplo, recordar que los booleanos en los templates van como símbolos en minúsculas tipo "true"/"false", y ajustar los tipos de los slots numéricos para que no diera error de parseo). Una vez resuelto, dejamos montadas las **6 reglas** de partida:

- "huirComestible" (prioridad 90): si somos comestibles y estamos libres, huir.
- "dispersar" (prioridad 40): si el grupo está apretado y Ms. Pac-Man no está encima, abrirse en las intersecciones.
- "emboscarPinky" (prioridad 20): solo para Pinky, prediciendo casillas por delante de Pac-Man.
- "guardarPPillInky" (prioridad 20): solo para Inky, si llega a una píldora de poder con ventaja frente a Pac-Man.
- "perseguir" (prioridad 10): regla base por defecto para Blinky y Sue, o cuando los otros no tienen rol activo.
- "pacmanCerca" (salience 10): regla intermedia. Si Pac-Man está a 10 o menos pasos, mete el hecho "(PACMAN_CERCA)" para que la regla de dispersión no se dispare y los fantasmas no se abran cuando están a punto de cazarla.

Probamos la "v0" en "VerPartida" y con "TrazaReglas". La traza salía limpia, sin excepciones de Jess, y vimos que las prioridades funcionaban como esperábamos.

---

## Sesión 2 (Viernes 30)· Ver las partidas y la idea de la v1

Una vez teníamos la base funcionando, nos pusimos a mirar varias partidas en pantalla con "VerPartida" para ver cómo se comportaban en juego real. 

En general se movían coordinados, pero cuando Ms. Pac-Man va directa a una píldora de poder, los fantasmas morían de forma absurda. Los fantasmas seguían en su persecución o emboscada a tope hacia ella, Pac-Man tocaba la píldora y justo en ese momento, se comía al fantasma que tenía a dos casillas sin que este pudiera hacer nada. En muchos turnos perdíamos dos o tres fantasmas seguidos por el mismo motivo.

Pensamos que la solución era meter una regla preventiva en "ghosts-v1.clp" en la que si vemos que Ms. Pac-Man está muy cerca de una píldora de poder y nosotros estamos cerca de ella, cancelar el ataque y salir corriendo antes de que se la coma.

Añadimos la regla R7 ("huirDeLaPPill"):

```clips
;; =================== v1: UN CAMBIO ===================
;; R7 . Ms. Pac-Man junto a una ppill y yo a menos de 20 de ella: no acercarse
(defrule huirDeLaPPill
   (MSPACMAN (distPPill ?p&:(< ?p 15)))
   (YO (nombre ?yo))
   (FANTASMA (nombre ?yo) (comestible false) (enCarcel false) (distPacman ?d&:(< ?d 20)))
   =>
   (assert (ACTION (id huir) (info "R7: Ms. Pac-Man junto a una PPill") (priority 45))))
```

Le pusimos prioridad a 45 para que estuviese por encima de dispersarse (40) y de los roles de ataque (20 y 10), pero sin interferir con la huida cuando ya somos comestibles (90).

Comprobamos con "TrazaReglas" que la regla se disparaba en las situaciones previstas y dejamos el código listo para medirlo contra la v0.


### Resultados frente a v0 (300 partidas)

#### puntos concedidos
| rival | v0 ± error tipico | v1 ± error tipico | D | 2 · ET_D | resultado |
| :--- | ---: | ---: | ---: | ---: | :--- |
| Basico | 2322 ± 58 | 2219 ± 50 | -103 | 153 | ruido |
| Intermedio | 4318 ± 142 | 4316 ± 143 | -2 | 402 | ruido |
| Avanzado | 8811 ± 142 | 9404 ± 150 | +593 | 412 | **peor** |
| media (3 rivales) | 5150 ± 69 | 5313 ± 71 | +163 | 199 | **ruido** |

*Veredicto de puntos:* v1 es mejor contra 0 rivales, peor contra 1 y no se distingue contra 2.

#### turnos por vida
| rival | v0 ± error tipico | v1 ± error tipico | D | 2 · ET_D | resultado |
| :--- | ---: | ---: | ---: | ---: | :--- |
| Basico | 578 ± 50 | 689 ± 60 | +111 | 157 | ruido |
| Intermedio | 2603 ± 84 | 2543 ± 87 | -61 | 241 | ruido |
| Avanzado | 2229 ± 77 | 2385 ± 78 | +155 | 218 | ruido |
| media (3 rivales) | 1804 ± 42 | 1872 ± 44 | +69 | 120 | **ruido** |

*Veredicto de turnos:* no hay diferencia estadística en ningún rival ($|D| \le 2\cdot ET_D$).

#### fantasmas comidos
| rival | v0 ± error tipico | v1 ± error tipico | D | 2 · ET_D | resultado |
| :--- | ---: | ---: | ---: | ---: | :--- |
| Basico | 2.22 ± 0.09 | 2.09 ± 0.08 | -0.13 | 0.25 | ruido |
| Intermedio | 4.52 ± 0.20 | 4.53 ± 0.20 | +0.00 | 0.56 | ruido |
| Avanzado | 11.77 ± 0.20 | 12.58 ± 0.20 | +0.81 | 0.57 | **peor** |
| media (3 rivales) | 6.17 ± 0.10 | 6.40 ± 0.10 | +0.23 | 0.28 | **ruido** |

*Veredicto de fantasmas comidos:* v1 es peor contra Avanzado y ruido contra los otros dos.

---

### ¿Por qué ha salido tan mal contra Avanzado?

La idea parecía buena en teoría, pero la puntuacion incluso empeora con el pacman en avanzado.

Pusimos de nuevo "VerPartida" contra el rival Avanzado y nos dimos cuenta de varias cosas:

1. Poner un radio de 15 pasos para la píldora hace que los fantasmas salgan corriendo antes de tiempo. 
2. Los fantasmas se acorralan solos para cuando pacman se ha comido la power pil, de forma que no tienen forma de huir



### Qué hacer en la siguiente versión (v2)
Lo dejamos aquí por hoy. Visto que la huida preventiva aislada de la v1 empeora el control del mapa, para la `v2` tenemos claro el plan:
- Quitar esa regla de huida preventiva por píldoras de poder y volver a la huida cuando son comestibles.
- Mejorar mas la intercepción anticipada de rutas (aprovechando la acción de emboscada) para que todos los fantasmas puedan hacerlo (no solo Pinkys).
- Controlar el final de partida supervisando las últimas píldoras ya que es mas facil campear las pocas pildoras del final.

---

## Sesión 3 (Viernes 30) Versión v2: Intercepción, control de comida y 15 reglas

Se ha creado una nueva versión de las reglas de los fantasmas (`ghosts-v2.clp`) a partir de la plantilla y de los problemas detectados en las versiones anteriores. Definimos quince reglas para controlar el comportamiento de BLINKY, PINKY, INKY y SUE. Usamos la estrategia de diferentes prioridades para decidir las acciones: Huir tiene prioridad 90, controlar comida 70, interceptar 60-65, dispersarse 40, guardar una píldora de poder / roles 20-25 y perseguir 10.

Detectaremos si Ms. Pac-Man está cerca cuando la distancia es de diez pasos o menos. 
También se comprueba si el grupo está demasiado junto para permitir que los fantasmas se distribuyan por el mapa. 
Se han añadido reglas para huir cuando el fantasma es comestible. 
También se permite perseguir cuando no es comestible, y se asignan comportamientos diferentes según el fantasma: PINKY puede emboscar/interceptar, INKY puede guardar una píldora de poder o sustituir a Sue en el control de comida, SUE y BLINKY controlan las últimas píldoras normales restantes (`COMIDA`), y el resto mantiene la presión o persecución. Por último, se han añadido reglas individuales de dispersión (`separarBlinky`, `separarPinky`, `separarInky`, `separarSue`) cuando están demasiado juntos y Pac-Man se encuentra lejos.

### Resultados de v2 frente a v1

#### Turnos por vida
| rival | v1 ± error tipico | v2 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Basico | 487 ± 42 | 440 ± 38 | -47 | 113 | ruido |
| Intermedio | 2910 ± 83 | 2939 ± 82 | +29 | 234 | ruido |
| Avanzado | 2171 ± 78 | 2034 ± 72 | -138 | 213 | ruido |
| MEDIA (3) | 1856 ± 41 | 1804 ± 39 | -52 | 112 | ruido |

La media de turnos por vida pasa de 1856 a 1804. v2 reduce los turnos por vida.

#### Fantasmas comidos
| rival | v1 ± error tipico | v2 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Basico | 2,05 ± 0,09 | 2,01 ± 0,09 | -0,04 | 0,26 | ruido |
| Intermedio | 3,31 ± 0,18 | 3,66 ± 0,18 | +0,34 | 0,52 | ruido |
| Avanzado | 12,14 ± 0,22 | 11,97 ± 0,21 | -0,17 | 0,61 | ruido |
| MEDIA (3) | 5,83 ± 0,10 | 5,88 ± 0,10 | +0,04 | 0,28 | ruido |

#### Puntos concedidos
| rival | v1 ± error tipico | v2 ± error tipico | D | 2 · ET_D | resultado |
| --- | ---: | ---: | ---: | ---: | --- |
| Basico | 2230 ± 57 | 2221 ± 54 | -10 | 157 | ruido |
| Intermedio | 3446 ± 131 | 3661 ± 136 | +214 | 378 | ruido |
| Avanzado | 9079 ± 156 | 8926 ± 152 | -152 | 436 | ruido |
| MEDIA (3) | 4918 ± 71 | 4936 ± 70 | +17 | 199 | ruido |

La media de puntos pasa de 4918 a 4936 (una diferencia imperceptible). Por lo tanto, v2 obtiene una puntuación ligeramente mayor.

### Valoración de los cambios

v2 da resultados muy parecidos a v1. Hay mejora contra el rival Avanzado (de 9079 a 8926 y los turnos por vida de 2171 a 2034), pero empeora un poco contra Intermedio. No hay mucha diferencia frente a los rivales individuales.


# **Diario de la Práctica 3**

## **MS PACMAN**
### Versiones

#### **1) MsPacMan_test** – Sábado 24 de octubre del 2026 (línea test)

Contenido/Cambios:
Este controlador parte del controlador anterior del grupo GRIM_LC con el objetivo de reutilizar el mapa de costes.

Dentro de lo planeado se necesita los siguientes archivos java para las accones:
* CazaAction
* ComerAction
* HuirAction
* IrPPillAction
* PerseguirAction
* MapaCostes

La relación sería el siguiente: Dependiendo de lo que se cumpla dentro de las reglas este accede a los archivos de las acciones y estas devuelven la fase que se las da al MapaCostes, de aquí se calcula el peso de los nodos y devuelve el siguiente movimiento para el pacman que sería el mejor.

Apoyo de IA: Si, pero al final no se realizo dicha versión ya que resulta que no se diferencia de una maquina de estados y preferimos optar por una base nueva que parte del ejemplo dado en la practica 3.

#### **2) MsPacMan_Version0** – Sábado 25 de octubre del 2026 (línea base)

Contenido/Cambios: Dentro de este controlador hemos añadido AcecharAction que tiene el objetivo de hacer que el pacman vaya a un cruce de aproximadamente 25 nodos más cercanos a un fantasma no comestible para cortarle el paso. Si el fantasma no es comestible y esta a menos de 6 nodos de él, el pacman intentará huir.

También se editó las acciones Comer,Huir,IrAPPill y Perseguir.

Partida MsPacMan:

== puntos: v1 frente a v0 (más es mejor) ==
rival                        v0               v1         D   2·ET_D
Random                1991 ± 74        2139 ± 87      +148      228  ruido
Aggressive           1598 ± 118        1442 ± 92      -156      299  ruido
Basico                1275 ± 61        1156 ± 54      -119      164  ruido
Intermedio            1236 ± 71        1419 ± 93      +183      233  ruido
Avanzado              1127 ± 55        1252 ± 57      +125      159  ruido
------------------------------------------------------------------------
MEDIA (5)             1445 ± 35        1482 ± 35       +36      100  ruido
v1 es mejor contra 0 rivales, peor contra 0 y no se distingue contra 5.

== turnos por vida: v1 frente a v0 (más es mejor) ==
rival                        v0               v1         D   2·ET_D
Random                 536 ± 24         632 ± 47       +96      105  ruido
Aggressive            890 ± 139        576 ± 108      -314      352  ruido
Basico                 254 ± 14         232 ± 10       -22       34  ruido
Intermedio             240 ± 39          198 ± 8       -42       79  ruido
Avanzado               224 ± 20         255 ± 26       +30       66  ruido
------------------------------------------------------------------------
MEDIA (5)              429 ± 30         379 ± 24       -50       77  ruido
v1 es mejor contra 0 rivales, peor contra 0 y no se distingue contra 5.

== fantasmas comidos: v1 frente a v0 (más es mejor) ==
rival                        v0               v1         D   2·ET_D
Random              1,43 ± 0,13      1,84 ± 0,17     +0,41     0,43  ruido
Aggressive          1,50 ± 0,18      1,13 ± 0,15     -0,37     0,46  ruido
Basico              0,67 ± 0,11      0,50 ± 0,09     -0,17     0,28  ruido
Intermedio          0,79 ± 0,12      1,07 ± 0,13     +0,28     0,36  ruido
Avanzado            0,54 ± 0,09      0,72 ± 0,09     +0,18     0,26  ruido
------------------------------------------------------------------------
MEDIA (5)           0,99 ± 0,06      1,05 ± 0,06     +0,07     0,16  ruido
v1 es mejor contra 0 rivales, peor contra 0 y no se distingue contra 5.


#### **2) MsPacMan_Version0.1** – Domingo 26 de octubre del 2026

Contenido/Cambios: Se descarta el uso e AcecharAction porque reduce la duración media de las partidas de 1074 a 744. El codigo se conserva en el repositorio como referencia.

#### **2) MsPacMan_Version0.2** – Lunes 27 de octubre del 2026

Contenido/Cambios: Se introduce un lookahead para HuirAction, ComerAction y irAPPill.

* Huir: evalúa alternativas para alejarse de fantasmas peligrosos y penaliza los callejones.
* Comer: sigue buscando píldoras, pero valora el peligro de la ruta.
* Ir a píldora de poder: aplica la misma búsqueda anticipada.

Partida: 100 partidas por versión y rival, semillas 1 a 100, comparando Ms. Pac-Man.

== puntos: v1 frente a v0 (más es mejor) ==
rival                        v0               v1         D   2·ET_D
Random               4148 ± 203       4511 ± 198      +363      568  ruido
Aggressive           2073 ± 114       2332 ± 100      +260      302  ruido
Basico                1820 ± 75        1822 ± 65        +2      199  ruido
Intermedio            1743 ± 78        1672 ± 83       -71      228  ruido
Avanzado              1403 ± 53        1358 ± 50       -45      145  ruido
------------------------------------------------------------------------
MEDIA (5)             2238 ± 52        2339 ± 50      +102      145  ruido
v1 es mejor contra 0 rivales, peor contra 0 y no se distingue contra 5.

== turnos por vida: v1 frente a v0 (más es mejor) ==
rival                        v0               v1         D   2·ET_D
Random                 959 ± 88        1153 ± 98      +195      262  ruido
Aggressive            852 ± 123       1548 ± 162      +696      407  mejor
Basico                 384 ± 17         409 ± 19       +26       52  ruido
Intermedio             319 ± 39         327 ± 54        +8      132  ruido
Avanzado               504 ± 86         524 ± 94       +20      255  ruido
------------------------------------------------------------------------
MEDIA (5)              603 ± 36         792 ± 44      +189      113  mejor
v1 es mejor contra 1 rivales, peor contra 0 y no se distingue contra 4.

== fantasmas comidos: v1 frente a v0 (más es mejor) ==
rival                        v0               v1         D   2·ET_D
Random              3,21 ± 0,23      3,23 ± 0,22     +0,02     0,64  ruido
Aggressive          2,02 ± 0,18      2,36 ± 0,17     +0,34     0,50  ruido
Basico              0,75 ± 0,09      0,70 ± 0,10     -0,05     0,27  ruido
Intermedio          1,01 ± 0,13      1,14 ± 0,12     +0,13     0,36  ruido
Avanzado            0,24 ± 0,05      0,59 ± 0,08     +0,35     0,19  mejor
------------------------------------------------------------------------
MEDIA (5)           1,45 ± 0,07      1,60 ± 0,07     +0,16     0,19  ruido
v1 es mejor contra 1 rivales, peor contra 0 y no se distingue contra 4.

Uso de la IA:No, el codigo del lookAhead es integrado de la práctica 2 de GRIM_LC, MsPacManV0ME.java.

clp usado final: mspacman-v1
```
(deftemplate MSPACMAN
   (slot distFantasma (type NUMBER))
   (slot distPPill    (type NUMBER))
   (slot ultima       (type SYMBOL)))

(deftemplate FANTASMA
   (slot nombre     (type SYMBOL))
   (slot comestible (type SYMBOL))
   (slot distancia  (type NUMBER))
   (slot tiempo     (type NUMBER)))

(deftemplate PELIGRO)
(deftemplate OPORTUNIDAD)
(deftemplate SIN-PILLS)

(deftemplate ACTION
   (slot id)
   (slot info (default ""))
   (slot priority (type NUMBER))
   (slot fantasma (type SYMBOL)))

;;; =====================================================================
;;;  BLOQUE 0 · Hechos intermedios
;;; =====================================================================

(defrule peligroCerca
   (declare (salience 30))
   (MSPACMAN (distFantasma ?d&:(< ?d 25)))
   =>
   (assert (PELIGRO)))

(defrule sigueElPeligro
   (declare (salience 30))
   (MSPACMAN (ultima huir) (distFantasma ?d&:(< ?d 30)))
   =>
   (assert (PELIGRO)))

(defrule oportunidadCaza
   (declare (salience 28))
   (not (PELIGRO))
   (not (OPORTUNIDAD))
   (MSPACMAN (distFantasma ?df&:(> ?df 30)))
   (FANTASMA (comestible true) (distancia ?d) (tiempo ?t&:(> ?t (+ ?d 2))))
   =>
   (assert (OPORTUNIDAD)))

;;; =====================================================================
;;;  BLOQUE 1 · Huir / power pill
;;; =====================================================================

;; R1a · con peligro y fantasma lejos (>25) -> a por la PPill SIEMPRE
(defrule aLaPPillLejos
   (declare (salience 26))
   (PELIGRO)
   (MSPACMAN (distPPill ?p&:(< ?p 999)) (distFantasma ?f&:(> ?f 25)))
   =>
   (assert (ACTION (id irAPPill) (priority 93)
                   (info "R1a: fantasma lejos, a por la PPill"))))

;; R1b · con peligro y fantasma cerca (<=25) -> a por la PPill solo si llego antes
(defrule aLaPPillCerca
   (declare (salience 25))
   (PELIGRO)
   (MSPACMAN (distPPill ?p) (distFantasma ?f&:(<= ?f 25)))
   (test (< (+ ?p 2) ?f))
   =>
   (assert (ACTION (id irAPPill) (priority 92)
                   (info "R1b: llego a la PPill antes que el fantasma"))))

;; R2 · con peligro -> huir (pierde contra aLaPPill* por priority)
(defrule huir
   (declare (salience 24))
   (PELIGRO)
   =>
   (assert (ACTION (id huir) (priority 80)
                   (info "R2: hay peligro"))))

;; R3 · peligro extremo (<=6) -> huir sí o sí
(defrule huirDesesperado
   (declare (salience 27))
   (MSPACMAN (distFantasma ?d&:(<= ?d 6)))
   =>
   (assert (ACTION (id huir) (priority 95)
                   (info "R3: peligro pegado"))))

;;; =====================================================================
;;;  BLOQUE 2 · Rematar y cazar
;;; =====================================================================

(defrule rematarAntesDeHuir
   (declare (salience 27))
   (MSPACMAN (distFantasma ?df))
   (FANTASMA (nombre ?g) (comestible true)
             (distancia ?dc&:(< ?dc ?df))
             (tiempo ?t&:(> ?t (+ ?dc 2))))
   (not (FANTASMA (comestible true) (distancia ?d2&:(< ?d2 ?dc))))
   =>
   (assert (ACTION (id perseguir) (fantasma ?g) (priority 85)
                   (info (str-cat "R-extra: rematar a " ?g " antes de huir")))))

(defrule cazar
   (declare (salience 22))
   (OPORTUNIDAD)
   (MSPACMAN (distFantasma ?df&:(> ?df 30)))
   (FANTASMA (nombre ?g) (comestible true)
             (distancia ?d) (tiempo ?t&:(> ?t (+ ?d 2))))
   (not (FANTASMA (comestible true) (distancia ?d2&:(< ?d2 ?d))))
   =>
   (assert (ACTION (id perseguir) (fantasma ?g) (priority 70)
                   (info (str-cat "R4: cazar a " ?g)))))

(defrule cazarContrareloj
   (declare (salience 21))
   (OPORTUNIDAD)
   (MSPACMAN (distFantasma ?df&:(> ?df 20)))
   (FANTASMA (nombre ?g) (comestible true)
             (distancia ?d&:(< ?d 15)) (tiempo ?t&:(> ?t 0)))
   =>
   (assert (ACTION (id perseguir) (fantasma ?g) (priority 65)
                   (info (str-cat "R5: cazar contra reloj a " ?g)))))

;;; =====================================================================
;;;  BLOQUE 4 · Comer / PPill de mantenimiento
;;; =====================================================================

(defrule comer
   (declare (salience 8))
   (not (PELIGRO))
   (not (OPORTUNIDAD))
   =>
   (assert (ACTION (id comer) (priority 10)
                   (info "R9: no pasa nada, comer"))))

(defrule irAPPillSiNoQuedanPills
   (declare (salience 7))
   (not (PELIGRO))
   (SIN-PILLS)
   (MSPACMAN (distPPill ?p&:(< ?p 999)))
   =>
   (assert (ACTION (id irAPPill) (priority 15)
                   (info "R11: sin píldoras normales, a por la PPill"))))

;;; =====================================================================
;;;  BLOQUE 5 · Anti-bloqueo (corregido: mira si SIGUE habiendo peligro)
;;; =====================================================================

(defrule noRepetirHuir
   (declare (salience 5))
   (MSPACMAN (ultima huir) (distFantasma ?d&:(> ?d 30)))
   (not (PELIGRO))
   =>
   (assert (ACTION (id comer) (priority 5)
                   (info "R12: ya no hace falta huir"))))

(defrule noRepetirCazar
   (declare (salience 5))
   (MSPACMAN (ultima perseguir))
   (not (FANTASMA (comestible true)))
   =>
   (assert (ACTION (id comer) (priority 5)
                   (info "R13: ya no hay comestibles"))))

;;; =====================================================================
;;;  BLOQUE 6 · Fallback
;;; =====================================================================

(defrule fallback
   (declare (salience -10))
   =>
   (assert (ACTION (id comer) (priority 0)
                   (info "R14: fallback"))))
```
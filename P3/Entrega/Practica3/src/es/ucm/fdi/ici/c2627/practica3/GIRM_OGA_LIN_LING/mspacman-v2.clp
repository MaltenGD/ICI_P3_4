; version 4 - 9 reglas

; hechos calculados en java: aqui solo comparamos medidas
(deftemplate MSPACMAN
 (slot distFantasma (type NUMBER) (default 10000))
 (slot distPPill (type NUMBER) (default 10000))
 (slot ultima (type SYMBOL) (default nada))
 (slot presaAnterior (type SYMBOL) (default ninguno))
 (slot objetivoAnterior (type INTEGER) (default -1))
 (slot pildoras (type INTEGER) (default 100))
 (slot poderes (type INTEGER) (default 0))
 (slot comida (type INTEGER) (default -1))
 (slot distComida (type NUMBER) (default 10000))
 (slot sinComer (type INTEGER) (default 0)))
(deftemplate FANTASMA
 (slot nombre (type SYMBOL)) (slot indice (type INTEGER) (default 0))
 (slot comestible (type SYMBOL)) (slot distancia (type NUMBER))
 (slot tiempo (type NUMBER)) (slot margen (type NUMBER) (default 10000))
 (slot nodo (type INTEGER) (default -1)))
(deftemplate OBJETIVO
 (slot tipo (type SYMBOL)) (slot nodo (type INTEGER))
 (slot distancia (type NUMBER)) (slot margen (type NUMBER))
 (slot cantidad (type INTEGER) (default 0))
 (slot fantasma (type SYMBOL) (default ninguno))
 (slot ventaja (type NUMBER) (default 0))
 (slot movimiento (type SYMBOL) (default NEUTRAL)))
(deftemplate SALIDA
 (slot indice (type INTEGER)) (slot movimiento (type SYMBOL))
 (slot nodo (type INTEGER)) (slot margen (type NUMBER))
 (slot visitas (type INTEGER) (default 0))
 (slot distComida (type NUMBER) (default 10000)))
(deftemplate PELIGRO)
(deftemplate ACOSO)
(deftemplate ACTION
 (slot id) (slot info (default "")) (slot priority (type INTEGER))
 (slot fantasma (type SYMBOL) (default ninguno))
 (slot nodo (type INTEGER) (default -1))
 (slot movimiento (type SYMBOL) (default NEUTRAL)))

 ; r1: siempre queda una propuesta para recoger comida
(defrule comer
 (MSPACMAN (comida ?n))
 => (assert (ACTION (id comer) (nodo ?n) (priority 10) (info "r1: comida por defecto"))))

 ; r2: encadenamos peligro antes de las reglas que consultan su ausencia
(defrule peligro
 (declare (salience 30))
 (MSPACMAN (distFantasma ?d&:(< ?d 20)))
 => (assert (PELIGRO)))

 ; r3: dos umbrales para no alternar cada turno
(defrule sigueElPeligro
 (declare (salience 30))
 (MSPACMAN (ultima huir) (distFantasma ?d&:(< ?d 30)))
 => (assert (PELIGRO)))

 ; r4: comparamos todos los perseguidores al medir el margen de cada salida
(defrule huir
 (PELIGRO)
 (SALIDA (indice ?i) (movimiento ?m) (nodo ?n) (margen ?r))
 (not (SALIDA (margen ?otro&:(> ?otro ?r))))
 (not (SALIDA (indice ?j&:(< ?j ?i)) (margen ?r)))
 => (assert (ACTION (id huir) (nodo ?n) (movimiento ?m) (priority 80)
  (info "r4: salida con mas margen"))))

 ; r5: conserva la idea del ejemplo, pero el objetivo debe superar el filtro
(defrule aLaPPill
 (PELIGRO)
 (MSPACMAN (distFantasma ?f))
 (OBJETIVO (tipo poder) (nodo ?n) (distancia ?p&:(< ?p ?f))
   (margen ?r&:(> ?r 3)) (movimiento ?m))
 => (assert (ACTION (id irAPPill) (nodo ?n) (movimiento ?m) (priority 90)
  (info "r5: poder alcanzable antes del peligro"))))

 ; r6: no basta con llegar antes del fin, dejamos tiempo para alcanzarlo en movimiento
(defrule cazar
 (not (PELIGRO))
 (FANTASMA (nombre ?g) (indice ?i) (comestible true) (nodo ?n)
   (distancia ?d) (tiempo ?t&:(> ?t (+ (* 2 ?d) 8))) (margen ?r&:(> ?r 3)))
 (not (FANTASMA (comestible true) (distancia ?d2&:(< ?d2 ?d))
   (tiempo ?t2&:(> ?t2 (+ (* 2 ?d2) 8))) (margen ?r2&:(> ?r2 3))))
 (not (FANTASMA (indice ?j&:(< ?j ?i)) (comestible true) (distancia ?d)
   (tiempo ?t3&:(> ?t3 (+ (* 2 ?d) 8))) (margen ?r3&:(> ?r3 3))))
 => (assert (ACTION (id perseguir) (fantasma ?g) (nodo ?n) (priority 60)
  (info (str-cat "r6: cazar a " ?g)))))

 ; r7: rematar puede ganar a huir, pero no ignora el margen del camino
(defrule rematar
 (FANTASMA (nombre ?g) (indice ?i) (comestible true) (nodo ?n)
   (distancia ?d) (tiempo ?t&:(> ?t (+ (* 2 ?d) 8))) (margen ?r&:(> ?r 3)))
 (not (FANTASMA (comestible true) (distancia ?d2&:(< ?d2 ?d))
   (tiempo ?t2&:(> ?t2 (+ (* 2 ?d2) 8))) (margen ?r2&:(> ?r2 3))))
 (not (FANTASMA (indice ?j&:(< ?j ?i)) (comestible true) (distancia ?d)
   (tiempo ?t3&:(> ?t3 (+ (* 2 ?d) 8))) (margen ?r3&:(> ?r3 3))))
 (test (< ?d 10))
 => (assert (ACTION (id perseguir) (fantasma ?g) (nodo ?n) (priority 85)
  (info (str-cat "r7: rematar a " ?g)))))

 ; r8: dos hechos distintos permiten detectar que no nos persigue solo uno
(defrule variosPerseguidores
 (declare (salience 40))
 (FANTASMA (indice ?i) (comestible false) (distancia ?a&:(< ?a 30)))
 (FANTASMA (indice ?j&:(> ?j ?i)) (comestible false) (distancia ?b&:(< ?b 30)))
 => (assert (ACOSO)))

 ; r9: con varios perseguidores no exigimos que el poder este mas cerca que ellos
(defrule poderConAcoso
 (ACOSO)
 (OBJETIVO (tipo poder) (nodo ?n) (distancia ?d&:(<= ?d 80))
   (margen ?r&:(> ?r 3)) (movimiento ?m))
 => (assert (ACTION (id irAPPill) (nodo ?n) (movimiento ?m) (priority 95)
  (info "r9: poder frente a varios perseguidores"))))

;;; mspacman-v1.clp  --  Grupo grupoYY
;;; Reglas: huir, irAPPill, perseguir, comer.
;;; Selección por priority del ACTION (mayor gana).

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
;;; Fantasmas v2: intercepcion y control de comida. ;;; 15 reglas. ;;; Prioridades: ;;; huir 90 > controlar comida 70 > interceptar 60 > ;;; dispersar 40 > roles 20 > perseguir 10.

(deftemplate FANTASMA (slot nombre (type SYMBOL)) (slot comestible (type SYMBOL)) (slot enCarcel (type SYMBOL)) (slot distPacman (type NUMBER)) (slot distOtro (type NUMBER)) (slot ventajaPPill (type NUMBER)))

(deftemplate MSPACMAN (slot distPPill (type NUMBER)))

(deftemplate GRUPO (slot apretado (type SYMBOL)))

(deftemplate COMIDA (slot restantes (type NUMBER)))

(deftemplate YO (slot nombre (type SYMBOL)))

(deftemplate PACMAN_CERCA)

(deftemplate ACTION (slot id) (slot info (default "")) (slot priority (type NUMBER)))

;; ================================================== ;; R1. Detectar si Pac-Man esta cerca. ;; ==================================================

(defrule pacmanCerca (declare (salience 10)) (YO (nombre ?yo)) (FANTASMA (nombre ?yo) (enCarcel false) (distPacman ?d&:(<= ?d 10))) => (assert (PACMAN_CERCA)))

;; ================================================== ;; HUIDA ;; ==================================================

;; R2. Huir si soy comestible.

(defrule huirComestible (YO (nombre ?yo)) (FANTASMA (nombre ?yo) (comestible true) (enCarcel false)) => (assert (ACTION (id huir) (info "R2: soy comestible") (priority 90))))

;; ================================================== ;; CONTROL DE COMIDA ;; ==================================================

;; R3. SUE controla la comida cuando quedan pocas pildoras.

(defrule controlarComidaSue (YO (nombre SUE)) (FANTASMA (nombre SUE) (comestible false) (enCarcel false)) (COMIDA (restantes ?n&:(and (> ?n 0) (<= ?n 10)))) => (assert (ACTION (id controlarPills) (info "R3: SUE controla las pildoras restantes") (priority 70))))

;; R4. BLINKY controla la comida cuando quedan muy pocas.

(defrule controlarComidaBlinky (YO (nombre BLINKY)) (FANTASMA (nombre BLINKY) (comestible false) (enCarcel false)) (COMIDA (restantes ?n&:(and (> ?n 0) (<= ?n 5)))) => (assert (ACTION (id controlarPills) (info "R4: BLINKY ayuda a controlar las ultimas pildoras") (priority 70))))

;; R5. INKY controla la comida si SUE no esta disponible.

(defrule controlarComidaInky (YO (nombre INKY)) (FANTASMA (nombre INKY) (comestible false) (enCarcel false)) (COMIDA (restantes ?n&:(and (> ?n 0) (<= ?n 5)))) (FANTASMA (nombre SUE) (enCarcel true)) => (assert (ACTION (id controlarPills) (info "R5: INKY sustituye a SUE en el control de comida") (priority 70))))

;; ================================================== ;; INTERCEPTACION ;; ==================================================

;; R6. Todos los fantasmas pueden intentar interceptar.

(defrule interceptar (YO (nombre ?yo)) (FANTASMA (nombre ?yo) (comestible false) (enCarcel false)) => (assert (ACTION (id emboscar) (info "R6: interceptar el recorrido de Pac-Man") (priority 60))))

;; R7. PINKY tiene prioridad estrategica para interceptar.

(defrule interceptarPinky (YO (nombre PINKY)) (FANTASMA (nombre PINKY) (comestible false) (enCarcel false)) => (assert (ACTION (id emboscar) (info "R7: PINKY se anticipa a Pac-Man") (priority 65))))

;; R8. SUE intercepta cuando Pac-Man esta lejos.

(defrule interceptarSue (YO (nombre SUE)) (FANTASMA (nombre SUE) (comestible false) (enCarcel false) (distPacman ?d&:(> ?d 10))) => (assert (ACTION (id emboscar) (info "R8: SUE intercepta desde lejos") (priority 62))))

;; R9. BLINKY persigue cuando Pac-Man esta cerca.

(defrule presionBlinky (YO (nombre BLINKY)) (FANTASMA (nombre BLINKY) (comestible false) (enCarcel false) (distPacman ?d&:(<= ?d 10))) => (assert (ACTION (id perseguir) (info "R9: BLINKY presiona a Pac-Man") (priority 25))))

;; ================================================== ;; CONTROL DE RUTAS ;; ==================================================

;; R10. INKY protege una pildora de poder ventajosa.

(defrule guardarPPillInky (YO (nombre INKY)) (FANTASMA (nombre INKY) (comestible false) (enCarcel false) (ventajaPPill ?v&:(>= ?v 0))) => (assert (ACTION (id guardarPPill) (info "R10: INKY controla una PPill") (priority 20))))

;; R11. BLINKY evita agruparse con los demas.

(defrule separarBlinky (GRUPO (apretado true)) (YO (nombre BLINKY)) (FANTASMA (nombre BLINKY) (comestible false) (enCarcel false)) (not (PACMAN_CERCA)) => (assert (ACTION (id dispersar) (info "R11: BLINKY cubre otra ruta") (priority 40))))

;; R12. PINKY evita agruparse con los demas.

(defrule separarPinky (GRUPO (apretado true)) (YO (nombre PINKY)) (FANTASMA (nombre PINKY) (comestible false) (enCarcel false)) (not (PACMAN_CERCA)) => (assert (ACTION (id dispersar) (info "R12: PINKY cubre otra ruta") (priority 40))))

;; R13. INKY evita agruparse con los demas.

(defrule separarInky (GRUPO (apretado true)) (YO (nombre INKY)) (FANTASMA (nombre INKY) (comestible false) (enCarcel false)) (not (PACMAN_CERCA)) => (assert (ACTION (id dispersar) (info "R13: INKY cubre otra ruta") (priority 40))))

;; ================================================== ;; COMPORTAMIENTO DE RESERVA ;; ==================================================

;; R14. SUE se dispersa si el grupo esta apretado.

(defrule separarSue (GRUPO (apretado true)) (YO (nombre SUE)) (FANTASMA (nombre SUE) (comestible false) (enCarcel false)) (not (PACMAN_CERCA)) => (assert (ACTION (id dispersar) (info "R14: SUE cubre otra ruta") (priority 40))))

;; R15. Perseguir como comportamiento de reserva.

(defrule perseguir (YO (nombre ?yo)) (FANTASMA (nombre ?yo) (comestible false) (enCarcel false)) => (assert (ACTION (id perseguir) (info "R15: perseguir") (priority 10))))
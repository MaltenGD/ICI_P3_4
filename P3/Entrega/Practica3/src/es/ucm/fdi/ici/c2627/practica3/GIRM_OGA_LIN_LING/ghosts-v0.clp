;;; Fantasmas v0: port fiel de GhostsV0Me (practica 2) a reglas. 6 reglas.
;;; Una sola base para los cuatro; las diferencias se escriben preguntando por YO.
;;; Prioridades: huir 90 > dispersar 40 > roles 20 > perseguir 10.

;; ---- Los hechos que aserta GhostsInput, iguales para los cuatro ---------
(deftemplate FANTASMA
   (slot nombre       (type SYMBOL))
   (slot comestible   (type SYMBOL))   ; true o false, en minusculas, como Java
   (slot enCarcel     (type SYMBOL))
   (slot distPacman   (type NUMBER))   ; lo que tarda en llegar a ella; 1000 en la carcel
   (slot distOtro     (type NUMBER))   ; al otro fantasma activo mas cercano; 1000 si no hay
   (slot ventajaPPill (type NUMBER)))  ; max(distPacman-distMia) sobre las ppills; -1000 si no hay

(deftemplate MSPACMAN
   (slot distPPill (type NUMBER)))     ; de Ms. Pac-Man a su PPill mas cercana; 1000 si no quedan

(deftemplate GRUPO
   (slot apretado (type SYMBOL)))      ; true si algun par de fantasmas activos esta a menos de 5

;; ---- Distinto en cada motor: quien soy yo --------------------------------
(deftemplate YO (slot nombre (type SYMBOL)))

;; ---- Hecho intermedio: lo asertan unas reglas y lo leen otras ------------
(deftemplate PACMAN_CERCA)

(deftemplate ACTION
   (slot id) (slot info (default "")) (slot priority (type NUMBER)))

;; =================== INTERMEDIOS ===================
;; R1 . Ms. Pac-Man a 10 o menos de mi: no es momento de dispersarse
(defrule pacmanCerca
   (declare (salience 10))
   (YO (nombre ?yo))
   (FANTASMA (nombre ?yo) (enCarcel false) (distPacman ?d&:(<= ?d 10)))
   =>
   (assert (PACMAN_CERCA)))

;; =================== HUIDA ===================
;; R2 . si soy comestible, huir
(defrule huirComestible
   (YO (nombre ?yo))
   (FANTASMA (nombre ?yo) (comestible true) (enCarcel false))
   =>
   (assert (ACTION (id huir) (info "R2: soy comestible") (priority 90))))

;; =================== PERSEGUIR (por defecto y por rol) ===================
;; R3 . si no soy comestible, perseguir (BLINKY y SUE, y cualquiera sin rol)
(defrule perseguir
   (YO (nombre ?yo))
   (FANTASMA (nombre ?yo) (comestible false) (enCarcel false))
   =>
   (assert (ACTION (id perseguir) (info "R3: no soy comestible") (priority 10))))

;; R4 . PINKY embosca
(defrule emboscarPinky
   (YO (nombre PINKY))
   (FANTASMA (nombre PINKY) (comestible false) (enCarcel false))
   =>
   (assert (ACTION (id emboscar) (info "R4: PINKY embosca") (priority 20))))

;; R5 . INKY guarda una ppill a la que llega antes que Ms. Pac-Man
(defrule guardarPPillInky
   (YO (nombre INKY))
   (FANTASMA (nombre INKY) (comestible false) (enCarcel false)
             (ventajaPPill ?v&:(>= ?v 0)))
   =>
   (assert (ACTION (id guardarPPill) (info "R5: INKY llega antes a la ppill") (priority 20))))

;; =================== DISPERSION ===================
;; R6 . el grupo esta apretado y Ms. Pac-Man no esta cerca: separarse
(defrule dispersar
   (GRUPO (apretado true))
   (YO (nombre ?yo))
   (FANTASMA (nombre ?yo) (comestible false) (enCarcel false))
   (not (PACMAN_CERCA))
   =>
   (assert (ACTION (id dispersar) (info "R6: grupo apretado y Ms. Pac-Man lejos") (priority 40))))

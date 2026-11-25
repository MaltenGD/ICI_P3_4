# Notas · Práctica 5 (Juegos por turnos) · Grupo GIRM_OGA_LIN_LING

**Integrantes:** Guillermo Isaac Ramos Medina, Óliver García Aguado, Lin Ling, Liling Chen  
**Grupo:** GIRM_OGA_LIN_LING  

---

## 1. Guía paso a paso

- **Alfa-beta (A1, A2, A3):** Comprobados con `comprobar.py a`. Todos en **BIEN**. Alfa-beta reduce de 8.902 a 519 las posiciones evaluadas a profundidad 3 desde la posición inicial. Minimax tarda 0,23 s en un medio juego típico.
- **MCTS (M1, M2, M3, M4, M5, M6):** Comprobados con `comprobar.py m`. Todos en **BIEN**. MCTS realiza ~1.123 iteraciones por segundo desde la posición inicial y resuelve mates tácticos en las pruebas de verificación.

---

## 2. Experimentos

- **Experimento MCTS (`mcts` vs `mcts-centro`):** Probamos añadir a la recompensa un término heurístico de ocupación del centro (+0.1 por pieza propia en d4, e4, d5, e5). Pasa del 62% al 68% total contra Aleatorio, Voraz y AlfaBeta2, arañando un 9% de victorias contra AlfaBeta2.
- **Experimento Alfa-Beta (`ab2` con profundidad 2):** Minimax con poda alfa-beta a profundidad 2 obtiene un 83% de puntos global, dominando a Voraz (94%) y puntuando un 59% contra AlfaBeta2.

---

## 3. Retos

### Resultados del banco de pruebas oficial (`comparar.py` - 256 partidas, 16 por apertura/color):

```
                            Aleatorio                 Voraz             AlfaBeta2             MCTS guia         TOTAL
mcts                 100% ±0 (16-0-0)      88% ±14 (13-2-1)        0% ±0 (0-0-16)       50% ±26 (8-0-8)       59% ±12
mcts-rival           100% ±0 (16-0-0)      81% ±18 (12-2-2)        0% ±0 (0-0-16)      62% ±25 (10-0-6)       61% ±12
mcts-capturas        100% ±0 (16-0-0)       59% ±21 (7-5-4)        0% ±0 (0-0-16)      28% ±20 (3-3-10)       47% ±12
mcts-comb            100% ±0 (16-0-0)       66% ±22 (9-3-4)        3% ±6 (0-1-15)      38% ±25 (6-0-10)       52% ±12
```

### Reto 1: El rival también elige (+2,5 pts · Violeta · MCTS)
- **Qué se probó:** Clase `MCTSRival(MCTS)` redefiniendo `seleccionar`: en los nodos donde mueve el rival (`nodo.color == self.yo`), UCB1 maximiza `1 - media` en lugar de la media. De este modo, MCTS modela que el rival busca minimizar nuestra recompensa y no confía en errores rivales forzados.
- **Resultado en `comparar.py`:** Frente a `MCTS guia` supera con claridad el 50% alcanzando un **62% ±25 (10 victorias, 0 tablas, 6 derrotas)**, tal como anticipaba la guía, y logra la puntuación global más alta de los MCTS (61% ±12).

### Reto 3: Simulaciones que capturan (+2,5 pts · Violeta · MCTS)
- **Qué se probó:** Clase `MCTSCapturas(MCTS)` redefiniendo `simular`: durante las jugadas aleatorias del rollout, si hay capturas legales (`tablero.is_capture(j)`), se elige al azar únicamente entre ellas para evitar ignorar piezas colgadas.
- **Resultado en `comparar.py`:** Baja al 47% global (28% contra `MCTS guia` y 59% contra Voraz) debido a que forzar cualquier captura indiscriminada incita a entregar piezas pesadas en capturas venenosas (por ejemplo comer un peón defendido con la dama). Sin embargo, combinada en `mcts-comb`, fue la única variante de MCTS capaz de arañar tablas a `AlfaBeta2` (3% ±6).

### Reto 4: Un híbrido
- **Qué se probó:** Clase `MCTSAlfaBeta(MCTS)`, redefiniendo `simular` para sustituir las simulaciones aleatorias por una evaluación mediante poda Alfa-Beta. Después de cada expansión, se evalúa la posición con Alfa-Beta a profundidad 1 o 2, y, se transforma el valor material obtenido mediante M1: 1 / (1 + exp(-valor / 3)). De esta forma, MCTS combina su exploración del árbol con una evaluación táctica más precisa.
- **Resultado en comparar.py:** Se realizaron 384 partidas, enfrentando las distintas variantes contra los cuatro rivales, con ocho aperturas y ambos colores.



384 partidas, 12 procesos, 0.3 s por jugada: hasta 10 minutos.
384/384 partidas  (9.3 min)
Puntos (victoria 1, tablas ½) y, entre paréntesis, ganadas-tablas-perdidas.
±: dos errores típicos. Si los intervalos de dos versiones se solapan mucho, no hay diferencia clara.

                            Aleatorio                 Voraz             AlfaBeta2             MCTS guia         TOTAL
mcts                 100% ±0 (16-0-0)      78% ±18 (11-3-2)        0% ±0 (0-0-16)       41% ±25 (6-1-9)       55% ±12
mcts-rival           100% ±0 (16-0-0)      88% ±17 (14-0-2)       6% ±12 (1-0-15)       50% ±26 (8-0-8)       61% ±12
mcts-capturas        94% ±12 (15-0-1)       62% ±23 (9-2-5)        0% ±0 (0-0-16)      16% ±18 (2-1-13)       43% ±12
mcts-comb            100% ±0 (16-0-0)      78% ±18 (11-3-2)        6% ±9 (0-2-14)      34% ±24 (5-1-10)       55% ±12
mcts-ab1             100% ±0 (16-0-0)      69% ±22 (10-2-4)        3% ±6 (0-1-15)      28% ±20 (3-3-10)       50% ±12
mcts-ab2             100% ±0 (16-0-0)      25% ±20 (3-2-11)       6% ±12 (1-0-15)      25% ±20 (3-2-11)       39% ±12

---

## 4. Jugador final

- **Versión elegida:** `jugador()` en `grupoGIRM_OGA_LIN_LING/__init__.py` instancia `MCTSRival(nombre="grupoGIRM_OGA_LIN_LING")`.
- **Justificación:** Es la variante MCTS más equilibrada y sólida tácticamente, demostrando una ventaja medible (62% de victorias) frente a la versión base de la guía en enfrentamiento directo.

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

```
384 partidas, 12 procesos, 0.3 s por jugada: hasta 10 minutos.
384/384 partidas  (9.3 min)
                            Aleatorio                 Voraz             AlfaBeta2             MCTS guia         TOTAL
mcts                 100% ±0 (16-0-0)      78% ±18 (11-3-2)        0% ±0 (0-0-16)       41% ±25 (6-1-9)       55% ±12
mcts-rival           100% ±0 (16-0-0)      88% ±17 (14-0-2)       6% ±12 (1-0-15)       50% ±26 (8-0-8)       61% ±12
mcts-capturas        94% ±12 (15-0-1)       62% ±23 (9-2-5)        0% ±0 (0-0-16)      16% ±18 (2-1-13)       43% ±12
mcts-comb            100% ±0 (16-0-0)      78% ±18 (11-3-2)        6% ±9 (0-2-14)      34% ±24 (5-1-10)       55% ±12
mcts-ab1             100% ±0 (16-0-0)      69% ±22 (10-2-4)        3% ±6 (0-1-15)      28% ±20 (3-3-10)       50% ±12
mcts-ab2             100% ±0 (16-0-0)      25% ±20 (3-2-11)       6% ±12 (1-0-15)      25% ±20 (3-2-11)       39% ±12
```


### Reto 2: Gestionar el tiempo (+1,75 pts · Azul · MCTS)
- **Qué se probó:** 
    * Clase `MCTS` cambios en `elegir`: si solo hay una jugada legal se devuelve sin crear el árbol. Dentro del bucle, cada cierto número de iteraciones se estima cuántas caben en el tiempo que queda (iteraciones/s × tiempo restante) y se para si `visitas de la segunda + caben < 0,9 × visitas de la primera`, porque la jugada ya está decidida. Se añadió la opción `parada=True/False` para comparar con y sin la regla.
    * Clase `Minimax` cambios en `elegir`: también devuelve directamente la jugada única. La regla de subir profundidad con 10 o menos piezas se probó primero tal cual (+1 con ≤16 piezas, +2 con ≤10), pero algunas jugadas tardaban de 0,5 a 2,6 s con 0,3 s de reloj, superando 1,5 veces el tiempo (pérdida de partida). Se sustituyó por **profundización iterativa con reloj**: se busca a profundidad 1, 2, 3… hasta gastar el 60 % del tiempo y se juega con la última profundidad completada. Así los finales, con pocas jugadas, llegan más hondo de forma natural sin pasarse.

- **Resultado en `comparar.py`:**  Se corrieron las versiones: MTCS sin y con las reglas junto con minimax.

```
192 partidas, 12 procesos, 0.3 s por jugada: hasta 5 minutos.

160/192 partidasOJO: grupoGIRM_OGA_LIN_LING-ab ha tardado 0.52 s y tenía 0.3 s
161/192 partidasOJO: grupoGIRM_OGA_LIN_LING-ab ha tardado 0.65 s y tenía 0.3 s
162/192 partidasOJO: grupoGIRM_OGA_LIN_LING-ab ha tardado 0.69 s y tenía 0.3 s
OJO: grupoGIRM_OGA_LIN_LING-ab ha tardado 0.51 s y tenía 0.3 s
OJO: grupoGIRM_OGA_LIN_LING-ab ha tardado 0.60 s y tenía 0.3 s
OJO: grupoGIRM_OGA_LIN_LING-ab ha tardado 0.52 s y tenía 0.3 s
192/192 partidas  (4.3 min)

Puntos (victoria 1, tablas ½) y, entre paréntesis, ganadas-tablas-perdidas.
±: dos errores típicos. Si los intervalos de dos versiones se solapan mucho, no hay diferencia clara.

                            Aleatorio                 Voraz             AlfaBeta2             MCTS guia         TOTAL
mcts                100% ±0 (16-0-0)       41% ±21 (4-5-7)        0% ±0 (0-0-16)       56% ±26 (9-0-7)       49% ±12
mcts-sinparada      100% ±0 (16-0-0)       47% ±21 (5-5-6)        0% ±0 (0-0-16)       56% ±22 (7-4-5)       51% ±12
minimax             100% ±0 (16-0-0)      100% ±0 (16-0-0)       47% ±23 (6-3-7)       97% ±6 (15-1-0)        86% ±8
```

**Conclusión:**
* `mcts` (con parada) 49 % ±12 frente a `mcts-sinparada` 51 % ±12: los intervalos se solapan, así que **la parada no cambia los puntos**. Con 0,3 s por jugada casi nunca se dispara; se notaría más con 1 s o más.
* `minimax` con iterativa llega al 86 % ±8 (100 % contra Voraz, 97 % contra `MCTS guia`, 47 % contra `AlfaBeta2`).
* La pasada con el +1/+2 fijo produjo decenas de avisos `OJO` (hasta 2,65 s). Con la iterativa solo quedan 6 en 192 partidas (0,51-0,69 s), porque el reloj se comprueba entre jugadas raíz y una llamada puede empezar justo antes de agotarse.
* Duración: 4,3 min con 3 versiones (192 partidas).

**Resultado en `comparar.py`:** 64 partidas por versión, dos pasadas separadas por cada tiempo:

| Tiempo/jugada | Versión | Minutos | Voraz | MCTS guía | TOTAL |
|---|---|---|---|---|---|
| 0,3 s | `mcts` (con parada) | 1,8 | 12 % ±14 | 53 % ±25 | 41 % ±12 |
| 0,3 s | `mcts-sinparada` | 1,9 | 53 % ±23 | 69 % ±22 | 55 % ±12 |
| 1,0 s | `mcts` (con parada) | 5,5 | 84 % ±18 | 50 % ±26 | 59 % ±12 |
| 1,0 s | `mcts-sinparada` | 6,3 | 84 % ±18 | 50 % ±26 | 59 % ±12 |

* **Tiempo:** a 0,3 s la parada apenas ahorra (1,8 frente a 1,9 min) porque casi nunca se dispara. A 1 s ahorra unos 0,8 min (5,5 frente a 6,3), alrededor de un 13 %.
* **Puntos:** a 1 s son idénticos (59 % ±12 en ambos). A 0,3 s la versión con parada sacó menos (41 % frente a 55 %), pero en una pasada anterior de 192 partidas la diferencia era nula (49 % frente a 51 %), así que lo atribuimos al ruido de usar solo 64 partidas.

* **Conclusión:** la parada ahorra tiempo sin perder fuerza, y el ahorro crece con el tiempo por jugada, porque hay más iteraciones y la estimación de las que caben es más fiable. Contra `AlfaBeta2` el MCTS sigue en 0 % con y sin parada, así que eso no depende de la gestión del tiempo.

### Reto 5: Profundización iterativa (+2,5 pts · Morado · Alfa-beta)

- **Qué se probó:** 
    * Clase `MinimaxIterativo` cambios en `elegir`: Se reescribió el método `elegir` para buscar a profundidad 1, 2, 3... hasta agotar el tiempo. Se introdujeron tres mejoras clave:

        * **Chequeo de tiempo dentro del bucle**: Comprobar el reloj al evaluar cada jugada (no al final de la profundidad) para abortar limpiamente si se agota el tiempo.

        * **Ordenación total por valor**: Ordenar todas las jugadas de la iteración anterior de mejor a peor. Esto multiplica la eficiencia de la poda Alfa-Beta (salto del 45% al 84% vs Minimax2).

        * **Margen del 92% + Factor 6**: El 95% provocaba abortos por latencia (bajón al 23%). El 92% con factor 6 maximiza el tiempo útil sin generar avisos OJO.

**Resultado en `comparar.py`:** Se corrieron las versiones con diferentes configuraciones de tiempo y márgenes. La siguiente tabla muestra la evolución del rendimiento a lo largo de las pruebas (32 partidas, 12 procesos, 1.0 s por jugada):

```
Versión / Cambio	        Margen	Factor	Puntos vs Minimax2	Puntos vs Minimax3	TOTAL
Iterativo (1er intento)	    80%	    10	    66% ±20	            38% ±21	            52% ±15
Iterativo (margen 85%)	    85%	    10	    72% ±20	            19% ±18	            45% ±16
Iterativo (margen 95%)	    95% 	10	    41% ±23	            6% ±9	            23% ±13
Iterativo (orden total) 	90%	    10	    84% ±18	            16% ±15	            50% ±17
Iterativo (final)	        92%	    6	    91% ±10	            31% ±22	            61% ±16
```

**Conclusión:** Pasamos del 23% al 61% global (91% vs Minimax2). La ordenación total es la clave. Contra Minimax3 (31%) seguimos en desventaja porque el rival se salta el límite de tiempo (1.83s vs 1.0s) y busca más profundo. Para ganarle, el siguiente paso obligatorio es mejorar la función evaluar con valor posicional.

---

## 4. Jugador final

- **Versión elegida:** `jugador()` en `grupoGIRM_OGA_LIN_LING/__init__.py` instancia `MCTSRival(nombre="grupoGIRM_OGA_LIN_LING")`.
- **Justificación:** Es la variante MCTS más equilibrada y sólida tácticamente, demostrando una ventaja medible (62% de victorias) frente a la versión base de la guía en enfrentamiento directo.

# Diario - Práctica 4: Ms. Pac-Man (Razonamiento Borroso)

**Integrantes:** Guillermo Isaac Ramos Medina, Óliver García Aguado, Lin Ling, Liling Chen  
**Grupo:** GIRM_OGA_LIN_LING  
**Controlador:** Ms. Pac-Man (Lógica Borrosa)  
**Rivales de referencia:** 5 fantasmas (Random, Aggressive, Basico, Intermedio, Avanzado)  
**Partidas por rival:** 300  
**Métricas fijadas:** Puntos, turnos por vida, fantasmas comidos  

---

## Sesión 1 (Jueves 12 de noviembre) porteo del pacman de la P3


Al revisar el proyecto de partida vimos que el `mspacman-v0.fcl` es una plantilla muy simplóna (solo 6 reglas de huir o comer sin píldoras de poder ni matices).
Decidimos que nuestra `v0`  iba a ser un porteo fiel de uno de los primeros pacman de la Práctica 3 y no una copia directa de la plantilla, este controlador agrega a la plantilla lo siguiente:
1. **Peligro escalonado:** En la P3 el pacman diferencia entre peligro cercano general ($d < 25$) y peligro crítico o desesperado ($d \le 6$) donde huir era la mayor priridad.
2. **Píldoras de poder con ventaja:** Comprobábamos si Pac-Man llegaba a la píldora antes que el fantasma. Si el fantasma estaba demasiado encima, no se arriesgaba y huía.
3. **Criterio de caza estricto:** Solo perseguir a un fantasma comestible si daba tiempo a alcanzarlo antes de que volviese a ser peligroso ($tiempo > 2 \cdot dist + 2$).
4. **Rematar:** Si un comestible estaba muy cerca, valía la pena rematarlo aunque hubiese peligro a distancia media.

Para trasladar esto a la arquitectura de `jFuzzyLogic`:
- En `MsPacManInput.java` calculamos en Java las distancias en pasos de camino acotadas a `LEJOS = 150` (evitando devolver `-1` para no tener problemas con los conjuntos borrosos): `peligro`, `comestible` (solo si da tiempo a cazarlo; si no, vale `LEJOS`), `ppill` y una nueva variable `ventajaPPill = peligro - ppill`.
- Las acciones (`ComerAction`, `HuirAction`, `PerseguirAction`, `IrAPPillAction`) ahora implementan `Action` directa, eliminando `RulesAction` y `parseFact` de Jess.
- En `mspacman-v0.fcl` definimos las 4 entradas, las 4 salidas (`comer`, `huir`, `perseguir`, `irAPPill`) con defuzzificación por Centro de Gravedad (`COG`) y particiones borrosas con términos trapezoidales y triangulares (`encima`, `cerca`, `media`, `lejos`).

Tuvimos que lidiar con un par de detalles de sintaxis de FCL con jFuzzyLogic: en una regla (`THEN`), jFuzzyLogic solo admite asignar a lo sumo dos salidas separadas por coma en una misma línea; si se ponen tres da error de parseo (`mismatched input ',' expecting SEMICOLON`). Lo solucionamos separandolo en una regla aparte.

---

## Sesión 2 (Viernes 13 de noviembre) · Caza, verificación de casos y resultados de la v0

Terminamos de ajustar las 9 reglas borrosas de la `v0` que completan el porteo de la P3:
- **R1 y R2 (Huida desesperada):** Si `peligro IS encima`, `huir IS mucho`, `comer IS poco` e `irAPPill IS poco`.
- **R3 (Huida estándar):** Si `peligro IS cerca`, `huir IS mucho`, `comer IS poco`.
- **R4 y R5 (Comida y retirada de huida):** Con peligro a distancia `media`, `comer IS algo`; con peligro `lejos`, `comer IS mucho` y `huir IS poco`.
- **R6 (Poder con ventaja):** Si `peligro IS cerca AND ppill IS cerca AND ventajaPPill IS ventaja`, `irAPPill IS mucho, huir IS poco`.
- **R7 (Caza estándar):** Si `comestible IS cerca AND peligro IS NOT cerca AND peligro IS NOT encima`, `perseguir IS mucho, comer IS poco`. La supresión de `comer` es obligatoria para romper el empate contra la regla de comer en ausencia de peligro.
- **R8 (Rematar antes de huir):** Si `comestible IS cerca AND peligro IS media`, `perseguir IS mucho, comer IS poco`.
- **R9 (Comer por defecto):** Si no hay peligro encima ni cerca y no hay comestibles que cazar, `comer IS mucho`.

### Verificación unitaria con `ProbarBorroso`

Comprobamos el comportamiento del motor borroso en frío con `ProbarBorroso`, diseñando 7 situaciones que replican las condiciones de la P3:
1. **Nada cerca:** entradas (150, 150, 150, -150) -> gana `comer` (8.7 vs 1.3). [BIEN]
2. **Peligro encima a 5 pasos (desesperado):** entradas (5, 150, 150, -150) -> gana `huir` (8.7 vs 1.3). [BIEN]
3. **Peligroso a 20 pasos sin píldora:** entradas (20, 150, 150, -150) -> gana `huir` (8.7 vs 1.3). [BIEN]
4. **Peligroso a 45 pasos (media):** entradas (45, 150, 150, -150) -> gana `comer` (6.5 vs 0.0). [BIEN]
5. **Comestible alcanzable a 10 pasos con peligro lejos:** entradas (150, 10, 150, -150) -> gana `perseguir` (8.7 vs 5.0). [BIEN]
6. **Peligro a 25 pasos y PPill a 8 pasos (ventaja +17):** entradas (25, 150, 8, +17) -> gana `irAPPill` (8.6 vs 5.0). [BIEN]
7. **Peligro encima a 5 pasos y PPill a 8 pasos (desesperado sin ventaja):** entradas (5, 150, 8, -3) -> gana `huir` (8.7 vs 1.3). [BIEN]

La curva de control de peligro muestra transiciones suaves:
- De 0 a 30 pasos gana `huir` con holgura.
- A partir de 35 pasos `comer` toma la delantera suavemente (6.9 a 35 pasos, subiendo hasta 8.7 a los 75 pasos).
- No hay escalones muertos ni empates no deseados.

Probamos también `VerPartida` con `TrazaBorrosa` conectada frente a los fantasmas agresivos para comprobar que las reglas se activan y que las 4 acciones se ejecutan cuando toca.

### Resultados de la línea base (MsPacMan v0 - Porteo P3)

Ejecutamos 300 partidas por rival frente a los cinco fantasmas de referencia (1500 partidas)

#### Puntos obtenidos
| rival | v0 ± error típico |
| :--- | ---: |
| Random | 4016.87 ± 113.39 |
| Aggressive | 3104.83 ± 112.44 |
| Basico | 2220.17 ± 68.43 |
| Intermedio | 2119.33 ± 70.92 |
| Avanzado | 1716.47 ± 48.06 |
| **MEDIA (5)** | **2635.53 ± 38.74** |

#### Turnos por vida
| rival | v0 ± error típico |
| :--- | ---: |
| Random | 668.08 ± 26.54 |
| Aggressive | 796.03 ± 72.28 |
| Basico | 286.83 ± 7.36 |
| Intermedio | 230.03 ± 7.27 |
| Avanzado | 392.41 ± 39.26 |
| **MEDIA (5)** | **474.68 ± 17.41** |

#### Fantasmas comidos
| rival | v0 ± error típico |
| :--- | ---: |
| Random | 4.38 ± 0.15 |
| Aggressive | 3.50 ± 0.15 |
| Basico | 2.33 ± 0.11 |
| Intermedio | 2.18 ± 0.11 |
| Avanzado | 1.52 ± 0.09 |
| **MEDIA (5)** | **2.78 ± 0.06** |

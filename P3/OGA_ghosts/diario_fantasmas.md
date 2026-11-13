# Diario de desarrollo - Práctica 2: Fantasmas (FSM)

**Integrante:** Oliver Garcia Aguado  
**Grupo:** `grupoPr2CY_OGA` · `Pr2CY_OGA`  
**Controlador:** Fantasmas (FSM jerárquica)  

---

# Diario · Práctica 2
**Integrantes:** Óliver García Aguado y Chenlinjia Yi
**Grupo:** grupoPr2CY_OGA · Pr2CY_OGA
**Controladores:** Ghosts (FSM) y Ms. Pac-Man

---

## Versión A — Diseño (entrega 1, 11 oct)

### Jueves 8 oct · Acciones

La idea general de la FSM se basará en el entorno: los fantasmas ya no tendrán una personalidad fija predefinida, ahora cualquier fantasma se rige por la situación del juego.

**Fantasmas — acciones definidas:**
EsperarEnNido, PatrullarZonaCentral, VagarAleatoriamente, PerseguirPacman, InterceptarCruce, CerrarPinzaCoordinada, HuirDePacman, VigilarPildoraDePoder, EsperarEmboscada, RetornarACentro, ExplorarPasilloLejano.

IA: no.

---

### Jueves 8 oct · Planteamiento de la FSM

Desarrollo la primera arquitectura de la FSM basada en 3 submáquinas de estados compuestas: ModoOfensivo, ModoDefensivo y ModoAleatorio.

*Creo que es sencillo dividir todo en 3 submáquinas "modulares", de forma que pueda ir iterando sobre cada una de ellas de forma independiente sin que afecten drásticamente a la máquina general.*

IA: no.

---

### Viernes 9 oct · Transiciones (Cálculos con el Input e Histéresis)

Condiciones para que la máquina tome decisiones a partir del Input:

- **PacmanCerca(d):** Ms. Pac-Man a menos de *d* nodos por camino más corto.
- **PildoraPoderActiva:** isEdible == true en el fantasma.
- **PildoraPoderDesactivada(d):** isEdible == false Y Ms. Pac-Man a más de *d* nodos.
- **CambiaRumboCruce(d):** Ms. Pac-Man orientada a una intersección a menos de *d* nodos.
- **AliadoFlanqueando(d):** Compañero fantasma posicionado en un pasillo paralelo a menos de *d* nodos.
- **NidoPenalizacion(t):** Tiempo transcurrido dentro del nido central mayor a *t* segundos.
- **LlegadaNido(d):** Distancia al nodo central de la cárcel menor a *d* nodos.
- **RastroPerdido(t, d):** Ms. Pac-Man fuera de cobertura a más de *d* nodos durante más de *t* segundos.

*Implementar las condiciones numéricas para estas transiciones en código va a ser difícil >.<*

---

### Viernes 9 oct · Diseño interno de submáquinas

Implemento los estados simples y las transiciones para el ModoOfensivo y el ModoDefensivo.

IA: no.

---

### Sábado 10 oct · Refactorización y simplificación jerárquica

Descarto la idea de implementar la submáquina ModoAleatorio ya que solo contenía 2 estados simples (VagarAleatoriamente y PatrullarZonaCentral); es mucho más simple y limpio mantener esos estados fuera.

---

### Lunes 12 oct · Entrega del diseño (con 1 día de retraso)

Termino la FSM y paso el borrador a limpio en Excalidraw.

*Creo que me ha quedado una máquina bastante grande como punto de partida y que quizás debería haber partido de algo un poco más simple, pero la estructura modular con 2 submáquinas la hace bastante sólida para pasar a código la semana que viene.*

IA: Sí, para pasar una fotografía de la máquina dibujada en papel a Excalidraw, pedí a la IA que generara código en formato Mermaid para importarlo.

Prompt: *"Convierte esta máquina de estados a una máquina de estados en formato mermaid, corrige únicamente las posibles faltas ortográficas."*

---

## Martes 13 oct · v0 (Pasar el diseño a código)

Una vez entregado el diseño el lunes, empezamos a programar la máquina de los fantasmas.

Al ponernos con el código nos encontramos con un problema en Java con la clase `Input`: el constructor de `Input` llama a `parseInput()` antes de que la clase hija termine de asignar a qué fantasma nos referimos. Para no hacer cosas raras con variables estáticas (que además están prohibidas), añadimos un método `parseFor(GHOST g)` en `GhostsInput` que calcula las distancias y los objetivos limpios para cada fantasma en cada tick.

Montamos la máquina tal y como la dibujamos:
- 10 estados simples con su propia acción cada uno (perseguir, interceptar, flanquear, acechar, patrullar, huir de pacman, huir a rincón, guardar respawn, reagrupar y esperar).
- 2 submáquinas compuestas (`ACTIVO` para el ataque y `COMESTIBLE` para cuando nos comen la píldora).
- Para que los fantasmas no vayan en fila india, pusimos transiciones con histéresis: si dos aliados están a menos de 8 nodos, uno pasa a flanquear hasta que se separan a más de 15.

IA: Le pedimos a la IA que nos generara el código a partir de nuestro diseño y que nos resuelva el problema de inicialización de `GhostsInput`.

Prompt:
Tengo diseñado el comportamiento de los 4 fantasmas para Ms. Pac-Man (FSM, SimpleState, CompoundState, Transition).
El diseño se encuentra en la carpeta Practica2Diseño y en el diario.md de la carpeta Practica2.

Escribe el código de GhostsInput (que herede de Input) y la clase GhostsV0 (subclase de GhostController) con las siguientes especificaciones exactas:
1. En GhostsInput no llames a los cálculos en parseInput() porque el constructor de Input en el motor llama a parseInput() antes de que pueda pasarle el GHOST al objeto. Crea un método parseFor(GHOST g) que se llame desde getMove() para cada fantasma antes de evaluar la máquina. Debe calcular: distToPacman, edibleTime, pacmanTargetJunction (el cruce al que apunta la dirección de Pac-Man caminando hasta 15 nodos), distToTargetJunction y minAllyDist (la distancia más corta al compañero más cercano).
2. GhostsV0 debe instanciar una FSM por cada fantasma en.
3. La FSM tiene 2 CompoundStates:
   - "ACTIVO": estado inicial "PERSEGUIR". Si distToTargetJunction < 20 transiciona a "INTERCEPTAR" (salida > 30). Si minAllyDist < 8 transiciona a "FLANQUEAR" (salida > 15). Si distToPacman < 35 transiciona a "ACECHAR" (salidas < 15 o > 45). Si distToPacman > 65 transiciona a "PATRULLAR" (salida < 45).
   - "COMESTIBLE": estado inicial "HUIR_PACMAN". Si distToPacman < 15 transiciona a "HUIR_RINCON" (salida > 25). Si edibleTime < 25 transiciona a "GUARDAR".
4. La máquina principal salta de ACTIVO a COMESTIBLE si edibleTime > 0, y vuelve a ACTIVO si edibleTime == 0.

Devuelve el código sin inventar estados nuevos y con el flag DEBUG en false.
FIN DEL PROMPT

Elegimos como rivales fijos los tres que están en el campus: `MsPacManBasico`, `MsPacManIntermedio` y `MsPacManAvanzado`. Simulamos 300 partidas contra cada uno:

### Resultados v0 (300 partidas por rival)

| Rival | Media | Error típico | Desv. típica | Mín | Máx | Turnos medio |
|-------|------:|-------------:|-------------:|----:|----:|-------------:|
| Básico | 4188.83 | 105.39 | 1825.42 | 760 | 11880 | 851.6 |
| Intermedio | 7585.33 | 103.53 | 1793.15 | 2230 | 12360 | 852.6 |
| Avanzado | 8466.83 | 104.29 | 1806.36 | 2780 | 13560 | 893.9 |

**Qué vemos jugando partidas:**
Contra el básico los números no están mal, pero contra el intermedio y el avanzado el pacman hizo muchos puntos. Hemos visto dos problemas claros:
1. Cuando Pac-Man come una píldora de poder, en `HUIR_PACMAN` los fantasmas eligen el movimiento que más los aleja paso a paso de forma voraz. AL no poderse hacer giros de 180 grados: si un fantasma se mete por un pasillo largo y Pac-Man se aproxima hacia el cruce que hay al final de ese pasillp queda atrapado en esa trayectoria sin alternativas de escape.
2. Al perseguir, se dispersan muy rápido a acechar o patrullar en cuanto Pac-Man se aleja un poco (los umbrales de 35 y 65 nodos eran demasiado generosos 0_0).

---

## Miércoles 14 oct · v1 (Primera mejora: huir a rincones y acoso continuo)

**Problema que atacamos:**
Evitar que los fantasmas queden atrapados en pasillos directos hacia Pac-Man cuando son comestibles, e insistir más en la persecución.

**Cambio pensado:**
1. En la submáquina `COMESTIBLE`, que no intenten huir mirando solo el siguiente paso: que fijen rumbo directo al cruce más alejado de Pac-Man (`HUIR_RINCON`) para dispersarse hacia zonas lejanas del laberinto.
2. En la submáquina `ACTIVO`, subimos el salto a patrullar de 65 a 90 nodos para que sigan persiguiendo aunque Pac-Man esté lejos.

IA: Sí. Decidimos nosotros los dos cambios concretos y le pedimos a la IA que aplicara las modificaciones en una nueva clase `GhostsV1` sin alterar las acciones existentes.

Prompt:
Tenemos implementada la clase GhostsV0 en Java con FSM jerárquica para los 4 fantasmas. Queremos crear GhostsV1 aplicando exactamente estos dos ajustes sin cambiar las clases de acción existentes:
1. En la submáquina COMESTIBLE, los fantasmas mueren en pasillos largos porque no pueden dar media vuelta de 180º cuando Pac-Man viene de frente. Cambia el estado inicial de COMESTIBLE para que arranque directamente en HUIR_RINCON (cruce lejano) en vez de HUIR_PACMAN, y haz que la transición de regreso a HUIR_RINCON se active mucho antes: si distToPacman < 35 (salida > 55). Además, sube el umbral de GUARDAR a edibleTime < 35 ticks tanto desde HUIR_PACMAN como desde HUIR_RINCON.
2. En la submáquina ACTIVO, los fantasmas abandonan la persecución demasiado pronto. Sube el umbral de transición de PERSEGUIR a PATRULLAR de 65 a 90 nodos (y regreso a PERSEGUIR si distToPacman < 50).
Genera la clase GhostsV1 heredando de GhostController con su getName() correspondiente ("GhostsV1_Pr2CY_OGA").
FIN DEL PROMPT

### Resultados v0 vs 1 (300 partidas)

| Rival | Media v0 | Media v1 | Diferencia |
|-------|--------:|--------:|---:|------:|
| Básico | 4188.83 | 4149.90 | -38.93 | 148.63 |
| Intermedio | 7585.33 | 7494.03 | -91.30 | 146.03 |
| Avanzado | 8466.83 | 8836.93 | +370.10 | 141.43 |

**Decisión y conclusiones:**
Contra básico e intermedio bajamos un poco los puntos (38 y 91 puntos menos), pero contra `MsPacManAvanzado` la mejora ha sido desastrosa: **empeora en 370 puntos**.

Pusimos el juego despacio para entender qué pasaba y el fallo fue que `MsPacManAvanzado` planifica rutas que barren las esquinas del laberinto para limpiar las píldoras. Al mandar a todos los fantasmas a buscar los cruces extremos de las esquinas, iban directos hacia donde Pac-Man se dirigía para limpiar el mapa y, como no pueden dar media vuelta de 180 grados para esquivarle de frente, los cazaba a todos juntos en cadena. Las esquinas fijas son un imán mortal contra rivales que limpian esquinas. Descartamos esta idea para la siguiente versión.

---

## Viernes 16 oct · v2 (Segunda mejora: huida dinámica y pinza coordinada)

**Problema que atacamos:**
El error de huir hacia esquinas fijas descubierto en v1 y la falta de coordinación ofensiva en los cruces.

**Cambio pensado antes de tocar código:**
1. Volver a una huida dinámica en `COMESTIBLE`: evaluar en cada cruce la salida que maximice el espacio abierto alejándose de Pac-Man, y solo saltar a cruce de esquina como emergencia si Pac-Man está encima (a menos de 12 nodos).
2. En ataque (`ACTIVO`), mejorar la pinza: si dos fantasmas se acercan a menos de 10 nodos, uno pasa rápido a `FLANQUEAR` buscando un cruce paralelo, mientras otro salta a `INTERCEPTAR` el cruce al que va Pac-Man a media distancia (22 a 32 nodos).

IA: Sí. Diseñamos la solución conceptual tras ver que las esquinas fijas eran el motivo del fracaso de v1, y le pedimos a la IA que implementara GhostsV2 con los umbrales de flanqueo e intercepción que definimos.

Prompt:
Al probar GhostsV1 hemos visto que mandar a los fantasmas a HUIR_RINCON hace que Pac-Man Avanzado los arrase porque sus rutas limpian las esquinas del mapa y los fantasmas no pueden dar la vuelta de 180º cuando se lo encuentran.

Genera la clase GhostsV2 en Java ajustando la FSM con estos criterios exactos:
1. En la submáquina COMESTIBLE:
   - Estado inicial vuelve a ser HUIR_PACMAN (huida dinámica).
   - Solo transiciona a HUIR_RINCON como último recurso de emergencia si distToPacman < 12 (salida > 28 hacia HUIR_PACMAN).
   - Ajusta la retirada a GUARDAR_RESPAWN a edibleTime < 20 ticks (para que no vuelvan al centro demasiado pronto).
2. En la submáquina ACTIVO, para mejorar el cerco en tenaza:
   - Si dos fantasmas aliados se acercan a menos de 10 nodos (minAllyDist < 10), el fantasma debe pasar a FLANQUEAR para buscar un cruce paralelo (salida > 18 hacia PERSEGUIR).
   - Para interceptar a Pac-Man, transiciona de PERSEGUIR a INTERCEPTAR si distToTargetJunction < 22 (salida > 32).
   - Ajusta ACECHAR a distToPacman < 22 (salidas < 10 para atacar de cerca o > 35 para retomar persecución).
   - Mantén PATRULLAR solo como caso residual si distToPacman > 75 (salida < 40).
Hereda de GhostController y pon su getName() como "GhostsV2_Pr2CY_OGA".
FIN DEL PROMPT

### Resultados v2 vs v1 (300 partidas)

| Rival | Media v1 | Media v2 | D | 
|-------|--------:|--------:|---:|
| Básico | 4149.90 | 4178.77 | +28.87 |
| Intermedio | 7494.03 | 7758.20 | +264.17 |
| Avanzado | 8836.93 | 8460.47 | -376.46 |

**Decisión:**
Contra el rival más fuerte (`MsPacManAvanzado`), `GhostsV2` logra una mejora de **376.46 puntos menos**. 

Comparando `v2` con el `v0`:
- Básico: 4178.77 frente a 4188.83 (prácticamente igual).
- Intermedio: 7758.20 frente a 7585.33 (solo cambia el error típico aleatorio).
- Avanzado: 8460.47 frente a 8466.83 (consistencia sólida y logramos que los fantasmas sobrevivan más turnos como comestibles).

Al corregir el fallo grave de `v1` y conseguir que los fantasmas rodeen mejor en los cruces sin estorbarse, nos quedamos con `v2`.

---

## Elección de versión para Fantasmas a concurso

Elegimos **GhostsV2** como nuestro controlador a concurso (la clase `Ghosts` hereda directamente de `GhostsV2`).
Motivo: Es la versión que mejor aguanta las rutas de `MsPacManAvanzado`, evitando muertes en cadena al huir de en los cruces y cerrando mejor los pasos con el flanqueo en tenaza.

---

Aquí termina la sección del controlador de los fantasmas ^_^

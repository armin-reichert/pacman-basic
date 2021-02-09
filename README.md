## Pac-Man and Ms. Pac-Man

A Pac-Man and Ms. Pac-Man game implementation (JDK version 8 or newer required=. Game levels, timing, ghost "AI" etc. follow the details given in the highly appreciated [Pac-Man Dossier](https://pacman.holenet.info) by Jamey Pittman. (Ms. Pac-Man details still missing).

The game is controlled by a finite-state machine with states INTRO, READY, HUNTING, CHANGING_LEVEL, PACMAN_DYING, GHOST_DYING, GAME_OVER. The user interface is decoupled from the game controller by an interface. A Swing implementation of this interface is provided in the subproject `pacman-ui-swing`.

The code here is more to the point than that in my other state-machine focussed [Pac-Man implementation](https://github.com/armin-reichert/pacman).

YouTube: https://www.youtube.com/watch?v=oZh2oAnoJbk

To build the executable, change to the `pacman-ui-swing` directory and enter `mvn clean install`.

### Intro scene
<img src="pacman/doc/intro.png">

### Pac-Man play scene
<img src="pacman/doc/playing.png">

### Ms. Pac-Man play scene
<img src="pacman/doc/mspacman_playing.png">

### Keys

- General
  - "d" = Toggle debug drawing mode
  - "s" = Toggle slow/normal 
  - "f" = Toggle fast/normal

- Intro screen
  - "v" = Toggle game variant (Pac-Man <-> Ms. Pac-Man)

- Play screen
  - "Escape" = Cancel game, return to intro screen
  - Cursor LEFT, RIGHT, UP, DOWN = Move Pac-Man
  - "a" = Toggle Pac-Man autopilot
  - "e" = Eat all non-energizer pellets
  - "l" = Add life
  - "n" = Enter next level
  - "x" = Kill all ghosts
  

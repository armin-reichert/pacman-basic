## Pac-Man and Ms. Pac-Man

A Pac-Man and Ms. Pac-Man game implementation with levels, timing, ghost "AI" etc. following the details given in the (highly appreciated) [Pac-Man Dossier](https://pacman.holenet.info) by Jamey Pittman.

This implementation follows the Model-View-Controller pattern:
- The game controller is a finite-state machine with states INTRO, READY, HUNTING, CHANGING_LEVEL, PACMAN_DYING, GHOST_DYING, GAME_OVER and INTERMISSION. 
- The views are decoupled from the controller by an interface [PacManGameUI](pacman/src/main/java/de/amr/games/pacman/ui/PacManGameUI.java). A Swing UI implementation is provided as the default, see subproject `pacman-ui-swing`. A JavaFX UI is provided in repository [pacman-javafx](https://github.com/armin-reichert/pacman-javafx).

The code here is more "to the point" than the one in my other [state-machine focussed implementation](https://github.com/armin-reichert/pacman).

YouTube: https://www.youtube.com/watch?v=oZh2oAnoJbk

To build the executable jar file, run `mvn clean install` in each subproject.

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
  

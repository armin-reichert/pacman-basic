## Pac-Man and Ms. Pac-Man

A Pac-Man and Ms. Pac-Man game implementation with levels, timing, ghost "AI" etc. following the details given in the (highly appreciated) [Pac-Man Dossier](https://pacman.holenet.info) by Jamey Pittman.

This implementation follows the Model-View-Controller pattern:
- The game controller is a finite-state machine with states INTRO, READY, HUNTING, LEVEL_STARTING, LEVEL_COMPLETE, PACMAN_DYING, GHOST_DYING, GAME_OVER and INTERMISSION. 
- The views are decoupled from the controller by an interface [PacManGameUI](pacman/src/main/java/de/amr/games/pacman/ui/PacManGameUI.java). A Swing UI implementation is provided as the default, see subproject `pacman-ui-swing`. A JavaFX UI is provided in repository [pacman-javafx](https://github.com/armin-reichert/pacman-javafx).

The code here is more "to the point" than the one in my other [state-machine focussed implementation](https://github.com/armin-reichert/pacman).

YouTube:

[![YouTube video](https://i9.ytimg.com/vi/q5biOTj9GIU/mq2.jpg?sqp=CMiitoIG&rs=AOn4CLC4DcaXdJYoXZrSsX7-OQMmH50QFQ)](https://youtu.be/q5biOTj9GIU)

### Build
To build the executable jar file, run `mvn clean install` in each subproject.

### Intro scene
<img src="pacman-core/doc/intro.png">

### Pac-Man play scene
<img src="pacman-core/doc/playing.png">

### Ms. Pac-Man play scene
<img src="pacman-core/doc/mspacman_playing.png">

### Keys

- Intro screen
  - "V" = Toggle game variant (Pac-Man <-> Ms. Pac-Man)

- Play screen
  - "Q" = Quit game, return to intro screen
  - Cursor LEFT, RIGHT, UP, DOWN = Move Pac-Man
  - "A" = Toggle Pac-Man autopilot
  - "E" = Eat all non-energizer pellets
  - "L" = Add life
  - "N" = Enter next level
  - "X" = Kill all ghosts
  

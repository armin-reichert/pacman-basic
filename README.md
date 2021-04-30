## Pac-Man and Ms. Pac-Man

A Pac-Man and Ms. Pac-Man game implementation with levels, timing, ghost "AI" etc. following the details given in the (highly appreciated) [Pac-Man Dossier](https://pacman.holenet.info) by Jamey Pittman. The Ms. Pac-Man levels probably are not 100% accurate because I could not find a similarly detailed description as the Pac-Man dossier. Any hints?

This implementation follows the Model-View-Controller pattern:
- The game controller is a finite-state machine with states INTRO, READY, HUNTING, LEVEL_STARTING, LEVEL_COMPLETE, PACMAN_DYING, GHOST_DYING, GAME_OVER and INTERMISSION. 
- The UI is decoupled from the controller by an interface [PacManGameUI](pacman-core/src/main/java/de/amr/games/pacman/ui/PacManGameUI.java). A Swing UI implementation can be found in the repository [pacman-ui-swing](https://github.com/armin-reichert/pacman-ui-swing), a JavaFX UI implementation with play scene in 2D and 3D is available in repository [pacman-javafx](https://github.com/armin-reichert/pacman-javafx).

The code here is more "to the point" than the one in my other [state-machine focussed implementation](https://github.com/armin-reichert/pacman).

YouTube:

[![YouTube video](pacman-core/doc/playing.png)](https://youtu.be/q5biOTj9GIU)

### Build
To build the executable jar file, run `mvn clean install`.

### Intro scene
<img src="pacman-core/doc/intro.png">

### Pac-Man play scene
<img src="pacman-core/doc/playing.png">

### Ms. Pac-Man play scene
<img src="pacman-core/doc/mspacman_playing.png">

# Pac-Man
Yet another Pac-Man game implementation, this time using just JDK functionality (version 8 or later). Game levels, timing, ghost "AI" etc. follow (for the most part) the details given in the highly appreciated [Pac-man Dossier](https://pacman.holenet.info) by Jamey Pittman.

The game is controlled by a finite-state machine with states INTRO, READY, HUNTING, CHANGING_LEVEL, PACMAN_DYING, GHOST_DYING, GAME_OVER. The user interface is decoupled from the game controller by an interface. A Swing implementation of this interface is provided.

The code here is more to the point than that in my other state-machine focussed [Pac-Man implementation](https://github.com/armin-reichert/pacman).

### Intro scene
<img src="PacManDataOriented/doc/intro.png">

### Play scene
<img src="PacManDataOriented/doc/playing.png">

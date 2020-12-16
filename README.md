# Pac-Man
Yet another Pac-Man game implementation, this one is using no additional libraries, just the JDK (8 or later). Levels, timing, ghost "AI" etc. for most parts follow the details given in the [Pac-man dossier](https://pacman.holenet.info).

The game is controlled by a finite-state machine with states INTRO, READY, HUNTING, CHANGING_LEVEL, PACMAN_DYING, GHOST_DYING, GAME_OVER. The user interface is decoupled from the game controller by an interface. A Swing implementation of this interface is provided.

The code here is more to the point than that in my other state-machine focussed [Pac-Man implementation](https://github.com/armin-reichert/pacman).

### Intro scene
<img src="PacManDataOriented/doc/intro.png">

### Play scene
<img src="PacManDataOriented/doc/playing.png">

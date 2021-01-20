## Pac-Man and Ms. Pac-Man

Another Pac-Man game implementation, this time using just JDK functionality (version 8 or later). Game levels, timing, ghost "AI" etc. follow (for the most part) the details given in the highly appreciated [Pac-Man Dossier](https://pacman.holenet.info) by Jamey Pittman.

Ms. Pac-Man ist still work in progress.

The game is controlled by a finite-state machine with states INTRO, READY, HUNTING, CHANGING_LEVEL, PACMAN_DYING, GHOST_DYING, GAME_OVER. The user interface is decoupled from the game controller by an interface. A Swing implementation of this interface is provided.

The code here is more to the point than that in my other state-machine focussed [Pac-Man implementation](https://github.com/armin-reichert/pacman).

YouTube Videos:

Pac-Man: https://www.youtube.com/watch?v=_L6YdSH7xis

Ms. Pac-Man: https://youtu.be/DkY83F6lCdo

To build the executable:
```mvn clean install assembly:single```

### Intro scene
<img src="pacman/doc/intro.png">

### Pac-Man play scene
<img src="pacman/doc/playing.png">

### Ms. Pac-Man play scene
<img src="pacman/doc/mspacman_playing.png">

### Keys

- General
  - "d" = Toggle debug drawing mode
  - "p" = Toggle pause game
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
  

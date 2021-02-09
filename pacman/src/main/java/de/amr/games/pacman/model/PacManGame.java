package de.amr.games.pacman.model;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.heaven.God.random;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.world.MapBasedPacManGameWorld;
import de.amr.games.pacman.world.WorldMap;

/**
 * Game model of the classic Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends AbstractPacManGame {

	private final MapBasedPacManGameWorld world;

	public PacManGame() {

		highscoreFileName = "hiscore-pacman.xml";

		bonusNames = new String[] { "CHERRIES", "STRAWBERRY", "PEACH", "APPLE", "GRAPES", "GALAXIAN", "BELL", "KEY" };
		bonusValues = new int[] { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };
		bonus = new Bonus();
		bonus.position = new V2f(13 * TS + HTS, 20 * TS);

		pac = new Pac("Pac-Man", RIGHT);

		ghosts = new Ghost[4];
		ghosts[BLINKY] = new Ghost(BLINKY, "Blinky", LEFT);
		ghosts[PINKY] = new Ghost(PINKY, "Pinky", UP);
		ghosts[INKY] = new Ghost(INKY, "Inky", DOWN);
		ghosts[CLYDE] = new Ghost(CLYDE, "Clyde", DOWN);

		world = new MapBasedPacManGameWorld();
		world.setMap(new WorldMap("/pacman/maps/map.txt"));
		world.setUpwardsBlocked(new V2i(12, 13), new V2i(15, 13), new V2i(12, 25), new V2i(15, 25));

		pac.world = world;
		for (Ghost ghost : ghosts) {
			ghost.world = world;
		}
		bonus.world = world;

		reset();
	}

	@Override
	public void buildLevel(int levelNumber) {
		log("Pac-Man classic level %d is getting created...", levelNumber);
		level = new GameLevel(PACMAN_LEVELS[levelNumber <= 21 ? levelNumber - 1 : 20]);
		level.setWorld(world);
		log("Pac-Man classic level %d created", levelNumber);
	}

	@Override
	public long bonusActivationTicks() {
		return clock.sec(9 + random.nextFloat());
	}

	@Override
	public int mapIndex(int mazeNumber) {
		return 0;
	}

	@Override
	public int mazeNumber(int levelNumber) {
		return 1;
	}
}
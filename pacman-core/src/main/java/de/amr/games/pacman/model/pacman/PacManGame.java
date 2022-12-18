/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.model.pacman;

import static de.amr.games.pacman.lib.V2i.v2i;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_RED_GHOST;

import java.util.List;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;

/**
 * Model of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends GameModel {

	//@formatter:off
	private static final byte[][] LEVELS = {
	/* 1*/ {0 /* CHERRIES */,    80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {1 /* STRAWBERRY */,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* Intermission scene 1 */
	/* 3*/ {2 /* PEACH */,       90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {2,                   90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {3 /* APPLE */,      100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* Intermission scene 2 */
	/* 6*/ {3,                  100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {4 /* GRAPES */,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {4,                  100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {5 /* GALAXIAN */,   100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/* Intermission scene 3 */
	/*10*/ {5,                  100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
	/*11*/ {6 /* BELL */,       100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {6,                  100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {7 /* KEY */,        100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/* Intermission scene 3 */
	/*14*/ {7,                  100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {7,                  100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {7,                  100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {7,                  100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/* Intermission scene 3 */
	/*18*/ {7,                  100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {7,                  100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {7,                  100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {7,                   90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	};
	/*@formatter:on*/

	private static final V2d BONUS_POSITION = World.halfTileRightOf(new V2i(13, 20));

	private static final short[] BONUS_VALUES = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

	private static final byte[][] MAP = {
	//@formatter:off
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,4,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,4,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,},
		{1,1,1,1,1,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,1,1,1,1,1,},
		{0,0,0,0,0,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,0,0,0,0,0,},
		{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1,},
		{2,2,2,2,2,2,3,0,0,0,1,0,0,0,0,0,0,1,0,0,0,3,2,2,2,2,2,2,},
		{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1,},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,0,0,0,0,0,},
		{1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,4,3,3,1,1,3,3,3,3,3,3,3,0,0,3,3,3,3,3,3,3,1,1,3,3,4,1,},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1,},
		{1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		//@formatter:on
	};

	// Tiles where chasing ghosts cannot move upwards
	public static final List<V2i> RED_ZONE = List.of(v2i(12, 14), v2i(15, 14), v2i(12, 26), v2i(15, 26));

	public static ArcadeWorld createWorld() {
		return new ArcadeWorld(MAP);
	}

	private StaticBonus bonus;

	@Override
	protected Pac createPac() {
		return new Pac("Pac-Man");
	}

	@Override
	protected Ghost[] createGhosts() {
		return new Ghost[] { //
				new Ghost(ID_RED_GHOST, "Blinky"), //
				new Ghost(ID_PINK_GHOST, "Pinky"), //
				new Ghost(ID_CYAN_GHOST, "Inky"), //
				new Ghost(ID_ORANGE_GHOST, "Clyde") //
		};
	}

	@Override
	protected GameLevel createLevel(int levelNumber) {
		checkLevelNumber(levelNumber);
		int mazeNumber = 1;
		var world = createWorld();
		var data = levelNumber <= LEVELS.length ? LEVELS[levelNumber - 1] : LEVELS[LEVELS.length - 1];
		var houseRules = createHouseRules(levelNumber);
		return GameLevel.of(levelNumber, mazeNumber, world, houseRules, GameLevel.USE_BONUS_FROM_DATA, data);
	}

	@Override
	protected void createBonus() {
		bonus = new StaticBonus(BONUS_POSITION);
	}

	@Override
	public GameVariant variant() {
		return GameVariant.PACMAN;
	}

	@Override
	public boolean isGhostAllowedMoving(Ghost ghost, Direction dir) {
		if (dir == Direction.UP && ghost.is(GhostState.HUNTING_PAC) && RED_ZONE.contains(ghost.tile())) {
			return false;
		}
		return super.isGhostAllowedMoving(ghost, dir);
	}

	@Override
	public StaticBonus bonus() {
		return bonus;
	}

	@Override
	protected void onBonusReached() {
		int ticks = 10 * FPS - RND.nextInt(FPS); // between 9 and 10 seconds
		bonus.setEdible(level.bonusIndex(), BONUS_VALUES[level.bonusIndex()], ticks);
		LOGGER.info("Bonus activated for %d ticks (%.2f seconds). %s", ticks, (double) ticks / FPS, bonus);
		GameEvents.publish(GameEventType.BONUS_GETS_ACTIVE, World.tileAt(bonus.entity().position()));
	}
}
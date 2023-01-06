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

import static de.amr.games.pacman.lib.math.Vector2i.v2i;
import static de.amr.games.pacman.lib.steering.NavigationPoint.np;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_RED_GHOST;
import static de.amr.games.pacman.model.common.world.World.halfTileRightOf;

import java.util.List;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.steering.NavigationPoint;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.ArcadeWorld;

/**
 * Model of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends GameModel {

	//@formatter:off
	public static final byte[][] MAP = {
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,4,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,4,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1},
		{1,1,1,1,1,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,1,1,1,1,1},
		{0,0,0,0,0,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,0,0,0,0,0},
		{0,0,0,0,0,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,0,0,0,0,0},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,0,0,0,0,0},
		{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1},
		{2,2,2,2,2,2,3,0,0,0,1,0,0,0,0,0,0,1,0,0,0,3,2,2,2,2,2,2},
		{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,0,0,0,0,0},
		{0,0,0,0,0,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,0,0,0,0,0},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,0,0,0,0,0},
		{1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,4,3,3,1,1,3,3,3,3,3,3,3,0,0,3,3,3,3,3,3,3,1,1,3,3,4,1},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1},
		{1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
	};
	//@formatter:on

	// Tiles where chasing ghosts cannot move upwards
	private static final List<Vector2i> RED_ZONE = List.of(v2i(12, 14), v2i(15, 14), v2i(12, 26), v2i(15, 26));

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
	protected ArcadeWorld createWorld(int levelNumber) {
		checkLevelNumber(levelNumber);
		var world = new ArcadeWorld(MAP);
		world.setUpwardBlockedTiles(RED_ZONE);
		return world;
	}

	@Override
	protected Bonus createBonus(int levelNumber) {
		checkLevelNumber(levelNumber);
		//@formatter:off
		var bonus = switch (levelNumber) {
		case 1      -> new StaticBonus((byte)0,  100); // Cherries
		case 2      -> new StaticBonus((byte)1,  300); // Strawberry
		case 3, 4   -> new StaticBonus((byte)2,  500); // Peach
		case 5, 6   -> new StaticBonus((byte)3,  700); // Apple
		case 7, 8   -> new StaticBonus((byte)4, 1000); // Grapes
		case 9, 10  -> new StaticBonus((byte)5, 2000); // Galaxian
		case 11, 12 -> new StaticBonus((byte)6, 3000); // Bell
		default     -> new StaticBonus((byte)7, 5000); // Key
		};
		//@formatter:on
		bonus.entity().setPosition(halfTileRightOf(13, 20));
		return bonus;
	}

	@Override
	public GameVariant variant() {
		return GameVariant.PACMAN;
	}

	@Override
	public void onBonusReached() {
		var bonus = level.bonus();
		int ticks = 10 * FPS - RND.nextInt(FPS); // between 9 and 10 seconds
		bonus.setEdible(ticks);
		LOGGER.info("Bonus activated for %d ticks (%.2f seconds): %s", ticks, (float) ticks / FPS, bonus);
		GameEvents.publish(GameEventType.BONUS_GETS_ACTIVE, bonus.entity().tile());
	}

	// experimental zone:

	private static final List<NavigationPoint> PACMAN_DEMO_LEVEL_ROUTE = List.of(np(12, 26), np(9, 26), np(12, 32),
			np(15, 32), np(24, 29), np(21, 23), np(18, 23), np(18, 20), np(18, 17), np(15, 14), np(12, 14), np(9, 17),
			np(6, 17), np(6, 11), np(6, 8), np(6, 4), np(1, 8), np(6, 8), np(9, 8), np(12, 8), np(6, 4), np(6, 8), np(6, 11),
			np(1, 8), np(6, 8), np(9, 8), np(12, 14), np(9, 17), np(6, 17), np(0, 17), np(21, 17), np(21, 23), np(21, 26),
			np(24, 29), /* avoid moving up: */ np(26, 29), np(15, 32), np(12, 32), np(3, 29), np(6, 23), np(9, 23),
			np(12, 26), np(15, 26), np(18, 23), np(21, 23), np(24, 29), /* avoid moving up: */ np(26, 29), np(15, 32),
			np(12, 32), np(3, 29), np(6, 23));

	private static final List<NavigationPoint> GHOST_0_DEMO_LEVEL_ROUTE = List.of(np(21, 4, Direction.DOWN),
			np(21, 8, Direction.DOWN), np(21, 11, Direction.RIGHT), np(26, 8, Direction.LEFT), np(21, 8, Direction.DOWN),
			np(26, 8, Direction.UP), np(26, 8, Direction.DOWN), np(21, 11, Direction.DOWN), np(21, 17, Direction.RIGHT), // enters

			np(99, 99, Direction.DOWN));

	@Override
	public List<NavigationPoint> getDemoLevelPacRoute() {
		return PACMAN_DEMO_LEVEL_ROUTE;
	}

	@Override
	public List<NavigationPoint> getDemoLevelGhostRoute(byte ghostID) {
		return switch (ghostID) {
		case Ghost.ID_RED_GHOST -> GHOST_0_DEMO_LEVEL_ROUTE;
		case Ghost.ID_PINK_GHOST -> List.of();
		case Ghost.ID_CYAN_GHOST -> List.of();
		case Ghost.ID_ORANGE_GHOST -> List.of();
		default -> throw new IllegalArgumentException();
		};
	}

}
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
package de.amr.games.pacman.model.common.world;

import static de.amr.games.pacman.lib.U.differsAtMost;
import static de.amr.games.pacman.lib.math.Vector2i.v2i;
import static de.amr.games.pacman.lib.steering.Direction.LEFT;
import static de.amr.games.pacman.lib.steering.Direction.RIGHT;
import static de.amr.games.pacman.lib.steering.Direction.UP;

import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * The ghost house from the Arcade version of the games. Door on top, three seats inside.
 * 
 * @author Armin Reichert
 */
public class ArcadeGhostHouse implements GhostHouse {

	public static final Vector2i SIZE_TILES = v2i(8, 5);
	public static final Vector2i TOP_LEFT_TILE = v2i(10, 15);
	public static final Vector2i DOOR_LEFT_TILE = v2i(13, 15);
	public static final Vector2i SEAT_LEFT_TILE = v2i(11, 17);
	public static final Vector2i SEAT_CENTER_TILE = v2i(13, 17);
	public static final Vector2i SEAT_RIGHT_TILE = v2i(15, 17);

	private final Door door;

	public ArcadeGhostHouse() {
		door = new Door(DOOR_LEFT_TILE, 2);
	}

	@Override
	public Vector2i sizeInTiles() {
		return SIZE_TILES;
	}

	@Override
	public Vector2i topLeftTile() {
		return TOP_LEFT_TILE;
	}

	@Override
	public Door door() {
		return door;
	}

	/**
	 * Ghosts are first moved to the horizontal center, then they raise until the entry position outside is reached.
	 */
	@Override
	public boolean leadOut(Creature ghost) {
		var reachedExit = false;
		var entryPosition = door.entryPosition();
		if (ghost.position().almostEquals(entryPosition, 0, 1)) {
			ghost.setPosition(entryPosition);
			reachedExit = true;
		} else if (differsAtMost(1, ghost.position().x(), entryPosition.x())) {
			// horizontal center reached: rise
			ghost.setPosition(entryPosition.x(), ghost.position().y());
			ghost.setMoveAndWishDir(UP);
			ghost.move();
		} else {
			// move sidewards until middle axis is reached
			ghost.setMoveAndWishDir(ghost.position().x() < entryPosition.x() ? RIGHT : LEFT);
			ghost.move();
		}
		return reachedExit;
	}

	@Override
	public boolean leadInside(Creature guy, Vector2f targetPosition) {
		var entryPosition = door.entryPosition();
		if (door().reachedBy(guy) && guy.moveDir() != Direction.DOWN) {
			guy.setPosition(entryPosition);
			guy.setMoveAndWishDir(Direction.DOWN);
		}
		var bottomY = SEAT_CENTER_TILE.y() * World.TS + World.HTS;
		if (guy.position().y() >= bottomY) {
			if (targetPosition.x() < entryPosition.x()) {
				guy.setMoveAndWishDir(LEFT);
			} else if (targetPosition.x() > entryPosition.x()) {
				guy.setMoveAndWishDir(RIGHT);
			}
		}
		guy.move();
		boolean reachedTarget = U.differsAtMost(1, guy.position().x(), targetPosition.x())
				&& guy.position().y() >= targetPosition.y();
		if (reachedTarget) {
			guy.setPosition(targetPosition);
		}
		return reachedTarget;
	}
}
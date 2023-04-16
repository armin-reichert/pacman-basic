/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.halfTileRightOf;

import java.util.Collections;
import java.util.List;

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

	private static final Vector2i SIZE_TILES = v2i(8, 5);
	private static final Vector2i TOP_LEFT_TILE = v2i(10, 15);
	private static final Vector2i DOOR_LEFT_TILE = v2i(13, 15);
	private static final int GROUND_Y = 17 * TS + HTS;

	private final List<Door> doors;
	private final List<Vector2f> seatPositions;

	public ArcadeGhostHouse() {
		doors = Collections.singletonList(new Door(DOOR_LEFT_TILE, 2));
		seatPositions = List.of(//
				halfTileRightOf(11, 17), halfTileRightOf(13, 17), halfTileRightOf(15, 17));
	}

	@Override
	public Vector2i size() {
		return SIZE_TILES;
	}

	@Override
	public Vector2i position() {
		return TOP_LEFT_TILE;
	}

	@Override
	public List<Door> doors() {
		return doors;
	}

	@Override
	public List<Vector2f> seatPositions() {
		return seatPositions;
	}

	/**
	 * Ghosts first move sidewards to the center, then they raise until the house entry/exit position outside is reached.
	 */
	@Override
	public boolean leadOutside(Creature ghost) {
		var theDoor = doors.get(0);
		var exitPosition = theDoor.entryPosition();
		if (ghost.position().y() <= exitPosition.y()) {
			ghost.setPosition(exitPosition);
			return true;
		}
		if (differsAtMost(ghost.velocity().length() / 2, ghost.position().x(), exitPosition.x())) {
			// center reached: start rising
			ghost.setPosition(exitPosition.x(), ghost.position().y());
			ghost.setMoveAndWishDir(UP);
		} else {
			// move sidewards until middle axis is reached
			ghost.setMoveAndWishDir(ghost.position().x() < exitPosition.x() ? RIGHT : LEFT);
		}
		ghost.move();
		return false;
	}

	/**
	 * Ghost moves down on the vertical axis to the center, then returns or moves sidewards to its seat.
	 */
	@Override
	public boolean leadInside(Creature ghost, Vector2f targetPosition) {
		var entryPosition = doors.get(0).entryPosition();
		if (ghost.position().almostEquals(entryPosition, ghost.velocity().length() / 2, 0)
				&& ghost.moveDir() != Direction.DOWN) {
			// just reached door, start sinking
			ghost.setPosition(entryPosition);
			ghost.setMoveAndWishDir(Direction.DOWN);
		} else if (ghost.position().y() >= GROUND_Y) {
			ghost.setPosition(ghost.position().x(), GROUND_Y);
			if (targetPosition.x() < entryPosition.x()) {
				ghost.setMoveAndWishDir(LEFT);
			} else if (targetPosition.x() > entryPosition.x()) {
				ghost.setMoveAndWishDir(RIGHT);
			}
		}
		ghost.move();
		boolean reachedTarget = differsAtMost(1, ghost.position().x(), targetPosition.x())
				&& ghost.position().y() >= targetPosition.y();
		if (reachedTarget) {
			ghost.setPosition(targetPosition);
		}
		return reachedTarget;
	}
}
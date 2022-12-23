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

import static de.amr.games.pacman.lib.math.Vector2i.v2i;
import static de.amr.games.pacman.lib.steering.Direction.LEFT;
import static de.amr.games.pacman.lib.steering.Direction.RIGHT;
import static de.amr.games.pacman.lib.steering.Direction.UP;
import static de.amr.games.pacman.model.common.world.World.HTS;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.lib.math.Vector2d;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * The ghost house from the Arcade version of the games. Door on top, three seats inside.
 * 
 * @author Armin Reichert
 */
public class ArcadeGhostHouse implements GhostHouse {

	public static final Vector2i SIZE_TILES = v2i(7, 4);
	public static final Vector2i TOP_LEFT_TILE = v2i(10, 15);
	public static final Vector2i DOOR_TILE_LEFT = v2i(13, 15);
	public static final Vector2i DOOR_TILE_RIGHT = v2i(14, 15);
	public static final Vector2d DOOR_CENTER = World.halfTileRightOf(DOOR_TILE_LEFT).plus(0, HTS);
	public static final Vector2i ENTRY_TILE = v2i(13, 14);
	public static final Vector2i SEAT_TILE_LEFT = v2i(11, 17);
	public static final Vector2i SEAT_TILE_CENTER = v2i(13, 17);
	public static final Vector2i SEAT_TILE_RIGHT = v2i(15, 17);

	@Override
	public Vector2i size() {
		return SIZE_TILES;
	}

	@Override
	public Vector2i topLeftTile() {
		return TOP_LEFT_TILE;
	}

	@Override
	public Stream<Vector2i> doorTiles() {
		return Stream.of(DOOR_TILE_LEFT, DOOR_TILE_RIGHT);
	}

	@Override
	public boolean isDoorTile(Vector2i tile) {
		return tile.equals(DOOR_TILE_LEFT) || tile.equals(DOOR_TILE_RIGHT);
	}

	@Override
	public Vector2i entryTile() {
		return ENTRY_TILE;
	}

	public Vector2d middleSeatCenterPosition() {
		return World.halfTileRightOf(SEAT_TILE_CENTER).plus(0, HTS);
	}

	@Override
	public boolean atHouseEntry(Creature guy) {
		var entryPos = World.halfTileRightOf(ENTRY_TILE);
		return guy.tile().y() == ENTRY_TILE.y() && U.insideRange(guy.position().x(), entryPos.x(), 1);
	}

	@Override
	public boolean leadGuyOutOfHouse(Creature guy) {
		var entryPos = World.halfTileRightOf(ENTRY_TILE);
		if (guy.position().x() == entryPos.x() && guy.position().y() <= entryPos.y()) {
			guy.setPosition(entryPos);
			return true;
		}
		var center = middleSeatCenterPosition();
		if (U.insideRange(guy.position().x(), center.x(), 1)) {
			// center horizontally before rising
			guy.setPosition(center.x(), guy.position().y());
			guy.setMoveAndWishDir(UP);
		} else {
			guy.setMoveAndWishDir(guy.position().x() < center.x() ? RIGHT : LEFT);
		}
		guy.move();
		return false;
	}

	@Override
	public boolean leadGuyInside(Creature guy, Vector2d targetPosition) {
		if (atHouseEntry(guy)) {
			guy.setPosition(World.halfTileRightOf(DOOR_TILE_LEFT)); // align
			guy.setMoveAndWishDir(Direction.DOWN);
		}
		var middlePosition = World.halfTileRightOf(SEAT_TILE_CENTER);
		if (guy.position().y() >= middlePosition.y()) {
			if (targetPosition.x() < middlePosition.x()) {
				guy.setMoveAndWishDir(LEFT);
			} else if (targetPosition.x() > middlePosition.x()) {
				guy.setMoveAndWishDir(RIGHT);
			}
		}
		guy.move();
		boolean reachedTarget = U.insideRange(guy.position().x(), targetPosition.x(), 1)
				&& guy.position().y() >= targetPosition.y();
		if (reachedTarget) {
			guy.setPosition(targetPosition);
		}
		return reachedTarget;
	}
}
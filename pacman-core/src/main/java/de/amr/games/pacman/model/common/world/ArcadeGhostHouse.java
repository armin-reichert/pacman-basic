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

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.V2i.v;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * The ghost house from the Arcade version of the games. Door on top, three seats inside.
 * 
 * @author Armin Reichert
 */
public class ArcadeGhostHouse implements GhostHouse {

	public static final V2i SIZE_TILES = v(7, 4);
	public static final V2i TOP_LEFT_TILE = v(10, 15);
	public static final V2i DOOR_TILE_LEFT = v(13, 15);
	public static final V2i DOOR_TILE_RIGHT = v(14, 15);
	public static final V2d DOOR_CENTER = new V2d(DOOR_TILE_LEFT.scaled(TS)).plus(TS, HTS);
	public static final V2i ENTRY_TILE = v(13, 14);
	public static final V2i SEAT_TILE_LEFT = v(11, 17);
	public static final V2i SEAT_TILE_CENTER = v(13, 17);
	public static final V2i SEAT_TILE_RIGHT = v(15, 17);

	@Override
	public V2i size() {
		return SIZE_TILES;
	}

	@Override
	public V2i topLeftTile() {
		return TOP_LEFT_TILE;
	}

	@Override
	public Stream<V2i> doorTiles() {
		return Stream.of(DOOR_TILE_LEFT, DOOR_TILE_RIGHT);
	}

	@Override
	public boolean isDoorTile(V2i tile) {
		return tile.equals(DOOR_TILE_LEFT) || tile.equals(DOOR_TILE_RIGHT);
	}

	@Override
	public V2i entryTile() {
		return ENTRY_TILE;
	}

	public V2d middleSeatCenterPosition() {
		return new V2d(SEAT_TILE_CENTER.scaled(TS)).plus(HTS, HTS);
	}

	@Override
	public boolean atHouseEntry(Creature guy) {
		var entryX = ENTRY_TILE.x() * TS + HTS;
		return guy.tile().y() == ENTRY_TILE.y() && U.insideRange(guy.getPosition().x(), entryX, 1);
	}

	@Override
	public boolean leadGuyOutOfHouse(Creature guy) {
		var entryPos = new V2d(ENTRY_TILE.scaled(TS).plus(HTS, 0));
		if (guy.getPosition().x() == entryPos.x() && guy.getPosition().y() <= entryPos.y()) {
			guy.setPosition(entryPos);
			return true;
		}
		var center = middleSeatCenterPosition();
		if (U.insideRange(guy.getPosition().x(), center.x(), 1)) {
			// center horizontally before rising
			guy.setPosition(center.x(), guy.getPosition().y());
			guy.setMoveAndWishDir(UP);
		} else {
			guy.setMoveAndWishDir(guy.getPosition().x() < center.x() ? RIGHT : LEFT);
		}
		guy.move();
		return false;
	}

	@Override
	public boolean leadGuyInside(Creature guy, V2d targetPosition) {
		if (atHouseEntry(guy)) {
			guy.setMoveAndWishDir(Direction.DOWN);
		}
		var middlePosition = new V2d(SEAT_TILE_CENTER).scaled(TS).plus(HTS, 0);
		if (guy.getPosition().y() >= middlePosition.y()) {
			if (targetPosition.x() < middlePosition.x()) {
				guy.setMoveAndWishDir(LEFT);
			} else if (targetPosition.x() > middlePosition.x()) {
				guy.setMoveAndWishDir(RIGHT);
			}
		}
		guy.move();
		return U.insideRange(guy.getPosition().x(), targetPosition.x(), 1) && guy.getPosition().y() >= targetPosition.y();
	}
}
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

	private final V2i size = v(7, 4);
	private final V2i topLeftTile = v(10, 15);
	private final V2i doorLeftTile = v(13, 15);
	private final V2i doorRightTile = v(14, 15);
	private final V2i entryTile = v(13, 14);
	private final V2i seatLeftTile = v(11, 17);
	private final V2i seatMiddleTile = v(13, 17);
	private final V2i seatRightTile = v(15, 17);

	public V2i doorLeftTile() {
		return doorLeftTile;
	}

	public V2i doorRightTile() {
		return doorRightTile;
	}

	public V2d doorsCenterPosition() {
		return new V2d(doorLeftTile().scaled(TS)).plus(TS, HTS);
	}

	@Override
	public V2i size() {
		return size;
	}

	@Override
	public V2i topLeftTile() {
		return topLeftTile;
	}

	@Override
	public Stream<V2i> doorTiles() {
		return Stream.of(doorLeftTile, doorRightTile);
	}

	@Override
	public boolean isDoorTile(V2i tile) {
		return tile.equals(doorLeftTile()) || tile.equals(doorRightTile());
	}

	@Override
	public V2i entryTile() {
		return entryTile;
	}

	public V2i seatLeftTile() {
		return seatLeftTile;
	}

	public V2i seatMiddleTile() {
		return seatMiddleTile;
	}

	public V2d middleSeatCenterPosition() {
		return new V2d(seatMiddleTile().scaled(TS)).plus(HTS, HTS);
	}

	public V2i seatRightTile() {
		return seatRightTile;
	}

	public V2d seatPosition(V2i seatTile) {
		return new V2d(seatTile).scaled(TS).plus(HTS, 0);
	}

	@Override
	public boolean atHouseEntry(Creature creature) {
		var entryX = entryTile.x() * TS + HTS;
		return creature.tile().y() == entryTile.y() && U.insideRange(creature.getPosition().x(), entryX, 1);
	}

	@Override
	public boolean leadGuyOutOfHouse(Creature guy) {
		var entryPos = new V2d(entryTile.scaled(TS).plus(HTS, 0));
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
		var middlePosition = new V2d(seatMiddleTile).scaled(TS).plus(HTS, 0);
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
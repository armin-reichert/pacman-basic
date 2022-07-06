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

	@Override
	public V2i size() {
		return size;
	}

	@Override
	public V2i topLeftTile() {
		return topLeftTile;
	}

	@Override
	public V2i doorLeftTile() {
		return doorLeftTile;
	}

	@Override
	public V2i doorRightTile() {
		return doorRightTile;
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
		return creature.tile().equals(entryTile) && U.insideRange(creature.offset().x, HTS, 1);
	}

	@Override
	public boolean leadGuestToHouseEntry(Creature guest) {
		var entryPos = new V2d(entryTile.scaled(TS).plus(HTS, 0));
		if (guest.getPosition().x == entryPos.x && guest.getPosition().y <= entryPos.y) {
			guest.setPosition(entryPos);
			return true;
		}
		var center = middleSeatCenterPosition();
		if (U.insideRange(guest.getPosition().x, center.x, 1)) {
			guest.setOffset(HTS, guest.offset().y); // center horizontally before rising
			guest.setBothDirs(UP);
		} else {
			guest.setBothDirs(guest.getPosition().x < center.x ? RIGHT : LEFT);
		}
		guest.move();
		return false;
	}

	@Override
	public boolean leadGuestToTile(Creature guest, V2i targetTile) {
		var tile = guest.tile();
		if (tile.equals(targetTile) && guest.offset().y >= 0) {
			return true;
		}
		var middle = seatMiddleTile();
		if (tile.equals(middle) && guest.offset().y >= 0) {
			if (targetTile.x < middle.x) {
				guest.setBothDirs(LEFT);
			} else if (targetTile.x > middle.x) {
				guest.setBothDirs(RIGHT);
			}
		}
		guest.move();
		return false;
	}
}
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

	@Override
	public V2i size() {
		return v(7, 4);
	}

	@Override
	public V2i topLeftTile() {
		return v(10, 15);
	}

	@Override
	public V2i doorTileLeft() {
		return v(13, 15);
	}

	@Override
	public V2i doorTileRight() {
		return v(14, 15);
	}

	@Override
	public V2i seatLeft() {
		return v(11, 17);
	}

	@Override
	public V2i seatMiddle() {
		return v(13, 17);
	}

	@Override
	public V2i seatRight() {
		return v(15, 17);
	}

	@Override
	public boolean leadToHouseEntry(Creature guest) {
		var entryPos = new V2d(entry()).scaled(TS).plus(HTS, 0);
		var guestPos = guest.getPosition();
		if (guestPos.x == entryPos.x && guestPos.y <= entryPos.y) {
			guest.setPosition(entryPos);
			return true;
		}
		var center = middleSeatCenter();
		if (U.insideRange(guestPos.x, center.x, 1)) {
			guest.setOffset(HTS, guest.offset().y); // center horizontally before rising
			guest.setBothDirs(UP);
		} else {
			guest.setBothDirs(guestPos.x < center.x ? RIGHT : LEFT);
		}
		guest.move();
		return false;
	}
}
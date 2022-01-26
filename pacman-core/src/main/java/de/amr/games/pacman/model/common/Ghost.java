/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.model.world.World.HTS;
import static de.amr.games.pacman.model.world.World.t;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Misc;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature {

	/** The ID (color) of the ghost (0=red, 1=pink, 2=cyan, 3=orange). */
	public final int id;

	/** The current state of the ghost. */
	public GhostState state;

	/** The home location of the ghost. */
	public V2i homeTile;

	/** The revival location of the ghost. */
	public V2i revivalTile;

	/** The bounty earned for killing this ghost. */
	public int bounty;

	/** Function computing the target tile of this ghost. */
	public Supplier<V2i> fnChasingTargetTile;

	/** The individual food counter, used to determine when the ghost can leave the house. */
	public int dotCounter;

	/** Global number of "dots" Pac-Man has to eat until ghost gets unlocked, */
	public int globalDotLimit;

	/** Individual nNumber of "dots" Pac-Man has to eat until ghost gets unlocked, */
	public int privateDotLimit;

	/** "Cruise Elroy" mode of the red ghost. Value is 1, 2 or -1, -2 (disabled modes). */
	public int elroy;

	public Ghost(int id, String name) {
		super(name);
		this.id = id;
	}

	public boolean is(GhostState ghostState) {
		return state == ghostState;
	}

	@Override
	public boolean canAccessTile(V2i tile) {
		if (world.ghostHouse().doorTiles().contains(tile)) {
			return is(GhostState.ENTERING_HOUSE) || is(GhostState.LEAVING_HOUSE);
		}
		if (world.isOneWayDown(tile)) {
			if (offset().y != 0) {
				return true; // maybe already on the way up
			}
			return !is(GhostState.HUNTING_PAC);
		}
		return super.canAccessTile(tile);
	}

	public boolean atGhostHouseDoor() {
		return tile().equals(world.ghostHouse().entryTile()) && Misc.differsAtMost(offset().x, HTS, 2);
	}

	/**
	 * Lets the ghost head for its current chasing target.
	 */
	public void chase() {
		headForTile(fnChasingTargetTile.get());
		tryMoving();
	}

	/**
	 * Lets the ghost head for its scatter tile.
	 */
	public void scatter() {
		headForTile(world.ghostScatterTile(id));
		tryMoving();
	}

	/**
	 * Lets the ghost choose some random direction whenever it enters a new tile.
	 * 
	 * TODO: this is not 100% what the Pac-Man dossier says.
	 */
	public void roam() {
		if (newTileEntered) {
			wishDir = Direction.shuffled().stream().filter(d -> d != dir.opposite() && canAccessTile(tile().plus(d.vec)))
					.findAny().orElse(wishDir);
		}
		tryMoving();
	}

	/**
	 * Lets the ghost return back to the ghost house.
	 * 
	 * @return {@code true} if the ghost has reached the house
	 */
	public boolean returnHome() {
		if (atGhostHouseDoor() && dir != Direction.DOWN) {
			// house reached, start entering
			setOffset(HTS, 0);
			setDir(Direction.DOWN);
			setWishDir(Direction.DOWN);
			forcedOnTrack = false;
			targetTile = revivalTile;
			state = GhostState.ENTERING_HOUSE;
			return true;
		}
		headForTile(targetTile);
		tryMoving();
		return false;
	}

	/**
	 * Lets the ghost enter the house and moving to its revival position.
	 * 
	 * @return {@code true} if the ghost has reached its revival position
	 */
	public boolean enterHouse() {
		V2i tile = tile();
		V2d offset = offset();
		// Target position inside house reached? Turn around and start leaving house.
		if (tile.equals(targetTile) && offset.y >= 0) {
			setWishDir(dir.opposite());
			state = GhostState.LEAVING_HOUSE;
			return true;
		}
		// Center reached? If target tile is left or right seat, move towards target tile
		if (tile.equals(world.ghostHouse().seat(1)) && offset.y >= 0) {
			Direction newDir = targetTile.x < world.ghostHouse().seat(1).x ? Direction.LEFT : Direction.RIGHT;
			setDir(newDir);
			setWishDir(newDir);
		}
		tryMovingTowards(dir);
		return false;
	}

	/**
	 * Lets the ghost leave the house from its home position towards the middle of the house and then upwards towards the
	 * house door.
	 * 
	 * @return {@code true} if the ghost has left the house
	 */
	public boolean leaveHouse() {
		V2i tile = tile();
		V2d offset = offset();
		// House left? Resume hunting.
		if (tile.equals(world.ghostHouse().entryTile()) && Misc.differsAtMost(offset.y, 0, 1)) {
			setOffset(HTS, 0);
			setDir(Direction.LEFT);
			setWishDir(Direction.LEFT);
			forcedOnTrack = true;
			state = GhostState.HUNTING_PAC;
			return true;
		}
		V2i middleSeat = world.ghostHouse().seat(1);
		int center = t(middleSeat.x) + HTS;
		int ground = t(middleSeat.y) + HTS;
		if (Misc.differsAtMost(position.x, center, 1)) {
			setOffset(HTS, offset.y);
			setDir(Direction.UP);
			setWishDir(Direction.UP);
		} else if (position.y < ground) {
			setDir(Direction.DOWN);
			setWishDir(Direction.DOWN);
		} else {
			Direction newDir = position.x < center ? Direction.RIGHT : Direction.LEFT;
			setDir(newDir);
			setWishDir(newDir);
		}
		tryMovingTowards(wishDir);
		return false;
	}

	/**
	 * Lets the ghost bounce at its home position inside the house.
	 * 
	 * @return {@code true}
	 */
	public boolean bounce() {
		int centerY = t(world.ghostHouse().seat(1).y);
		if (position.y < centerY - HTS || position.y > centerY + HTS) {
			Direction opposite = dir.opposite();
			setDir(opposite);
			setWishDir(opposite);
		}
		tryMoving();
		return true;
	}

	@Override
	public String toString() {
		return String.format("[Ghost %s: state=%s, position=%s, tile=%s, offset=%s, velocity=%s, dir=%s, wishDir=%s]", name,
				state, position, tile(), offset(), velocity, dir, wishDir);
	}
}
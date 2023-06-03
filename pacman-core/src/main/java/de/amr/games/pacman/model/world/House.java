/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.checkTileNotNull;

import java.util.Arrays;
import java.util.List;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;

/**
 * @author Armin Reichert
 */
public final class House {
	private final Vector2i minTile;
	private final Vector2i size;
	private final Door door;
	private final List<Vector2f> seatPositions;
	private final Vector2f center;

	/**
	 * @param minTile       top-left tile
	 * @param size          size in tiles
	 * @param door          door of this house
	 * @param seatPositions left-upper corners of the seats (each seat is supposed as single-tile square)
	 */
	public House(Vector2i minTile, Vector2i size, Door door, Vector2f seat1, Vector2f seat2, Vector2f seat3) {
		checkTileNotNull(minTile);
		checkNotNull(size);
		checkNotNull(door);
		checkNotNull(seat1);
		checkNotNull(seat2);
		checkNotNull(seat3);
		if (size.x() < 1 || size.y() < 1) {
			throw new IllegalArgumentException("House size must be larger than one square tile but is: " + size);
		}
		this.minTile = minTile;
		this.size = size;
		this.center = minTile.toFloatVec().scaled(TS).plus(size.toFloatVec().scaled(HTS));
		this.door = door;
		this.seatPositions = Arrays.asList(seat1, seat2, seat3);
	}

	public Vector2i topLeftTile() {
		return minTile;
	}

	public Vector2i size() {
		return size;
	}

	public Door door() {
		return door;
	}

	public List<Vector2f> seatPositions() {
		return seatPositions;
	}

	public Vector2f center() {
		return center;
	}

	public Vector2f seatPosition(int i) {
		return seatPositions.get(i);
	}

	/**
	 * @param tile some tile
	 * @return tells if the given tile is part of this house
	 */
	public boolean contains(Vector2i tile) {
		Vector2i max = minTile.plus(size().minus(1, 1));
		return tile.x() >= minTile.x() && tile.x() <= max.x() //
				&& tile.y() >= minTile.y() && tile.y() <= max.y();
	}
}
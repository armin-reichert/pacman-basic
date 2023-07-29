/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.checkTileNotNull;

import java.util.*;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;

/**
 * @author Armin Reichert
 */
public final class House {
	private final Vector2i minTile;
	private final Vector2i size;
	private final Door door;
	private final Map<String, Vector2f> seatPositions = new HashMap<>(3);
	private final Vector2f center;

	/**
	 * @param minTile       top-left tile
	 * @param size          size in tiles
	 * @param door          door of this house
	 */
	public House(Vector2i minTile, Vector2i size, Door door) {
		checkTileNotNull(minTile);
		checkNotNull(size);
		checkNotNull(door);
		if (size.x() < 1 || size.y() < 1) {
			throw new IllegalArgumentException("House size must be larger than one square tile but is: " + size);
		}
		this.minTile = minTile;
		this.size = size;
		this.center = minTile.toFloatVec().scaled(TS).plus(size.toFloatVec().scaled(HTS));
		this.door = door;
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

	public void setSeat(String id, Vector2f position) {
		seatPositions.put(id, position);
	}

	public Vector2f getSeat(String id) {
		return seatPositions.get(id);
	}

	public Vector2f center() {
		return center;
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
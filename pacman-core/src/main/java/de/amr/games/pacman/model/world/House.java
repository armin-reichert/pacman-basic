/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.checkTileNotNull;

import java.util.List;
import java.util.Objects;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;

/**
 * @author Armin Reichert
 */
public final class House {
	private final Vector2i topLeftTile;
	private final Vector2i size;
	private final Door door;
	private final List<Vector2f> seatPositions;
	private final Vector2f center;

	public House(Vector2i topLeftTile, Vector2i size, Door door, List<Vector2f> seatPositions) {
		checkTileNotNull(topLeftTile);
		checkNotNull(size);
		checkNotNull(door);
		checkNotNull(seatPositions);
		if (seatPositions.size() != 3) {
			throw new IllegalArgumentException("There must be exactly 3 seat positions");
		}
		if (seatPositions.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("House seat position must not be null");
		}
		this.topLeftTile = topLeftTile;
		this.size = size;
		this.center = topLeftTile.toFloatVec().scaled(TS).plus(size.toFloatVec().scaled(HTS));
		this.door = door;
		this.seatPositions = seatPositions;

	}

	public Vector2i topLeftTile() {
		return topLeftTile;
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
		Vector2i bottomRightTileOutside = topLeftTile.plus(size());
		return tile.x() >= topLeftTile.x() && tile.x() < bottomRightTileOutside.x() //
				&& tile.y() >= topLeftTile.y() && tile.y() < bottomRightTileOutside.y();
	}
}
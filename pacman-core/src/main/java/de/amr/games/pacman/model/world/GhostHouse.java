package de.amr.games.pacman.model.world;

import java.util.List;

import de.amr.games.pacman.lib.V2i;

/**
 * Ghost house interface.
 * 
 * @author Armin Reichert
 */
public interface GhostHouse {

	V2i topLeftTile();

	default boolean contains(V2i tile) {
		V2i bottomRightTile = topLeftTile().plus(numTilesX(), numTilesY());
		return tile.x >= topLeftTile().x && tile.x <= bottomRightTile.x //
				&& tile.y >= topLeftTile().y && tile.y <= bottomRightTile.y;
	}

	int numTilesX();

	int numTilesY();

	V2i seat(int index);

	V2i entryTile();

	List<V2i> doorTiles();
}
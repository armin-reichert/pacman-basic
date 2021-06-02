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

	V2i bottomRightTile();

	default boolean contains(V2i tile) {
		return tile.x >= topLeftTile().x && tile.x <= bottomRightTile().x //
				&& tile.y >= topLeftTile().y && tile.y <= bottomRightTile().y;
	}

	default int numTilesX() {
		return bottomRightTile().x - topLeftTile().x;
	}

	default int numTilesY() {
		return bottomRightTile().y - topLeftTile().y;
	}

	V2i seat(int index);

	V2i entryTile();

	List<V2i> doorTiles();
}
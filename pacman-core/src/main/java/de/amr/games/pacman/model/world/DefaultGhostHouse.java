package de.amr.games.pacman.model.world;

import java.util.List;

import de.amr.games.pacman.lib.V2i;

/**
 * Default implementation of ghost house interface.
 * 
 * @author Armin Reichert
 */
public class DefaultGhostHouse implements GhostHouse {

	public final V2i topLeftTile;
	public final V2i size;
	public List<V2i> seats;
	public V2i entryTile;
	public List<V2i> doorTiles;

	public DefaultGhostHouse(V2i topLeftTile, V2i size) {
		this.topLeftTile = topLeftTile;
		this.size = size;
	}

	@Override
	public int numTilesX() {
		return size.x;
	}

	@Override
	public int numTilesY() {
		return size.y;
	}

	@Override
	public V2i topLeftTile() {
		return topLeftTile;
	}

	@Override
	public V2i seat(int index) {
		return seats.get(index);
	}

	@Override
	public V2i entryTile() {
		return entryTile;
	}

	@Override
	public List<V2i> doorTiles() {
		return doorTiles;
	}
}
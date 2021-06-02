package de.amr.games.pacman.model.world;

import java.util.List;

import de.amr.games.pacman.lib.V2i;

/**
 * Default implementation of ghost house interface.
 * 
 * @author Armin Reichert
 */
public class DefaultGhostHouse implements GhostHouse {

	public V2i topLeftTile;
	public V2i bottomRightTile;
	public List<V2i> seats;
	public V2i entryTile;
	public List<V2i> doorTiles;

	@Override
	public V2i topLeftTile() {
		return topLeftTile;
	}

	@Override
	public V2i bottomRightTile() {
		return bottomRightTile;
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
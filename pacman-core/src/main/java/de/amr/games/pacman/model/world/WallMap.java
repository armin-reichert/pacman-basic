package de.amr.games.pacman.model.world;

public interface WallMap {

	/**
	 * Resultion of this wall map. Each tile is divided into this number of blocks horizontally and
	 * vertically.
	 * 
	 * @return resolution of this map
	 */
	int resolution();

	/**
	 * @return boolean array indicating where a wall should be placed
	 */
	boolean[][] wallInfo();

}
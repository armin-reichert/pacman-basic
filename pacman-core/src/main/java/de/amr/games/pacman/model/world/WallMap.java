package de.amr.games.pacman.model.world;

/**
 * Provides information about the location of walls around inaccessible areas in a world map.
 * 
 * @author Armin Reichert
 */
public interface WallMap {

	public static final byte EMPTY = 0;
	public static final byte CORNER = 1;
	public static final byte HORIZONTAL = 2;
	public static final byte VERTICAL = 3;

	/**
	 * Resolution of this wall map. Each tile is divided into this number of blocks horizontally and
	 * vertically.
	 * 
	 * @return resolution of this map
	 */
	int resolution();

	/**
	 * @return boolean array of size {@code resolution * world.numRows() x resolution * world.numCols()}
	 *         indicating where a wall should be placed
	 */
	byte[][] wallInfo();
}
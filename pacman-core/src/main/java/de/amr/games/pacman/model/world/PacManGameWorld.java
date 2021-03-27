package de.amr.games.pacman.model.world;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;

/**
 * Interface of the world as seen by game controller.
 * 
 * @author Armin Reichert
 */
public interface PacManGameWorld {

	/** Tile size in pixels. */
	public static final int TS = 8;

	/** Half tile size in pixels. */
	public static final int HTS = 4;

	/** Pixels corresponding to the given number of tiles. */
	public static int t(int numTiles) {
		return numTiles * TS;
	}

	/** Tile position of a given pixel position. */
	public static V2i tile(V2d position) {
		return new V2i((int) position.x / TS, (int) position.y / TS);
	}

	/** Tile offset of a given pixel position. */
	public static V2d offset(V2d position) {
		return new V2d(position.x - (int) (position.x / TS) * TS, position.y - (int) (position.y / TS) * TS);
	}

	/**
	 * @return Number of tiles in horizontal direction.
	 */
	int numCols();

	/**
	 * @return Number of tiles in vertical direction.
	 */
	int numRows();

	/**
	 * @return Stream over all tiles in the world from top to bottom left-to-right.
	 */
	default Stream<V2i> tiles() {
		return IntStream.range(0, numCols() * numRows()).mapToObj(index -> new V2i(index % numCols(), index / numCols()));
	}

	/**
	 * @param tile a tile
	 * @return Index of the tile in order top-to-bottom, left-to-right.
	 */
	default int index(V2i tile) {
		return numCols() * tile.y + tile.x;
	}

	/**
	 * @param tile a tile
	 * @return tells if this tile is located inside the map area
	 */
	default boolean insideMap(V2i tile) {
		return 0 <= tile.x && tile.x < numCols() && 0 <= tile.y && tile.y < numRows();
	}

	/**
	 * @return Pac-Man's home position
	 */
	V2i pacHome();

	/**
	 * @param ghost a ghost
	 * @return ghost's home position
	 */
	V2i ghostHome(int ghost);

	/**
	 * @param ghost a ghost
	 * @return ghost scattering target (outside of the accessible maze)
	 */
	V2i ghostScatterTile(int ghost);

	/**
	 * @return ghost house entry (left one of the two tiles above the doors)
	 */
	V2i houseEntry();

	/**
	 * @return middle position inside the house
	 */
	V2i houseSeatCenter();

	/**
	 * @return left position inside the house
	 */
	V2i houseSeatLeft();

	/**
	 * @return right position inside the house
	 */
	V2i houseSeatRight();

	/**
	 * @return number of portals tiles
	 */
	int numPortals();

	/**
	 * @param i index
	 * @return i'th portal tile at the left edge of the world
	 */
	V2i portalLeft(int i);

	/**
	 * @param i index
	 * @return i'th portal tile at the right edge of the world
	 */
	V2i portalRight(int i);

	/**
	 * @param tile a tile
	 * @return tells if the tile is a portal
	 */
	boolean isPortal(V2i tile);

	/**
	 * @param tile a tile
	 * @return tells if the tile can only be access from above
	 */
	boolean isUpwardsBlocked(V2i tile);

	/**
	 * @param tile a tile
	 * @return tells if the tile is an intersection (waypoint)
	 */
	boolean isIntersection(V2i tile);

	/**
	 * @param tile a tile
	 * @return tells if the tile is a wall
	 */
	boolean isWall(V2i tile);

	/**
	 * @param tile a tile
	 * @return tells if the tile is part of a tunnel
	 */
	boolean isTunnel(V2i tile);

	/**
	 * @param tile a tile
	 * @return tells if the tile contains a ghosthouse door
	 */
	boolean isGhostHouseDoor(V2i tile);

	/**
	 * @param tile a tile
	 * @return tells if the tile may contain food (not if it currently contains food!)
	 */
	boolean isFoodTile(V2i tile);

	/**
	 * @param tile a tile
	 * @return tells if the tile may contain an energizer
	 */
	boolean isEnergizerTile(V2i tile);

	/**
	 * @return all energizer tiles in the world
	 */
	Stream<V2i> energizerTiles();
}
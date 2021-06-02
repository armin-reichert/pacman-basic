package de.amr.games.pacman.model.world;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;

/**
 * Interface for accessing the game world.
 * 
 * @author Armin Reichert
 */
public interface PacManGameWorld {

	/** Default world width in number of tiles. */
	public static final int DEFAULT_WIDTH = 28;

	/** Default world height in number of tiles. */
	public static final int DEFAULT_HEIGHT = 36;

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
	 * 
	 * @return optional map of the world
	 */
	Optional<WorldMap> getMap();

	/**
	 * @return all tiles in the world in order top to bottom, left-to-right.
	 */
	default Stream<V2i> tiles() {
		int w = numCols(), h = numRows();
		return IntStream.range(0, w * h).mapToObj(i -> new V2i(i % w, i / w));
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
	 * @return tells if this tile is located inside the world bounds
	 */
	default boolean insideWorld(V2i tile) {
		return 0 <= tile.x && tile.x < numCols() && 0 <= tile.y && tile.y < numRows();
	}

	/**
	 * @return the player's home tile
	 */
	V2i playerHomeTile();

	/**
	 * @param ghost a ghost
	 * @return ghost's home tile
	 */
	V2i ghostHomeTile(int ghost);

	/**
	 * @param ghost a ghost
	 * @return ghost scattering target tile (an inaccessible tile)
	 */
	V2i ghostScatterTile(int ghost);

	/**
	 * @return player start direction
	 */
	Direction playerStartDirection();

	/**
	 * @param ghostID ghost ID (0-3)
	 * @return ghost start direction
	 */
	Direction ghostStartDirection(int ghostID);

	/**
	 * @return list of portals from north to south.
	 */
	List<Portal> portals();

	/**
	 * @param tile a tile
	 * @return tells if the tile is a portal
	 */
	boolean isPortal(V2i tile);

	/**
	 * @param tile a tile
	 * @return tells if the tile can only be traversed from top to bottom
	 */
	boolean isOneWayDown(V2i tile);

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
	 * @return the ghost house in this world
	 */
	GhostHouse ghostHouse();

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

	/**
	 * @return bonus location in case this is fixed
	 */
	V2i bonusTile();

	/**
	 * 
	 * @param bonusIndex first or second bonus (index 0 or 1)
	 * @return number of pellets to eat for earning bonus
	 */
	int pelletsToEatForBonus(int bonusIndex);
}
package de.amr.games.pacman.game.core;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;

/**
 * Interface of world as seen by game.
 * 
 * @author Armin Reichert
 */
public interface PacManGameWorld {

	public static final byte SPACE = 0, WALL = 1, PILL = 2, ENERGIZER = 3, DOOR = 4, TUNNEL = 5;

	public static final byte TS = 8, HTS = 4;

	public static int t(int nTiles) {
		return nTiles * TS;
	}

	public static V2i tile(V2f position) {
		return new V2i((int) position.x / TS, (int) position.y / TS);
	}

	public static V2f offset(V2f position) {
		return new V2f(position.x - (int) (position.x / TS) * TS, position.y - (int) (position.y / TS) * TS);
	}

	PacManGameLevel createLevel(int levelNumber);

	int xTiles();

	int yTiles();

	default int tileIndex(int x, int y) {
		return xTiles() * y + x;
	}

	boolean inMapRange(int x, int y);

	String pacName();

	Direction pacStartDirection();

	V2i pacHome();

	String ghostName(int ghost);

	Direction ghostStartDirection(int ghost);

	V2i ghostHome(int ghost);

	V2i ghostScatterTile(int ghost);

	V2i houseEntry();

	V2i houseCenter();

	V2i houseLeft();

	V2i houseRight();

	int numPortals();

	V2i portalLeft(int i);

	V2i portalRight(int i);

	boolean isAccessible(int x, int y);

	default boolean isAccessible(V2i tile) {
		return isAccessible(tile.x, tile.y);
	}

	boolean isPortal(int x, int y);

	default boolean isPortal(V2i tile) {
		return isPortal(tile.x, tile.y);
	}

	boolean isIntersection(int x, int y);

	default boolean isIntersection(V2i tile) {
		return isIntersection(tile.x, tile.y);
	}

	boolean isTunnel(int x, int y);

	default boolean isTunnel(V2i tile) {
		return isTunnel(tile.x, tile.y);
	}

	boolean isUpwardsBlocked(int x, int y);

	default boolean isUpwardsBlocked(V2i tile) {
		return isUpwardsBlocked(tile.x, tile.y);
	}

	boolean isGhostHouseDoor(int x, int y);

	default boolean isGhostHouseDoor(V2i tile) {
		return isGhostHouseDoor(tile.x, tile.y);
	}

	byte mapData(int x, int y);

	boolean isFoodTile(int x, int y);

	default boolean isFoodTile(V2i tile) {
		return isFoodTile(tile.x, tile.y);
	}

	boolean isEnergizerTile(int x, int y);

	default boolean isEnergizerTile(V2i tile) {
		return isEnergizerTile(tile.x, tile.y);
	}
}
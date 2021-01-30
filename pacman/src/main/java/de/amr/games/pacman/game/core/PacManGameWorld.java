package de.amr.games.pacman.game.core;

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

	default int tileIndex(V2i tile) {
		return xTiles() * tile.y + tile.x;
	}

	boolean inMapRange(V2i tile);

	V2i pacHome();

	V2i ghostHome(int ghost);

	V2i ghostScatterTile(int ghost);

	V2i houseEntry();

	V2i houseCenter();

	V2i houseLeft();

	V2i houseRight();

	int numPortals();

	V2i portalLeft(int i);

	V2i portalRight(int i);

	boolean isAccessible(V2i tile);

	boolean isPortal(V2i tile);

	boolean isIntersection(V2i tile);

	boolean isTunnel(V2i tile);

	boolean isUpwardsBlocked(V2i tile);

	boolean isGhostHouseDoor(V2i tile);

	byte data(V2i tile);

	boolean isFoodTile(V2i tile);

	boolean isEnergizerTile(V2i tile);
}
package de.amr.games.pacman.worlds;

import de.amr.games.pacman.lib.V2i;

/**
 * Interface of world as seen by game.
 * 
 * @author Armin Reichert
 */
public interface PacManGameWorld {

	static final int TS = 8, HTS = TS / 2;

	static int t(int nTiles) {
		return nTiles * TS;
	}

	static final byte BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3;

	static final byte SPACE = 0, WALL = 1, FOOD = 2;

	V2i size();

	default boolean inMapRange(int x, int y) {
		V2i size = size();
		return 0 <= x && x < size.x && 0 <= y && y < size.y;
	}

	V2i portalLeft();

	V2i portalRight();

	V2i pacManHome();

	V2i scatterTileTopLeft();

	V2i scatterTileTopRight();

	V2i scatterTileBottomLeft();

	V2i scatterTileBottomRight();

	V2i houseEntry();

	V2i houseCenter();

	V2i houseLeft();

	V2i houseRight();

	V2i bonusTile();

	boolean isWall(int x, int y);

	boolean isAccessible(int x, int y);

	boolean isPortal(int x, int y);

	boolean isIntersection(int x, int y);

	boolean isTunnel(int x, int y);

	boolean isUpwardsBlocked(int x, int y);

	boolean isGhostHouseDoor(int x, int y);

	int totalFoodCount();

	int foodRemaining();

	default int eatenFoodCount() {
		return totalFoodCount() - foodRemaining();
	}

	void restoreFood();

	void removeFood(int x, int y);

	boolean foodRemoved(int x, int y);

	boolean isFoodTile(int x, int y);

	boolean isEnergizerTile(int x, int y);

	default boolean containsFood(int x, int y) {
		return isFoodTile(x, y) && !foodRemoved(x, y);
	}
}
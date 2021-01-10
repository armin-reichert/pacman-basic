package de.amr.games.pacman.worlds;

import de.amr.games.pacman.core.PacManGameLevel;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Interface of world as seen by game.
 * 
 * @author Armin Reichert
 */
public interface PacManGameWorld {

	public static final byte SPACE = 0, WALL = 1, FOOD = 2;

	public static final int TS = 8, HTS = 4;

	public static int t(int nTiles) {
		return nTiles * TS;
	}

	PacManGameLevel level(int levelNumber);

	V2i sizeInTiles();

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

	V2i bonusTile();

	int numPortals();

	V2i portalLeft(int i);

	V2i portalRight(int i);

	boolean isWall(int x, int y);

	boolean isAccessible(int x, int y);

	boolean isPortal(int x, int y);

	boolean isIntersection(int x, int y);

	boolean isTunnel(int x, int y);

	boolean isUpwardsBlocked(int x, int y);

	boolean isGhostHouseDoor(int x, int y);

	int totalFoodCount();

	int foodRemaining();

	int eatenFoodCount();

	void restoreFood();

	void removeFood(int x, int y);

	boolean foodRemoved(int x, int y);

	boolean isFoodTile(int x, int y);

	boolean isEnergizerTile(int x, int y);

	boolean containsFood(int x, int y);
}
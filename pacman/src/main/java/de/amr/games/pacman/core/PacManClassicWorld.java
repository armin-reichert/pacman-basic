package de.amr.games.pacman.core;

import java.util.BitSet;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.MapReader;
import de.amr.games.pacman.lib.V2i;

/**
 * The game world used by the classic Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManClassicWorld implements PacManGameWorld {

	private final byte[][] map;
	private final V2i size = new V2i(28, 36);
	private final V2i portalLeft = new V2i(-1, 17);
	private final V2i portalRight = new V2i(28, 17);
	private final V2i pacManHome = new V2i(13, 26);
	private final V2i scatterTileTopLeft = new V2i(2, 0);
	private final V2i scatterTileTopRight = new V2i(25, 0);
	private final V2i scatterTileBottomLeft = new V2i(0, 35);
	private final V2i scatterTileBottomRight = new V2i(27, 35);
	private final V2i houseEntry = new V2i(13, 14);
	private final V2i houseCenter = new V2i(13, 17);
	private final V2i houseLeft = new V2i(11, 17);
	private final V2i houseRight = new V2i(15, 17);
	private final V2i bonusTile = new V2i(13, 20);
	private final int totalFoodCount;
	private final BitSet eaten = new BitSet();

	private int foodRemaining;

	public PacManClassicWorld() {
		map = MapReader.readMap("/worlds/pacMan_classic/map.txt");
		int food = 0;
		for (int x = 0; x < size.x; ++x) {
			for (int y = 0; y < size.y; ++y) {
				if (map[y][x] == FOOD) {
					++food;
				}
			}
		}
		totalFoodCount = food;
		foodRemaining = totalFoodCount;
	}

	@Override
	public V2i size() {
		return size;
	}

	@Override
	public V2i portalLeft() {
		return portalLeft;
	}

	@Override
	public V2i portalRight() {
		return portalRight;
	}

	@Override
	public V2i pacManHome() {
		return pacManHome;
	}

	@Override
	public V2i scatterTileTopLeft() {
		return scatterTileTopLeft;
	}

	@Override
	public V2i scatterTileTopRight() {
		return scatterTileTopRight;
	}

	@Override
	public V2i scatterTileBottomLeft() {
		return scatterTileBottomLeft;
	}

	@Override
	public V2i scatterTileBottomRight() {
		return scatterTileBottomRight;
	}

	@Override
	public V2i houseEntry() {
		return houseEntry;
	}

	@Override
	public V2i houseCenter() {
		return houseCenter;
	}

	@Override
	public V2i houseLeft() {
		return houseLeft;
	}

	@Override
	public V2i houseRight() {
		return houseRight;
	}

	@Override
	public V2i bonusTile() {
		return bonusTile;
	}

	@Override
	public int totalFoodCount() {
		return totalFoodCount;
	}

	@Override
	public int foodRemaining() {
		return foodRemaining;
	}

	private int tileIndex(int x, int y) {
		return y * size.x + x;
	}

	private boolean isTile(int x, int y, int xx, int yy) {
		return x == xx && y == yy;
	}

	private byte map(int x, int y) {
		return map[y][x];
	}

	@Override
	public boolean isWall(int x, int y) {
		return inMapRange(x, y) && map(x, y) == WALL;
	}

	@Override
	public boolean isGhostHouseDoor(int x, int y) {
		return isTile(x, y, 13, 15) || isTile(x, y, 14, 15);
	}

	public boolean isInsideGhostHouse(int x, int y) {
		return x >= 10 && x <= 17 && y >= 15 && y <= 22;
	}

	@Override
	public boolean isTunnel(int x, int y) {
		return y == 17 && (x <= 5 || x >= 21);
	}

	@Override
	public boolean isUpwardsBlocked(int x, int y) {
		return isTile(x, y, 12, 13) || isTile(x, y, 15, 13) || isTile(x, y, 12, 25) || isTile(x, y, 15, 25);
	}

	@Override
	public boolean isFoodTile(int x, int y) {
		return inMapRange(x, y) && map(x, y) == FOOD;
	}

	@Override
	public boolean isEnergizerTile(int x, int y) {
		return isTile(x, y, 1, 6) || isTile(x, y, 26, 6) || isTile(x, y, 1, 26) || isTile(x, y, 26, 26);
	}

	@Override
	public boolean isIntersection(int x, int y) {
		if (isInsideGhostHouse(x, y) || isGhostHouseDoor(x, y + 1)) {
			return false;
		}
		return Stream.of(Direction.values()).filter(dir -> isAccessible(x + dir.vec.x, y + dir.vec.y)).count() >= 3;
	}

	@Override
	public boolean isAccessible(int x, int y) {
		return !isWall(x, y) || isPortal(x, y);
	}

	@Override
	public boolean isPortal(int x, int y) {
		return isTile(x, y, portalLeft.x, portalLeft.y) || isTile(x, y, portalRight.x, portalRight.y);
	}

	@Override
	public boolean foodRemoved(int x, int y) {
		return eaten.get(tileIndex(x, y));
	}

	@Override
	public void removeFood(int x, int y) {
		if (!foodRemoved(x, y)) {
			eaten.set(tileIndex(x, y));
			--foodRemaining;
		}
	}

	@Override
	public void restoreFood() {
		eaten.clear();
		foodRemaining = totalFoodCount;
	}
}
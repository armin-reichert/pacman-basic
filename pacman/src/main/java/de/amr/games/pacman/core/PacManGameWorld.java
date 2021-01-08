package de.amr.games.pacman.core;

import java.util.BitSet;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * The game world.
 * 
 * @author Armin Reichert
 */
public class PacManGameWorld {

	public static final int TS = 8, HTS = TS / 2;

	public static int t(int nTiles) {
		return nTiles * TS;
	}

	public final V2i size = new V2i(28, 36);

	public final V2i portalLeft = new V2i(-1, 17);
	public final V2i portalRight = new V2i(28, 17);

	public final V2i pacManHome = new V2i(13, 26);

	public final V2i scatterTileTopLeft = new V2i(2, 0);
	public final V2i scatterTileTopRight = new V2i(25, 0);
	public final V2i scatterTileBottomLeft = new V2i(0, 35);
	public final V2i scatterTileBottomRight = new V2i(27, 35);

	public final V2i houseEntry = new V2i(13, 14);
	public final V2i houseCenter = new V2i(13, 17);
	public final V2i houseLeft = new V2i(11, 17);
	public final V2i houseRight = new V2i(15, 17);

	public final V2i bonusTile = new V2i(13, 20);

	public final int totalFoodCount;
	public int foodRemaining;
	private final BitSet eaten = new BitSet();

	private final String map =
	//@formatter:off
		"1111111111111111111111111111" +
		"1111111111111111111111111111" +
		"1111111111111111111111111111" +
		"1111111111111111111111111111" +
		"1222222222222112222222222221" +
		"1211112111112112111112111121" +
		"1211112111112112111112111121" +
		"1211112111112112111112111121" +
		"1222222222222222222222222221" +
		"1211112112111111112112111121" +
		"1211112112111111112112111121" +
		"1222222112222112222112222221" +
		"1111112111110110111112111111" +
		"1111112111110110111112111111" +
		"1111112110000000000112111111" +
		"1111112110111001110112111111" +
		"1111112110100000010112111111" +
		"0000002000100000010002000000" +
		"1111112110100000010112111111" +
		"1111112110111111110112111111" +
		"1111112110000000000112111111" +
		"1111112110111111110112111111" +
		"1111112110111111110112111111" +
		"1222222222222112222222222221" +
		"1211112111112112111112111121" +
		"1211112111112112111112111121" +
		"1222112222222002222222112221" +
		"1112112112111111112112112111" +
		"1112112112111111112112112111" +
		"1222222112222112222112222221" +
		"1211111111112112111111111121" +
		"1211111111112112111111111121" +
		"1222222222222222222222222221" +
		"1111111111111111111111111111" +
		"1111111111111111111111111111" +
		"1111111111111111111111111111";
	//@formatter:on

	public PacManGameWorld() {
//		totalFoodCount = (int) map.chars().filter(c -> c == '2').count();
		int food = 0;
		for (int i = 0; i < map.length(); ++i) {
			if (map.charAt(i) == '2') {
				++food;
			}
		}
		totalFoodCount = food;
		foodRemaining = totalFoodCount;
	}

	private int tileIndex(int x, int y) {
		return y * size.x + x;
	}

	private boolean isTile(int x, int y, int xx, int yy) {
		return x == xx && y == yy;
	}

	private char map(int x, int y) {
		return map.charAt(tileIndex(x, y));
	}

	public boolean inMapRange(int x, int y) {
		return 0 <= x && x < size.x && 0 <= y && y < size.y;
	}

	public boolean isWall(int x, int y) {
		return inMapRange(x, y) && map(x, y) == '1';
	}

	public boolean isGhostHouseDoor(int x, int y) {
		return isTile(x, y, 13, 15) || isTile(x, y, 14, 15);
	}

	public boolean isInsideGhostHouse(int x, int y) {
		return x >= 10 && x <= 17 && y >= 15 && y <= 22;
	}

	public boolean isInsideTunnel(int x, int y) {
		return y == 17 && (x <= 5 || x >= 21);
	}

	public boolean isUpwardsBlocked(int x, int y) {
		return isTile(x, y, 12, 13) || isTile(x, y, 15, 13) || isTile(x, y, 12, 25) || isTile(x, y, 15, 25);
	}

	public boolean isFoodTile(int x, int y) {
		return inMapRange(x, y) && map(x, y) == '2';
	}

	public boolean isEnergizerTile(int x, int y) {
		return isTile(x, y, 1, 6) || isTile(x, y, 26, 6) || isTile(x, y, 1, 26) || isTile(x, y, 26, 26);
	}

	public boolean isIntersectionTile(int x, int y) {
		if (isInsideGhostHouse(x, y) || isGhostHouseDoor(x, y + 1)) {
			return false;
		}
		return Stream.of(Direction.values()).filter(dir -> isAccessibleTile(x + dir.vec.x, y + dir.vec.y)).count() >= 3;
	}

	public boolean isAccessibleTile(int x, int y) {
		return !isWall(x, y) || isPortalTile(x, y);
	}

	public boolean isPortalTile(int x, int y) {
		return isTile(x, y, portalLeft.x, portalLeft.y) || isTile(x, y, portalRight.x, portalRight.y);
	}

	public boolean foodRemoved(int x, int y) {
		return eaten.get(tileIndex(x, y));
	}

	public void removeFood(int x, int y) {
		if (!foodRemoved(x, y)) {
			eaten.set(tileIndex(x, y));
			--foodRemaining;
		}
	}

	public void restoreFood() {
		eaten.clear();
		foodRemaining = totalFoodCount;
	}
}
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
public class World {

	public static final int TS = 8, HTS = TS / 2;

	public static int t(int nTiles) {
		return nTiles * TS;
	}

	public final V2i size = new V2i(28, 36);

	public final V2i portalLeft = new V2i(-1, 17);
	public final V2i portalRight = new V2i(size.x, 17);

	public final V2i pacManHome = new V2i(13, 26);

	public final V2i upperRightScatterTile = new V2i(size.x - 3, 0);
	public final V2i upperLeftScatterTile = new V2i(2, 0);
	public final V2i lowerRightScatterTile = new V2i(size.x - 1, size.y - 1);
	public final V2i lowerLeftScatterTile = new V2i(0, size.y - 1);

	public final V2i houseEntry = new V2i(13, 14);
	public final V2i houseCenter = new V2i(13, 17);
	public final V2i houseLeft = new V2i(11, 17);
	public final V2i houseRight = new V2i(15, 17);

	public final int totalFoodCount = 244;
	public int foodRemaining;

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

	private final BitSet eaten = new BitSet(244);

	private int index(int x, int y) {
		return y * size.x + x;
	}

	private boolean is(int x, int y, int xx, int yy) {
		return x == xx & y == yy;
	}

	private char map(int x, int y) {
		return map.charAt(index(x, y));
	}

	public boolean inMapRange(int x, int y) {
		return 0 <= x && x < size.x && 0 <= y && y < size.y;
	}

	public boolean isWall(int x, int y) {
		return inMapRange(x, y) && map(x, y) == '1';
	}

	public boolean isGhostHouseDoor(int x, int y) {
		return is(x, y, 13, 15) || is(x, y, 14, 15);
	}

	public boolean isInsideGhostHouse(int x, int y) {
		return x >= 10 && x <= 17 && y >= 15 && y <= 22;
	}

	public boolean isInsideTunnel(int x, int y) {
		return y == 17 && (x <= 5 || x >= 21);
	}

	public boolean isUpwardsBlocked(int x, int y) {
		return is(x, y, 12, 13) || is(x, y, 15, 13) || is(x, y, 12, 25) || is(x, y, 15, 25);
	}

	public boolean isFoodTile(int x, int y) {
		return inMapRange(x, y) && map(x, y) == '2';
	}

	public boolean isEnergizerTile(int x, int y) {
		return is(x, y, 1, 6) || is(x, y, 26, 6) || is(x, y, 1, 26) || is(x, y, 26, 26);
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
		return is(x, y, portalLeft.x, portalLeft.y) || is(x, y, portalRight.x, portalRight.y);
	}

	public boolean isBonusTile(int x, int y) {
		return is(x, y, 13, 20);
	}

	public boolean foodRemoved(int x, int y) {
		return eaten.get(index(x, y));
	}

	public void removeFood(int x, int y) {
		if (!foodRemoved(x, y)) {
			eaten.set(index(x, y));
			--foodRemaining;
		}
	}

	public void restoreFood() {
		eaten.clear();
		foodRemaining = totalFoodCount;
	}
}
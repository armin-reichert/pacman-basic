package de.amr.games.pacman;

import java.util.BitSet;
import java.util.stream.Stream;

import de.amr.games.pacman.common.Direction;
import de.amr.games.pacman.common.V2i;

public class PacManGameWorld {

	public static final int TS = 8;
	public static final int HTS = TS / 2;

	public static final int WORLD_WIDTH_TILES = 28;
	public static final int WORLD_HEIGHT_TILES = 36;

	public static final V2i PORTAL_LEFT = new V2i(-1, 17);
	public static final V2i PORTAL_RIGHT = new V2i(WORLD_WIDTH_TILES, 17);

	public static final V2i PACMAN_HOME = new V2i(13, 26);

	public static final V2i UPPER_RIGHT_CORNER = new V2i(WORLD_WIDTH_TILES - 3, 0);
	public static final V2i UPPER_LEFT_CORNER = new V2i(2, 0);
	public static final V2i LOWER_RIGHT_CORNER = new V2i(WORLD_WIDTH_TILES - 1, WORLD_HEIGHT_TILES - 1);
	public static final V2i LOWER_LEFT_CORNER = new V2i(0, WORLD_HEIGHT_TILES - 1);

	public static final V2i HOUSE_ENTRY = new V2i(13, 14);
	public static final V2i HOUSE_CENTER = new V2i(13, 17);
	public static final V2i HOUSE_LEFT = new V2i(11, 17);
	public static final V2i HOUSE_RIGHT = new V2i(15, 17);

	public static final int TOTAL_FOOD_COUNT = 244;

	private static int index(int x, int y) {
		return y * WORLD_WIDTH_TILES + x;
	}

	private static boolean is(int x, int y, int xx, int yy) {
		return x == xx & y == yy;
	}

	private final String[] map = {
			//@formatter:off
			"1111111111111111111111111111",
			"1111111111111111111111111111",
			"1111111111111111111111111111",
			"1111111111111111111111111111",
			"1222222222222112222222222221",
			"1211112111112112111112111121",
			"1211112111112112111112111121",
			"1211112111112112111112111121",
			"1222222222222222222222222221",
			"1211112112111111112112111121",
			"1211112112111111112112111121",
			"1222222112222112222112222221",
			"1111112111110110111112111111",
			"1111112111110110111112111111",
			"1111112110000000000112111111",
			"1111112110111001110112111111",
			"1111112110100000010112111111",
			"0000002000100000010002000000",
			"1111112110100000010112111111",
			"1111112110111111110112111111",
			"1111112110000000000112111111",
			"1111112110111111110112111111",
			"1111112110111111110112111111",
			"1222222222222112222222222221",
			"1211112111112112111112111121",
			"1211112111112112111112111121",
			"1222112222222002222222112221",
			"1112112112111111112112112111",
			"1112112112111111112112112111",
			"1222222112222112222112222221",
			"1211111111112112111111111121",
			"1211111111112112111111111121",
			"1222222222222222222222222221",
			"1111111111111111111111111111",
			"1111111111111111111111111111",
			"1111111111111111111111111111",
			//@formatter:on
	};

	private final BitSet eatenFood = new BitSet(244);
	public int foodRemaining;

	private char map(int x, int y) {
		return map[y].charAt(x);
	}

	public boolean inMapRange(int x, int y) {
		return 0 <= x && x < WORLD_WIDTH_TILES && 0 <= y && y < WORLD_HEIGHT_TILES;
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
		return is(x, y, PORTAL_LEFT.x, PORTAL_LEFT.y) || is(x, y, PORTAL_RIGHT.x, PORTAL_RIGHT.y);
	}

	public boolean isBonusTile(int x, int y) {
		return is(x, y, 13, 20);
	}

	public boolean hasEatenFood(int x, int y) {
		return eatenFood.get(index(x, y));
	}

	public void restoreFood() {
		eatenFood.clear();
		foodRemaining = TOTAL_FOOD_COUNT;
	}

	public void eatFood(int x, int y) {
		eatenFood.set(index(x, y));
		--foodRemaining;
	}
}
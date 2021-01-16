package de.amr.games.pacman.world;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Common base of Pac-Man classic and Ms. Pac-man worlds. All maps used in these game variants have
 * the same ghost house structure and location.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractPacManGameWorld implements PacManGameWorld {

	private static byte decode(char c) throws Exception {
		switch (c) {
		case ' ':
			return SPACE;
		case '#':
			return WALL;
		case 'T':
			return TUNNEL;
		case '-':
			return DOOR;
		case '.':
			return PILL;
		case '*':
			return ENERGIZER;
		default:
			throw new Exception(String.format("Unknown map character: '%c'", c));
		}
	}

	private static final V2i HOUSE_ENTRY = new V2i(13, 14);
	private static final V2i HOUSE_LEFT = new V2i(11, 17);
	private static final V2i HOUSE_CENTER = new V2i(13, 17);
	private static final V2i HOUSE_RIGHT = new V2i(15, 17);

	private static final V2i PAC_HOME = new V2i(13, 26);

	private static final V2i[] GHOST_HOME_TILES = { HOUSE_ENTRY, HOUSE_CENTER, HOUSE_LEFT, HOUSE_RIGHT };
	private static final Direction[] GHOST_START_DIRECTIONS = { LEFT, UP, DOWN, DOWN };
	private static final V2i[] GHOST_SCATTER_TILES = { new V2i(25, 0), new V2i(2, 0), new V2i(27, 35), new V2i(27, 35) };

	protected final V2i size = new V2i(28, 36);
	protected final List<V2i> portalsLeft = new ArrayList<>(2);
	protected final List<V2i> portalsRight = new ArrayList<>(2);
	protected final List<V2i> energizerTiles = new ArrayList<>(4);

	protected byte[][] map;
	protected final BitSet eaten = new BitSet();
	protected int totalFoodCount;
	protected int foodRemaining;

	protected int tileIndex(int x, int y) {
		return sizeInTiles().x * y + x;
	}

	protected void loadMap(String path) {
		map = new byte[size.y][size.x];
		try (InputStream is = getClass().getResourceAsStream(path)) {
			if (is == null) {
				throw new RuntimeException("Resource not found: " + path);
			}
			BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
			int y = 0;
			for (String line = rdr.readLine(); line != null; line = rdr.readLine()) {
				if (line.startsWith("!") || line.isBlank()) {
					continue; // skip comments and blank lines
				}
				for (int x = 0; x < size.x; ++x) {
					try {
						map[y][x] = decode(line.charAt(x));
					} catch (Exception cause) {
						throw new RuntimeException(String.format("Error decoding map '%s', line %d, column %d", path, y, x), cause);
					}
				}
				++y;
			}
		} catch (IOException e) {
			throw new RuntimeException(String.format("Error reading map '%s'", path, e));
		}

		// find portals
		portalsLeft.clear();
		portalsRight.clear();
		for (int y = 0; y < size.y; ++y) {
			if (map[y][0] == TUNNEL && map[y][size.x - 1] == TUNNEL) {
				portalsLeft.add(new V2i(-1, y));
				portalsRight.add(new V2i(size.x, y));
			}
		}

		// find food
		energizerTiles.clear();
		totalFoodCount = 0;
		for (int x = 0; x < size.x; ++x) {
			for (int y = 0; y < size.y; ++y) {
				if (map[y][x] == PILL) {
					++totalFoodCount;
				} else if (map[y][x] == ENERGIZER) {
					++totalFoodCount;
					energizerTiles.add(new V2i(x, y));
				}
			}
		}
		eaten.clear();
		foodRemaining = totalFoodCount;
		log("Map '%s' loaded, total food count=%d (%d pellets + %d energizers)", path, totalFoodCount,
				totalFoodCount - energizerTiles.size(), energizerTiles.size());
	}

	@Override
	public V2i sizeInTiles() {
		return size;
	}

	@Override
	public boolean inMapRange(int x, int y) {
		V2i size = sizeInTiles();
		return 0 <= x && x < size.x && 0 <= y && y < size.y;
	}

	@Override
	public V2i pacHome() {
		return PAC_HOME;
	}

	@Override
	public Direction ghostStartDirection(int ghost) {
		return GHOST_START_DIRECTIONS[ghost];
	}

	@Override
	public V2i ghostHome(int ghost) {
		return GHOST_HOME_TILES[ghost];
	}

	@Override
	public V2i ghostScatterTile(int ghost) {
		return GHOST_SCATTER_TILES[ghost];
	}

	@Override
	public int numPortals() {
		return portalsLeft.size();
	}

	@Override
	public V2i portalLeft(int i) {
		return portalsLeft.get(i);
	}

	@Override
	public V2i portalRight(int i) {
		return portalsRight.get(i);
	}

	@Override
	public V2i houseEntry() {
		return HOUSE_ENTRY;
	}

	@Override
	public V2i houseCenter() {
		return HOUSE_CENTER;
	}

	@Override
	public V2i houseLeft() {
		return HOUSE_LEFT;
	}

	@Override
	public V2i houseRight() {
		return HOUSE_RIGHT;
	}

	@Override
	public boolean isAccessible(int x, int y) {
		return !isWall(x, y) || isPortal(x, y);
	}

	@Override
	public boolean isWall(int x, int y) {
		return inMapRange(x, y) && map[y][x] == WALL;
	}

	@Override
	public boolean isTunnel(int x, int y) {
		return inMapRange(x, y) && map[y][x] == TUNNEL;
	}

	@Override
	public boolean isGhostHouseDoor(int x, int y) {
		return inMapRange(x, y) && map[y][x] == DOOR;
	}

	@Override
	public boolean isPortal(int x, int y) {
		V2i tile = new V2i(x, y);
		return portalsLeft.contains(tile) || portalsRight.contains(tile);
	}

	private boolean isInsideGhostHouse(int x, int y) {
		return x >= 10 && x <= 17 && y >= 15 && y <= 22;
	}

	@Override
	public boolean isIntersection(int x, int y) {
		if (isInsideGhostHouse(x, y) || isGhostHouseDoor(x, y + 1)) {
			return false;
		}
		return Stream.of(Direction.values()).filter(dir -> isAccessible(x + dir.vec.x, y + dir.vec.y)).count() >= 3;
	}

	@Override
	public int totalFoodCount() {
		return totalFoodCount;
	}

	@Override
	public int foodRemaining() {
		return foodRemaining;
	}

	@Override
	public int eatenFoodCount() {
		return totalFoodCount - foodRemaining;
	}

	@Override
	public boolean isFoodTile(int x, int y) {
		return inMapRange(x, y) && (map[y][x] == PILL || map[y][x] == ENERGIZER);
	}

	@Override
	public boolean isEnergizerTile(int x, int y) {
		return energizerTiles.contains(new V2i(x, y));
	}

	@Override
	public boolean isFoodRemoved(int x, int y) {
		return eaten.get(tileIndex(x, y));
	}

	@Override
	public boolean containsFood(int x, int y) {
		return isFoodTile(x, y) && !isFoodRemoved(x, y);
	}

	@Override
	public void removeFood(int x, int y) {
		if (!isFoodRemoved(x, y)) {
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
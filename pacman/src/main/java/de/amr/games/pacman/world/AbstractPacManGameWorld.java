package de.amr.games.pacman.world;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import de.amr.games.pacman.lib.V2i;

public abstract class AbstractPacManGameWorld implements PacManGameWorld {

	private static byte decode(char c) {
		switch (c) {
		case ' ':
			return SPACE;
		case '#':
			return WALL;
		case '-':
			return DOOR;
		case '.':
			return PILL;
		case '*':
			return ENERGIZER;
		default:
			throw new IllegalArgumentException("Unknown map character: " + c);
		}
	}

	protected byte[][] map;
	protected V2i size = new V2i(28, 36);
	protected List<V2i> portalsLeft = new ArrayList<>(2);
	protected List<V2i> portalsRight = new ArrayList<>(2);
	protected List<V2i> energizerTiles = new ArrayList<>(4);

	protected BitSet eaten = new BitSet();
	protected int totalFoodCount;
	protected int foodRemaining;

	protected int tileIndex(int x, int y) {
		return sizeInTiles().x * y + x;
	}

	protected boolean isTile(int x, int y, int xx, int yy) {
		return x == xx && y == yy;
	}

	protected void loadMap(String path) {
		map = new byte[size.y][size.x];
		try (InputStream is = getClass().getResourceAsStream(path)) {
			if (is == null) {
				throw new RuntimeException("Resource not found: " + path);
			}
			BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
			String line = rdr.readLine();
			int y = 0;
			while (line != null) {
				if (line.startsWith("!") || line.isBlank()) {
					// skip comments and blank lines
				} else {
					for (int x = 0; x < size.x; ++x) {
						map[y][x] = decode(line.charAt(x));
					}
					++y;
				}
				line = rdr.readLine();
			}
		} catch (Exception x) {
			throw new RuntimeException("Error reading map from path " + path, x);
		}
		findPortals();
		findFoodTiles();
	}

	protected void findPortals() {
		portalsLeft.clear();
		portalsRight.clear();
		for (int y = 0; y < size.y; ++y) {
			if (map[y][0] == SPACE && map[y][size.x - 1] == SPACE) {
				portalsLeft.add(new V2i(0, y));
				portalsRight.add(new V2i(size.x - 1, y));
			}
		}
	}

	protected void findFoodTiles() {
		energizerTiles.clear();
		int food = 0;
		for (int x = 0; x < size.x; ++x) {
			for (int y = 0; y < size.y; ++y) {
				if (map[y][x] == PILL) {
					++food;
				} else if (map[y][x] == ENERGIZER) {
					++food;
					energizerTiles.add(new V2i(x, y));
				}
			}
		}
		totalFoodCount = food;
		restoreFood();
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
	public int totalFoodCount() {
		return totalFoodCount;
	}

	@Override
	public int foodRemaining() {
		return foodRemaining;
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
	public boolean isAccessible(int x, int y) {
		return !isWall(x, y) || isPortal(x, y);
	}

	@Override
	public boolean isWall(int x, int y) {
		return inMapRange(x, y) && map[y][x] == WALL;
	}

	@Override
	public boolean isGhostHouseDoor(int x, int y) {
		return inMapRange(x, y) && map[y][x] == DOOR;
	}

	@Override
	public boolean isPortal(int x, int y) {
		for (int i = 0; i < numPortals(); ++i) {
			if (isTile(x, y, portalsLeft.get(i).x, portalsLeft.get(i).y)
					|| isTile(x, y, portalsRight.get(i).x, portalsRight.get(i).y)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int eatenFoodCount() {
		return totalFoodCount() - foodRemaining();
	}

	@Override
	public boolean containsFood(int x, int y) {
		return isFoodTile(x, y) && !foodRemoved(x, y);
	}

}
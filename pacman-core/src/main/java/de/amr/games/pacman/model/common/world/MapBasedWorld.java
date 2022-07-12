/*
MIT License

Copyright (c) 2022 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package de.amr.games.pacman.model.common.world;

import static de.amr.games.pacman.lib.V2i.v;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;

/**
 * @author Armin Reichert
 */
public abstract class MapBasedWorld implements World {

	//@formatter:off
	public static final byte SPACE           = 0;
	public static final byte WALL            = 1;
	public static final byte TUNNEL          = 2;
	public static final byte PELLET          = 3;
	public static final byte ENERGIZER       = 4;
	public static final byte PELLET_EATEN    = 5;
	public static final byte ENERGIZER_EATEN = 6;
	//@formatter:on

	protected static byte[][] copyArray2D(byte[][] arr) {
		return Arrays.stream(arr).map(byte[]::clone).toArray(byte[][]::new);
	}

	protected final V2i size;
	protected final byte[][] map;
	protected final List<Portal> portals;
	protected final List<V2i> energizerTiles;
	protected final int totalFoodCount;
	protected int foodRemaining;

	protected MapBasedWorld(byte[][] mapData, int sizeX, int sizeY) {
		map = copyArray2D(mapData);
		size = v(sizeX, sizeY);
		energizerTiles = tiles().filter(this::isEnergizerTile).toList();
		totalFoodCount = (int) tiles().filter(this::isFoodTile).count();
		foodRemaining = totalFoodCount;
		portals = findPortals();
	}

	protected ArrayList<Portal> findPortals() {
		var portalList = new ArrayList<Portal>();
		for (int row = 0; row < size.y; ++row) {
			if (map[row][0] == TUNNEL && map[row][size.x - 1] == TUNNEL) {
				portalList.add(new HorizontalPortal(v(0, row), v(size.x - 1, row)));
			}
		}
		portalList.trimToSize();
		return portalList;
	}

	protected byte content(V2i tile) {
		return insideMap(tile) ? map[tile.y][tile.x] : SPACE;
	}

	@Override
	public int numCols() {
		return size.x;
	}

	@Override
	public int numRows() {
		return size.y;
	}

	@Override
	public List<Portal> portals() {
		return portals;
	}

	@Override
	public boolean belongsToPortal(V2i tile) {
		return portals.stream().anyMatch(portal -> portal.contains(tile));
	}

	@Override
	public boolean isWall(V2i tile) {
		return content(tile) == WALL;
	}

	@Override
	public boolean isTunnel(V2i tile) {
		return content(tile) == TUNNEL;
	}

	@Override
	public boolean isFoodTile(V2i tile) {
		byte data = content(tile);
		return data == PELLET || data == PELLET_EATEN || data == ENERGIZER || data == ENERGIZER_EATEN;
	}

	@Override
	public boolean isEnergizerTile(V2i tile) {
		byte data = content(tile);
		return data == ENERGIZER || data == ENERGIZER_EATEN;
	}

	@Override
	public Stream<V2i> energizerTiles() {
		return energizerTiles.stream();
	}

	@Override
	public void removeFood(V2i tile) {
		byte data = content(tile);
		if (data == ENERGIZER) {
			map[tile.y][tile.x] = ENERGIZER_EATEN;
			--foodRemaining;
		} else if (data == PELLET) {
			map[tile.y][tile.x] = PELLET_EATEN;
			--foodRemaining;
		}
	}

	@Override
	public boolean containsFood(V2i tile) {
		byte data = content(tile);
		return data == PELLET || data == ENERGIZER;
	}

	@Override
	public boolean containsEatenFood(V2i tile) {
		byte data = content(tile);
		return data == PELLET_EATEN || data == ENERGIZER_EATEN;
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
	public void resetFood() {
		for (int row = 0; row < size.y; ++row) {
			for (int col = 0; col < size.x; ++col) {
				if (map[row][col] == PELLET_EATEN) {
					map[row][col] = PELLET;
				} else if (map[row][col] == ENERGIZER_EATEN) {
					map[row][col] = ENERGIZER;
				}
			}
		}
		foodRemaining = totalFoodCount;
	}
}
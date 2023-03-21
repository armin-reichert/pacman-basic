/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import static de.amr.games.pacman.lib.math.Vector2i.v2i;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.math.Vector2i;

/**
 * @author Armin Reichert
 */
public abstract class TileMapWorld implements World {

	//@formatter:off
	private static final byte TILE_SPACE           = 0;
	private static final byte TILE_WALL            = 1;
	private static final byte TILE_TUNNEL          = 2;
	private static final byte TILE_PELLET          = 3;
	private static final byte TILE_ENERGIZER       = 4;
	//@formatter:on

	private final byte[][] tileMap;
	protected List<Portal> portals;
	protected List<Vector2i> energizerTiles;
	protected int totalFoodCount;
	protected int foodRemaining;
	private final BitSet eatenSet;

	protected TileMapWorld(byte[][] tileMap) {
		this.tileMap = validateTileMapData(tileMap);
		energizerTiles = tiles().filter(this::isEnergizerTile).toList();
		totalFoodCount = (int) tiles().filter(this::isFoodTile).count();
		foodRemaining = totalFoodCount;
		portals = findPortals();
		eatenSet = new BitSet(numRows() * numCols());
	}

	private byte[][] validateTileMapData(byte[][] data) {
		if (data == null) {
			throw new IllegalArgumentException("Map is null");
		}
		if (data.length == 0) {
			throw new IllegalArgumentException("Map has no rows");
		}
		var firstRow = data[0];
		if (firstRow.length == 0) {
			throw new IllegalArgumentException("Map has no columns");
		}
		for (int i = 0; i < data.length; ++i) {
			if (data[i].length != firstRow.length) {
				throw new IllegalArgumentException("Map has differently sized rows");
			}
		}
		for (int i = 0; i < data.length; ++i) {
			for (int j = 0; j < firstRow.length; ++j) {
				byte content = data[i][j];
				if (content < TILE_SPACE || content > TILE_ENERGIZER) {
					throw new IllegalArgumentException(
							"Map has invalid content '%s' at row %d column %d".formatted(content, i, j));
				}
			}
		}
		return data;
	}

	protected ArrayList<Portal> findPortals() {
		var portalList = new ArrayList<Portal>();
		for (int row = 0; row < numRows(); ++row) {
			if (content(row, 0) == TILE_TUNNEL && content(row, numCols() - 1) == TILE_TUNNEL) {
				portalList.add(new HorizontalPortal(v2i(0, row), v2i(numCols() - 1, row)));
			}
		}
		portalList.trimToSize();
		return portalList;
	}

	/**
	 * @param tile some tile (may be outside world bound)
	 * @return the content at the given tile or empty space if outside world
	 */
	protected byte content(Vector2i tile) {
		return content(tile.y(), tile.x(), TILE_SPACE);
	}

	protected byte content(int row, int col) {
		if (!insideBounds(row, col)) {
			throwOutOfBoundsError(row, col);
		}
		return tileMap[row][col];
	}

	protected byte content(int row, int col, byte defaultContent) {
		if (!insideBounds(row, col)) {
			return defaultContent;
		}
		return tileMap[row][col];
	}

	protected boolean insideBounds(int row, int col) {
		return 0 <= row && row < numRows() && 0 <= col && col < numCols();
	}

	protected void throwOutOfBoundsError(int row, int col) {
		throw new IndexOutOfBoundsException(
				"Coordinate (%d, %d) is outside of map bounds (%d rows, %d cols)".formatted(row, col, numRows(), numCols()));
	}

	@Override
	public int numCols() {
		return tileMap[0].length;
	}

	@Override
	public int numRows() {
		return tileMap.length;
	}

	@Override
	public List<Portal> portals() {
		return Collections.unmodifiableList(portals);
	}

	@Override
	public boolean belongsToPortal(Vector2i tile) {
		Objects.requireNonNull(tile);
		return portals.stream().anyMatch(portal -> portal.contains(tile));
	}

	@Override
	public boolean isWall(Vector2i tile) {
		Objects.requireNonNull(tile);
		return content(tile) == TILE_WALL;
	}

	@Override
	public boolean isTunnel(Vector2i tile) {
		Objects.requireNonNull(tile);
		return content(tile) == TILE_TUNNEL;
	}

	@Override
	public boolean isFoodTile(Vector2i tile) {
		Objects.requireNonNull(tile);
		byte data = content(tile);
		return data == TILE_PELLET || data == TILE_ENERGIZER;
	}

	@Override
	public boolean isEnergizerTile(Vector2i tile) {
		Objects.requireNonNull(tile);
		byte data = content(tile);
		return data == TILE_ENERGIZER;
	}

	@Override
	public Stream<Vector2i> energizerTiles() {
		return energizerTiles.stream();
	}

	@Override
	public void removeFood(Vector2i tile) {
		Objects.requireNonNull(tile);
		if (insideBounds(tile) && containsFood(tile)) {
			eatenSet.set(index(tile));
			--foodRemaining;
		}
	}

	@Override
	public boolean containsFood(Vector2i tile) {
		Objects.requireNonNull(tile);
		if (insideBounds(tile)) {
			byte data = content(tile);
			return (data == TILE_PELLET || data == TILE_ENERGIZER) && !eatenSet.get(index(tile));
		}
		return false;
	}

	@Override
	public boolean containsEatenFood(Vector2i tile) {
		Objects.requireNonNull(tile);
		if (insideBounds(tile)) {
			return eatenSet.get(index(tile));
		}
		return false;
	}

	@Override
	public int foodRemaining() {
		return foodRemaining;
	}

	@Override
	public int eatenFoodCount() {
		return totalFoodCount - foodRemaining;
	}
}
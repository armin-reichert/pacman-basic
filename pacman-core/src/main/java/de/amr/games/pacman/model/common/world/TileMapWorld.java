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

	protected final TileMap tileMap;
	protected List<Portal> portals;
	protected List<Vector2i> energizerTiles;
	protected int totalFoodCount;
	protected int foodRemaining;
	private final BitSet eatenSet;

	protected TileMapWorld(byte[][] tileMapData) {
		Objects.requireNonNull(tileMapData);
		tileMap = new TileMap(tileMapData);
		analyzeMap();
		eatenSet = new BitSet(numRows() * numCols());
	}

	private void analyzeMap() {
		energizerTiles = tiles().filter(this::isEnergizerTile).toList();
		totalFoodCount = (int) tiles().filter(this::isFoodTile).count();
		foodRemaining = totalFoodCount;
		portals = findPortals();
	}

	protected ArrayList<Portal> findPortals() {
		var portalList = new ArrayList<Portal>();
		for (int row = 0; row < tileMap.numRows(); ++row) {
			if (tileMap.get(row, 0) == TILE_TUNNEL && tileMap.get(row, tileMap.numCols() - 1) == TILE_TUNNEL) {
				portalList.add(new HorizontalPortal(v2i(0, row), v2i(tileMap.numCols() - 1, row)));
			}
		}
		portalList.trimToSize();
		return portalList;
	}

	protected byte content(Vector2i tile) {
		return tileMap.get(tile.y(), tile.x(), TILE_SPACE);
	}

	@Override
	public int numCols() {
		return tileMap.numCols();
	}

	@Override
	public int numRows() {
		return tileMap.numRows();
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
		return content(Objects.requireNonNull(tile)) == TILE_WALL;
	}

	@Override
	public boolean isTunnel(Vector2i tile) {
		return content(Objects.requireNonNull(tile)) == TILE_TUNNEL;
	}

	@Override
	public boolean isFoodTile(Vector2i tile) {
		byte data = content(Objects.requireNonNull(tile));
		return data == TILE_PELLET || data == TILE_ENERGIZER;
	}

	@Override
	public boolean isEnergizerTile(Vector2i tile) {
		byte data = content(Objects.requireNonNull(tile));
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
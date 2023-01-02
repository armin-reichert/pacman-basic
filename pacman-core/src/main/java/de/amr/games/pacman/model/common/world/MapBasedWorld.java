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
import static de.amr.games.pacman.model.common.world.WorldMap.ENERGIZER;
import static de.amr.games.pacman.model.common.world.WorldMap.ENERGIZER_EATEN;
import static de.amr.games.pacman.model.common.world.WorldMap.PELLET;
import static de.amr.games.pacman.model.common.world.WorldMap.PELLET_EATEN;
import static de.amr.games.pacman.model.common.world.WorldMap.SPACE;
import static de.amr.games.pacman.model.common.world.WorldMap.TUNNEL;
import static de.amr.games.pacman.model.common.world.WorldMap.WALL;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.math.Vector2i;

/**
 * @author Armin Reichert
 */
public abstract class MapBasedWorld implements World {

	protected final WorldMap map;
	protected List<Portal> portals;
	protected List<Vector2i> energizerTiles;
	protected int totalFoodCount;
	protected int foodRemaining;

	protected MapBasedWorld(byte[][] mapData) {
		map = new WorldMap(mapData);
		analyzeMap();
	}

	private void analyzeMap() {
		energizerTiles = tiles().filter(this::isEnergizerTile).toList();
		totalFoodCount = (int) tiles().filter(this::isFoodTile).count();
		foodRemaining = totalFoodCount;
		portals = findPortals();
	}

	protected ArrayList<Portal> findPortals() {
		var portalList = new ArrayList<Portal>();
		for (int row = 0; row < map.numRows(); ++row) {
			if (map.get(row, 0) == TUNNEL && map.get(row, map.numCols() - 1) == TUNNEL) {
				portalList.add(new HorizontalPortal(v2i(0, row), v2i(map.numCols() - 1, row)));
			}
		}
		portalList.trimToSize();
		return portalList;
	}

	protected byte content(Vector2i tile) {
		return map.get(tile.y(), tile.x(), SPACE);
	}

	@Override
	public int numCols() {
		return map.numCols();
	}

	@Override
	public int numRows() {
		return map.numRows();
	}

	@Override
	public List<Portal> portals() {
		return portals;
	}

	@Override
	public boolean belongsToPortal(Vector2i tile) {
		return portals.stream().anyMatch(portal -> portal.contains(tile));
	}

	@Override
	public boolean isWall(Vector2i tile) {
		return content(tile) == WALL;
	}

	@Override
	public boolean isTunnel(Vector2i tile) {
		return content(tile) == TUNNEL;
	}

	@Override
	public boolean isFoodTile(Vector2i tile) {
		byte data = content(tile);
		return data == PELLET || data == PELLET_EATEN || data == ENERGIZER || data == ENERGIZER_EATEN;
	}

	@Override
	public boolean isEnergizerTile(Vector2i tile) {
		byte data = content(tile);
		return data == ENERGIZER || data == ENERGIZER_EATEN;
	}

	@Override
	public Stream<Vector2i> energizerTiles() {
		return energizerTiles.stream();
	}

	@Override
	public void removeFood(Vector2i tile) {
		byte data = content(tile);
		if (data == ENERGIZER) {
			map.set(tile.y(), tile.x(), ENERGIZER_EATEN);
			--foodRemaining;
		} else if (data == PELLET) {
			map.set(tile.y(), tile.x(), PELLET_EATEN);
			--foodRemaining;
		}
	}

	@Override
	public boolean containsFood(Vector2i tile) {
		byte data = content(tile);
		return data == PELLET || data == ENERGIZER;
	}

	@Override
	public boolean containsEatenFood(Vector2i tile) {
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
		for (int row = 0; row < map.numRows(); ++row) {
			for (int col = 0; col < map.numCols(); ++col) {
				if (map.get(row, col) == PELLET_EATEN) {
					map.set(row, col, PELLET);
				} else if (map.get(row, col) == ENERGIZER_EATEN) {
					map.set(row, col, ENERGIZER);
				}
			}
		}
		foodRemaining = totalFoodCount;
	}
}
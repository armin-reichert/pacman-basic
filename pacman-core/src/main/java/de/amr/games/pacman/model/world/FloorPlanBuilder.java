/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.V2i;

/**
 * Scans a world map and creates a floor plan at a given resolution. Walls are placed at the outmost
 * border of inaccessible areas. The {@code resolution} value must be a divisor of the tile size (8,
 * 4, 2, 1) and determines the number of vertical and horizontal scan lines used for each tile. For
 * example, if the resolution is set to 8, each tile is divided into 64 parts from which the wall
 * structure is computed, and each wall will have a thickness of 1.
 * 
 * @author Armin Reichert
 */
class FloorPlanBuilder {

	private final int resolution;

	public FloorPlanBuilder(int resolution) {
		this.resolution = resolution;
	}

	private V2i northOf(int tileX, int tileY, int i) {
		int dy = i / resolution == 0 ? -1 : 0;
		return new V2i(tileX, tileY + dy);
	}

	private V2i southOf(int tileX, int tileY, int i) {
		int dy = i / resolution == resolution - 1 ? 1 : 0;
		return new V2i(tileX, tileY + dy);
	}

	private V2i westOf(int tileX, int tileY, int i) {
		int dx = i % resolution == 0 ? -1 : 0;
		return new V2i(tileX + dx, tileY);
	}

	private V2i eastOf(int tileX, int tileY, int i) {
		int dx = i % resolution == resolution - 1 ? 1 : 0;
		return new V2i(tileX + dx, tileY);
	}

	FloorPlan build(PacManGameWorld world) {
		int numBlocksX = resolution * world.numCols();
		int numBlocksY = resolution * world.numRows();
		byte[][] info = new byte[numBlocksY][numBlocksX];

		// scan for walls
		for (int y = 0; y < numBlocksY; ++y) {
			for (int x = 0; x < numBlocksX; ++x) {
				V2i tile = new V2i(x / resolution, y / resolution);
				info[y][x] = world.isWall(tile) ? FloorPlan.CORNER : FloorPlan.EMPTY;
			}
		}

		// clear blocks inside wall regions
		for (int y = 0; y < numBlocksY; ++y) {
			int tileY = y / resolution;
			for (int x = 0; x < numBlocksX; ++x) {
				int tileX = x / resolution;
				int i = (y % resolution) * resolution + (x % resolution);
				V2i n = northOf(tileX, tileY, i), e = eastOf(tileX, tileY, i), s = southOf(tileX, tileY, i),
						w = westOf(tileX, tileY, i);
				if (world.isWall(n) && world.isWall(e) && world.isWall(s) && world.isWall(w)) {
					V2i se = southOf(e.x, e.y, i);
					V2i sw = southOf(w.x, w.y, i);
					V2i ne = northOf(e.x, e.y, i);
					V2i nw = northOf(w.x, w.y, i);
					if (world.isWall(se) && !world.isWall(nw) || !world.isWall(se) && world.isWall(nw)
							|| world.isWall(sw) && !world.isWall(ne) || !world.isWall(sw) && world.isWall(ne)) {
						// keep corner of wall region
					} else {
						info[y][x] = FloorPlan.EMPTY;
					}
				}
			}
		}

		// separate horizontal walls, vertical walls and corners
		for (int y = 0; y < numBlocksY; ++y) {
			int blockStartX = -1;
			int blockLen = 0;
			for (int x = 0; x < numBlocksX; ++x) {
				if (info[y][x] == FloorPlan.CORNER) {
					if (blockStartX == -1) {
						blockStartX = x;
						blockLen = 1;
					} else {
						if (x == numBlocksX - 1) {
							info[y][x] = FloorPlan.CORNER;
						} else {
							info[y][x] = FloorPlan.HWALL;
						}
						++blockLen;
					}
				} else {
					if (blockLen == 1) {
						info[y][blockStartX] = FloorPlan.CORNER;
					} else if (blockLen > 1) {
						info[y][blockStartX + blockLen - 1] = FloorPlan.CORNER;
					}
					blockStartX = -1;
					blockLen = 0;
				}
			}
		}

		for (int x = 0; x < numBlocksX; ++x) {
			int blockStartY = -1;
			int blockLen = 0;
			for (int y = 0; y < numBlocksY; ++y) {
				if (info[y][x] == FloorPlan.CORNER) {
					if (blockStartY == -1) {
						blockStartY = y;
						blockLen = 1;
					} else {
						info[y][x] = (y == numBlocksY - 1) ? FloorPlan.CORNER : FloorPlan.VWALL;
						++blockLen;
					}
				} else {
					if (blockLen == 1) {
						info[blockStartY][x] = FloorPlan.CORNER;
					} else if (blockLen > 1) {
						info[blockStartY + blockLen - 1][x] = FloorPlan.CORNER;
					}
					blockStartY = -1;
					blockLen = 0;
				}
			}
		}
		return new FloorPlan(info);
	}
}
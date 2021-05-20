package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.V2i;

/**
 * Scans a map for inaccessible areas and creates a map indicating where walls should be placed.
 * These walls are located at the outside border of the inaccessible areas. The {@code resolution}
 * value must be a divisor of the tile size (8, 4, 2, 1) and determines the number of vertical and
 * horizontal scan lines for each tile. For example, if the resolution is set to 8, each tile is
 * divided into 64 parts from which the wall structure is computed and each wall will have a
 * thickness of 1.
 * 
 * @author Armin Reichert
 */
class WallScanner {

	private final int resolution;

	public WallScanner(int resolution) {
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

	WallMap scan(PacManGameWorld world) {
		int numBlocksX = resolution * world.numCols();
		int numBlocksY = resolution * world.numRows();
		byte[][] wm = new byte[numBlocksY][numBlocksX];

		// scan for walls
		for (int y = 0; y < numBlocksY; ++y) {
			for (int x = 0; x < numBlocksX; ++x) {
				V2i tile = new V2i(x / resolution, y / resolution);
				wm[y][x] = world.isWall(tile) ? WallMap.CORNER : WallMap.EMPTY;
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
						wm[y][x] = WallMap.EMPTY;
					}
				}
			}
		}

		// separate horizontal walls, vertical walls and corners
		for (int y = 0; y < numBlocksY; ++y) {
			int blockStartX = -1;
			int blockLen = 0;
			for (int x = 0; x < numBlocksX; ++x) {
				if (wm[y][x] == WallMap.CORNER) {
					if (blockStartX == -1) {
						blockStartX = x;
						blockLen = 1;
					} else {
						if (x == numBlocksX - 1) {
							wm[y][x] = WallMap.CORNER;
						} else {
							wm[y][x] = WallMap.HORIZONTAL;
						}
						++blockLen;
					}
				} else {
					if (blockLen == 1) {
						wm[y][blockStartX] = WallMap.CORNER;
					} else if (blockLen > 1) {
						wm[y][blockStartX + blockLen - 1] = WallMap.CORNER;
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
				if (wm[y][x] == WallMap.CORNER) {
					if (blockStartY == -1) {
						blockStartY = y;
						blockLen = 1;
					} else {
						wm[y][x] = (y == numBlocksY - 1) ? WallMap.CORNER : WallMap.VERTICAL;
						++blockLen;
					}
				} else {
					if (blockLen == 1) {
						wm[blockStartY][x] = WallMap.CORNER;
					} else if (blockLen > 1) {
						wm[blockStartY + blockLen - 1][x] = WallMap.CORNER;
					}
					blockStartY = -1;
					blockLen = 0;
				}
			}
		}
		return new WallMap(resolution, wm);
	}
}
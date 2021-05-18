package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.V2i;

/**
 * Scans a world for inaccessible areas and creates a map indicating where walls should be placed.
 * These walls are located at the outside border of the inaccessible areas. The {@code resolution}
 * value must be a divisor of the tile size (8, 4, 2, 1) and determines the number of vertical and
 * horizontal scan lines for each tile. For example, if the resolution is set to 8, each tile is
 * divided into 64 parts from which the wall structure is computed and each wall will have a
 * thickness of 1.
 * 
 * @author Armin Reichert
 */
public class WallScanner {

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

	public byte[][] scan(PacManGameWorld world) {
		int numBlocksX = resolution * world.numCols();
		int numBlocksY = resolution * world.numRows();
		byte[][] wallMap = new byte[numBlocksY][numBlocksX];

		// scan for walls
		for (int y = 0; y < numBlocksY; ++y) {
			for (int x = 0; x < numBlocksX; ++x) {
				V2i tile = new V2i(x / resolution, y / resolution);
				wallMap[y][x] = world.isWall(tile) ? WallMap.CORNER : WallMap.EMPTY;
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
						wallMap[y][x] = WallMap.EMPTY;
					}
				}
			}
		}

		// separate horizontal walls, vertical walls and corners
		for (int y = 0; y < numBlocksY; ++y) {
			int horizontalWallStart = -1;
			for (int x = 0; x < numBlocksX; ++x) {
				if (wallMap[y][x] != WallMap.EMPTY) {
					if (horizontalWallStart == -1) {
						horizontalWallStart = x;
					} else {
						wallMap[y][x] = x < numBlocksX - 1 ? WallMap.HORIZONTAL : WallMap.CORNER;
					}
				} else {
					horizontalWallStart = -1;
				}
			}
		}

		for (int x = 0; x < numBlocksX; ++x) {
			int verticalWallStart = -1;
			for (int y = 0; y < numBlocksY; ++y) {
				if (wallMap[y][x] != WallMap.EMPTY) {
					if (verticalWallStart == -1) {
						verticalWallStart = y;
					} else {
						wallMap[y][x] = (y == numBlocksY - 1) ? WallMap.CORNER : WallMap.VERTICAL;
					}
				} else {
					verticalWallStart = -1;
				}
			}
		}

		return wallMap;
	}
}
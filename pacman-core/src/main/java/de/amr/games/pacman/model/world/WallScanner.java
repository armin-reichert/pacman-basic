package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.V2i;

/**
 * Scans a world for inaccessible areas and creates a map indicating where walls should be placed.
 * These walls are located at the outside border of the wall blocks.
 * 
 * @author Armin Reichert
 */
public class WallScanner {

	private int resolution;
	private int numBlocksX;
	private int numBlocksY;
	private boolean[][] wallMap;

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

	public boolean[][] scan(PacManGameWorld world) {
		numBlocksX = resolution * world.numCols();
		numBlocksY = resolution * world.numRows();
		wallMap = new boolean[numBlocksY][numBlocksX];
		// scan for walls
		for (int y = 0; y < numBlocksY; ++y) {
			for (int x = 0; x < numBlocksX; ++x) {
				V2i tile = new V2i(x / resolution, y / resolution);
				wallMap[y][x] = world.isWall(tile);
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
						wallMap[y][x] = false;
					}
				}
			}
		}
		return wallMap;
	}
}
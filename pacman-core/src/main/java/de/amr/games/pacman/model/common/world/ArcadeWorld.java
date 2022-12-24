/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
import static de.amr.games.pacman.model.common.actors.Ghost.ID_CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_RED_GHOST;
import static de.amr.games.pacman.model.common.world.World.halfTileRightOf;

import java.util.Optional;

import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.actors.Ghost;

/**
 * Implements all stuff that is common to the original Arcade worlds like ghost house position, ghost and player start
 * positions and direction etc.
 * 
 * @author Armin Reichert
 */
public class ArcadeWorld extends MapBasedWorld {

	/** Number of tiles in x-direction (horizontally. */
	public static final int TILES_X = 28;

	/** Number of tiles in y-direction (vertically. */
	public static final int TILES_Y = 36;

	/** Size of Arcade game world in pixels (28x8=224, 36x8=288). */
	public static final Vector2i SIZE_PX = v2i(TILES_X * TS, TILES_Y * TS);

	/** Scatter target north-west corner. */
	public static final Vector2i SCATTER_TILE_NW = new Vector2i(2, 0);

	/** Scatter target north-east corner. */
	public static final Vector2i SCATTER_TILE_NE = new Vector2i(25, 0);

	/** Scatter target south-east corner. */
	public static final Vector2i SCATTER_TILE_SE = new Vector2i(27, 34);

	/** Scatter target south-west corner. */
	public static final Vector2i SCATTER_TILE_SW = new Vector2i(0, 34);

	private final ArcadeGhostHouse house = new ArcadeGhostHouse();

	private EntityAnimation levelCompleteAnimation;

	public ArcadeWorld(byte[][] mapData) {
		super(mapData, TILES_X, TILES_Y);
	}

	@Override
	public void assignGhostPositions(Ghost[] ghosts) {
		ghosts[ID_RED_GHOST].setHomePosition(halfTileRightOf(ArcadeGhostHouse.ENTRY_TILE));
		ghosts[ID_RED_GHOST].setRevivalPosition(halfTileRightOf(ArcadeGhostHouse.SEAT_TILE_CENTER));
		ghosts[ID_RED_GHOST].setScatterTile(ArcadeWorld.SCATTER_TILE_NE);

		ghosts[ID_PINK_GHOST].setHomePosition(halfTileRightOf(ArcadeGhostHouse.SEAT_TILE_CENTER));
		ghosts[ID_PINK_GHOST].setRevivalPosition(halfTileRightOf(ArcadeGhostHouse.SEAT_TILE_CENTER));
		ghosts[ID_PINK_GHOST].setScatterTile(ArcadeWorld.SCATTER_TILE_NW);

		ghosts[ID_CYAN_GHOST].setHomePosition(halfTileRightOf(ArcadeGhostHouse.SEAT_TILE_LEFT));
		ghosts[ID_CYAN_GHOST].setRevivalPosition(halfTileRightOf(ArcadeGhostHouse.SEAT_TILE_LEFT));
		ghosts[ID_CYAN_GHOST].setScatterTile(ArcadeWorld.SCATTER_TILE_SE);

		ghosts[ID_ORANGE_GHOST].setHomePosition(halfTileRightOf(ArcadeGhostHouse.SEAT_TILE_RIGHT));
		ghosts[ID_ORANGE_GHOST].setRevivalPosition(halfTileRightOf(ArcadeGhostHouse.SEAT_TILE_RIGHT));
		ghosts[ID_ORANGE_GHOST].setScatterTile(ArcadeWorld.SCATTER_TILE_SW);
	}

	@Override
	public Vector2f pacStartPosition() {
		return new Vector2f(13 * TS + HTS, 26 * TS);
	}

	@Override
	public ArcadeGhostHouse ghostHouse() {
		// WTF! I learned today, 2022-05-27, that Java allows co-variant return types since JDK 5.0!
		return house;
	}

	/**
	 * @return (optional) animation played when level has been completed
	 */
	@Override
	public Optional<EntityAnimation> levelCompleteAnimation() {
		return Optional.ofNullable(levelCompleteAnimation);
	}

	/**
	 * @param animation animation played when level has been completed
	 */
	public void setLevelCompleteAnimation(EntityAnimation animation) {
		this.levelCompleteAnimation = animation;
	}
}
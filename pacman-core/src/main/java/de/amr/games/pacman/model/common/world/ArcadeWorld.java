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
import static de.amr.games.pacman.model.common.world.World.halfTileRightOf;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.GameModel;

/**
 * Implements all stuff that is common to the original Arcade worlds like ghost house position, ghost and player start
 * positions and direction etc.
 * 
 * @author Armin Reichert
 */
public class ArcadeWorld extends MapBasedWorld {

	public static final Vector2i SIZE_TILES = v2i(28, 36);
	public static final Vector2i SIZE_PX = SIZE_TILES.scaled(TS);

	//@formatter:off
	private static final Vector2f[] GHOST_INITIAL_POSITIONS = {
			halfTileRightOf(ArcadeGhostHouse.ENTRY),
			halfTileRightOf(ArcadeGhostHouse.SEAT_CENTER),
			halfTileRightOf(ArcadeGhostHouse.SEAT_LEFT),
			halfTileRightOf(ArcadeGhostHouse.SEAT_RIGHT)
	};
	
	private static final Vector2f[] GHOST_REVIVAL_POSITIONS = {
			halfTileRightOf(ArcadeGhostHouse.SEAT_CENTER),
			halfTileRightOf(ArcadeGhostHouse.SEAT_CENTER),
			halfTileRightOf(ArcadeGhostHouse.SEAT_LEFT),
			halfTileRightOf(ArcadeGhostHouse.SEAT_RIGHT)
	};
	
	private static final Vector2i[] GHOST_SCATTER_TARGET_TILES = {
			v2i(25, 0), v2i(2, 0), v2i(27, 34), v2i(0, 34)
	};
	//@formatter:on

	private final ArcadeGhostHouse house = new ArcadeGhostHouse();
	private List<Vector2i> upwardBlockedTiles = List.of();
	private EntityAnimation levelCompleteAnimation;

	public ArcadeWorld(byte[][] mapData) {
		super(mapData);
	}

	/**
	 * @param upwardBlockedTiles the upwardBlockedTiles to set
	 */
	public void setUpwardBlockedTiles(List<Vector2i> upwardBlockedTiles) {
		this.upwardBlockedTiles = upwardBlockedTiles;
	}

	public List<Vector2i> upwardBlockedTiles() {
		return Collections.unmodifiableList(upwardBlockedTiles);
	}

	@Override
	public Vector2f pacStartPosition() {
		return new Vector2f(13 * TS + HTS, 26 * TS);
	}

	@Override
	public Vector2f ghostInitialPosition(byte ghostID) {
		GameModel.checkGhostID(ghostID);
		return GHOST_INITIAL_POSITIONS[ghostID];
	}

	@Override
	public Vector2f ghostRevivalPosition(byte ghostID) {
		GameModel.checkGhostID(ghostID);
		return GHOST_REVIVAL_POSITIONS[ghostID];
	}

	@Override
	public Vector2i ghostScatterTargetTile(byte ghostID) {
		GameModel.checkGhostID(ghostID);
		return GHOST_SCATTER_TARGET_TILES[ghostID];
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
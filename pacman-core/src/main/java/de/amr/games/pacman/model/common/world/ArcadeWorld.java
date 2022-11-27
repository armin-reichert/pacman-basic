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

import static de.amr.games.pacman.lib.V2i.v2i;

import java.util.Optional;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.EntityAnimation;

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
	public static final V2i SIZE_PX = v2i(TILES_X * TS, TILES_Y * TS);

	private final ArcadeGhostHouse house = new ArcadeGhostHouse();

	private EntityAnimation levelCompleteAnimation;

	public ArcadeWorld(byte[][] mapData) {
		super(mapData, TILES_X, TILES_Y);
	}

	@Override
	public V2d pacStartPosition() {
		return new V2d(13 * TS + HTS, 26 * TS);
	}

	@Override
	public ArcadeGhostHouse ghostHouse() {
		// WTF! I learned today, 2022-05-27, that Java allows co-variant return types since JDK 5.0!
		return house;
	}

	/**
	 * @return (optional) animation played when level has been completed
	 */
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
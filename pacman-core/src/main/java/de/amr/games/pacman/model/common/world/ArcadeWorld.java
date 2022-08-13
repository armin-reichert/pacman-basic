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

import static de.amr.games.pacman.lib.NavigationPoint.np;
import static de.amr.games.pacman.lib.V2i.v;

import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.NavigationPoint;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.EntityAnimation;

/**
 * Implements all stuff that is common to the original Arcade worlds like ghost house position, ghost and player start
 * positions and direction etc.
 * 
 * @author Armin Reichert
 */
public class ArcadeWorld extends MapBasedWorld {

	public static final int TILES_X = 28;
	public static final int TILES_Y = 36;
	public static final V2i WORLD_SIZE = v(TILES_X * TS, TILES_Y * TS);

	public static final V2i LEFT_LOWER_CORNER = v(0, 34);
	public static final V2i RIGHT_LOWER_CORNER = v(27, 34);
	public static final V2i LEFT_UPPER_CORNER = v(2, 0);
	public static final V2i RIGHT_UPPER_CORNER = v(25, 0);

	//@formatter:off
	public static final List<NavigationPoint> ATTRACT_ROUTE_PACMAN = List.of(
		np(12, 26), np( 9, 26),  np(12, 32), np(15, 32), np(24, 29),
		np(21, 23), np(18, 23),  np(18, 20), np(18, 17), np(15, 14),
		np(12, 14), np( 9, 17),  np( 6, 17), np( 6, 11), np( 6, 8), 
		np( 6, 4),  np( 1,  8),  np( 6,  8), np( 9,  8), np(12, 8),
		np( 6, 4),  np( 6,  8),  np( 6, 11), np( 1,  8), np( 6, 8),
		np( 9, 8),  np(12, 14),  np( 9, 17), np( 6, 17), np(0, 17),
		np(21, 17), np(21, 23),  np(21, 26), np(24, 29),
		/* avoid moving up: */ np(26, 29), 
		np(15, 32), np(12, 32),  np(3, 29),  np( 6, 23), np(9, 23),
		np(12, 26), np(15, 26), np(18, 23), np(21, 23), np(24, 29),
		/* avoid moving up: */ np(26, 29), 
		np(15, 32), np(12, 32), np(3, 29), np(6, 23)
	);
	
	public static final List<NavigationPoint> ATTRACT_FRIGHTENED_RED = List.of(
			np(21, 4,  Direction.DOWN),
			np(21, 8,  Direction.DOWN),
			np(21, 11, Direction.RIGHT),
			np(26,  8, Direction.LEFT),
			np(21, 8,  Direction.DOWN),
			np(26,  8, Direction.UP),
			np(26,  8, Direction.DOWN),
			np(21, 11, Direction.DOWN),
			np(21, 17, Direction.RIGHT), // enters right tunnel
			
			np(99,99,Direction.DOWN)
	);
	
	public static final List<NavigationPoint> ATTRACT_FRIGHTENED_PINK = List.of();
	public static final List<NavigationPoint> ATTRACT_FRIGHTENED_CYAN = List.of();
	public static final List<NavigationPoint> ATTRACT_FRIGHTENED_ORANGE = List.of();
	
	//@formatter:on

	public static final List<V2i> ATTRACT_ROUTE_MS_PACMAN = List.of();

	private final ArcadeGhostHouse house = new ArcadeGhostHouse();

	private EntityAnimation flashingAnimation;

	public ArcadeWorld(byte[][] mapData) {
		super(mapData, TILES_X, TILES_Y);
	}

	@Override
	public ArcadeGhostHouse ghostHouse() {
		// WTF! I learned today, 2022-05-27, that Java allows co-variant return types since JDK 5.0!
		return house;
	}

	public Optional<EntityAnimation> flashingAnimation() {
		return Optional.ofNullable(flashingAnimation);
	}

	public void setFlashingAnimation(EntityAnimation mazeFlashingAnimation) {
		this.flashingAnimation = mazeFlashingAnimation;
	}
}
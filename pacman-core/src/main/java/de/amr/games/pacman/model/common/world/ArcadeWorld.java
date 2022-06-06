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

import static de.amr.games.pacman.lib.V2i.v;

import de.amr.games.pacman.lib.V2i;

/**
 * Implements all stuff that is common to the original Arcade worlds like ghost house position, ghost and player start
 * positions and direction etc.
 * 
 * @author Armin Reichert
 */
public class ArcadeWorld extends MapBasedWorld {

	public static int TILES_X = 28;
	public static int TILES_Y = 36;

	public static V2i SIZE = v(TILES_X * TS, TILES_Y * TS);

	public final V2i leftLowerTarget = v(0, 34);
	public final V2i rightLowerTarget = v(27, 34);
	public final V2i leftUpperTarget = v(2, 0);
	public final V2i rightUpperTarget = v(25, 0);
	public final ArcadeGhostHouse house = new ArcadeGhostHouse();

	public ArcadeWorld(byte[][] mapData) {
		super(mapData, TILES_X, TILES_Y);
		tiles() //
				.filter(tile -> !house.contains(tile)) //
				.filter(tile -> tile.x > 0 && tile.x < numCols() - 1) //
				.filter(tile -> tile.neighbors().filter(nb -> isWall(nb) || house.isDoor(nb)).count() <= 1) //
				.map(this::index) //
				.forEach(intersections::set);
	}

	@Override
	public ArcadeGhostHouse ghostHouse() {
		// WTF! I learned today, 2022-05-27, that Java allows co-variant return types since JDK 5.0!
		return house;
	}
}
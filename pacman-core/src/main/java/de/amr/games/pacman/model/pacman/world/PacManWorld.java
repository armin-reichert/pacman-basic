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
package de.amr.games.pacman.model.pacman.world;

import java.util.List;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.world.SimpleWorld;

/**
 * Pac-Man game world.
 * 
 * @author Armin Reichert
 */
public class PacManWorld extends SimpleWorld {

	private static final String[] MAP = {
			//@formatter:off
			"############################",
			"############################",
			"############################",
			"############################",
			"#............##............#",
			"#.####.#####.##.#####.####.#",
			"#*####.#####.##.#####.####*#",
			"#.####.#####.##.#####.####.#",
			"#..........................#",
			"#.####.##.########.##.####.#",
			"#.####.##.########.##.####.#",
			"#......##....##....##......#",
			"######.##### ## #####.######",
			"     #.##### ## #####.#     ",
			"     #.##          ##.#     ",
			"     #.## ###LR### ##.#     ",
			"######.## #      # ##.######",
			"TTTTTT.   #      #   .TTTTTT",
			"######.## #      # ##.######",
			"     #.## ######## ##.#     ",
			"     #.##          ##.#     ",
			"     #.## ######## ##.#     ",
			"######.## ######## ##.######",
			"#............##............#",
			"#.####.#####.##.#####.####.#",
			"#.####.#####.##.#####.####.#",
			"#*..##.......  .......##..*#",
			"###.##.##.########.##.##.###",
			"###.##.##.########.##.##.###",
			"#......##....##....##......#",
			"#.##########.##.##########.#",
			"#.##########.##.##########.#",
			"#..........................#",
			"############################",
			"############################",
			"############################",
			//@formatter:on
	};

	public PacManWorld() {
		super(MAP);
		upwardsBlockedTiles = List.of(v(12, 13), v(15, 13), v(12, 25), v(15, 25));
		pelletsToEatForBonus[0] = 70;
		pelletsToEatForBonus[1] = 170;
	}

	@Override
	public V2i bonusTile() {
		return v(13, 20);
	}
}
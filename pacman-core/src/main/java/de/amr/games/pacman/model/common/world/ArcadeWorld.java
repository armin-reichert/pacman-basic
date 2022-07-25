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
import static de.amr.games.pacman.model.common.actors.Ghost.CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.RED_GHOST;

import java.util.Optional;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.model.common.actors.Ghost;

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

	public static final String ATTRACT_MODE_ROUTE_PACMAN = "ldrruluuullluuulrrruddlrrdllldddrllurrrurrrlluu";
	public static final String ATTRACT_MODE_ROUTE_MS_PACMAN = ""; // TODO

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

	@Override
	public void initGhosts(Ghost[] ghosts) {
		var blinky = ghosts[RED_GHOST];
		blinky.homePosition = house.seatPosition(house.entryTile());
		blinky.revivalTile = house.seatMiddleTile();
		blinky.scatterTile = ArcadeWorld.RIGHT_UPPER_CORNER;

		var pinky = ghosts[PINK_GHOST];
		pinky.homePosition = house.seatPosition(house.seatMiddleTile());
		pinky.revivalTile = house.seatMiddleTile();
		pinky.scatterTile = ArcadeWorld.LEFT_UPPER_CORNER;

		var inky = ghosts[CYAN_GHOST];
		inky.homePosition = house.seatPosition(house.seatLeftTile());
		inky.revivalTile = house.seatLeftTile();
		inky.scatterTile = ArcadeWorld.RIGHT_LOWER_CORNER;

		var clyde = ghosts[ORANGE_GHOST];
		clyde.homePosition = house.seatPosition(house.seatRightTile());
		clyde.revivalTile = house.seatRightTile();
		clyde.scatterTile = ArcadeWorld.LEFT_LOWER_CORNER;
	}

	public Optional<EntityAnimation> flashingAnimation() {
		return Optional.ofNullable(flashingAnimation);
	}

	public void setFlashingAnimation(EntityAnimation mazeFlashingAnimation) {
		this.flashingAnimation = mazeFlashingAnimation;
	}
}
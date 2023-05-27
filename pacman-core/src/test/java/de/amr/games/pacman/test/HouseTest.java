/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.test;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.v2i;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.world.Door;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.World;

/**
 * @author Armin Reichert
 */
public class HouseTest {

	static final Vector2i POSITION = v2i(16, 20);
	static final Vector2i SIZE = v2i(10, 8);
	static final Door DOOR = new Door(v2i(5, 10), v2i(6, 10));
	static final List<Vector2f> SEATS = List.of(//
			World.halfTileRightOf(5, 12), //
			World.halfTileRightOf(7, 12), //
			World.halfTileRightOf(9, 12));
	static final Vector2f CENTER = World.halfTileRightOf(7, 12).plus(0, HTS);

	@Test(expected = NullPointerException.class)
	public void testLeftDoorWingNotNull() {
		new Door(null, v2i(0, 0));
	}

	@Test(expected = NullPointerException.class)
	public void testRightDoorWingNotNull() {
		new Door(v2i(0, 0), null);
	}

	@Test(expected = NullPointerException.class)
	public void testPositionNotNull() {
		new House(null, SIZE, DOOR, SEATS, CENTER);
	}

	@Test(expected = NullPointerException.class)
	public void testSizeNotNull() {
		new House(POSITION, null, DOOR, SEATS, CENTER);
	}

	@Test(expected = NullPointerException.class)
	public void testDoorNotNull() {
		new House(POSITION, SIZE, null, SEATS, CENTER);
	}

	@Test(expected = NullPointerException.class)
	public void testSeatsNotNull() {
		new House(POSITION, SIZE, DOOR, null, CENTER);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSeatCount() {
		new House(POSITION, SIZE, DOOR, List.of(), CENTER);
	}

	@Test(expected = NullPointerException.class)
	public void testSeatContainsNull() {
		new House(POSITION, SIZE, DOOR, List.of(null, SEATS.get(0)), CENTER);
	}

	@Test
	public void testHouseProperties() {
		var house = new House(POSITION, SIZE, DOOR, SEATS, CENTER);
		Assert.assertEquals(POSITION, house.topLeftTile());
		Assert.assertEquals(SIZE, house.size());
		Assert.assertEquals(DOOR, house.door());
		Assert.assertEquals(SEATS, house.seatPositions());
	}
}
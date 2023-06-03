/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.test;

import static de.amr.games.pacman.lib.Globals.v2i;
import static de.amr.games.pacman.model.world.World.halfTileRightOf;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.world.Door;
import de.amr.games.pacman.model.world.House;

/**
 * @author Armin Reichert
 */
public class HouseTest {

	static final Vector2i POSITION = v2i(16, 20);
	static final Vector2i SIZE = v2i(10, 8);
	static final Door DOOR = new Door(v2i(5, 10), v2i(6, 10));

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
		new House(null, SIZE, DOOR, halfTileRightOf(5, 12), halfTileRightOf(7, 12), halfTileRightOf(9, 12));
	}

	@Test(expected = NullPointerException.class)
	public void testSizeNotNull() {
		new House(POSITION, null, DOOR, halfTileRightOf(5, 12), halfTileRightOf(7, 12), halfTileRightOf(9, 12));
	}

	@Test(expected = NullPointerException.class)
	public void testDoorNotNull() {
		new House(POSITION, SIZE, null, halfTileRightOf(5, 12), halfTileRightOf(7, 12), halfTileRightOf(9, 12));
	}

	@Test
	public void testHouseProperties() {
		var house = new House(POSITION, SIZE, DOOR, halfTileRightOf(5, 12), halfTileRightOf(7, 12), halfTileRightOf(9, 12));
		Assert.assertEquals(POSITION, house.topLeftTile());
		Assert.assertEquals(SIZE, house.size());
		Assert.assertEquals(DOOR, house.door());
		Assert.assertEquals(List.of(halfTileRightOf(5, 12), halfTileRightOf(7, 12), halfTileRightOf(9, 12)),
				house.seatPositions());
	}
}
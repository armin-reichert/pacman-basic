/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.test;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.Door;
import org.junit.Test;

import static de.amr.games.pacman.lib.Globals.halfTileRightOf;
import static de.amr.games.pacman.lib.Globals.v2i;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * @author Armin Reichert
 */
public class HouseTest {

	static final Vector2i ARCADE_HOUSE_POSITION = v2i(10, 15);
	static final Vector2i ARCADE_HOUSE_SIZE = v2i(8, 5);
	static final Door ARCADE_HOUSE_DOOR = new Door(v2i(13, 15), v2i(14, 15));

	@Test
	public void testDoorWingNotNull() {
		assertThrows(NullPointerException.class, () -> new Door(null, v2i(0, 0)));
		assertThrows(NullPointerException.class, () -> new Door(v2i(0, 0), null));
	}

	@Test
	public void testArcadeHouseProperties() {
		var house = GameModel.createArcadeHouse();
		assertEquals(ARCADE_HOUSE_POSITION, house.topLeftTile());
		assertEquals(ARCADE_HOUSE_SIZE, house.size());
		assertEquals(ARCADE_HOUSE_DOOR, house.door());
		assertEquals(halfTileRightOf(11, 17), house.seat("left"));
		assertEquals(halfTileRightOf(13, 17), house.seat("middle"));
		assertEquals(halfTileRightOf(15, 17), house.seat("right"));
	}
}
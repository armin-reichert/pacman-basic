/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Globals.checkTileNotNull;

import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Vector2i;

/**
 * @author Armin Reichert
 */
public class FoodStorage {

	private final World world;
	private final List<Vector2i> energizerTiles;
	private final BitSet eaten;
	private final long totalCount;
	private long uneatenCount;

	public FoodStorage(World world) {
		this.world = world;
		energizerTiles = world.tiles().filter(world::isEnergizerTile).collect(Collectors.toList());
		eaten = new BitSet(world.numCols() * world.numRows());
		totalCount = world.tiles().filter(world::isFoodTile).count();
		uneatenCount = totalCount;
	}

	public Stream<Vector2i> energizerTiles() {
		return energizerTiles.stream();
	}

	private int index(Vector2i tile) {
		return world.numCols() * tile.y() + tile.x();
	}

	public void removeFood(Vector2i tile) {
		checkTileNotNull(tile);
		if (world.insideBounds(tile) && hasFoodAt(tile)) {
			eaten.set(index(tile));
			--uneatenCount;
		}
	}

	public boolean hasFoodAt(Vector2i tile) {
		checkTileNotNull(tile);
		if (world.insideBounds(tile)) {
			byte data = world.contentOrSpace(tile);
			return (data == World.PELLET || data == World.ENERGIZER) && !eaten.get(index(tile));
		}
		return false;
	}

	public boolean hasEatenFoodAt(Vector2i tile) {
		checkTileNotNull(tile);
		if (world.insideBounds(tile)) {
			return eaten.get(index(tile));
		}
		return false;
	}

	public long totalCount() {
		return totalCount;
	}

	public long uneatenCount() {
		return uneatenCount;
	}

	public long eatenCount() {
		return totalCount - uneatenCount;
	}
}
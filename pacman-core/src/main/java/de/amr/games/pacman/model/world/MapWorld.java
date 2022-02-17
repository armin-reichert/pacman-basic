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
package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.lib.Misc.trim;
import static java.util.function.Predicate.not;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Game world using map(s).
 * 
 * @author Armin Reichert
 */
public class MapWorld implements World {

	private WorldMap map;
	private V2i size;
	private BitSet eaten;

	private DefaultGhostHouse house;
	private List<V2i> scatterTiles;
	private V2i pacman_home;
	private V2i pacman_start_dir;
	private List<V2i> ghost_start_dir;
	private V2i bonus_home;
	private V2i bonus_pellets_to_eat;
	private List<V2i> upwardsBlockedTiles;
	private List<Portal> portals;
	private BitSet intersections;
	private List<V2i> energizerTiles;
	private int totalFoodCount;
	private int foodRemaining;

	public MapWorld(String mapPath) {
		setMap(WorldMap.load(mapPath));
	}

	public void setMap(WorldMap map) {
		this.map = map;

		size = map.vector("size");

		house = new DefaultGhostHouse(map.vector("house_top_left"),
				map.vector("house_bottom_right").minus(map.vector("house_top_left")));
		house.entryTile = map.vector("house_entry");
		house.seatTiles = List.of(map.vector("house_seat_left"), map.vector("house_seat_center"),
				map.vector("house_seat_right"));
		house.doorTiles = trim(tiles().filter(this::isGhostHouseDoor).collect(Collectors.toList()));

		pacman_home = map.vector("pacman_home");
		pacman_start_dir = map.vector("pacman_start_dir");
		ghost_start_dir = map.vectorList("ghost_start_dir");
		bonus_home = map.vectorOptional("bonus_home").orElse(V2i.NULL);
		bonus_pellets_to_eat = map.vector("bonus_pellets_to_eat");
		scatterTiles = map.vectorList("scatter");
		upwardsBlockedTiles = map.vectorList("upwards_blocked");

		portals = trim(IntStream.range(0, size.y)
				.filter(y -> map.data(0, y) == WorldMap.TUNNEL && map.data(size.x - 1, y) == WorldMap.TUNNEL).boxed()
				.map(y -> new Portal(new V2i(-1, y), new V2i(size.x, y))) //
				.collect(Collectors.toList()));

		intersections = new BitSet();
		tiles() //
				.filter(tile -> !house.contains(tile)) //
				.filter(tile -> !isGhostHouseDoor(tile.plus(Direction.DOWN.vec))) //
				.filter(tile -> neighbors(tile).filter(not(this::isWall)).count() > 2) //
				.map(this::index) //
				.forEach(intersections::set);

		energizerTiles = trim(tiles().filter(tile -> map.data(tile) == WorldMap.ENERGIZER).collect(Collectors.toList()));
	}

	private boolean isGhostHouseDoor(V2i tile) {
		return insideWorld(tile) && map.data(tile) == WorldMap.DOOR;
	}

	@Override
	public int numCols() {
		return size.x;
	}

	@Override
	public int numRows() {
		return size.y;
	}

	@Override
	public Optional<WorldMap> getMap() {
		return Optional.of(map);
	}

	private Stream<V2i> neighbors(V2i tile) {
		return Stream.of(Direction.values()).map(dir -> tile.plus(dir.vec));
	}

	@Override
	public GhostHouse ghostHouse() {
		return house;
	}

	@Override
	public V2i playerHomeTile() {
		return pacman_home;
	}

	@Override
	public Direction playerStartDirection() {
		return Direction.of(pacman_start_dir);
	}

	@Override
	public Direction ghostStartDirection(int ghostID) {
		return Direction.of(ghost_start_dir.get(ghostID));
	}

	@Override
	public V2i ghostScatterTile(int ghostID) {
		return scatterTiles.get(ghostID);
	}

	@Override
	public List<Portal> portals() {
		return Collections.unmodifiableList(portals);
	}

	@Override
	public boolean isOneWayDown(V2i tile) {
		return upwardsBlockedTiles.contains(tile);
	}

	@Override
	public boolean isWall(V2i tile) {
		return insideWorld(tile) && map.data(tile) == WorldMap.WALL;
	}

	@Override
	public boolean isTunnel(V2i tile) {
		return insideWorld(tile) && map.data(tile) == WorldMap.TUNNEL;
	}

	@Override
	public boolean isPortal(V2i tile) {
		return portals.stream().anyMatch(portal -> portal.left.equals(tile) || portal.right.equals(tile));
	}

	@Override
	public boolean isIntersection(V2i tile) {
		return intersections.get(index(tile));
	}

	@Override
	public boolean isFoodTile(V2i tile) {
		return insideWorld(tile) && (map.data(tile) == WorldMap.PILL || map.data(tile) == WorldMap.ENERGIZER);
	}

	@Override
	public boolean isEnergizerTile(V2i tile) {
		return energizerTiles.contains(tile);
	}

	@Override
	public List<V2i> energizerTiles() {
		return Collections.unmodifiableList(energizerTiles);
	}

	@Override
	public V2i bonusTile() {
		return bonus_home;
	}

	@Override
	public int pelletsToEatForBonus(int bonusIndex) {
		return bonusIndex == 0 ? bonus_pellets_to_eat.x : bonus_pellets_to_eat.y;
	}

	@Override
	public boolean isFoodEaten(V2i tile) {
		return eaten.get(index(tile));
	}

	@Override
	public boolean containsFood(V2i tile) {
		return isFoodTile(tile) && !isFoodEaten(tile);
	}

	@Override
	public void removeFood(V2i tile) {
		if (containsFood(tile)) {
			eaten.set(index(tile));
			--foodRemaining;
		}
	}

	@Override
	public int foodRemaining() {
		return foodRemaining;
	}

	@Override
	public int eatenFoodCount() {
		return totalFoodCount - foodRemaining;
	}

	@Override
	public void resetFood() {
		totalFoodCount = (int) tiles().filter(this::isFoodTile).count();
		foodRemaining = totalFoodCount;
		eaten = new BitSet();
		long energizerCount = tiles().filter(this::isEnergizerTile).count();

		log("Total food: %d (%d pellets, %d energizers)", totalFoodCount, totalFoodCount - energizerCount, energizerCount);
	}
}
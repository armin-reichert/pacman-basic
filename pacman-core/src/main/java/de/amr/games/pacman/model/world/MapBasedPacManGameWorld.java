package de.amr.games.pacman.model.world;

import static java.util.function.Predicate.not;

import java.util.BitSet;
import java.util.List;
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
public class MapBasedPacManGameWorld implements PacManGameWorld {

	private WorldMap map;
	private V2i size;
	private V2i house_top_left;
	private V2i house_bottom_right;
	private V2i house_entry;
	private V2i house_seat_left;
	private V2i house_seat_center;
	private V2i house_seat_right;
	private List<V2i> scatterTiles;
	private V2i pacman_home;
	private V2i bonus_home;
	private List<V2i> upwardsBlockedTiles;
	private List<Integer> portalRows;
	private BitSet intersections;
	private List<V2i> energizerTiles;

	public void setMap(WorldMap map) {
		this.map = map;

		size = map.vector("size");
		house_top_left = map.vector("house_top_left");
		house_bottom_right = map.vector("house_bottom_right");
		house_entry = map.vector("house_entry");
		house_seat_left = map.vector("house_seat_left");
		house_seat_center = map.vector("house_seat_center");
		house_seat_right = map.vector("house_seat_right");
		pacman_home = map.vector("pacman_home");
		bonus_home = map.vectorOptional("bonus_home").orElse(V2i.NULL);
		scatterTiles = map.vectorList("scatter");
		upwardsBlockedTiles = map.vectorList("upwards_blocked");

		// Collect portal tiles
		portalRows = IntStream.range(0, size.y)
				.filter(y -> map.data(0, y) == WorldMap.TUNNEL && map.data(size.x - 1, y) == WorldMap.TUNNEL)
				.mapToObj(Integer::valueOf).collect(Collectors.toList());

		/*
		 * Collect "waypoints".
		 */
		intersections = new BitSet();
		tiles() //
				.filter(not(this::isGhostHouse)) //
				.filter(tile -> !isGhostHouseDoor(tile.plus(Direction.DOWN.vec))) //
				.filter(tile -> neighbors(tile).filter(not(this::isWall)).count() > 2) //
				.map(this::index) //
				.forEach(intersections::set);

		// Collect energizer tiles
		energizerTiles = tiles().filter(tile -> map.data(tile) == WorldMap.ENERGIZER).collect(Collectors.toList());
	}

	private Stream<V2i> neighbors(V2i tile) {
		return Stream.of(Direction.values()).map(dir -> tile.plus(dir.vec));
	}

	@Override
	public boolean isGhostHouse(V2i tile) {
		return tile.x >= house_top_left.x && tile.x <= house_bottom_right.x //
				&& tile.y >= house_top_left.y && tile.y <= house_bottom_right.y;
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
	public V2i pacHome() {
		return pacman_home;
	}

	@Override
	public V2i ghostHome(int ghostID) {
		return ghostID == 0 ? house_entry
				: ghostID == 1 ? house_seat_center : ghostID == 2 ? house_seat_left : house_seat_right;
	}

	@Override
	public V2i ghostScatterTile(int ghostID) {
		return scatterTiles.get(ghostID);
	}

	@Override
	public int numPortals() {
		return portalRows.size();
	}

	@Override
	public V2i portalLeft(int i) {
		return new V2i(-1, portalRows.get(i));
	}

	@Override
	public V2i portalRight(int i) {
		return new V2i(size.x, portalRows.get(i));
	}

	@Override
	public boolean isUpwardsBlocked(V2i tile) {
		return upwardsBlockedTiles.contains(tile);
	}

	@Override
	public V2i houseEntry() {
		return house_entry;
	}

	@Override
	public V2i houseSeatCenter() {
		return house_seat_center;
	}

	@Override
	public V2i houseSeatLeft() {
		return house_seat_left;
	}

	@Override
	public V2i houseSeatRight() {
		return house_seat_right;
	}

	@Override
	public boolean isWall(V2i tile) {
		return insideMap(tile) && map.data(tile) == WorldMap.WALL;
	}

	@Override
	public boolean isTunnel(V2i tile) {
		return insideMap(tile) && map.data(tile) == WorldMap.TUNNEL;
	}

	@Override
	public boolean isGhostHouseDoor(V2i tile) {
		return insideMap(tile) && map.data(tile) == WorldMap.DOOR;
	}

	@Override
	public boolean isPortal(V2i tile) {
		return (tile.x == -1 || tile.x == size.x) && portalRows.contains(tile.y);
	}

	@Override
	public boolean isIntersection(V2i tile) {
		return intersections.get(index(tile));
	}

	@Override
	public boolean isFoodTile(V2i tile) {
		return insideMap(tile) && (map.data(tile) == WorldMap.PILL || map.data(tile) == WorldMap.ENERGIZER);
	}

	@Override
	public boolean isEnergizerTile(V2i tile) {
		return energizerTiles.contains(tile);
	}

	@Override
	public Stream<V2i> energizerTiles() {
		return energizerTiles.stream();
	}

	@Override
	public V2i bonusHomeTile() {
		return bonus_home;
	}
}
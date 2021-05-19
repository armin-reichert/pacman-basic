package de.amr.games.pacman.model.world;

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
public class MapBasedPacManGameWorld implements PacManGameWorld {

	private WorldMap map;
	private WallMap wallMap;
	private V2i size;
	private V2i house_top_left;
	private V2i house_bottom_right;
	private V2i house_entry;
	private V2i house_seat_left;
	private V2i house_seat_center;
	private V2i house_seat_right;
	private List<V2i> scatterTiles;
	private V2i pacman_home;
	private V2i pacman_start_dir;
	private List<V2i> ghost_start_dir;
	private V2i bonus_home;
	private List<V2i> upwardsBlockedTiles;
	private List<Portal> portals;
	private BitSet intersections;
	private List<V2i> energizerTiles;

	public void setMap(WorldMap map) {
		this.map = map;
		wallMap = null; // invalidate

		size = map.vector("size");
		house_top_left = map.vector("house_top_left");
		house_bottom_right = map.vector("house_bottom_right");
		house_entry = map.vector("house_entry");
		house_seat_left = map.vector("house_seat_left");
		house_seat_center = map.vector("house_seat_center");
		house_seat_right = map.vector("house_seat_right");
		pacman_home = map.vector("pacman_home");
		pacman_start_dir = map.vector("pacman_start_dir");
		ghost_start_dir = map.vectorList("ghost_start_dir");
		bonus_home = map.vectorOptional("bonus_home").orElse(V2i.NULL);
		scatterTiles = map.vectorList("scatter");
		upwardsBlockedTiles = map.vectorList("upwards_blocked");

		portals = IntStream.range(0, size.y)
				.filter(y -> map.data(0, y) == WorldMap.TUNNEL && map.data(size.x - 1, y) == WorldMap.TUNNEL).boxed()
				.map(y -> new Portal(new V2i(-1, y), new V2i(size.x, y))) //
				.collect(Collectors.toList());

		intersections = new BitSet();
		tiles() //
				.filter(not(this::isGhostHousePart)) //
				.filter(tile -> !isGhostHouseDoor(tile.plus(Direction.DOWN.vec))) //
				.filter(tile -> neighbors(tile).filter(not(this::isWall)).count() > 2) //
				.map(this::index) //
				.forEach(intersections::set);

		energizerTiles = tiles().filter(tile -> map.data(tile) == WorldMap.ENERGIZER).collect(Collectors.toList());
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

	@Override
	public Optional<WallMap> getWallMap(int resolution) {
		if (wallMap == null || wallMap.resolution() != resolution) {
			createWallMap(resolution);
		}
		return Optional.of(wallMap);
	}

	private void createWallMap(int resolution) {
		wallMap = new WallScanner(resolution).scan(this);
	}

	private Stream<V2i> neighbors(V2i tile) {
		return Stream.of(Direction.values()).map(dir -> tile.plus(dir.vec));
	}

	@Override
	public boolean isGhostHousePart(V2i tile) {
		return tile.x >= house_top_left.x && tile.x <= house_bottom_right.x //
				&& tile.y >= house_top_left.y && tile.y <= house_bottom_right.y;
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
	public V2i ghostHomeTile(int ghostID) {
		return ghostID == 0 ? house_entry
				: ghostID == 1 ? house_seat_center : ghostID == 2 ? house_seat_left : house_seat_right;
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
	public V2i houseEntryLeftPart() {
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
		return insideWorld(tile) && map.data(tile) == WorldMap.WALL;
	}

	@Override
	public boolean isTunnel(V2i tile) {
		return insideWorld(tile) && map.data(tile) == WorldMap.TUNNEL;
	}

	@Override
	public boolean isGhostHouseDoor(V2i tile) {
		return insideWorld(tile) && map.data(tile) == WorldMap.DOOR;
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
	public Stream<V2i> energizerTiles() {
		return energizerTiles.stream();
	}

	@Override
	public V2i bonusTile() {
		return bonus_home;
	}
}
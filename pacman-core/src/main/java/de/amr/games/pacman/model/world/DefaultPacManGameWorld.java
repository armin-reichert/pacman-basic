package de.amr.games.pacman.model.world;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Default world of Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class DefaultPacManGameWorld implements PacManGameWorld {

	protected WorldMap map;
	protected List<V2i> portalsLeft;
	protected List<V2i> portalsRight;
	protected BitSet intersections;
	protected List<V2i> energizerTiles;

	public void setMap(WorldMap worldMap) {
		map = worldMap;

		// find portal tiles
		portalsLeft = new ArrayList<>(2);
		portalsRight = new ArrayList<>(2);
		for (int y = 0; y < map.size.y; ++y) {
			if (map.data(0, y) == WorldMap.TUNNEL && map.data(map.size.x - 1, y) == WorldMap.TUNNEL) {
				portalsLeft.add(new V2i(-1, y));
				portalsRight.add(new V2i(map.size.x, y));
			}
		}

		// find intersections ("waypoints"), i.e. tiles with at least 3 accessible neighbor tiles
		intersections = new BitSet();
		//@formatter:off
		tiles()
			.filter(tile -> !map.isInsideGhostHouse(tile))
			.filter(tile -> !isGhostHouseDoor(tile.plus(Direction.DOWN.vec)))
			.filter(tile -> neighborTiles(tile).filter(neighbor-> !isWall(neighbor)).count() >= 3)
			.map(tile -> index(tile))
			.forEach(intersections::set);
		//@formatter:on

		// find energizer tiles
		energizerTiles = tiles().filter(tile -> map.data(tile) == WorldMap.ENERGIZER).collect(Collectors.toList());
	}

	private Stream<V2i> neighborTiles(V2i tile) {
		return Stream.of(Direction.values()).map(dir -> tile.plus(dir.vec));
	}

	@Override
	public int numCols() {
		return map.size.x;
	}

	@Override
	public int numRows() {
		return map.size.y;
	}

	@Override
	public V2i pacHome() {
		return map.pacman_home;
	}

	@Override
	public V2i ghostHome(int ghostID) {
		return ghostID == 0 ? map.house_entry
				: ghostID == 1 ? map.house_seat_center : ghostID == 2 ? map.house_seat_left : map.house_seat_right;
	}

	@Override
	public V2i ghostScatterTile(int ghostID) {
		return map.scatterTiles.get(ghostID);
	}

	@Override
	public int numPortals() {
		return portalsLeft.size();
	}

	@Override
	public V2i portalLeft(int i) {
		return portalsLeft.get(i);
	}

	@Override
	public V2i portalRight(int i) {
		return portalsRight.get(i);
	}

	@Override
	public boolean isUpwardsBlocked(V2i tile) {
		return map.upwardsBlockedTiles.contains(tile);
	}

	@Override
	public V2i houseEntry() {
		return map.house_entry;
	}

	@Override
	public V2i houseSeatCenter() {
		return map.house_seat_center;
	}

	@Override
	public V2i houseSeatLeft() {
		return map.house_seat_left;
	}

	@Override
	public V2i houseSeatRight() {
		return map.house_seat_right;
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
		return portalsLeft.contains(tile) || portalsRight.contains(tile);
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
}
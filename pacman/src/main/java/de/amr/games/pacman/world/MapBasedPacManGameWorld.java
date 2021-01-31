package de.amr.games.pacman.world;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.WorldMap.DOOR;
import static de.amr.games.pacman.world.WorldMap.ENERGIZER;
import static de.amr.games.pacman.world.WorldMap.PILL;
import static de.amr.games.pacman.world.WorldMap.TUNNEL;
import static de.amr.games.pacman.world.WorldMap.WALL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Common base of Pac-Man classic and Ms. Pac-man worlds. All maps used in these game variants have
 * the same ghost house structure and location.
 * 
 * @author Armin Reichert
 */
public class MapBasedPacManGameWorld implements PacManGameWorld {

	// these are assumed to be equal in all world maps
	private final V2i houseEntry = new V2i(13, 14);
	private final V2i houseLeft = new V2i(11, 17);
	private final V2i houseMiddle = new V2i(13, 17);
	private final V2i houseRight = new V2i(15, 17);
	private final V2i pacHome = new V2i(13, 26);
	private final V2i[] ghostHomes = { houseEntry, houseMiddle, houseLeft, houseRight };
	private final V2i[] ghostScatterTargets = { new V2i(25, 0), new V2i(2, 0), new V2i(27, 35), new V2i(27, 35) };

	protected WorldMap map;

	protected final List<V2i> portalsLeft = new ArrayList<>(2);
	protected final List<V2i> portalsRight = new ArrayList<>(2);
	protected BitSet intersections;
	protected List<V2i> upwardsBlockedTiles;
	protected List<V2i> energizerTiles;

	@Override
	public void loadMap(String path) {
		map = new WorldMap(path);

		upwardsBlockedTiles = Collections.emptyList();

		// find portals
		portalsLeft.clear();
		portalsRight.clear();
		for (int y = 0; y < map.sizeY(); ++y) {
			if (map.data(0, y) == TUNNEL && map.data(map.sizeX() - 1, y) == TUNNEL) {
				portalsLeft.add(new V2i(-1, y));
				portalsRight.add(new V2i(map.sizeX(), y));
			}
		}

		// find intersections ("waypoints"), i.e. tiles with at least 3 accessible neighbor tiles
		intersections = new BitSet();
		//@formatter:off
		tiles()
			.filter(tile -> !isInsideGhostHouse(tile))
			.filter(tile -> !isGhostHouseDoor(tile.sum(DOWN.vec)))
			.filter(tile -> neighborTiles(tile).filter(neighbor-> !isWall(neighbor)).count() >= 3)
			.map(tile -> index(tile))
			.forEach(intersections::set);
		//@formatter:on

		// find energizer tiles
		energizerTiles = tiles().filter(tile -> data(tile) == ENERGIZER).collect(Collectors.toList());

		log("World map is '%s'", path);
	}

	private Stream<V2i> neighborTiles(V2i tile) {
		return Stream.of(Direction.values()).map(dir -> tile.sum(dir.vec));
	}

	@Override
	public int xTiles() {
		return map.sizeX();
	}

	@Override
	public int yTiles() {
		return map.sizeY();
	}

	@Override
	public byte data(V2i tile) {
		return map.data(tile);
	}

	@Override
	public boolean insideMap(V2i tile) {
		return 0 <= tile.x && tile.x < map.sizeX() && 0 <= tile.y && tile.y < map.sizeY();
	}

	@Override
	public V2i pacHome() {
		return pacHome;
	}

	@Override
	public V2i ghostHome(int ghost) {
		return ghostHomes[ghost];
	}

	@Override
	public V2i ghostScatterTile(int ghost) {
		return ghostScatterTargets[ghost];
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
	public void setUpwardsBlocked(V2i... tiles) {
		upwardsBlockedTiles = Arrays.asList(tiles);
	}

	@Override
	public boolean isUpwardsBlocked(V2i tile) {
		return upwardsBlockedTiles.contains(tile);
	}

	@Override
	public V2i houseEntry() {
		return houseEntry;
	}

	@Override
	public V2i houseCenter() {
		return houseMiddle;
	}

	@Override
	public V2i houseLeft() {
		return houseLeft;
	}

	@Override
	public V2i houseRight() {
		return houseRight;
	}

	@Override
	public boolean isWall(V2i tile) {
		return insideMap(tile) && data(tile) == WALL;
	}

	@Override
	public boolean isTunnel(V2i tile) {
		return insideMap(tile) && data(tile) == TUNNEL;
	}

	@Override
	public boolean isGhostHouseDoor(V2i tile) {
		return insideMap(tile) && data(tile) == DOOR;
	}

	@Override
	public boolean isPortal(V2i tile) {
		return portalsLeft.contains(tile) || portalsRight.contains(tile);
	}

	private boolean isInsideGhostHouse(V2i tile) {
		return tile.x >= 10 && tile.x <= 17 && tile.y >= 15 && tile.y <= 22;
	}

	@Override
	public boolean isIntersection(V2i tile) {
		return intersections.get(index(tile));
	}

	@Override
	public boolean isFoodTile(V2i tile) {
		return insideMap(tile) && (data(tile) == PILL || data(tile) == ENERGIZER);
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
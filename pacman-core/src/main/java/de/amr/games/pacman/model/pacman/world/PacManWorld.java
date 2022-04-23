/**
 * 
 */
package de.amr.games.pacman.model.pacman.world;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.lib.Misc.trim;
import static java.util.function.Predicate.not;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.GhostHouse;
import de.amr.games.pacman.model.common.world.Portal;
import de.amr.games.pacman.model.common.world.World;

/**
 * Pac-Man game world.
 * 
 * @author Armin Reichert
 */
public class PacManWorld implements World {

	private String[] map = {
		//@formatter:off
		"############################",
		"############################",
		"############################",
		"############################",
		"#............##............#",
		"#.####.#####.##.#####.####.#",
		"#*####.#####.##.#####.####*#",
		"#.####.#####.##.#####.####.#",
		"#..........................#",
		"#.####.##.########.##.####.#",
		"#.####.##.########.##.####.#",
		"#......##....##....##......#",
		"######.##### ## #####.######",
		"     #.##### ## #####.#     ",
		"     #.##          ##.#     ",
		"     #.## ###LR### ##.#     ",
		"######.## #      # ##.######",
		"TTTTTT.   #      #   .TTTTTT",
		"######.## #      # ##.######",
		"     #.## ######## ##.#     ",
		"     #.##          ##.#     ",
		"     #.## ######## ##.#     ",
		"######.## ######## ##.######",
		"#............##............#",
		"#.####.#####.##.#####.####.#",
		"#.####.#####.##.#####.####.#",
		"#*..##.......  .......##..*#",
		"###.##.##.########.##.##.###",
		"###.##.##.########.##.##.###",
		"#......##....##....##......#",
		"#.##########.##.##########.#",
		"#.##########.##.##########.#",
		"#..........................#",
		"############################",
		"############################",
		"############################",
		//@formatter:on
	};

	private List<Portal> portals;
	private List<V2i> upwardsBlockedTiles;
	private BitSet intersections;
	private GhostHouse house;
	private BitSet eaten;
	private int totalFoodCount;
	private int foodRemaining;

	private static V2i v(int x, int y) {
		return new V2i(x, y);
	}

	private char map(V2i tile) {
		if (insideWorld(tile)) {
			return map[tile.y].charAt(tile.x);
		}
		return ' ';
	}

	private boolean isDoor(V2i tile) {
		return isLeftDoorWing(tile) || isRightDoorWing(tile);
	}

	private Stream<V2i> neighbors(V2i tile) {
		return Stream.of(Direction.values()).map(dir -> tile.plus(dir.vec));
	}

	public PacManWorld() {
		house = new GhostHouse(v(10, 15), v(7, 4));
		house.entry = v(13, 14);
		house.seatLeft = v(11, 17);
		house.seatCenter = v(13, 17);
		house.seatRight = v(15, 17);
		house.doorTiles = trim(tiles().filter(this::isDoor).collect(Collectors.toList()));

		upwardsBlockedTiles = List.of(v(12, 13), v(15, 13), v(12, 25), v(15, 25));
		portals = List.of(new Portal(v(-1, 17), v(28, 17)));
		intersections = new BitSet();
		tiles() //
				.filter(tile -> !house.contains(tile)) //
				.filter(tile -> !isDoor(tile.plus(Direction.DOWN.vec))) //
				.filter(tile -> neighbors(tile).filter(not(this::isWall)).count() > 2) //
				.map(this::index) //
				.forEach(intersections::set);
	}

	@Override
	public int numCols() {
		return 28;
	}

	@Override
	public int numRows() {
		return 36;
	}

	@Override
	public V2i playerHomeTile() {
		return v(13, 26);
	}

	@Override
	public V2i ghostScatterTile(int ghostID) {
		return switch (ghostID) {
		case GameModel.RED_GHOST -> v(25, 0);
		case GameModel.PINK_GHOST -> v(2, 0);
		case GameModel.CYAN_GHOST -> v(27, 34);
		case GameModel.ORANGE_GHOST -> v(0, 34);
		default -> throw new IllegalArgumentException();
		};
	}

	@Override
	public Direction playerStartDirection() {
		return Direction.LEFT;
	}

	@Override
	public Direction ghostStartDirection(int ghostID) {
		return switch (ghostID) {
		case GameModel.RED_GHOST -> Direction.LEFT;
		case GameModel.PINK_GHOST -> Direction.DOWN;
		case GameModel.CYAN_GHOST -> Direction.UP;
		case GameModel.ORANGE_GHOST -> Direction.UP;
		default -> throw new IllegalArgumentException();
		};
	}

	@Override
	public Collection<Portal> portals() {
		return Collections.unmodifiableList(portals);
	}

	@Override
	public Portal randomPortal() {
		return portals.get(new Random().nextInt(portals.size()));
	}

	@Override
	public boolean isPortal(V2i tile) {
		return portals.stream().anyMatch(portal -> portal.left.equals(tile) || portal.right.equals(tile));
	}

	@Override
	public boolean isOneWayDown(V2i tile) {
		return upwardsBlockedTiles.contains(tile);
	}

	@Override
	public boolean isIntersection(V2i tile) {
		return intersections.get(index(tile));
	}

	@Override
	public boolean isWall(V2i tile) {
		return map(tile) == '#';
	}

	@Override
	public boolean isTunnel(V2i tile) {
		return map(tile) == 'T';
	}

	@Override
	public GhostHouse ghostHouse() {
		return house;
	}

	@Override
	public boolean isLeftDoorWing(V2i tile) {
		return map(tile) == 'L';
	}

	@Override
	public boolean isRightDoorWing(V2i tile) {
		return map(tile) == 'R';
	}

	@Override
	public boolean isFoodTile(V2i tile) {
		return map(tile) == '.' || map(tile) == '*';
	}

	@Override
	public boolean isEnergizerTile(V2i tile) {
		return map(tile) == '*';
	}

	@Override
	public Collection<V2i> energizerTiles() {
		return tiles().filter(this::isEnergizerTile).collect(Collectors.toList());
	}

	@Override
	public V2i bonusTile() {
		return v(13, 20);
	}

	@Override
	public int pelletsToEatForBonus(int bonusIndex) {
		return switch (bonusIndex) {
		case 0 -> 70;
		case 1 -> 170;
		default -> throw new IllegalArgumentException();
		};
	}

	@Override
	public void removeFood(V2i tile) {
		if (containsFood(tile)) {
			eaten.set(index(tile));
			--foodRemaining;
		}
	}

	@Override
	public boolean containsFood(V2i tile) {
		return isFoodTile(tile) && !isFoodEaten(tile);
	}

	@Override
	public boolean isFoodEaten(V2i tile) {
		return eaten.get(index(tile));
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
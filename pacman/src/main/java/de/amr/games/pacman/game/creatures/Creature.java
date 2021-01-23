package de.amr.games.pacman.game.creatures;

import static de.amr.games.pacman.game.worlds.PacManGameWorld.TS;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static java.lang.Math.abs;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.game.worlds.PacManGameWorld;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;

/**
 * Base class for Pac-Man, the ghosts and the bonus. Creatures can move through the maze.
 * 
 * @author Armin Reichert
 */
public class Creature {

	/** Left upper corner of TSxTS collision box. Sprites can be larger. */
	public V2f position = V2f.NULL;

	/** The current move direction. */
	public Direction dir;

	/** The intended move direction that will be taken as soon as possible. */
	public Direction wishDir;

	/** The tile that the guy tries to reach. Can be inaccessible or outside of the maze. */
	public V2i targetTile = V2i.NULL;

	/** Relative speed (between 0 and 1). */
	public float speed;

	/** If the creature is drawn on the screen. */
	public boolean visible;

	/** If the creature entered a new tile with its last movement or placement. */
	public boolean changedTile;

	/** If the creature could move in the last try. */
	public boolean couldMove;

	/** If the next move will in any case take the intended direction if possible. */
	public boolean forcedDirection;

	/** If movement is constrained to be aligned with the tiles. */
	public boolean forcedOnTrack;

	public final Random rnd = new Random();

	public void placeAt(V2i tile, float offsetX, float offsetY) {
		position = new V2f(tile.x * TS + offsetX, tile.y * TS + offsetY);
		changedTile = true;
	}

	public V2i tile() {
		return PacManGameWorld.tile(position);
	}

	public V2f offset() {
		return PacManGameWorld.offset(position);
	}

	public void setOffset(float offsetX, float offsetY) {
		placeAt(tile(), offsetX, offsetY);
	}

	public boolean canAccessTile(PacManGameWorld world, int x, int y) {
		if (world.isPortal(x, y)) {
			return true;
		}
		if (world.isGhostHouseDoor(x, y)) {
			return false;
		}
		return world.inMapRange(x, y) && !world.isWall(x, y);
	}

	public boolean canAccessTile(PacManGameWorld world, V2i tile) {
		return canAccessTile(world, tile.x, tile.y);
	}

	public void tryMoving(PacManGameWorld world) {
		V2i guyLocation = tile();
		// teleport?
		for (int i = 0; i < world.numPortals(); ++i) {
			if (guyLocation.equals(world.portalRight(i)) && dir == RIGHT) {
				placeAt(world.portalLeft(i), 0, 0);
				return;
			}
			if (guyLocation.equals(world.portalLeft(i)) && dir == LEFT) {
				placeAt(world.portalRight(i), 0, 0);
				return;
			}
		}
		tryMoving(world, wishDir);
		if (couldMove) {
			dir = wishDir;
		} else {
			tryMoving(world, dir);
		}
	}

	public void tryMoving(PacManGameWorld world, Direction moveDir) {
		// 100% speed corresponds to 1.25 pixels/tick (75px/sec)
		float pixels = speed * 1.25f;

		V2i guyLocationBeforeMove = tile();
		V2f offset = offset();
		V2i neighbor = guyLocationBeforeMove.sum(moveDir.vec);

		// check if guy can change its direction now
		if (forcedOnTrack && canAccessTile(world, neighbor)) {
			if (moveDir == LEFT || moveDir == RIGHT) {
				if (abs(offset.y) > pixels) {
					couldMove = false;
					return;
				}
				setOffset(offset.x, 0);
			} else if (moveDir == UP || moveDir == DOWN) {
				if (abs(offset.x) > pixels) {
					couldMove = false;
					return;
				}
				setOffset(0, offset.y);
			}
		}

		V2f velocity = new V2f(moveDir.vec).scaled(pixels);
		V2f newPosition = position.sum(velocity);
		V2i newTile = PacManGameWorld.tile(newPosition);
		V2f newOffset = PacManGameWorld.offset(newPosition);

		// block moving into inaccessible tile
		if (!canAccessTile(world, newTile)) {
			couldMove = false;
			return;
		}

		// align with edge of inaccessible neighbor
		if (!canAccessTile(world, neighbor)) {
			if (moveDir == RIGHT && newOffset.x > 0 || moveDir == LEFT && newOffset.x < 0) {
				setOffset(0, offset.y);
				couldMove = false;
				return;
			}
			if (moveDir == DOWN && newOffset.y > 0 || moveDir == UP && newOffset.y < 0) {
				setOffset(offset.x, 0);
				couldMove = false;
				return;
			}
		}

		placeAt(newTile, newOffset.x, newOffset.y);
		changedTile = !tile().equals(guyLocationBeforeMove);
		couldMove = true;
	}

	public void headForTargetTile(PacManGameWorld world) {
		newWishDir(world, false).ifPresent(newWishDir -> wishDir = newWishDir);
		tryMoving(world);
	}

	public Optional<Direction> newWishDir(PacManGameWorld world, boolean randomWalk) {
		if (couldMove && !changedTile) {
			return Optional.empty();
		}
		if (forcedDirection) {
			forcedDirection = false;
			return Optional.of(wishDir);
		}
		V2i location = tile();
		if (world.isPortal(location)) {
			return Optional.empty();
		}
		if (randomWalk) {
			return randomMoveDirection(world);
		}
		return targetDirection(world);
	}

	private static final Direction[] DIRECTION_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	public Optional<Direction> targetDirection(PacManGameWorld world) {
		double minDist = Double.MAX_VALUE;
		Direction minDistDir = null;
		for (Direction targetDir : DIRECTION_PRIORITY) {
			if (targetDir == dir.opposite()) {
				continue;
			}
			V2i neighbor = tile().sum(targetDir.vec);
			if (!canAccessTile(world, neighbor)) {
				continue;
			}
			double dist = neighbor.euclideanDistance(targetTile);
			if (dist < minDist) {
				minDist = dist;
				minDistDir = targetDir;
			}
		}
		return Optional.ofNullable(minDistDir);
	}

	public void wanderRandomly(PacManGameWorld world, float speed) {
		V2i location = tile();
		if (world.isIntersection(location) || !couldMove) {
			randomMoveDirection(world).ifPresent(randomDir -> wishDir = randomDir);
		}
		this.speed = speed;
		tryMoving(world);
	}

	public Optional<Direction> randomMoveDirection(PacManGameWorld world) {
		List<Direction> dirs = accessibleDirections(world, tile(), dir.opposite()).collect(Collectors.toList());
		return dirs.isEmpty() ? Optional.empty() : Optional.of(dirs.get(rnd.nextInt(dirs.size())));
	}

	public Stream<Direction> accessibleDirections(PacManGameWorld world, V2i tile, Direction... excludedDirections) {
		//@formatter:off
		return Stream.of(Direction.values())
			.filter(direction -> Stream.of(excludedDirections).noneMatch(excludedDir -> excludedDir == direction))
			.filter(direction -> world.isAccessible(tile.sum(direction.vec)));
		//@formatter:on
	}

	public void forceTurningBack() {
		wishDir = dir.opposite();
		forcedDirection = true;
	}
}
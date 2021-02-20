package de.amr.games.pacman.model;

import static de.amr.games.pacman.heaven.God.random;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static java.lang.Math.abs;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.world.PacManGameWorld;

/**
 * Base class for Pac-Man, Ms. Pac-Man the ghosts and the bonus. Creatures can move through the
 * world.
 * 
 * @author Armin Reichert
 */
public class Creature extends GameEntity {

	/** The world where this creature lives. */
	public PacManGameWorld world;

	/** Relative speed (between 0 and 1). */
	public float speed = 0.0f;

	/** The current move direction. Initially, (s)he moves to the right direction :-) */
	public Direction dir = Direction.RIGHT;

	/** The intended move direction that will be taken as soon as possible. */
	public Direction wishDir = Direction.RIGHT;

	/** The first move direction. */
	public Direction startDir = Direction.RIGHT;

	/** The tile that the guy tries to reach. Can be inaccessible or outside of the maze. */
	public V2i targetTile = V2i.NULL;

	/** If the creature entered a new tile with its last movement or placement. */
	public boolean changedTile = true;

	/** If the creature could move in the last try. */
	public boolean couldMove = true;

	/** If the next move will in any case take the intended direction if possible. */
	public boolean forcedDirection = false;

	/** If movement is constrained to be aligned with the tiles. */
	public boolean forcedOnTrack = false;

	/**
	 * Places this creature at the given tile with the given position offsets. Sets the
	 * {@code changedTile} flag to trigger a potential steering.
	 * 
	 * @param tile    the tile where this creature will be placed
	 * @param offsetX the pixel offset in x-direction
	 * @param offsetY the pixel offset in y-direction
	 */
	public void placeAt(V2i tile, float offsetX, float offsetY) {
		setPosition(tile.x * TS + offsetX, tile.y * TS + offsetY);
		changedTile = true;
	}

	/**
	 * @return the current tile position
	 */
	public V2i tile() {
		return PacManGameWorld.tile(position);
	}

	/**
	 * @return the current pixel offset
	 */
	public V2f offset() {
		return PacManGameWorld.offset(position);
	}

	public void setOffset(float offsetX, float offsetY) {
		placeAt(tile(), offsetX, offsetY);
	}

	public boolean canAccessTile(V2i tile) {
		if (world.insideMap(tile)) {
			if (world.isWall(tile)) {
				return false;
			}
			if (world.isGhostHouseDoor(tile)) {
				return false;
			}
			return true;
		} else {
			return world.isPortal(tile);
		}
	}

	public boolean meets(Creature other) {
		return tile().equals(other.tile());
	}

	@Override
	public void move() {
		velocity = new V2f(dir.vec).scaled(speed);
		position = position.sum(velocity);
	}

	public void tryMoving() {
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
		tryMoving(wishDir);
		if (couldMove) {
			dir = wishDir;
		} else {
			tryMoving(dir);
		}
	}

	public void tryMoving(Direction moveDir) {
		// 100% speed corresponds to 1.25 pixels/tick (75px/sec)
		float pixels = speed * 1.25f;

		V2i guyLocationBeforeMove = tile();
		V2f offset = offset();
		V2i neighbor = guyLocationBeforeMove.sum(moveDir.vec);

		// check if guy can change its direction now
		if (forcedOnTrack && canAccessTile(neighbor)) {
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

		velocity = new V2f(moveDir.vec).scaled(pixels);
		V2f newPosition = position.sum(velocity);
		V2i newTile = PacManGameWorld.tile(newPosition);
		V2f newOffset = PacManGameWorld.offset(newPosition);

		// block moving into inaccessible tile
		if (!canAccessTile(newTile)) {
			couldMove = false;
			return;
		}

		// align with edge of inaccessible neighbor
		if (!canAccessTile(neighbor)) {
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

	public void headForTargetTile() {
		newWishDir(false).ifPresent(newWishDir -> wishDir = newWishDir);
		tryMoving();
	}

	public Optional<Direction> newWishDir(boolean randomWalk) {
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
			return randomMoveDirection();
		}
		return targetDirection();
	}

	private static final Direction[] DIRECTION_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	public Optional<Direction> targetDirection() {
		double minDist = Double.MAX_VALUE;
		Direction minDistDir = null;
		for (Direction targetDir : DIRECTION_PRIORITY) {
			if (targetDir == dir.opposite()) {
				continue;
			}
			V2i neighbor = tile().sum(targetDir.vec);
			if (!canAccessTile(neighbor)) {
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

	public void wanderRandomly() {
		V2i location = tile();
		if (world.isIntersection(location) || !couldMove) {
			randomMoveDirection().ifPresent(randomDir -> wishDir = randomDir);
		}
		tryMoving();
	}

	public Optional<Direction> randomMoveDirection() {
		List<Direction> dirs = accessibleDirections(tile(), dir.opposite()).collect(Collectors.toList());
		return dirs.isEmpty() ? Optional.empty() : Optional.of(dirs.get(random.nextInt(dirs.size())));
	}

	public Stream<Direction> accessibleDirections(V2i tile, Direction... excludedDirections) {
		//@formatter:off
		return Stream.of(Direction.values())
			.filter(direction -> Stream.of(excludedDirections).noneMatch(excludedDir -> excludedDir == direction))
			.filter(direction -> canAccessTile(tile.sum(direction.vec)));
		//@formatter:on
	}

	public void forceTurningBack() {
		wishDir = dir.opposite();
		forcedDirection = true;
	}
}
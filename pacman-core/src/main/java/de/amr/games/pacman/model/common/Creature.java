package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;
import static java.lang.Math.abs;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.world.PacManGameWorld;

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
	public double speed = 0.0;

	/**
	 * The current move direction. Initially, (s)he moves to the right direction :-)
	 */
	public Direction dir = Direction.RIGHT;

	/** The intended move direction that will be taken as soon as possible. */
	public Direction wishDir = Direction.RIGHT;

	/** The first move direction. */
	public Direction startDir = Direction.RIGHT;

	/** The target tile, can be inaccessible or outside of the maze. */
	public V2i targetTile = V2i.NULL;

	/** If the creature entered a new tile with its last movement or placement. */
	public boolean changedTile = true;

	/** If the creature got stuck in the maze. */
	public boolean stuck = false;

	/** If the next move must take the intended direction. */
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
	public void placeAt(V2i tile, double offsetX, double offsetY) {
		setPosition(t(tile.x) + offsetX, t(tile.y) + offsetY);
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
	public V2d offset() {
		return PacManGameWorld.offset(position);
	}

	public void setOffset(double offsetX, double offsetY) {
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

	public void forceTurningBack() {
		wishDir = dir.opposite();
		forcedDirection = true;
	}

	@Override
	public void move() {
		velocity = new V2d(dir.vec).scaled(speed);
		position = position.plus(velocity);
	}

	public void tryMoving() {
		V2i guyLocation = tile();
		// teleport?
		for (int i = 0; i < world.numPortals(); ++i) {
			if (guyLocation.equals(world.portalRight(i)) && dir == Direction.RIGHT) {
				placeAt(world.portalLeft(i), 0, 0);
				return;
			}
			if (guyLocation.equals(world.portalLeft(i)) && dir == Direction.LEFT) {
				placeAt(world.portalRight(i), 0, 0);
				return;
			}
		}
		tryMoving(wishDir);
		if (!stuck) {
			dir = wishDir;
		} else {
			tryMoving(dir);
		}
	}

	public void tryMoving(Direction moveDir) {
		// 100% speed corresponds to 1.25 pixels/tick (75px/sec)
		double pixels = speed * 1.25f;

		V2i guyLocationBeforeMove = tile();
		V2d offset = offset();
		V2i neighbor = guyLocationBeforeMove.plus(moveDir.vec);

		// check if guy can change its direction now
		if (forcedOnTrack && canAccessTile(neighbor)) {
			if (moveDir == Direction.LEFT || moveDir == Direction.RIGHT) {
				if (abs(offset.y) > pixels) {
					stuck = true;
					return;
				}
				setOffset(offset.x, 0);
			} else if (moveDir == Direction.UP || moveDir == Direction.DOWN) {
				if (abs(offset.x) > pixels) {
					stuck = true;
					return;
				}
				setOffset(0, offset.y);
			}
		}

		velocity = new V2d(moveDir.vec).scaled(pixels);
		V2d newPosition = position.plus(velocity);
		V2i newTile = PacManGameWorld.tile(newPosition);
		V2d newOffset = PacManGameWorld.offset(newPosition);

		// block moving into inaccessible tile
		if (!canAccessTile(newTile)) {
			stuck = true;
			return;
		}

		// align with edge of inaccessible neighbor
		if (!canAccessTile(neighbor)) {
			if (moveDir == Direction.RIGHT && newOffset.x > 0 || moveDir == Direction.LEFT && newOffset.x < 0) {
				setOffset(0, offset.y);
				stuck = true;
				return;
			}
			if (moveDir == Direction.DOWN && newOffset.y > 0 || moveDir == Direction.UP && newOffset.y < 0) {
				setOffset(offset.x, 0);
				stuck = true;
				return;
			}
		}

		placeAt(newTile, newOffset.x, newOffset.y);
		changedTile = !tile().equals(guyLocationBeforeMove);
		stuck = false;
	}

	public void headForTargetTile() {
		newWishDir(false).ifPresent(newWishDir -> wishDir = newWishDir);
		tryMoving();
	}

	public Optional<Direction> newWishDir(boolean randomWalk) {
		if (!stuck && !changedTile) {
			return Optional.empty();
		}
		if (forcedDirection) {
			forcedDirection = false;
			return Optional.of(wishDir);
		}
		if (world.isPortal(tile())) {
			return Optional.empty();
		}
		if (randomWalk) {
			return randomMoveDirection();
		}
		return targetDirection();
	}

	private static final Direction[] DIRECTION_PRIORITY = { Direction.UP, Direction.LEFT, Direction.DOWN,
			Direction.RIGHT };

	/**
	 * As described in the Pac-Man dossier: ghosts check all accessble neighbor tiles in the order
	 * UP,LEFT,DOWN,RIGHT and select the tile with the minimal distance to the current target tile.
	 * Reversing the current direction is not allowed.
	 * 
	 * @return next direction Pac-Man will take
	 */
	private Optional<Direction> targetDirection() {
		V2i currentTile = tile();
		double minDist = Double.MAX_VALUE;
		Direction minDistDir = null;
		for (Direction targetDir : DIRECTION_PRIORITY) {
			if (targetDir == dir.opposite()) {
				continue;
			}
			V2i neighbor = currentTile.plus(targetDir.vec);
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

	public void walkRandomly() {
		if (stuck || world.isIntersection(tile())) {
			wishDir = randomMoveDirection().orElse(wishDir);
		}
		tryMoving();
	}

	private Optional<Direction> randomMoveDirection() {
		List<Direction> dirs = accessibleDirections(tile(), dir.opposite()).collect(Collectors.toList());
		return dirs.isEmpty() ? Optional.empty() : Optional.of(dirs.get(new Random().nextInt(dirs.size())));
	}

	private Stream<Direction> accessibleDirections(V2i tile, Direction... excludedDirections) {
		//@formatter:off
		return Stream.of(Direction.values())
			.filter(direction -> Stream.of(excludedDirections).noneMatch(excludedDir -> excludedDir == direction))
			.filter(direction -> canAccessTile(tile.plus(direction.vec)));
		//@formatter:on
	}
}
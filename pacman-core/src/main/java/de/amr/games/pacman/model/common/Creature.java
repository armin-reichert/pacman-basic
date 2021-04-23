package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
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
 * Base class for creatures which can move through the world.
 * 
 * @author Armin Reichert
 */
public class Creature extends GameEntity {

	private static final Direction[] PRIORITY = { UP, LEFT, DOWN, RIGHT };

	/** The world where this creature lives. */
	public PacManGameWorld world;

	/** Relative speed (between 0 and 1). */
	public double speed = 0.0;

	/**
	 * The current move direction. Initially, (s)he moves to the right direction :-)
	 */
	private Direction dir = RIGHT;

	/** The intended move direction that will be taken as soon as possible. */
	private Direction wishDir = RIGHT;

	/** The first move direction. */
	public Direction startDir = RIGHT;

	/** The target tile, can be inaccessible or outside of the world! */
	public V2i targetTile = V2i.NULL;

	/** If the creature entered a new tile with its last movement or placement. */
	public boolean newTileEntered = true;

	/** If the creature got stuck in the world. */
	public boolean stuck = false;

	/** If the created is forced take its current wish direction. */
	public boolean forced = false;

	/**
	 * If movement is constrained to be aligned with the "track" defined by the
	 * tiles.
	 */
	public boolean forcedOnTrack = false;

	/**
	 * Places this creature at the given tile with the given position offsets. Sets
	 * the {@link #newTileEntered} flag to trigger steering.
	 * 
	 * @param tile    the tile where this creature will be placed
	 * @param offsetX the pixel offset in x-direction
	 * @param offsetY the pixel offset in y-direction
	 */
	public void placeAt(V2i tile, double offsetX, double offsetY) {
		setPosition(t(tile.x) + offsetX, t(tile.y) + offsetY);
		newTileEntered = true;
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

	/**
	 * Places the creature on its current tile with given offset. This is for
	 * example used to place the ghosts exactly between two tiles.
	 * 
	 * @param offsetX offset in x-direction
	 * @param offsetY offset in y-direction
	 */
	public void setOffset(double offsetX, double offsetY) {
		placeAt(tile(), offsetX, offsetY);
	}

	public void setDir(Direction d) {
		dir = d;
	}

	public Direction dir() {
		return dir;
	}

	public void setWishDir(Direction d) {
		wishDir = d;
	}

	public Direction wishDir() {
		return wishDir;
	}

	public boolean canAccessTile(V2i tile) {
		if (world.insideMap(tile)) {
			return !world.isWall(tile) && !world.isGhostHouseDoor(tile);
		} else {
			return world.isPortal(tile);
		}
	}

	public boolean meets(Creature other) {
		return tile().equals(other.tile());
	}

	public void forceTurningBack() {
		wishDir = dir.opposite();
		forced = true;
	}

	@Override
	public void move() {
		velocity = new V2d(dir.vec).scaled(speed);
		position = position.plus(velocity);
	}

	public void tryMoving() {
		V2i currentTile = tile();
		// teleport?
		if (dir == RIGHT) {
			for (int i = 0; i < world.numPortals(); ++i) {
				if (currentTile.equals(world.portalRight(i))) {
					placeAt(world.portalLeft(i), 0, 0);
					return;
				}
			}
		} else if (dir == LEFT) {
			for (int i = 0; i < world.numPortals(); ++i) {
				if (currentTile.equals(world.portalLeft(i))) {
					placeAt(world.portalRight(i), 0, 0);
					return;
				}
			}
		}
		tryMovingTowards(wishDir);
		if (!stuck) {
			dir = wishDir;
		} else {
			tryMovingTowards(dir);
		}
	}

	public void tryMovingTowards(Direction d) {
		// 100% speed corresponds to 1.25 pixels/tick (75px/sec at 60Hz)
		final double moveDistance = speed * 1.25f;
		final V2i tileBefore = tile();
		final V2d offset = offset();
		final V2i neighbor = tileBefore.plus(d.vec);

		// check if guy can change its direction at this position
		if (forcedOnTrack && canAccessTile(neighbor)) {
			if (d == LEFT || d == RIGHT) {
				if (abs(offset.y) > moveDistance) {
					stuck = true;
					return;
				}
				setOffset(offset.x, 0);
			} else if (d == UP || d == DOWN) {
				if (abs(offset.x) > moveDistance) {
					stuck = true;
					return;
				}
				setOffset(0, offset.y);
			}
		}

		velocity = new V2d(d.vec).scaled(moveDistance);

		final V2d newPosition = position.plus(velocity);
		final V2i newTile = PacManGameWorld.tile(newPosition);
		final V2d newOffset = PacManGameWorld.offset(newPosition);

		// avoid moving into inaccessible neighbor tile
		if (!canAccessTile(newTile)) {
			stuck = true;
			return;
		}

		// align with edge of inaccessible neighbor
		if (!canAccessTile(neighbor)) {
			if (d == RIGHT && newOffset.x > 0 || d == LEFT && newOffset.x < 0) {
				setOffset(0, offset.y);
				stuck = true;
				return;
			}
			if (d == DOWN && newOffset.y > 0 || d == UP && newOffset.y < 0) {
				setOffset(offset.x, 0);
				stuck = true;
				return;
			}
		}

		// yes, we can (move)
		stuck = false;
		placeAt(newTile, newOffset.x, newOffset.y);
		newTileEntered = !tile().equals(tileBefore);
	}

	public void selectDirectionTowardsTarget() {
		newWishDir().ifPresent(this::setWishDir);
	}

	public void selectRandomDirection() {
		if (stuck || world.isIntersection(tile())) {
			randomAccessibleDirection().ifPresent(this::setWishDir);
		}
	}

	protected Optional<Direction> randomAccessibleDirection() {
		List<Direction> dirs = accessibleFromExcept(tile(), dir.opposite()).collect(Collectors.toList());
		return dirs.isEmpty() ? Optional.empty() : Optional.of(dirs.get(new Random().nextInt(dirs.size())));
	}

	private Optional<Direction> newWishDir() {
		if (forced) {
			forced = false;
			return Optional.of(wishDir);
		}
		if (!stuck && !newTileEntered) {
			return Optional.empty();
		}
		if (world.isPortal(tile())) {
			return Optional.empty();
		}
		return bestDirectionTowardsTargetTile();
	}

	/**
	 * As described in the Pac-Man dossier: checks all accessible neighbor tiles in
	 * order UP, LEFT, DOWN, RIGHT and selects the one with smallest Euclidean
	 * distance to the target tile. Reversing the current direction is not allowed.
	 * 
	 * @return next direction creature wants to take
	 */
	private Optional<Direction> bestDirectionTowardsTargetTile() {
		if (targetTile == null) {
			return Optional.empty();
		}
		final V2i currentTile = tile();
		double minDist = Double.MAX_VALUE;
		Direction bestDir = null;
		for (Direction d : PRIORITY) {
			if (d == dir.opposite()) {
				continue;
			}
			V2i neighbor = currentTile.plus(d.vec);
			if (canAccessTile(neighbor)) {
				double dist = neighbor.euclideanDistance(targetTile);
				if (dist < minDist) {
					minDist = dist;
					bestDir = d;
				}
			}
		}
		return Optional.ofNullable(bestDir);
	}

	private Stream<Direction> accessibleFromExcept(V2i tile, Direction... excludedDirs) {
		//@formatter:off
		return Stream.of(Direction.values())
			.filter(d -> Stream.of(excludedDirs).noneMatch(excluded -> excluded == d))
			.filter(d -> canAccessTile(tile.plus(d.vec)));
		//@formatter:on
	}
}
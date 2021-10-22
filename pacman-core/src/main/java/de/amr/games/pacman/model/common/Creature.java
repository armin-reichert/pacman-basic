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
package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;
import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.model.world.Portal;

/**
 * Base class for creatures which can move through the world.
 * 
 * @author Armin Reichert
 */
public class Creature extends GameEntity {

	private static final Direction[] TURN_PRIORITY = { Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT };

	/** Readable name, for display and logging purposes. */
	public final String name;

	/** The world where this creature lives. */
	public PacManGameWorld world;

	/** Relative speed (fraction of full speed), value between 0 and 1. */
	protected double speed = 0.0;

	/** The current move direction. */
	protected Direction dir = Direction.RIGHT;

	/** The intended move direction. Will be taken as soon as possible. */
	protected Direction wishDir = Direction.RIGHT;

	/** The target tile. Can be inaccessible or outside of the world. */
	public V2i targetTile = V2i.NULL;

	/** If the creature entered a new tile with its last movement or placement. */
	public boolean newTileEntered = true;

	/** If the creature got stuck in the world. */
	public boolean stuck = false;

	/** If the created is forced take its current wish direction. */
	public boolean forced = false;

	/**
	 * If movement is constrained to be aligned with the "track" defined by the tiles.
	 */
	public boolean forcedOnTrack = false;

	public Creature(String name) {
		this.name = name;
	}

	/**
	 * Places this creature at the given tile with the given position offsets. Sets the
	 * {@link #newTileEntered} flag to trigger steering.
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
	 * Places the creature on its current tile with given offset. This is for example used to place the
	 * ghosts exactly between two tiles.
	 * 
	 * @param offsetX offset in x-direction
	 * @param offsetY offset in y-direction
	 */
	public void setOffset(double offsetX, double offsetY) {
		placeAt(tile(), offsetX, offsetY);
	}

	public void setDir(Direction dir) {
		this.dir = Objects.requireNonNull(dir);
		updateVelocity();
	}

	public Direction dir() {
		return dir;
	}

	public void setWishDir(Direction dir) {
		wishDir = dir;
	}

	public Direction wishDir() {
		return wishDir;
	}

	public double speed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
		updateVelocity();
	}

	private void updateVelocity() {
		velocity = new V2d(dir.vec).scaled(speed);
	}

	public void move() {
		position = position.plus(velocity);
	}

	public boolean canAccessTile(V2i tile) {
		if (world.insideWorld(tile)) {
			return !world.isWall(tile) && !world.ghostHouse().doorTiles().contains(tile);
		} else {
			return world.isPortal(tile);
		}
	}

	public boolean meets(Creature other) {
		return tile().equals(other.tile());
	}

	public void forceTurningBack() {
		if (canAccessTile(tile().plus(dir.opposite().vec))) {
			wishDir = dir.opposite();
			dir = wishDir;
			forced = true;
		}
	}

	public void tryMoving() {
		V2i currentTile = tile();
		// teleport?
		if (dir == Direction.RIGHT) {
			for (Portal portal : world.portals()) {
				if (currentTile.equals(portal.right)) {
					placeAt(portal.left, 0, 0);
					return;
				}
			}
		} else if (dir == Direction.LEFT) {
			for (Portal portal : world.portals()) {
				if (currentTile.equals(portal.left)) {
					placeAt(portal.right, 0, 0);
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

	public void tryMovingTowards(Direction direction) {
		// 100% speed corresponds to 1.25 pixels/tick (75px/sec at 60Hz)
		final double moveDistance = speed * 1.25f;
		final V2i tileBefore = tile();
		final V2d offset = offset();
		final V2i neighbor = tileBefore.plus(direction.vec);

		// check if guy can turn towards given direction at current position
		if (forcedOnTrack && canAccessTile(neighbor)) {
			if (direction == Direction.LEFT || direction == Direction.RIGHT) {
				if (abs(offset.y) > moveDistance) {
					stuck = true;
					return;
				}
				setOffset(offset.x, 0);
			} else if (direction == Direction.UP || direction == Direction.DOWN) {
				if (abs(offset.x) > moveDistance) {
					stuck = true;
					return;
				}
				setOffset(0, offset.y);
			}
		}

		velocity = new V2d(direction.vec).scaled(moveDistance);

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
			if (direction == Direction.RIGHT && newOffset.x > 0 || direction == Direction.LEFT && newOffset.x < 0) {
				setOffset(0, offset.y);
				stuck = true;
				return;
			}
			if (direction == Direction.DOWN && newOffset.y > 0 || direction == Direction.UP && newOffset.y < 0) {
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

	public void setRandomDirection() {
		if (!newTileEntered && !stuck) {
			return;
		}
		List<Direction> dirs = new ArrayList<>();
		for (Direction dir : Direction.values()) {
			if (dir != this.dir.opposite() && canAccessTile(tile().plus(dir.vec))) {
				dirs.add(dir);
			}
		}
		if (!dirs.isEmpty()) {
			Direction randomDir = dirs.get(new Random().nextInt(dirs.size()));
			setWishDir(randomDir);
		}
	}

	/**
	 * As described in the Pac-Man dossier: checks all accessible neighbor tiles in order UP, LEFT,
	 * DOWN, RIGHT and selects the one with smallest Euclidean distance to the target tile. Reversing
	 * the move direction is not allowed.
	 */
	public void setDirectionTowardsTarget() {
		if (forced) {
			forced = false;
			return;
		}
		if (!stuck && !newTileEntered) {
			return;
		}
		if (world.isPortal(tile())) {
			return;
		}
		if (targetTile == null) {
			return;
		}
		final V2i currentTile = tile();
		double minDist = Double.MAX_VALUE;
		for (Direction direction : TURN_PRIORITY) {
			if (direction == dir.opposite()) {
				continue;
			}
			final V2i neighborTile = currentTile.plus(direction.vec);
			if (canAccessTile(neighborTile)) {
				final double distanceToTargetTile = neighborTile.euclideanDistance(targetTile);
				if (distanceToTargetTile < minDist) {
					minDist = distanceToTargetTile;
					wishDir = direction;
				}
			}
		}
	}
}
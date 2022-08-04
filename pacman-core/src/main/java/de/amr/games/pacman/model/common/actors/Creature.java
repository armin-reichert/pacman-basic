/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.model.common.actors;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.tileAtPosition;

import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.World;

/**
 * Base class for creatures which can move through the world.
 * 
 * @author Armin Reichert
 */
public class Creature extends Entity {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	protected static final Direction[] TURN_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	/** Readable name, for display and logging purposes. */
	public final String name;

	/** The world where this creature moves through. May be null. */
	protected World world;

	/** The current move direction. */
	protected Direction moveDir = RIGHT;

	/** The wish direction. Will be taken as soon as possible. */
	protected Direction wishDir = RIGHT;

	/** The target tile. Can be inaccessible or outside of the world. */
	public V2i targetTile = null;

	/** Tells if the creature entered a new tile with its last move or placement. */
	public boolean newTileEntered = true;

	/** Triggers reversing the move direction at next possible point in time. */
	public boolean reverse = false;

	/** Tells if the creature got stuck. */
	public boolean stuck = false;

	public Creature(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("%s: pos=%s, tile=%s, velocity=%s, speed=%.2f, dir=%s, wishDir=%s", name, position, tile(),
				velocity, velocity.length(), moveDir(), wishDir());
	}

	// bounding box is square of one tile
	public V2d center() {
		return position.plus(HTS, HTS);
	}

	// tile position is the tile containing the center of the bounding box
	public V2i tile() {
		return World.tileAtPosition(center());
	}

	public static V2d offset(V2d pos, V2i tile) {
		return pos.minus(World.positionAtTileLeftUpperCorner(tile));
	}

	// offset: (0, 0) if centered, range: [-4, +4)
	public V2d offset() {
		return offset(position, tile());
	}

	public boolean sameTile(Creature other) {
		Objects.requireNonNull(other);
		return tile().equals(other.tile());
	}

	public void placeAtTile(int tileX, int tileY, double offsetX, double offsetY) {
		setPosition(tileX * World.TS + offsetX, tileY * World.TS + offsetY);
		newTileEntered = true;
	}

	public void placeAtTile(V2i tile, double offsetX, double offsetY) {
		placeAtTile(tile.x(), tile.y(), offsetX, offsetY);
	}

	public void placeAtTile(V2i tile) {
		placeAtTile(tile.x(), tile.y(), 0, 0);
	}

	/**
	 * @param tile some tile inside or outside of the world
	 * @return if this creature can access the given tile
	 */
	public boolean canAccessTile(V2i tile) {
		if (world == null) {
			return false;
		}
		if (world.insideMap(tile)) {
			return !world.isWall(tile) && !world.ghostHouse().isDoorTile(tile);
		}
		return world.belongsToPortal(tile);
	}

	protected boolean isForbiddenDirection(Direction dir) {
		return dir == moveDir.opposite();
	}

	public Optional<World> getWorld() {
		return Optional.ofNullable(world);
	}

	public void setWorld(World world) {
		this.world = Objects.requireNonNull(world);
	}

	/**
	 * Sets the move direction and updates the velocity vector.
	 * 
	 * @param dir the new move direction
	 */
	public void setMoveDir(Direction dir) {
		moveDir = Objects.requireNonNull(dir);
		double speed = velocity.length();
		velocity = new V2d(dir.vec).scaled(speed);
	}

	public Direction moveDir() {
		return moveDir;
	}

	public void setWishDir(Direction dir) {
		wishDir = Objects.requireNonNull(dir);
	}

	public Direction wishDir() {
		return wishDir;
	}

	public void setBothDirs(Direction dir) {
		setMoveDir(dir);
		setWishDir(dir);
	}

	public void forceTurningBack() {
		reverse = true;
		LOGGER.info("%s got signal to reverse direction", name);
	}

	/**
	 * Sets the fraction of the given base speed.
	 * 
	 * @param fraction  fraction of base speed
	 * @param baseSpeed base speed (speed at fraction=1.0)
	 */
	protected void setRelSpeed(double fraction, double baseSpeed) {
		if (fraction < 0) {
			throw new IllegalArgumentException("Negative speed fraction: " + fraction);
		}
		setAbsSpeed(fraction * baseSpeed);
	}

	/**
	 * Sets the speed as a fraction of the game base speed (1.25 pixels/sec).
	 * 
	 * @param fraction fraction of base speed
	 */
	public void setRelSpeed(double fraction) {
		setRelSpeed(fraction, GameModel.BASE_SPEED);
	}

	/**
	 * Sets the absolute speed and updates the velocity vector.
	 * 
	 * @param pixelsPerTick speed in pixels per tick
	 */
	public void setAbsSpeed(double pixelsPerTick) {
		velocity = pixelsPerTick == 0 ? V2d.NULL : new V2d(moveDir.vec).scaled(pixelsPerTick);
	}

	public V2i tilesAhead(int n) {
		return tile().plus(moveDir.vec.scaled(n));
	}

	public V2i tilesAheadWithOverflowBug(int n) {
		return moveDir == UP ? tilesAhead(n).minus(n, 0) : tilesAhead(n);
	}

	public void tryReachingTargetTile() {
		computeDirectionTowardsTarget();
		tryMoving();
	}

	/**
	 * As described in the Pac-Man dossier: checks all accessible neighbor tiles in order UP, LEFT, DOWN, RIGHT and
	 * selects the one with smallest Euclidean distance to the target tile. Reversing the move direction is not allowed.
	 */
	public void computeDirectionTowardsTarget() {
		if (world == null || targetTile == null || world.belongsToPortal(tile())) {
			return;
		}
		if (!newTileEntered && !stuck) {
			return;
		}
		double minDist = Double.MAX_VALUE;
		for (var dir : TURN_PRIORITY) {
			var neighborTile = tile().plus(dir.vec);
			if (!isForbiddenDirection(dir) && canAccessTile(neighborTile)) {
				double d = neighborTile.euclideanDistance(targetTile);
				if (d < minDist) {
					minDist = d;
					wishDir = dir;
				}
			}
		}
	}

	/**
	 * Move through the world.
	 * <p>
	 * First checks if the creature can teleport, then if the creature can move to its wish direction. If this is not
	 * possible, it keeps moving to its current move direction.
	 */
	public void tryMoving() {
		world.portals().forEach(portal -> portal.teleport(this));
		if (reverse && newTileEntered) {
			wishDir = moveDir.opposite();
			reverse = false;
		}
		stuck = false;
		var moved = tryMoving(wishDir);
		if (!moved) {
			moved = tryMoving(moveDir);
			stuck = !moved;
		} else {
			moveDir = wishDir;
		}
	}

	protected boolean tryMoving(Direction dir) {
		return switch (dir) {
		case LEFT -> toDir(Direction.LEFT, 0, HTS);
		case RIGHT -> toDir(Direction.RIGHT, TS, HTS);
		case UP -> toDir(Direction.UP, HTS, 0);
		case DOWN -> toDir(Direction.DOWN, HTS, TS);
		};
	}

	private boolean sameOrientation(Direction d1, Direction d2) {
		return d1.isHorizontal() && d2.isHorizontal() || d1.isVertical() && d2.isVertical();
	}

	private boolean offsetAllowsTurningTo(Direction dir) {
		var offset = dir.isHorizontal() ? offset().y() : offset().x();
		return Math.abs(offset) <= 0.5;
	}

	private boolean toDir(Direction dir, double sx, double sy) {
		var newVelocity = new V2d(dir.vec).scaled(velocity.length());
		var sensorPosition = position.plus(sx, sy).plus(newVelocity);
		var sensorTile = tileAtPosition(sensorPosition);
		if (sameOrientation(moveDir, dir)) {
			if (!canAccessTile(sensorTile)) {
				placeAtTile(tile());
			} else {
				velocity = newVelocity;
				move();
				return true;
			}
		} else { // turn to dir
			if (canAccessTile(sensorTile) && offsetAllowsTurningTo(dir)) {
				velocity = newVelocity;
				move();
				return true;
			}
		}
		return false;
	}
}
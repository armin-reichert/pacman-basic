/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import static de.amr.games.pacman.lib.steering.Direction.DOWN;
import static de.amr.games.pacman.lib.steering.Direction.LEFT;
import static de.amr.games.pacman.lib.steering.Direction.RIGHT;
import static de.amr.games.pacman.lib.steering.Direction.UP;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.tileAt;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;

/**
 * Base class for all creatures which can move through the world.
 * 
 * @author Armin Reichert
 */
public class Creature extends Entity {

	protected static final Logger LOG = LogManager.getFormatterLogger();

	protected static final Direction[] DIRECTION_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	private final String name;
	private Direction moveDir;
	private Direction wishDir;
	private Vector2i targetTile;

	private MoveResult moveResult = new MoveResult();
	private boolean newTileEntered;
	private boolean tunnelEntered;
	private boolean stuck;

	protected boolean shouldReverse;
	protected boolean canTeleport;

	public Creature(String name) {
		this.name = (name != null) ? name : "Creature@%d".formatted(hashCode());
	}

	public void reset() {
		visible = false;
		position = Vector2f.ZERO;
		velocity = Vector2f.ZERO;
		acceleration = Vector2f.ZERO;
		moveDir = RIGHT;
		wishDir = RIGHT;
		targetTile = null;

		shouldReverse = false;
		canTeleport = true;

		newTileEntered = true;
		tunnelEntered = false;
		stuck = false;

		moveResult.reset();
	}

	@Override
	public String toString() {
		return "%s: pos=%s, tile=%s, velocity=%s, speed=%.2f, moveDir=%s, wishDir=%s".formatted(name, position, tile(),
				velocity, velocity.length(), moveDir(), wishDir());
	}

	/** Readable name, for display and logging purposes. */
	public String name() {
		return name;
	}

	/** Tells if the creature entered a new tile with its last move or placement. */
	public boolean isNewTileEntered() {
		return newTileEntered;
	}

	public void setNewTileEntered(boolean newTileEntered) {
		this.newTileEntered = newTileEntered;
	}

	public void setCanTeleport(boolean canTeleport) {
		this.canTeleport = canTeleport;
	}

	public boolean canTeleport() {
		return canTeleport;
	}

	/** Tells if the creature got stuck. */
	public boolean isStuck() {
		return stuck;
	}

	public boolean isTunnelEntered() {
		return tunnelEntered;
	}

	/**
	 * Sets the tile this creature tries to reach. May be an unreachable tile or <code>null</code>.
	 * 
	 * @param tile some tile or <code>null</code>
	 */
	public void setTargetTile(Vector2i tile) {
		targetTile = tile;
	}

	/** (Optional) target tile. Can be inaccessible or outside of the world. */
	public Optional<Vector2i> targetTile() {
		return Optional.ofNullable(targetTile);
	}

	public void placeAtTile(int tx, int ty, float ox, float oy) {
		var prevTile = tile();
		setPosition(tx * TS + ox, ty * TS + oy);
		newTileEntered = !tile().equals(prevTile);
	}

	public void placeAtTile(Vector2i tile, float ox, float oy) {
		GameModel.checkTileNotNull(tile);
		placeAtTile(tile.x(), tile.y(), ox, oy);
	}

	public void placeAtTile(Vector2i tile) {
		GameModel.checkTileNotNull(tile);
		placeAtTile(tile.x(), tile.y(), 0, 0);
	}

	/**
	 * @param tile some tile inside or outside of the world
	 * @return if this creature can access the given tile
	 */
	public boolean canAccessTile(Vector2i tile, GameLevel level) {
		GameModel.checkTileNotNull(tile);
		GameModel.checkLevelNotNull(level);
		if (level.world().insideBounds(tile)) {
			return !level.world().isWall(tile) && !level.world().ghostHouse().door().contains(tile);
		}
		return level.world().belongsToPortal(tile);
	}

	/**
	 * Sets the move direction and updates the velocity vector.
	 * 
	 * @param dir the new move direction
	 */
	public void setMoveDir(Direction dir) {
		GameModel.checkDirectionNotNull(dir);
		if (moveDir != dir) {
			moveDir = dir;
			LOG.trace("%-6s: New moveDir: %s. %s", name, moveDir, this);
			velocity = moveDir.vector().toFloatVec().scaled(velocity.length());
		}
	}

	/** The current move direction. */
	public Direction moveDir() {
		return moveDir;
	}

	public void setWishDir(Direction dir) {
		GameModel.checkDirectionNotNull(dir);
		if (wishDir != dir) {
			wishDir = dir;
			LOG.trace("%-6s: New wishDir: %s. %s", name, wishDir, this);
		}
	}

	/** The wish direction. Will be taken as soon as possible. */
	public Direction wishDir() {
		return wishDir;
	}

	public void setMoveAndWishDir(Direction dir) {
		setWishDir(dir);
		setMoveDir(dir);
	}

	public void reverseDirectionASAP() {
		shouldReverse = true;
		LOG.trace("%s (moveDir=%s, wishDir=%s) got signal to reverse direction", name, moveDir, wishDir);
	}

	/**
	 * @param level game level
	 * @return if the creature can reverse its direction
	 */
	public boolean canReverse(GameLevel level) {
		return newTileEntered;
	}

	/**
	 * Sets the speed as a fraction of the game base speed (1.25 pixels/sec).
	 * 
	 * @param fraction fraction of base speed
	 */
	public void setRelSpeed(float fraction) {
		if (fraction < 0) {
			throw new IllegalArgumentException("Negative speed fraction: " + fraction);
		}
		setPixelSpeed(fraction * GameModel.SPEED_100_PERCENT_PX);
	}

	/**
	 * Sets the absolute speed and updates the velocity vector.
	 * 
	 * @param pixels speed in pixels per tick
	 */
	public void setPixelSpeed(float pixels) {
		if (pixels < 0) {
			throw new IllegalArgumentException("Negative speed: " + pixels);
		}
		velocity = pixels == 0 ? Vector2f.ZERO : moveDir.vector().toFloatVec().scaled(pixels);
	}

	/**
	 * Sets the new wish direction for reaching the target tile.
	 * 
	 * @param game the game model
	 */
	public void navigateTowardsTarget(GameLevel level) {
		GameModel.checkLevelNotNull(level);
		if (!newTileEntered && !stuck) {
			return; // we don't need no navigation, dim dit diddit diddit dim dit diddit diddit...
		}
		if (targetTile == null) {
			return;
		}
		if (level.world().belongsToPortal(tile())) {
			return; // inside portal, no navigation happens
		}
		computeTargetDirection(level).ifPresent(this::setWishDir);
	}

	/*
	 * For each neighbor tile, compute distance to target tile, select direction with smallest distance.
	 */
	private Optional<Direction> computeTargetDirection(GameLevel level) {
		Direction targetDir = null;
		Vector2i currentTile = tile();
		float minDistance = Float.MAX_VALUE;
		for (var dir : DIRECTION_PRIORITY) {
			if (dir == moveDir.opposite()) {
				continue; // reversing the move direction is not allowed
			}
			var neighborTile = currentTile.plus(dir.vector());
			if (canAccessTile(neighborTile, level)) {
				float distance = neighborTile.euclideanDistance(targetTile);
				if (distance < minDistance) {
					minDistance = distance;
					targetDir = dir;
				}
			}
		}
		return Optional.ofNullable(targetDir);
	}

	private MoveResult tryTeleport(GameLevel level) {
		MoveResult mr = MoveResult.notMoved("No teleport");
		if (canTeleport) {
			for (var portal : level.world().portals()) {
				mr = portal.teleport(this);
				if (mr.teleported) {
					newTileEntered = true;
					break;
				}
			}
		}
		return mr;
	}

	/**
	 * Move through the world.
	 * <p>
	 * First checks if the creature can teleport, then if the creature can move to its wish direction. If this is not
	 * possible, it keeps moving to its current move direction.
	 */
	public void tryMoving(GameLevel level) {
		GameModel.checkLevelNotNull(level);
		Vector2i tileBeforeMove = tile();
		MoveResult mr = tryTeleport(level);
		if (mr.teleported) {
			return;
		}
		if (shouldReverse && canReverse(level)) {
			setWishDir(moveDir.opposite());
			shouldReverse = false;
		}
		mr = tryMoving(wishDir, level);
		if (mr.moved) {
			setMoveDir(wishDir);
		} else {
			mr = tryMoving(moveDir, level);
		}
		stuck = !mr.moved;
		newTileEntered = !tileBeforeMove.equals(tile());
		tunnelEntered = !level.world().isTunnel(tileBeforeMove) && level.world().isTunnel(tile());
		if (tunnelEntered) {
			LOG.info("%s entered tunnel", name);
		}
		if (mr.moved) {
			LOG.trace("%-6s: %s %s", name, mr.message, this);
		}
	}

	private MoveResult tryMoving(Direction dir, GameLevel level) {
		var aroundCorner = !dir.sameOrientation(moveDir);
		var dirVector = dir.vector().toFloatVec();
		var newVelocity = dirVector.scaled(velocity.length());
		var touchPosition = center().plus(dirVector.scaled(HTS)).plus(newVelocity);
		var touchedTile = tileAt(touchPosition);
		if (!canAccessTile(touchedTile, level)) {
			if (!aroundCorner) {
				placeAtTile(tile()); // adjust if blocked and moving forward
			}
			return MoveResult.notMoved("Not moved: Cannot move into tile %s", touchedTile);
		}
		if (aroundCorner) {
			if (atTurnPositionTo(dir)) {
				placeAtTile(tile()); // adjust if moving around corner
			} else {
				return MoveResult.notMoved("Wants to take corner towards %s but not at turn position", dir);
			}
		}
		setVelocity(newVelocity);
		move();
		return MoveResult.moved("Moved %5s (%.2f pixels)", dir, newVelocity.length());
	}

	private boolean atTurnPositionTo(Direction dir) {
		var offset = dir.isHorizontal() ? offset().y() : offset().x();
		return Math.abs(offset) <= 1;
	}
}
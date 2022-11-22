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
import static de.amr.games.pacman.model.common.world.World.originOfTile;
import static de.amr.games.pacman.model.common.world.World.tileAt;

import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;

/**
 * Base class for creatures which can move through the world.
 * 
 * @author Armin Reichert
 */
public class Creature extends Entity {

	// common logger for creature and subclasses
	protected static final Logger LOGGER = LogManager.getFormatterLogger();

	// make lint happy
	protected static final String MSG_GAME_NULL = "Game must not be null";
	protected static final String MSG_TILE_NULL = "Tile must not be null";

	protected static final Direction[] TURN_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	/** Readable name, for display and logging purposes. */
	public final String name;

	/** The current move direction. */
	private Direction moveDir;

	/** The wish direction. Will be taken as soon as possible. */
	private Direction wishDir;

	/** The target tile. Can be inaccessible or outside of the world. */
	private V2i targetTile;

	/** Tells if the creature entered a new tile with its last move or placement. */
	protected boolean newTileEntered;

	/** Triggers reversing the move direction at next possible point in time. */
	protected boolean reverse;

	/** Tells if the creature got stuck. */
	protected boolean stuck;

	/** Tells if the creature can get teleported. */
	protected boolean canTeleport;

	protected Creature(String name) {
		this.name = name;
		reset();
	}

	public void reset() {
		visible = false;
		position = V2d.NULL;
		velocity = V2d.NULL;
		acceleration = V2d.NULL;
		moveDir = RIGHT;
		wishDir = RIGHT;
		targetTile = null;
		newTileEntered = true;
		reverse = false;
		stuck = false;
		canTeleport = true;
	}

	@Override
	public String toString() {
		return String.format("%s: pos=%s, tile=%s, velocity=%s, speed=%.2f, dir=%s, wishDir=%s", name, position, tile(),
				velocity, velocity.length(), moveDir(), wishDir());
	}

	// position vector stores upper left corner of bounding box which is a square of one tile
	public V2d center() {
		return position.plus(HTS, HTS);
	}

	// current tile is the tile containing the center of the bounding box
	public V2i tile() {
		return tileAt(center());
	}

	// offset: (0, 0) if centered, range: [-4, +4)
	public V2d offset() {
		return position.minus(originOfTile(tile()));
	}

	public boolean isNewTileEntered() {
		return newTileEntered;
	}

	public boolean isStuck() {
		return stuck;
	}

	public void setTargetTile(V2i tile) {
		targetTile = tile;
	}

	public Optional<V2i> targetTile() {
		return Optional.ofNullable(targetTile);
	}

	public boolean sameTile(Creature other) {
		Objects.requireNonNull(other, "Creature must not be null");
		return tile().equals(other.tile());
	}

	public void placeAtTile(int tx, int ty, double ox, double oy) {
		var prevTile = tile();
		setPosition(tx * TS + ox, ty * TS + oy);
		newTileEntered = !tile().equals(prevTile);
	}

	public void placeAtTile(V2i tile, double ox, double oy) {
		Objects.requireNonNull(tile, MSG_TILE_NULL);
		placeAtTile(tile.x(), tile.y(), ox, oy);
	}

	public void placeAtTile(V2i tile) {
		Objects.requireNonNull(tile, MSG_TILE_NULL);
		placeAtTile(tile.x(), tile.y(), 0, 0);
	}

	/**
	 * @param tile some tile inside or outside of the world
	 * @return if this creature can access the given tile
	 */
	public boolean canAccessTile(V2i tile, GameModel game) {
		Objects.requireNonNull(tile, MSG_TILE_NULL);
		var world = game.level.world();
		if (world.insideMap(tile)) {
			return !world.isWall(tile) && !world.ghostHouse().isDoorTile(tile);
		}
		return world.belongsToPortal(tile);
	}

	/**
	 * Sets the move direction and updates the velocity vector.
	 * 
	 * @param dir the new move direction
	 */
	public void setMoveDir(Direction dir) {
		Objects.requireNonNull(dir, "Move direction must not be null");
		if (moveDir != dir) {
			moveDir = dir;
			LOGGER.trace("%-6s: New moveDir: %s. %s", name, moveDir, this);
			velocity = new V2d(moveDir.vec).scaled(velocity.length());
		}
	}

	public Direction moveDir() {
		return moveDir;
	}

	public void setWishDir(Direction dir) {
		Objects.requireNonNull(dir, "Wish direction must not be null");
		if (wishDir != dir) {
			wishDir = dir;
			LOGGER.trace("%-6s: New wishDir: %s. %s", name, wishDir, this);
		}
	}

	public Direction wishDir() {
		return wishDir;
	}

	public void setMoveAndWishDir(Direction dir) {
		Objects.requireNonNull(dir, "Move/Wish direction must not be null");
		setWishDir(dir);
		setMoveDir(dir);
	}

	public void forceTurningBack() {
		reverse = true;
		LOGGER.trace("%s (moveDir=%s, wishDir=%s) got signal to reverse direction", name, moveDir, wishDir);
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

	/**
	 * Sets a new direction for reaching the current target. Navigation is only triggered when a new tile is entered, the
	 * creature got stuck and a target tile is set. Inside portal tiles no navigation happens.
	 * 
	 * @param game the game model
	 */
	public void navigateTowardsTarget(GameModel game) {
		Objects.requireNonNull(game, MSG_GAME_NULL);
		if ((newTileEntered || stuck) && targetTile != null && !game.level.world().belongsToPortal(tile())) {
			bestDirection(game).ifPresent(this::setWishDir);
		}
	}

	/**
	 * @param game game model
	 * @return As described in the Pac-Man dossier: checks all accessible neighbor tiles in order UP, LEFT, DOWN, RIGHT
	 *         and selects the one with smallest Euclidean distance to the target tile. Reversing the move direction is
	 *         not allowed.
	 */
	private Optional<Direction> bestDirection(GameModel game) {
		Objects.requireNonNull(game, MSG_GAME_NULL);
		Direction bestDir = null;
		double minDist = Double.MAX_VALUE;
		for (var dir : TURN_PRIORITY) {
			var neighborTile = tile().plus(dir.vec);
			if (dir != moveDir.opposite() && canAccessTile(neighborTile, game)) {
				double d = neighborTile.euclideanDistance(targetTile);
				if (d < minDist) {
					minDist = d;
					bestDir = dir;
				}
			}
		}
		return Optional.ofNullable(bestDir);
	}

	/**
	 * Move through the world.
	 * <p>
	 * First checks if the creature can teleport, then if the creature can move to its wish direction. If this is not
	 * possible, it keeps moving to its current move direction.
	 */
	public void tryMoving(GameModel game) {
		Objects.requireNonNull(game, MSG_GAME_NULL);
		var tileBefore = tile();
		if (canTeleport) {
			for (var portal : game.level.world().portals()) {
				if (portal.teleport(this)) {
					break;
				}
			}
		}
		if (reverse && newTileEntered) {
			setWishDir(moveDir.opposite());
			reverse = false;
		}
		var result = tryMoving(wishDir, game);
		logMoveResult(result);
		if (result.moved()) {
			setMoveDir(wishDir);
		} else {
			result = tryMoving(moveDir, game);
			logMoveResult(result);
		}
		stuck = !result.moved();
		newTileEntered = !tileBefore.equals(tile());
	}

	private void logMoveResult(MoveResult result) {
		if (result.moved()) {
			LOGGER.trace(() -> "%-6s: %s %s".formatted(name, result.message(), this));
		} else {
			LOGGER.trace("%-6s: not moving", name);
		}
	}

	private MoveResult tryMoving(Direction dir, GameModel game) {
		Objects.requireNonNull(dir, "Direction must not be null");
		double speed = velocity.length();
		V2d dirVector = dir.vec.toDoubleVec();
		if (speed > 1.0) {
			var result = takeSmallStep(dir, game, dirVector, 0.5 * speed);
			if (result.moved()) {
				return takeSmallStep(dir, game, dirVector, 0.5 * speed);
			}
			return result;
		}
		return takeSmallStep(dir, game, dirVector, speed);
	}

	private MoveResult takeSmallStep(Direction dir, GameModel game, V2d dirVector, double speed) {
		var turn = !dir.sameOrientation(moveDir);
		var newVelocity = dirVector.scaled(speed);
		var touchPosition = center().plus(dirVector.scaled(HTS)).plus(newVelocity);

		if (!canAccessTile(tileAt(touchPosition), game)) {
			if (!turn) {
				placeAtTile(tile()); // adjust exactly over tile if blocked
			}
			return new MoveResult(false, "Not moved: Cannot access tile %s", tileAt(touchPosition));
		}

		if (turn && !atTurnPositionTo(dir)) {
			return new MoveResult(false, "Wants to move %s but not at turn position", dir);
		}

		setVelocity(newVelocity);
		move();
		return new MoveResult(true, "Moved %5s", dir);
	}

	protected boolean atTurnPositionTo(Direction dir) {
		var offset = dir.isHorizontal() ? offset().y() : offset().x();
		return Math.abs(offset) <= 0.5;
	}
}
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

	private static final Logger LOGGER = LogManager.getFormatterLogger();

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

	protected boolean canTeleport;

	protected Creature(String name) {
		this.name = name;
		reset();
	}

	@Override
	public void reset() {
		super.reset();
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

	// bounding box is square of one tile, position stores left upper corner
	public V2d center() {
		return position.plus(HTS - 0.1, HTS - 0.1);
	}

	// tile position is the tile containing the center of the bounding box
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
		return tile().equals(other.tile());
	}

	public boolean insideTunnel(GameModel game) {
		return game.world().isTunnel(tile());
	}

	public boolean insidePortal(GameModel game) {
		return game.world().belongsToPortal(tile());
	}

	public void placeAtTile(int tx, int ty, double ox, double oy) {
		setPosition(tx * TS + ox, ty * TS + oy);
		newTileEntered = true;
	}

	public void placeAtTile(V2i tile, double ox, double oy) {
		placeAtTile(tile.x(), tile.y(), ox, oy);
	}

	public void placeAtTile(V2i tile) {
		placeAtTile(tile.x(), tile.y(), 0, 0);
	}

	/**
	 * @param tile some tile inside or outside of the world
	 * @return if this creature can access the given tile
	 */
	public boolean canAccessTile(V2i tile, GameModel game) {
		var world = game.world();
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
		if (moveDir != dir) {
			moveDir = dir;
			velocity = new V2d(moveDir.vec).scaled(velocity.length());
			LOGGER.trace("%s: New moveDir: %s (tile: %s%s)", name, moveDir, tile(), reverse ? ", reverse" : "");
		}
	}

	public Direction moveDir() {
		return moveDir;
	}

	public void setWishDir(Direction dir) {
		if (wishDir != dir) {
			wishDir = dir;
			LOGGER.trace("%s: New wishDir: %s (tile %s%s)", name, wishDir, tile(), reverse ? ", reverse" : "");
		}
	}

	public Direction wishDir() {
		return wishDir;
	}

	public void setMoveAndWishDir(Direction dir) {
		setMoveDir(dir);
		setWishDir(dir);
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
	 * Bounces up and down at the given y-position.
	 * 
	 * @param zeroY  zero level y position
	 * @param extent max deviation from zero level
	 */
	protected void bounce(double zeroY, double extent) {
		if (position.y() <= zeroY - extent) {
			setMoveAndWishDir(DOWN);
		} else if (position.y() >= zeroY + extent) {
			setMoveAndWishDir(UP);
		}
		move();
	}

	/**
	 * Sets a new direction for reaching the current target. Navigation is only triggered when a new tile is entered, the
	 * creature got stuck and a target tile is set. Inside portal tiles no navigation happens.
	 * 
	 * @param game the game model
	 */
	public void navigateTowardsTarget(GameModel game) {
		if ((newTileEntered || stuck) && targetTile != null && !insidePortal(game)) {
			bestDirection(game).ifPresent(dir -> {
				setWishDir(dir);
				LOGGER.info("New wish dir: %6s for %s to target %s", wishDir, this, targetTile);
			});
		}
	}

	/**
	 * @param game game model
	 * @return As described in the Pac-Man dossier: checks all accessible neighbor tiles in order UP, LEFT, DOWN, RIGHT
	 *         and selects the one with smallest Euclidean distance to the target tile. Reversing the move direction is
	 *         not allowed.
	 */
	private Optional<Direction> bestDirection(GameModel game) {
//		if (tile().equals(targetTile)) {
//			return Optional.of(wishDir);
//		}
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
		var tileBefore = tile();
		if (canTeleport) {
			game.world().portals().forEach(portal -> portal.teleport(this));
		}
		if (reverse && newTileEntered) {
			setWishDir(moveDir.opposite());
			reverse = false;
		}
		var result = tryMoving(wishDir, game);
		if (result.moved()) {
			setMoveDir(wishDir);
		} else {
			LOGGER.info(result);
			result = tryMoving(moveDir, game);
			if (!result.moved()) {
				LOGGER.info(result);
			}
		}
		stuck = !result.moved();
		newTileEntered = !tileBefore.equals(tile());
	}

	/**
	 * @param newDir some direction
	 * @return if creature could move
	 */
	private MoveResult tryMoving(Direction newDir, GameModel game) {
		var newDirVec = new V2d(newDir.vec);
		var newVelocity = newDirVec.scaled(velocity.length());
		var touchPosition = center().plus(newDirVec.scaled(HTS)).plus(newVelocity);
		var canAccessTouchedTile = canAccessTile(tileAt(touchPosition), game);
		var isTurn = !newDir.sameOrientation(moveDir);

		MoveResult result = null;
		if (!canAccessTouchedTile) {
			if (!isTurn) {
				placeAtTile(tile());
			}
			result = new MoveResult(false, "Cannot access tile %s", tileAt(touchPosition));
		} else if (isTurn && !atTurnPositionTo(newDir)) {
			result = new MoveResult(false, "Wants to turn to %s but not at turn position", newDir);
		} else {
			result = new MoveResult(true, "");
			velocity = newVelocity;
			move();
		}
		return result;
	}

	protected boolean atTurnPositionTo(Direction dir) {
		var offset = dir.isHorizontal() ? offset().y() : offset().x();
		return Math.abs(offset) <= 0.5;
	}
}
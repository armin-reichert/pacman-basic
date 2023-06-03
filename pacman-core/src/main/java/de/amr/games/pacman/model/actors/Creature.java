/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkDirectionNotNull;
import static de.amr.games.pacman.lib.Globals.checkLevelNotNull;
import static de.amr.games.pacman.lib.Globals.checkTileNotNull;
import static de.amr.games.pacman.model.world.World.tileAt;

import java.util.List;
import java.util.Optional;

import org.tinylog.Logger;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.Portal;
import de.amr.games.pacman.model.world.World;

/**
 * Base class for all creatures which can live inside a game level and can move through its world.
 * 
 * @author Armin Reichert
 */
public abstract class Creature extends Entity {

	protected static final Direction[] DIRECTION_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	private Direction moveDir;
	private Direction wishDir;
	private Vector2i targetTile;

	private GameLevel level;

	private MoveResult moveResult;
	protected boolean newTileEntered; // TODO put this into move result but currently it has another lifetime
	protected boolean gotReverseCommand;
	protected boolean canTeleport;

	protected float corneringSpeedUp = 0;

	protected Creature(String name) {
		super(name);
	}

	@Override
	public String toString() {
		return "Creature [moveDir=" + moveDir + ", wishDir=" + wishDir + ", targetTile=" + targetTile + ", newTileEntered="
				+ newTileEntered + ", gotReverseCommand=" + gotReverseCommand + ", canTeleport=" + canTeleport
				+ ", corneringSpeedUp=" + corneringSpeedUp + ", name=" + name + ", visible=" + visible + ", position="
				+ position + ", velocity=" + velocity + ", acceleration=" + acceleration + "]";
	}

	public void reset() {
		// entity
		visible = false;
		position = Vector2f.ZERO;
		velocity = Vector2f.ZERO;
		acceleration = Vector2f.ZERO;

		moveDir = RIGHT;
		wishDir = RIGHT;
		targetTile = null;

		gotReverseCommand = false;
		canTeleport = true;

		moveResult = null;
		newTileEntered = true;
	}

	public GameLevel level() {
		return level;
	}

	protected World world() {
		return level.world();
	}

	public House house() {
		return world().house();
	}

	public boolean insideHouse() {
		return house().contains(tile());
	}

	protected GameModel game() {
		return level.game();
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(GameLevel level) {
		checkLevelNotNull(level);
		this.level = level;
	}

	/**
	 * @return if the creature can reverse its direction
	 */
	public abstract boolean canReverse();

	/** Tells if the creature entered a new tile with its last move or placement. */
	public boolean isNewTileEntered() {
		return newTileEntered;
	}

	/**
	 * Set teleport capability for this creature.
	 * 
	 * @param canTeleport if this creature can teleport
	 */
	public void setCanTeleport(boolean canTeleport) {
		this.canTeleport = canTeleport;
	}

	/**
	 * @return if this creature can teleport
	 */
	public boolean canTeleport() {
		return canTeleport;
	}

	/**
	 * Sets the tile this creature tries to reach. May be an unreachable tile or <code>null</code>.
	 * 
	 * @param tile some tile or <code>null</code>
	 */
	public void setTargetTile(Vector2i tile) {
		targetTile = tile;
	}

	/**
	 * @return (Optional) target tile. Can be inaccessible or outside of the world.
	 */
	public Optional<Vector2i> targetTile() {
		return Optional.ofNullable(targetTile);
	}

	/**
	 * Places this creature at the given tile coordinate with the given tile offsets. Updates the
	 * <code>newTileEntered</code> state.
	 * 
	 * @param tx tile x-coordinate (grid column)
	 * @param ty tile y-coordinate (grid row)
	 * @param ox x-offset inside tile
	 * @param oy y-offset inside tile
	 */
	public void placeAtTile(int tx, int ty, float ox, float oy) {
		var prevTile = tile();
		setPosition(tx * TS + ox, ty * TS + oy);
		newTileEntered = !tile().equals(prevTile);
	}

	/**
	 * Places this creature at the given tile coordinate with the given tile offsets. Updates the
	 * <code>newTileEntered</code> state.
	 * 
	 * @param tile tile
	 * @param ox   x-offset inside tile
	 * @param oy   y-offset inside tile
	 */
	public void placeAtTile(Vector2i tile, float ox, float oy) {
		checkTileNotNull(tile);
		placeAtTile(tile.x(), tile.y(), ox, oy);
	}

	/**
	 * Places this creature exactly (no offsets) at the given tile coordinate. Updates the <code>newTileEntered</code>
	 * state.
	 * 
	 * @param tile tile
	 */
	public void placeAtTile(Vector2i tile) {
		checkTileNotNull(tile);
		placeAtTile(tile.x(), tile.y(), 0, 0);
	}

	/**
	 * Simulates the overflow bug from the original Arcade version.
	 * 
	 * @param numTiles number of tiles
	 * @return the tile located the given number of tiles in front of the creature (towards move direction). In case
	 *         creature looks up, additional n tiles are added towards left. This simulates an overflow error in the
	 *         original Arcade game.
	 */
	public Vector2i tilesAheadBuggy(int numTiles) {
		Vector2i ahead = tile().plus(moveDir().vector().scaled(numTiles));
		return moveDir() == Direction.UP ? ahead.minus(numTiles, 0) : ahead;
	}

	/**
	 * @param tile some tile inside or outside of the world
	 * @return if this creature can access the given tile
	 */
	public boolean canAccessTile(Vector2i tile) {
		checkTileNotNull(tile);
		if (world().insideBounds(tile)) {
			return !world().isWall(tile) && !house().door().occupies(tile);
		}
		return world().belongsToPortal(tile);
	}

	/**
	 * Sets the move direction and updates the velocity vector.
	 * 
	 * @param dir the new move direction
	 */
	public void setMoveDir(Direction dir) {
		checkDirectionNotNull(dir);
		if (moveDir != dir) {
			moveDir = dir;
			Logger.trace("{}: New moveDir: {}. {}", name, moveDir, this);
			velocity = moveDir.vector().toFloatVec().scaled(velocity.length());
		}
	}

	/** @return The current move direction. */
	public Direction moveDir() {
		return moveDir;
	}

	/**
	 * Sets the wish direction and updates the velocity vector.
	 * 
	 * @param dir the new wish direction
	 */
	public void setWishDir(Direction dir) {
		checkDirectionNotNull(dir);
		if (wishDir != dir) {
			wishDir = dir;
			Logger.trace("{}: New wishDir: {}. {}", name, wishDir, this);
		}
	}

	/** @return The wish direction. Will be taken as soon as possible. */
	public Direction wishDir() {
		return wishDir;
	}

	/**
	 * Sets both directions at once.
	 * 
	 * @param dir the new wish and move direction
	 */
	public void setMoveAndWishDir(Direction dir) {
		setWishDir(dir);
		setMoveDir(dir);
	}

	/**
	 * Signals that this creature should reverse its move direction as soon as possible.
	 */
	public void reverseAsSoonAsPossible() {
		gotReverseCommand = true;
		newTileEntered = false;
		Logger.trace("{} (moveDir={}, wishDir={}) got command to reverse direction", name, moveDir, wishDir);
	}

	/**
	 * Sets the speed as a fraction of the base speed (1.25 pixels/sec).
	 * 
	 * @param fraction fraction of base speed
	 */
	public void setRelSpeed(float fraction) {
		if (fraction < 0) {
			throw new IllegalArgumentException("Negative speed fraction: " + fraction);
		}
		setPixelSpeed(fraction * GameModel.SPEED_PX_100_PERCENT);
	}

	/**
	 * Sets the absolute speed and updates the velocity vector.
	 * 
	 * @param pixelSpeed speed in pixels
	 */
	public void setPixelSpeed(float pixelSpeed) {
		if (pixelSpeed < 0) {
			throw new IllegalArgumentException("Negative pixel speed: " + pixelSpeed);
		}
		velocity = pixelSpeed == 0 ? Vector2f.ZERO : moveDir.vector().toFloatVec().scaled(pixelSpeed);
	}

	/**
	 * Sets the new wish direction for reaching the target tile.
	 */
	public void navigateTowardsTarget() {
		if (!newTileEntered && moved()) {
			return; // we don't need no navigation, dim dit diddit diddit dim dit diddit diddit...
		}
		if (targetTile == null) {
			return;
		}
		if (world().belongsToPortal(tile())) {
			return; // inside portal, no navigation happens
		}
		computeTargetDirection().ifPresent(this::setWishDir);
	}

	private Optional<Direction> computeTargetDirection() {
		final var currentTile = tile();
		Direction targetDir = null;
		float minDistance = Float.MAX_VALUE;
		for (var dir : DIRECTION_PRIORITY) {
			if (dir == moveDir.opposite()) {
				continue; // reversing the move direction is not allowed
			}
			final var neighborTile = currentTile.plus(dir.vector());
			if (canAccessTile(neighborTile)) {
				final var distance = neighborTile.euclideanDistance(targetTile);
				if (distance < minDistance) {
					minDistance = distance;
					targetDir = dir;
				}
			}
		}
		return Optional.ofNullable(targetDir);
	}

	public boolean moved() {
		return moveResult != null && moveResult.moved;
	}

	public boolean teleported() {
		return moveResult != null && moveResult.teleported;
	}

	public boolean enteredTunnel() {
		return moveResult != null && moveResult.tunnelEntered;
	}

	/**
	 * Tries moving through the game level.
	 * <p>
	 * First checks if the creature can teleport, then if the creature can move to its wish direction. If this is not
	 * possible, it keeps moving to its current move direction.
	 */
	public void tryMoving() {
		moveResult = new MoveResult();
		tryTeleport(world().portals());
		if (!moveResult.teleported) {
			checkReverseCommand();
			tryMoving(wishDir);
			if (moveResult.moved) {
				setMoveDir(wishDir);
			} else {
				tryMoving(moveDir);
			}
		}
		if (moveResult.teleported || moveResult.moved) {
			Logger.trace("{}: {} {} {}", name, moveResult, moveResult.summary(), this);
		}
	}

	private void checkReverseCommand() {
		if (gotReverseCommand && canReverse()) {
			setWishDir(moveDir.opposite());
			gotReverseCommand = false;
			Logger.trace("{}: [turned around]", name);
		}
	}

	private void tryTeleport(List<Portal> portals) {
		if (canTeleport) {
			for (var portal : portals) {
				teleport(portal);
				if (moveResult.teleported) {
					return;
				}
			}
		}
	}

	private void teleport(Portal portal) {
		var tile = tile();
		var oldPosition = position;
		if (tile.y() == portal.leftTunnelEnd().y() && position.x() < (portal.leftTunnelEnd().x() - portal.depth()) * TS) {
			placeAtTile(portal.rightTunnelEnd());
			moveResult.teleported = true;
			moveResult.messages.add(String.format("%s: Teleported from %s to %s", name, oldPosition, position));
		} else if (tile.equals(portal.rightTunnelEnd().plus(portal.depth(), 0))) {
			placeAtTile(portal.leftTunnelEnd().minus(portal.depth(), 0));
			moveResult.teleported = true;
			moveResult.messages.add(String.format("%s: Teleported from %s to %s", name, oldPosition, position));
		}
	}

	private void tryMoving(Direction dir) {
		final var tileBeforeMove = tile();
		final var aroundCorner = !dir.sameOrientation(moveDir);
		final var dirVector = dir.vector().toFloatVec();
		final var newVelocity = dirVector.scaled(velocity.length());
		final var touchPosition = center().plus(dirVector.scaled(HTS)).plus(newVelocity);
		final var touchedTile = tileAt(touchPosition);

		if (!canAccessTile(touchedTile)) {
			if (!aroundCorner) {
				placeAtTile(tile()); // adjust if blocked and moving forward
			}
			moveResult.messages.add(String.format("Cannot move %s into tile %s", dir, touchedTile));
			return;
		}

		if (aroundCorner) {
			var offset = dir.isHorizontal() ? offset().y() : offset().x();
			boolean atTurnPosition = Math.abs(offset) <= 1; // TODO <= pixelspeed?
			if (atTurnPosition) {
				placeAtTile(tile()); // adjust if moving around corner
			} else {
				moveResult.messages.add(String.format("Wants to take corner towards %s but not at turn position", dir));
				return;
			}
		}

		if (aroundCorner && corneringSpeedUp > 0) {
			setVelocity(newVelocity.plus(dirVector.scaled(corneringSpeedUp)));
			Logger.trace("{} velocity around corner: {}", name(), velocity.length());
			move();
		} else {
			setVelocity(newVelocity);
			move();
		}
		setVelocity(newVelocity);

		newTileEntered = !tileBeforeMove.equals(tile());
		moveResult.moved = true;
		moveResult.tunnelEntered = !world().isTunnel(tileBeforeMove) && world().isTunnel(tile());
		moveResult.messages.add(String.format("%5s (%.2f pixels)", dir, newVelocity.length()));
	}
}
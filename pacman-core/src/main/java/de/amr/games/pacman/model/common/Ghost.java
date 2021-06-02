package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.world.PacManGameWorld;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature {

	private static boolean differsAtMost(double value, double target, double tolerance) {
		return Math.abs(value - target) <= tolerance;
	}

	public static final int BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3, SUE = 3;

	/** The unique ID of the ghost (0..3). */
	public final int id;

	/** The current state of the ghost. */
	public GhostState state;

	/** The bounty earned for killing this ghost. */
	public int bounty;

	/**
	 * The individual food counter, used to compute when the ghost can leave the house.
	 */
	public int dotCounter;

	/**
	 * The "Cruise Elroy" mode of Blinky, the red ghost. Value is 1, 2 or -1, -2 (disabled Elroy mode).
	 */
	public int elroy;

	@Override
	public String toString() {
		return String.format("%s: state: %s position: %s, speed=%.2f, dir=%s, wishDir=%s", name, state, position, speed,
				dir(), wishDir());
	}

	public Ghost(int id, String name, PacManGameWorld world) {
		super(world, name);
		this.id = id;
	}

	public boolean is(GhostState ghostState) {
		return state == ghostState;
	}

	@Override
	public boolean canAccessTile(V2i tile) {
		if (world.ghostHouse().doorTiles().contains(tile)) {
			return is(GhostState.ENTERING_HOUSE) || is(GhostState.LEAVING_HOUSE);
		}
		// TODO there is still a bug causing ghost get stuck
		if (world.isOneWayDown(tile)) {
			if (offset().y != 0) {
				return true; // maybe already on the way up
			}
			return !is(GhostState.HUNTING_PAC);
		}
		return super.canAccessTile(tile);
	}

	public boolean atGhostHouseDoor() {
		return tile().equals(world.ghostHouse().entryTile()) && differsAtMost(offset().x, HTS, 2);
	}

	/**
	 * Lets the ghost return back to the ghost house.
	 * 
	 * @return {@code true} if the ghost has reached the house
	 */
	public boolean returnHome() {
		if (atGhostHouseDoor() && dir() != Direction.DOWN) {
			// house reached, start entering
			setOffset(HTS, 0);
			setDir(Direction.DOWN);
			setWishDir(Direction.DOWN);
			forcedOnTrack = false;
			targetTile = (id == BLINKY) ? world.ghostHouse().seat(1) : world.ghostHomeTile(id);
			state = GhostState.ENTERING_HOUSE;
			return true;
		}
		setDirectionTowardsTarget();
		tryMoving();
		return false;
	}

	/**
	 * Lets the ghost enter the house and moving to its home position.
	 * 
	 * @return {@code true} if the ghost has reached its home position
	 */
	public boolean enterHouse() {
		V2i tile = tile();
		V2d offset = offset();
		// Target position inside house reached? Turn around and start leaving house.
		if (tile.equals(targetTile) && offset.y >= 0) {
			setWishDir(dir().opposite());
			state = GhostState.LEAVING_HOUSE;
			return true;
		}
		// Center reached? If target tile is left or right seat, move towards target tile
		if (tile.equals(world.ghostHouse().seat(1)) && offset.y >= 0) {
			Direction dir = targetTile.x < world.ghostHouse().seat(1).x ? Direction.LEFT : Direction.RIGHT;
			setDir(dir);
			setWishDir(dir);
		}
		tryMovingTowards(dir());
		return false;
	}

	/**
	 * Lets the ghost leave the house from its home position to the middle of the house and then upwards
	 * to the house door.
	 * 
	 * @return {@code true} if the ghost has left the house
	 */
	public boolean leaveHouse() {
		V2i tile = tile();
		V2d offset = offset();
		// House left? Resume hunting.
		if (tile.equals(world.ghostHouse().entryTile()) && differsAtMost(offset.y, 0, 1)) {
			setOffset(HTS, 0);
			setDir(Direction.LEFT);
			setWishDir(Direction.LEFT);
			forcedOnTrack = true;
			state = GhostState.HUNTING_PAC;
			return true;
		}
		V2i houseCenter = world.ghostHouse().seat(1);
		int center = t(houseCenter.x) + HTS;
		int ground = t(houseCenter.y) + HTS;
		if (differsAtMost(position.x, center, 1)) {
			setOffset(HTS, offset.y);
			setDir(Direction.UP);
			setWishDir(Direction.UP);
		} else if (position.y < ground) {
			setDir(Direction.DOWN);
			setWishDir(Direction.DOWN);
		} else {
			Direction newDir = position.x < center ? Direction.RIGHT : Direction.LEFT;
			setDir(newDir);
			setWishDir(newDir);
		}
		tryMovingTowards(wishDir());
		return false;
	}

	/**
	 * Lets the ghost bounce at its home position inside the house.
	 * 
	 * @return {@code true}
	 */
	public boolean bounce() {
		int centerY = t(world.ghostHouse().seat(1).y);
		if (position.y < centerY - HTS || position.y > centerY + HTS) {
			Direction opposite = dir().opposite();
			setDir(opposite);
			setWishDir(opposite);
		}
		tryMoving();
		return true;
	}
}
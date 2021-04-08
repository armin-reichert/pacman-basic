package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature {

	public static final int BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3, SUE = 3;

	/** The unique ID of the ghost (0..3). */
	public final int id;

	/** The readable name of the ghost. */
	public String name;

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
		return String.format("%s: position: %s, speed=%.2f, dir=%s, wishDir=%s", name, position, speed, dir, wishDir);
	}

	public Ghost(int ghostID, String ghostName, Direction ghostStartDir) {
		id = ghostID;
		name = ghostName;
		dir = wishDir = startDir = ghostStartDir;
	}

	public boolean is(GhostState ghostState) {
		return state == ghostState;
	}

	/**
	 * Updates speed and behavior depending on current state.
	 * 
	 * TODO: not sure about correct speed
	 * 
	 * @param level current game level
	 */
	public void update(GameLevel level) {
		switch (state) {
		case LOCKED:
			if (atGhostHouseDoor()) {
				speed = 0;
			} else {
				speed = level.ghostSpeed / 2;
				bounce();
			}
			break;
		case ENTERING_HOUSE:
			speed = level.ghostSpeed * 2;
			enterHouse();
			break;
		case LEAVING_HOUSE:
			speed = level.ghostSpeed / 2;
			leaveHouse();
			break;
		case FRIGHTENED:
			if (world.isTunnel(tile())) {
				speed = level.ghostSpeedTunnel;
			} else {
				speed = level.ghostSpeedFrightened;
			}
			walkRandomly();
			break;
		case HUNTING_PAC:
			if (world.isTunnel(tile())) {
				speed = level.ghostSpeedTunnel;
			} else if (elroy == 1) {
				speed = level.elroy1Speed;
			} else if (elroy == 2) {
				speed = level.elroy2Speed;
			} else {
				speed = level.ghostSpeed;
			}
			if (targetTile != null) {
				headForTargetTile();
			} else {
				walkRandomly();
			}
			break;
		case DEAD:
			speed = level.ghostSpeed * 2;
			returnHome();
			break;
		default:
			throw new IllegalArgumentException("Illegal ghost state: " + state);
		}
	}

	@Override
	public boolean canAccessTile(V2i tile) {
		if (world.isGhostHouseDoor(tile)) {
			return is(GhostState.ENTERING_HOUSE) || is(GhostState.LEAVING_HOUSE);
		}
		// TODO there is still a bug causing ghost get stuck
		if (world.isUpwardsBlocked(tile)) {
			if (offset().y != 0) {
				return true; // maybe already on the way up
			}
			return !is(GhostState.HUNTING_PAC);
		}
		return super.canAccessTile(tile);
	}

	public boolean atGhostHouseDoor() {
		return tile().equals(world.houseEntry()) && differsAtMost(offset().x, HTS, 2);
	}

	private void returnHome() {
		// Ghost house door reached? Start falling into house.
		if (atGhostHouseDoor()) {
			setOffset(HTS, 0);
			dir = wishDir = Direction.DOWN;
			forcedOnTrack = false;
			targetTile = (id == 0) ? world.houseSeatCenter() : world.ghostHome(id);
			state = GhostState.ENTERING_HOUSE;
			return;
		}
		headForTargetTile();
	}

	private void enterHouse() {
		V2i location = tile();
		V2d offset = offset();
		// Target inside house reached? Start leaving house.
		if (location.equals(targetTile) && offset.y >= 0) {
			wishDir = dir.opposite();
			state = GhostState.LEAVING_HOUSE;
			return;
		}
		// House center reached? Move sidewards towards target tile
		if (location.equals(world.houseSeatCenter()) && offset.y >= 0) {
			wishDir = targetTile.x < world.houseSeatCenter().x ? Direction.LEFT : Direction.RIGHT;
		}
		tryMoving(wishDir);
	}

	private void leaveHouse() {
		V2i location = tile();
		V2d offset = offset();
		// House left? Resume hunting.
		if (location.equals(world.houseEntry()) && differsAtMost(offset.y, 0, 1)) {
			setOffset(HTS, 0);
			dir = wishDir = Direction.LEFT;
			forcedOnTrack = true;
			state = GhostState.HUNTING_PAC;
			return;
		}
		V2i houseCenter = world.houseSeatCenter();
		int center = t(houseCenter.x) + HTS;
		int ground = t(houseCenter.y) + HTS;
		if (differsAtMost(position.x, center, 1)) {
			setOffset(HTS, offset.y);
			wishDir = Direction.UP;
		} else if (position.y < ground) {
			wishDir = Direction.DOWN;
		} else {
			wishDir = position.x < center ? Direction.RIGHT : Direction.LEFT;
		}
		tryMoving(wishDir);
	}

	private void bounce() {
		int centerY = t(world.houseSeatCenter().y);
		if (position.y < centerY - HTS || position.y > centerY + HTS) {
			wishDir = dir.opposite();
		}
		tryMoving();
	}

	private static boolean differsAtMost(double value, double target, double tolerance) {
		return Math.abs(value - target) <= tolerance;
	}
}
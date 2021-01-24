package de.amr.games.pacman.game.creatures;

import static de.amr.games.pacman.game.core.PacManGame.differsAtMost;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.BLINKY;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.HTS;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.t;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;

import de.amr.games.pacman.game.core.PacManGameLevel;
import de.amr.games.pacman.game.worlds.PacManGameWorld;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature {

	/** The unique ID of the ghost (0..3). */
	public final byte id;

	/** The readable name of the ghost. */
	public final String name;

	/** The current state of the ghost. */
	public GhostState state;

	/** The bounty earned for killing this ghost. */
	public int bounty;

	/** The individual food counter, used to compute when the ghost can leave the house. */
	public int dotCounter;

	/**
	 * The "Cruise Elroy" mode of Blinky, the red ghost. Value is 1, 2 or -1, -2 (disabled Elroy mode).
	 */
	public byte elroy;

	public Ghost(int ghostID, PacManGameWorld world) {
		super(world);
		id = (byte) ghostID;
		name = world.ghostName(ghostID);
	}

	/**
	 * Updates speed and behavior depending on current state.
	 * 
	 * TODO: not sure about correct speed
	 * 
	 * @param level current game level
	 */
	public void update(PacManGameLevel level) {
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
			wanderRandomly();
			break;
		case HUNTING:
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
				wanderRandomly();
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
	public boolean canAccessTile(int x, int y) {
		if (world.isGhostHouseDoor(x, y)) {
			return state == GhostState.ENTERING_HOUSE || state == GhostState.LEAVING_HOUSE;
		}
		if (world.isUpwardsBlocked(x, y) && wishDir == UP && state == GhostState.HUNTING) {
			return false;
		}
		return super.canAccessTile(x, y);
	}

	public boolean atGhostHouseDoor() {
		return tile().equals(world.houseEntry()) && differsAtMost(offset().x, HTS, 2);
	}

	private void returnHome() {
		// Ghost house door reached? Start falling into house.
		if (atGhostHouseDoor()) {
			setOffset(HTS, 0);
			dir = wishDir = DOWN;
			forcedOnTrack = false;
			targetTile = id == BLINKY ? world.houseCenter() : world.ghostHome(id);
			state = GhostState.ENTERING_HOUSE;
			return;
		}
		headForTargetTile();
	}

	private void enterHouse() {
		V2i location = tile();
		V2f offset = offset();
		// Target inside house reached? Start leaving house.
		if (location.equals(targetTile) && offset.y >= 0) {
			wishDir = dir.opposite();
			state = GhostState.LEAVING_HOUSE;
			return;
		}
		// House center reached? Move sidewards towards target tile
		if (location.equals(world.houseCenter()) && offset.y >= 0) {
			wishDir = targetTile.x < world.houseCenter().x ? LEFT : RIGHT;
		}
		tryMoving(wishDir);
	}

	private void leaveHouse() {
		V2i location = tile();
		V2f offset = offset();
		// House left? Resume hunting.
		if (location.equals(world.houseEntry()) && differsAtMost(offset.y, 0, 1)) {
			setOffset(HTS, 0);
			dir = wishDir = LEFT;
			forcedOnTrack = true;
			state = GhostState.HUNTING;
			return;
		}
		V2i houseCenter = world.houseCenter();
		int center = t(houseCenter.x) + HTS;
		int ground = t(houseCenter.y) + HTS;
		if (differsAtMost(position.x, center, 1)) {
			setOffset(HTS, offset.y);
			wishDir = UP;
		} else if (position.y < ground) {
			wishDir = DOWN;
		} else {
			wishDir = position.x < center ? RIGHT : LEFT;
		}
		tryMoving(wishDir);
	}

	private void bounce() {
		V2i houseCenter = world.houseCenter();
		int ceiling = t(houseCenter.y) - HTS - 1, ground = t(houseCenter.y) + HTS;
		if (position.y <= ceiling || position.y >= ground) {
			wishDir = dir.opposite();
		}
		tryMoving();
	}
}
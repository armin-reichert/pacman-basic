package de.amr.games.pacman.game.creatures;

import static de.amr.games.pacman.game.core.PacManGame.differsAtMost;
import static de.amr.games.pacman.game.worlds.PacManClassicWorld.BLINKY;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.HTS;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.TS;
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
	public byte elroyMode;

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
			speed = level.ghostSpeedFrightened;
			wanderRandomly();
			break;
		case HUNTING:
			speed = level.ghostSpeed;
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
		if (atGhostHouseDoor()) {
			setOffset(HTS, 0);
			dir = wishDir = DOWN;
			forcedOnTrack = false;
			state = GhostState.ENTERING_HOUSE;
			targetTile = id == BLINKY ? world.houseCenter() : world.ghostHome(id); // TODO
			return;
		}
		headForTargetTile();
	}

	private void enterHouse() {
		V2i ghostLocation = tile();
		V2f offset = offset();
		// Target reached? Revive and start leaving house.
		if (ghostLocation.equals(targetTile) && offset.y >= 0) {
			state = GhostState.LEAVING_HOUSE;
			wishDir = dir.opposite();
			return;
		}
		// House center reached? Move sidewards towards target tile
		if (ghostLocation.equals(world.houseCenter()) && offset.y >= 0) {
			wishDir = targetTile.x < world.houseCenter().x ? LEFT : RIGHT;
		}
		tryMoving(wishDir);
	}

	private void leaveHouse() {
		V2i ghostLocation = tile();
		V2f ghostOffset = offset();

		// leaving house complete?
		if (ghostLocation.equals(world.houseEntry()) && differsAtMost(ghostOffset.y, 0, 1)) {
			setOffset(HTS, 0);
			dir = wishDir = LEFT;
			forcedOnTrack = true;
			state = GhostState.HUNTING;
			return;
		}

		V2i centerTile = world.houseCenter();
		int centerX = centerTile.x * TS + HTS;
		int groundY = centerTile.y * TS + HTS;
		if (differsAtMost(position.x, centerX, 1)) {
			setOffset(HTS, ghostOffset.y);
			wishDir = UP;
		} else if (position.y < groundY) {
			wishDir = DOWN;
		} else {
			wishDir = position.x < centerX ? RIGHT : LEFT;
		}
		tryMoving(wishDir);
	}

	private void bounce() {
		int ceiling = t(world.houseCenter().y) - 5, ground = t(world.houseCenter().y) + 4;
		if (position.y <= ceiling || position.y >= ground) {
			wishDir = dir.opposite();
		}
		tryMoving();
	}
}
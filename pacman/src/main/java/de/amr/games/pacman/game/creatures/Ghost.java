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

	public Ghost(int id, String name) {
		this.id = (byte) id;
		this.name = name;
	}

	public void update(PacManGameWorld world, PacManGameLevel level) {
		switch (state) {
		case LOCKED:
			if (!atGhostHouseDoor(world)) {
				bounce(world, level);
			}
			break;
		case ENTERING_HOUSE:
			enterHouse(world);
			break;
		case LEAVING_HOUSE:
			leaveHouse(world, level);
			break;
		case FRIGHTENED:
			wanderRandomly(world, level.ghostSpeedFrightened);
			break;
		case HUNTING:
			if (targetTile != null) {
				headForTargetTile(world);
			} else {
				wanderRandomly(world, level.ghostSpeed);
			}
			break;
		case DEAD:
			returnHome(world);
			break;
		default:
			throw new IllegalArgumentException("Illegal ghost state: " + state);
		}
	}

	@Override
	public boolean canAccessTile(PacManGameWorld world, int x, int y) {
		if (world.isGhostHouseDoor(x, y)) {
			return state == GhostState.ENTERING_HOUSE || state == GhostState.LEAVING_HOUSE;
		}
		if (world.isUpwardsBlocked(x, y) && wishDir == UP && state == GhostState.HUNTING) {
			return false;
		}
		return super.canAccessTile(world, x, y);
	}

	public boolean atGhostHouseDoor(PacManGameWorld world) {
		return tile().equals(world.houseEntry()) && differsAtMost(offset().x, HTS, 2);
	}

	public void returnHome(PacManGameWorld world) {
		if (atGhostHouseDoor(world)) {
			setOffset(HTS, 0);
			dir = wishDir = DOWN;
			forcedOnTrack = false;
			state = GhostState.ENTERING_HOUSE;
			targetTile = id == BLINKY ? world.houseCenter() : world.ghostHome(id); // TODO
			return;
		}
		headForTargetTile(world);
	}

	public void enterHouse(PacManGameWorld world) {
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
		tryMoving(world, wishDir);
	}

	public void leaveHouse(PacManGameWorld world, PacManGameLevel level) {
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
		speed = level.ghostSpeed / 2; // TODO speed correct?
		tryMoving(world, wishDir);
	}

	public void bounce(PacManGameWorld world, PacManGameLevel level) {
		int ceiling = t(world.houseCenter().y) - 5, ground = t(world.houseCenter().y) + 4;
		if (position.y <= ceiling || position.y >= ground) {
			wishDir = dir.opposite();
		}
		speed = level.ghostSpeed / 2; // TODO speed correct?
		tryMoving(world);
	}

}
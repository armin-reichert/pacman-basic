package de.amr.games.pacman.game.creatures;

import static de.amr.games.pacman.game.worlds.PacManGameWorld.HTS;
import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;

import java.util.Optional;

import de.amr.games.pacman.game.core.PacManGame;
import de.amr.games.pacman.game.worlds.PacManGameWorld;
import de.amr.games.pacman.lib.Direction;
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

	/** The tile that the ghost tries to reach. Can be inaccessible or outside of the maze. */
	public V2i targetTile = V2i.NULL;

	public Ghost(int id, String name) {
		this.id = (byte) id;
		this.name = name;
	}

	@Override
	public boolean canAccessTile(PacManGameWorld world, int x, int y) {
		if (world.isGhostHouseDoor(x, y)) {
			return state == GhostState.ENTERING_HOUSE || state == GhostState.LEAVING_HOUSE;
		}
		return super.canAccessTile(world, x, y);
	}

	public boolean atGhostHouseDoor(PacManGameWorld world) {
		return tile().equals(world.houseEntry()) && PacManGame.differsAtMost(offset().x, HTS, 2);
	}

	private static final Direction[] DIRECTION_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	public Optional<Direction> targetDirection(PacManGameWorld world) {
		double minDist = Double.MAX_VALUE;
		Direction minDistDir = null;
		for (Direction targetDir : DIRECTION_PRIORITY) {
			if (targetDir == dir.opposite()) {
				continue;
			}
			V2i neighbor = tile().sum(targetDir.vec);
			if (!canAccessTile(world, neighbor)) {
				continue;
			}
			if (targetDir == UP && state == GhostState.HUNTING && world.isUpwardsBlocked(neighbor)) {
				continue;
			}
			double dist = neighbor.euclideanDistance(targetTile);
			if (dist < minDist) {
				minDist = dist;
				minDistDir = targetDir;
			}
		}
		return Optional.ofNullable(minDistDir);
	}

	public Optional<Direction> newWishDir(PacManGameWorld world) {
		if (couldMove && !changedTile) {
			return Optional.empty();
		}
		if (forcedDirection) {
			forcedDirection = false;
			return Optional.of(wishDir);
		}
		V2i ghostLocation = tile();
		if (world.isPortal(ghostLocation)) {
			return Optional.empty();
		}
		if (state == GhostState.FRIGHTENED && world.isIntersection(ghostLocation)) {
			return randomAccessibleDirection(world, ghostLocation, dir.opposite());
		}
		return targetDirection(world);
	}
}
package de.amr.games.pacman.core;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Controls Pac-Man movement.
 * 
 * @author Armin Reichert
 */
public class Autopilot {

	private final Game game;
	private final PacMan pacMan;
	private final Ghost[] ghosts;
	private int escapingTicks;

	public Autopilot(Game game) {
		this.game = game;
		pacMan = game.pacMan;
		ghosts = game.ghosts;
	}

	public void controlPacMan() {
		V2i pacManTile = pacMan.tile();
		V2i targetTile = null;

		if (escapingTicks > 0) {
			--escapingTicks;
			return;
		}
		Ghost hunter = huntingGhostInFront(pacMan, 4); // where is Hunter?
		if (hunter != null) {
			pacMan.wishDir = escapeDirection(hunter.dir.opposite());
			log("Ghost %s going %s is heading me, moving %s to escape", hunter.name(), hunter.dir, pacMan.wishDir);
			escapingTicks = 6; // TODO how long is best?
			return;
		}

		if (pacMan.couldMove && !game.world.isIntersectionTile(pacManTile.x, pacManTile.y))
			return;

		Ghost frightenedGhostNearby = frightenedGhostMaxDistFromPacMan(8);
		if (frightenedGhostNearby != null) {
			log("Frightened ghost %s is near Pac-Man", frightenedGhostNearby.name());
			targetTile = frightenedGhostNearby.tile();
		} else if (game.bonusAvailableTicks > 0) {
			log("Chasing bonus");
			targetTile = game.world.bonusTile;
		} else {
			targetTile = tileWithMaxDistFromGhosts(foodTilesWithMinDistFromPacMan());
		}

		if (targetTile != null) {
			log("Selected target tile %s", targetTile);
			approachTarget(targetTile);
		}
	}

	private void approachTarget(V2i targetTile) {
		double minDist = Double.MAX_VALUE;
		Direction minDistDir = null;
		for (Direction dir : Direction.shuffled()) {
			if (dir == pacMan.dir.opposite()) {
				continue;
			}
			V2i neighbor = pacMan.tile().sum(dir.vec);
			if (!game.canAccessTile(pacMan, neighbor.x, neighbor.y)) {
				continue;
			}
			double dist = neighbor.euclideanDistance(targetTile);
			if (dist < minDist) {
				minDist = dist;
				minDistDir = dir;
			}
		}
		pacMan.wishDir = minDistDir;
	}

	private Ghost frightenedGhostMaxDistFromPacMan(int maxTilesAway) {
		for (Ghost ghost : ghosts) {
			if (ghost.frightened && ghost.tile().manhattanDistance(pacMan.tile()) < maxTilesAway) {
				return ghost;
			}
		}
		return null;
	}

	private Ghost huntingGhostInFront(PacMan pacMan, int maxTiles) {
		V2i pacManTile = pacMan.tile();
		for (int n = 1; n <= maxTiles; ++n) {
			V2i ahead = pacManTile.sum(pacMan.dir.vec.scaled(n));
			for (Ghost ghost : ghosts) {
				if (ghost.tile().equals(ahead) && game.isGhostHunting(ghost) && ghost.dir == pacMan.dir.opposite()) {
					return ghost;
				}
			}
		}
		return null;
	}

	private Direction escapeDirection(Direction forbidden) {
		V2i pacManTile = pacMan.tile();
		for (Direction dir : Direction.shuffled()) {
			if (dir == forbidden) {
				continue;
			}
			V2i neighbor = pacManTile.sum(dir.vec);
			if (game.canAccessTile(pacMan, neighbor.x, neighbor.y)) {
				return dir;
			}
		}
		return null;
	}

	private List<V2i> foodTilesWithMinDistFromPacMan() {
		List<V2i> foodTiles = new ArrayList<>();
		V2i pacManTile = pacMan.tile();
		double minDist = Double.MAX_VALUE;
		for (int x = 0; x < game.world.size.x; ++x) {
			for (int y = 0; y < game.world.size.y; ++y) {
				if (!game.world.isFoodTile(x, y) || game.world.foodRemoved(x, y)) {
					continue;
				}
				V2i foodTile = new V2i(x, y);
				double dist = pacManTile.manhattanDistance(foodTile);
				if (dist < minDist) {
					minDist = dist;
					foodTiles.clear();
					foodTiles.add(foodTile);
				} else if (dist == minDist) {
					foodTiles.add(foodTile);
				}
			}
		}
		log("Nearest food tiles from Pac-Man location %s:", pacManTile);
		for (V2i t : foodTiles) {
			log("\t%s (%.2g tiles away from Pac-Man, %.2g tiles away from ghosts)", t, t.manhattanDistance(pacManTile),
					minDistanceFromGhosts(t));
		}
		return foodTiles;
	}

	private V2i tileWithMaxDistFromGhosts(List<V2i> tiles) {
		V2i farestTile = null;
		double maxDist = -1;
		for (V2i tile : tiles) {
			double dist = minDistanceFromGhosts(tile);
			if (dist > maxDist) {
				maxDist = dist;
				farestTile = tile;
			}
		}
		return farestTile;
	}

	private double minDistanceFromGhosts(V2i tile) {
		double minDist = Double.MAX_VALUE;
		for (Ghost ghost : ghosts) {
			double dist = tile.manhattanDistance(ghost.tile());
			if (dist < minDist) {
				minDist = dist;
			}
		}
		return minDist;
	}
}
package de.amr.games.pacman.game.core;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.game.creatures.GhostState;
import de.amr.games.pacman.game.creatures.Pac;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Controls Pac-Man movement.
 * 
 * @author Armin Reichert
 */
public class Autopilot {

	private static final int MAX_GHOST_AHEAD_DETECTION_DIST = 4; // tiles
	private static final int MAX_GHOST_BEHIND_DETECTION_DIST = 2; // tiles
	private static final int MAX_GHOST_CHASE_DIST = 10; // tiles
	private static final int MAX_BONUS_HARVEST_DIST = 20; // tiles

	private PacManGame game;
	private Pac pac;
	private Ghost[] ghosts;

	public void steerPac(PacManGame game) {
		this.game = game;
		this.pac = game.pac;
		this.ghosts = game.ghosts;
		V2i pacManTile = pac.tile();

		if (pac.couldMove && !pac.changedTile) {
			return;
		}

		if (pac.forcedDirection) {
			pac.forcedDirection = false;
			return;
		}

		pac.targetTile = null;

		Ghost hunterAhead = findHuntingGhostAhead(); // Where is Hunter?
		if (hunterAhead != null) {
			Direction escapeDir = null;
			Ghost hunterBehind = findHuntingGhostBehind();
			if (hunterBehind != null) {
				escapeDir = findEscapeDirectionExcluding(EnumSet.of(pac.dir, pac.dir.opposite()));
				log("Detected ghost %s behind, escape direction is %s", hunterAhead.name, escapeDir);
			} else {
				escapeDir = findEscapeDirectionExcluding(EnumSet.of(pac.dir));
				log("Detected ghost %s ahead, escape direction is %s", hunterAhead.name, escapeDir);
			}
			if (escapeDir != null) {
				pac.wishDir = escapeDir;
			}
			pac.forcedDirection = true;
			return;
		}

		// when not escaping ghost, keep move direction at least until next intersection
		if (pac.couldMove && !game.world.isIntersection(pacManTile))
			return;

		Ghost prey = findFrightenedGhostInReach();
		if (prey != null && pac.powerTicksLeft >= game.clock.sec(1)) {
			log("Detected frightened ghost %s %.0g tiles away", prey.name, prey.tile().manhattanDistance(pacManTile));
			pac.targetTile = prey.tile();
		} else if (game.bonus.edibleTicksLeft > 0
				&& game.bonus.tile().manhattanDistance(pacManTile) <= MAX_BONUS_HARVEST_DIST) {
			log("Detected active bonus");
			pac.targetTile = game.bonus.tile();
		} else {
			V2i foodTile = findTileFarestFromGhosts(findNearestFoodTiles());
			pac.targetTile = foodTile;
		}
		approachTarget();
	}

	private void approachTarget() {
		if (pac.targetTile == null) {
			return;
		}
		double minDist = Double.MAX_VALUE;
		Direction minDistDir = null;
		for (Direction dir : Direction.shuffled()) {
			if (dir == pac.dir.opposite()) {
				continue;
				/*
				 * TODO sometimes reversing direction can be useful but in most cases, it leads to bouncing.
				 */
			}
			V2i neighbor = pac.tile().sum(dir.vec);
			if (!pac.canAccessTile(game.world, neighbor.x, neighbor.y)) {
				continue;
			}
			double dist = neighbor.euclideanDistance(pac.targetTile);
			if (dist < minDist) {
				minDist = dist;
				minDistDir = dir;
			}
		}
		if (minDistDir != null) {
			pac.wishDir = minDistDir;
			log("Approach target tile %s from tile %s by turning %s", pac.tile(), pac.targetTile, pac.wishDir);
		}
	}

	private Ghost findFrightenedGhostInReach() {
		for (Ghost ghost : ghosts) {
			if (ghost.state == GhostState.FRIGHTENED && ghost.tile().manhattanDistance(pac.tile()) < MAX_GHOST_CHASE_DIST) {
				return ghost;
			}
		}
		return null;
	}

	private Ghost findHuntingGhostAhead() {
		V2i pacManTile = pac.tile();
		boolean energizerFound = false;
		for (int i = 1; i <= MAX_GHOST_AHEAD_DETECTION_DIST; ++i) {
			V2i ahead = pacManTile.sum(pac.dir.vec.scaled(i));
			if (!pac.canAccessTile(game.world, ahead)) {
				break;
			}
			if (game.world.isEnergizerTile(ahead) && !game.world.isFoodRemoved(ahead)) {
				energizerFound = true;
			}
			V2i aheadLeft = ahead.sum(pac.dir.turnLeft().vec), aheadRight = ahead.sum(pac.dir.turnRight().vec);
			for (Ghost ghost : ghosts) {
				if (ghost.state != GhostState.HUNTING) {
					continue;
				}
				if (ghost.tile().equals(ahead) || ghost.tile().equals(aheadLeft) || ghost.tile().equals(aheadRight)) {
					if (energizerFound) {
						log("Ignore hunting ghost ahead, energizer comes first!");
						return null;
					}
					return ghost;
				}
			}
		}
		return null;
	}

	private Ghost findHuntingGhostBehind() {
		V2i pacManTile = pac.tile();
		for (int i = 1; i <= MAX_GHOST_BEHIND_DETECTION_DIST; ++i) {
			V2i behind = pacManTile.sum(pac.dir.opposite().vec.scaled(i));
			if (!pac.canAccessTile(game.world, behind)) {
				break;
			}
			for (Ghost ghost : ghosts) {
				if (ghost.state == GhostState.HUNTING && ghost.tile().equals(behind)) {
					return ghost;
				}
			}
		}
		return null;
	}

	private Direction findEscapeDirectionExcluding(Collection<Direction> forbidden) {
		V2i pacManTile = pac.tile();
		List<Direction> escapes = new ArrayList<>(4);
		for (Direction dir : Direction.shuffled()) {
			if (forbidden.contains(dir)) {
				continue;
			}
			V2i neighbor = pacManTile.sum(dir.vec);
			if (pac.canAccessTile(game.world, neighbor)) {
				escapes.add(dir);
			}
		}
		for (Direction escape : escapes) {
			V2i escapeTile = pacManTile.sum(escape.vec);
			if (game.world.isTunnel(escapeTile)) {
				return escape;
			}
		}
		return escapes.isEmpty() ? null : escapes.get(0);
	}

	private List<V2i> findNearestFoodTiles() {
		long time = System.nanoTime();
		List<V2i> foodTiles = new ArrayList<>();
		V2i pacManTile = pac.tile();
		double minDist = Double.MAX_VALUE;
		for (int x = 0; x < game.world.sizeInTiles().x; ++x) {
			for (int y = 0; y < game.world.sizeInTiles().y; ++y) {
				if (!game.world.isFoodTile(x, y) || game.world.isFoodRemoved(x, y)) {
					continue;
				}
				V2i foodTile = new V2i(x, y);
				if (game.world.isEnergizerTile(foodTile) && pac.powerTicksLeft > game.clock.sec(1)
						&& game.world.foodRemaining() > 1) {
					continue;
				}
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
		time = System.nanoTime() - time;
		log("Nearest food tiles from Pac-Man location %s: (time %.2f millis)", pacManTile, time / 1_000_000f);
		for (V2i t : foodTiles) {
			log("\t%s (%.2g tiles away from Pac-Man, %.2g tiles away from ghosts)", t, t.manhattanDistance(pacManTile),
					minDistanceFromGhosts(t));
		}
		return foodTiles;
	}

	private V2i findTileFarestFromGhosts(List<V2i> tiles) {
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
		return Stream.of(ghosts).map(Ghost::tile).mapToDouble(tile::manhattanDistance).min().getAsDouble();
	}
}
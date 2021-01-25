package de.amr.games.pacman.game.core;

import static de.amr.games.pacman.game.heaven.God.clock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.game.creatures.GhostState;
import de.amr.games.pacman.game.creatures.Pac;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.lib.V2i;

/**
 * Controls Pac-Man movement.
 * 
 * @author Armin Reichert
 */
public class Autopilot implements Consumer<Pac> {

	static class AutoPilotInfo {

		Ghost hunterAhead;
		double hunterAheadDistance;
		Ghost hunterBehind;
		double hunterBehindDistance;
		List<Ghost> frightenedGhosts;
		List<Double> frightenedGhostsDistance;

		@Override
		public String toString() {
			String s = "-- Begin autopilot info\n";
			if (hunterAhead != null) {
				s += String.format("Hunter ahead:  %s, distance: %.2g\n", hunterAhead.name, hunterAheadDistance);
			} else {
				s += "No hunter ahead\n";
			}
			if (hunterBehind != null) {
				s += String.format("Hunter behind: %s, distance: %.2g\n", hunterBehind.name, hunterBehindDistance);
			} else {
				s += "No hunter behind\n";
			}
			for (int i = 0; i < frightenedGhosts.size(); ++i) {
				Ghost ghost = frightenedGhosts.get(i);
				s += String.format("Prey: %s, distance: %.2g\n", ghost.name, frightenedGhostsDistance.get(i));
			}
			if (frightenedGhosts.isEmpty()) {
				s += "No prey\n";
			}
			s += "-- End autopilot info";
			return s;
		}
	}

	private static final int MAX_GHOST_AHEAD_DETECTION_DIST = 4; // tiles
	private static final int MAX_GHOST_BEHIND_DETECTION_DIST = 2; // tiles
	private static final int MAX_GHOST_CHASE_DIST = 10; // tiles
	private static final int MAX_BONUS_HARVEST_DIST = 20; // tiles

	public static boolean logEnabled = true;

	private final PacManGameController controller;
	private AutoPilotInfo data;

	private void log(String msg, Object... args) {
		if (logEnabled) {
			Logging.log(msg, args);
		}
	}

	public Autopilot(PacManGameController controller) {
		this.controller = controller;
	}

	@Override
	public void accept(Pac pac) {
		if (pac.couldMove && !pac.changedTile) {
			return;
		}
		if (pac.forcedDirection) {
			pac.forcedDirection = false;
			return;
		}
		collectData(controller.game);
		if (data.hunterAhead != null || data.hunterBehind != null || !data.frightenedGhosts.isEmpty()) {
			log("\n%s", data);
		}
		takeAction(controller.game);
	}

	private void collectData(PacManGameModel game) {
		data = new AutoPilotInfo();
		Ghost hunterAhead = findHuntingGhostAhead(game); // Where is Hunter?
		if (hunterAhead != null) {
			data.hunterAhead = hunterAhead;
			data.hunterAheadDistance = game.pac.tile().manhattanDistance(hunterAhead.tile());
		}
		Ghost hunterBehind = findHuntingGhostBehind(game);
		if (hunterBehind != null) {
			data.hunterBehind = hunterBehind;
			data.hunterBehindDistance = game.pac.tile().manhattanDistance(hunterBehind.tile());
		}
		data.frightenedGhosts = Stream.of(game.ghosts).filter(ghost -> ghost.is(GhostState.FRIGHTENED))
				.filter(ghost -> ghost.tile().manhattanDistance(game.pac.tile()) <= MAX_GHOST_CHASE_DIST)
				.collect(Collectors.toList());
		data.frightenedGhostsDistance = data.frightenedGhosts.stream()
				.map(ghost -> ghost.tile().manhattanDistance(game.pac.tile())).collect(Collectors.toList());
	}

	private void takeAction(PacManGameModel game) {
		if (data.hunterAhead != null) {
			Direction escapeDir = null;
			if (data.hunterBehind != null) {
				escapeDir = findEscapeDirectionExcluding(game, EnumSet.of(game.pac.dir, game.pac.dir.opposite()));
				log("Detected ghost %s behind, escape direction is %s", data.hunterAhead.name, escapeDir);
			} else {
				escapeDir = findEscapeDirectionExcluding(game, EnumSet.of(game.pac.dir));
				log("Detected ghost %s ahead, escape direction is %s", data.hunterAhead.name, escapeDir);
			}
			if (escapeDir != null) {
				game.pac.wishDir = escapeDir;
				game.pac.forcedDirection = true;
			}
			return;
		}

		// when not escaping ghost, keep move direction at least until next intersection
		if (game.pac.couldMove && !game.world.isIntersection(game.pac.tile()))
			return;

		if (data.frightenedGhosts.size() != 0 && game.pac.powerTicksLeft >= clock.sec(1)) {
			Ghost prey = data.frightenedGhosts.get(0);
			log("Detected frightened ghost %s %.0g tiles away", prey.name, prey.tile().manhattanDistance(game.pac.tile()));
			game.pac.targetTile = prey.tile();
		} else if (game.bonus.edibleTicksLeft > 0
				&& game.bonus.tile().manhattanDistance(game.pac.tile()) <= MAX_BONUS_HARVEST_DIST) {
			log("Detected active bonus");
			game.pac.targetTile = game.bonus.tile();
		} else {
			V2i foodTile = findTileFarestFromGhosts(game, findNearestFoodTiles(game));
			game.pac.targetTile = foodTile;
		}
		approachTarget(game);
	}

	private void approachTarget(PacManGameModel game) {
		if (game.pac.targetTile == null) {
			return;
		}
		double minDist = Double.MAX_VALUE;
		Direction minDistDir = null;
		for (Direction dir : Direction.shuffled()) {
			if (dir == game.pac.dir.opposite()) {
				continue;
				/*
				 * TODO sometimes reversing direction can be useful but in most cases, it leads to bouncing.
				 */
			}
			V2i neighbor = game.pac.tile().sum(dir.vec);
			if (!game.pac.canAccessTile(neighbor.x, neighbor.y)) {
				continue;
			}
			double dist = neighbor.euclideanDistance(game.pac.targetTile);
			if (dist < minDist) {
				minDist = dist;
				minDistDir = dir;
			}
		}
		if (minDistDir != null) {
			game.pac.wishDir = minDistDir;
			log("Approach target tile %s from tile %s by turning %s", game.pac.tile(), game.pac.targetTile, game.pac.wishDir);
		}
	}

	private Ghost findFrightenedGhostInReach(PacManGameModel game) {
		for (Ghost ghost : game.ghosts) {
			if (ghost.state == GhostState.FRIGHTENED
					&& ghost.tile().manhattanDistance(game.pac.tile()) < MAX_GHOST_CHASE_DIST) {
				return ghost;
			}
		}
		return null;
	}

	private Ghost findHuntingGhostAhead(PacManGameModel game) {
		V2i pacManTile = game.pac.tile();
		boolean energizerFound = false;
		for (int i = 1; i <= MAX_GHOST_AHEAD_DETECTION_DIST; ++i) {
			V2i ahead = pacManTile.sum(game.pac.dir.vec.scaled(i));
			if (!game.pac.canAccessTile(ahead)) {
				break;
			}
			if (game.world.isEnergizerTile(ahead) && !game.world.isFoodRemoved(ahead)) {
				energizerFound = true;
			}
			V2i aheadLeft = ahead.sum(game.pac.dir.turnLeft().vec), aheadRight = ahead.sum(game.pac.dir.turnRight().vec);
			for (Ghost ghost : game.ghosts) {
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

	private Ghost findHuntingGhostBehind(PacManGameModel game) {
		V2i pacManTile = game.pac.tile();
		for (int i = 1; i <= MAX_GHOST_BEHIND_DETECTION_DIST; ++i) {
			V2i behind = pacManTile.sum(game.pac.dir.opposite().vec.scaled(i));
			if (!game.pac.canAccessTile(behind)) {
				break;
			}
			for (Ghost ghost : game.ghosts) {
				if (ghost.state == GhostState.HUNTING && ghost.tile().equals(behind)) {
					return ghost;
				}
			}
		}
		return null;
	}

	private Direction findEscapeDirectionExcluding(PacManGameModel game, Collection<Direction> forbidden) {
		V2i pacManTile = game.pac.tile();
		List<Direction> escapes = new ArrayList<>(4);
		for (Direction dir : Direction.shuffled()) {
			if (forbidden.contains(dir)) {
				continue;
			}
			V2i neighbor = pacManTile.sum(dir.vec);
			if (game.pac.canAccessTile(neighbor)) {
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

	private List<V2i> findNearestFoodTiles(PacManGameModel game) {
		long time = System.nanoTime();
		List<V2i> foodTiles = new ArrayList<>();
		V2i pacManTile = game.pac.tile();
		double minDist = Double.MAX_VALUE;
		for (int x = 0; x < game.world.sizeInTiles().x; ++x) {
			for (int y = 0; y < game.world.sizeInTiles().y; ++y) {
				if (!game.world.isFoodTile(x, y) || game.world.isFoodRemoved(x, y)) {
					continue;
				}
				V2i foodTile = new V2i(x, y);
				if (game.world.isEnergizerTile(foodTile) && game.pac.powerTicksLeft > clock.sec(1)
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
					minDistanceFromGhosts(game));
		}
		return foodTiles;
	}

	private V2i findTileFarestFromGhosts(PacManGameModel game, List<V2i> tiles) {
		V2i farestTile = null;
		double maxDist = -1;
		for (V2i tile : tiles) {
			double dist = minDistanceFromGhosts(game);
			if (dist > maxDist) {
				maxDist = dist;
				farestTile = tile;
			}
		}
		return farestTile;
	}

	private double minDistanceFromGhosts(PacManGameModel game) {
		return Stream.of(game.ghosts).map(Ghost::tile).mapToDouble(game.pac.tile()::manhattanDistance).min().getAsDouble();
	}
}
package de.amr.games.pacman.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.common.PacManGameModel;
import de.amr.games.pacman.model.pacman.Bonus;

/**
 * Controls automatic movement of the player.
 * 
 * @author Armin Reichert
 */
public class Autopilot implements PlayerControl {

	static class AutopilotData {

		static final int MAX_GHOST_AHEAD_DETECTION_DIST = 4; // tiles
		static final int MAX_GHOST_BEHIND_DETECTION_DIST = 2; // tiles
		static final int MAX_GHOST_CHASE_DIST = 10; // tiles
		static final int MAX_BONUS_HARVEST_DIST = 20; // tiles

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

	public static boolean logEnabled;

	private static void log(String msg, Object... args) {
		if (logEnabled) {
			Logging.log(msg, args);
		}
	}

	private final Supplier<PacManGameModel> game;

	public Autopilot(Supplier<PacManGameModel> game) {
		this.game = game;
	}

	private PacManGameModel game() {
		return game.get();
	}

	@Override
	public void steer(Pac player) {
		if (player.forced) {
			player.forced = false;
			return;
		}
		if (!player.stuck && !player.newTileEntered) {
			return;
		}
		AutopilotData data = collectData();
		if (data.hunterAhead != null || data.hunterBehind != null || !data.frightenedGhosts.isEmpty()) {
			log("\n%s", data);
		}
		takeAction(data);
	}

	private AutopilotData collectData() {
		AutopilotData data = new AutopilotData();
		Ghost hunterAhead = findHuntingGhostAhead(); // Where is Hunter?
		if (hunterAhead != null) {
			data.hunterAhead = hunterAhead;
			data.hunterAheadDistance = game().player().tile().manhattanDistance(hunterAhead.tile());
		}
		Ghost hunterBehind = findHuntingGhostBehind();
		if (hunterBehind != null) {
			data.hunterBehind = hunterBehind;
			data.hunterBehindDistance = game().player().tile().manhattanDistance(hunterBehind.tile());
		}
		data.frightenedGhosts = game().ghosts(GhostState.FRIGHTENED)
				.filter(ghost -> ghost.tile().manhattanDistance(game().player().tile()) <= AutopilotData.MAX_GHOST_CHASE_DIST)
				.collect(Collectors.toList());
		data.frightenedGhostsDistance = data.frightenedGhosts.stream()
				.map(ghost -> ghost.tile().manhattanDistance(game().player().tile())).collect(Collectors.toList());
		return data;
	}

	private void takeAction(AutopilotData data) {
		if (data.hunterAhead != null) {
			Direction escapeDir = null;
			if (data.hunterBehind != null) {
				escapeDir = findEscapeDirectionExcluding(EnumSet.of(game().player().dir(), game().player().dir().opposite()));
				log("Detected ghost %s behind, escape direction is %s", data.hunterAhead.name, escapeDir);
			} else {
				escapeDir = findEscapeDirectionExcluding(EnumSet.of(game().player().dir()));
				log("Detected ghost %s ahead, escape direction is %s", data.hunterAhead.name, escapeDir);
			}
			if (escapeDir != null) {
				game().player().setWishDir(escapeDir);
				game().player().forced = true;
			}
			return;
		}

		// when not escaping ghost, keep move direction at least until next intersection
		if (!game().player().stuck && !game().currentLevel().world.isIntersection(game().player().tile()))
			return;

		if (data.frightenedGhosts.size() != 0 && game().player().powerTimer.ticksRemaining() >= 1 * 60) {
			Ghost prey = data.frightenedGhosts.get(0);
			log("Detected frightened ghost %s %.0g tiles away", prey.name,
					prey.tile().manhattanDistance(game().player().tile()));
			game().player().targetTile = prey.tile();
		} else if (game().bonus().state == Bonus.EDIBLE
				&& game().bonus().tile().manhattanDistance(game().player().tile()) <= AutopilotData.MAX_BONUS_HARVEST_DIST) {
			log("Detected active bonus");
			game().player().targetTile = game().bonus().tile();
		} else {
			V2i foodTile = findTileFarestFromGhosts(findNearestFoodTiles());
			game().player().targetTile = foodTile;
		}
		if (game().player().targetTile != null) {
			game().player().setDirectionTowardsTarget();
		}
	}

	private Ghost findHuntingGhostAhead() {
		V2i pacManTile = game().player().tile();
		boolean energizerFound = false;
		for (int i = 1; i <= AutopilotData.MAX_GHOST_AHEAD_DETECTION_DIST; ++i) {
			V2i ahead = pacManTile.plus(game().player().dir().vec.scaled(i));
			if (!game().player().canAccessTile(ahead)) {
				break;
			}
			if (game().currentLevel().world.isEnergizerTile(ahead) && !game().currentLevel().isFoodRemoved(ahead)) {
				energizerFound = true;
			}
			V2i aheadLeft = ahead.plus(game().player().dir().turnLeft().vec),
					aheadRight = ahead.plus(game().player().dir().turnRight().vec);
			for (Ghost ghost : game().ghosts(GhostState.HUNTING_PAC).toArray(Ghost[]::new)) {
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
		V2i pacManTile = game().player().tile();
		for (int i = 1; i <= AutopilotData.MAX_GHOST_BEHIND_DETECTION_DIST; ++i) {
			V2i behind = pacManTile.plus(game().player().dir().opposite().vec.scaled(i));
			if (!game().player().canAccessTile(behind)) {
				break;
			}
			for (Ghost ghost : game().ghosts().toArray(Ghost[]::new)) {
				if (ghost.state == GhostState.HUNTING_PAC && ghost.tile().equals(behind)) {
					return ghost;
				}
			}
		}
		return null;
	}

	private Direction findEscapeDirectionExcluding(Collection<Direction> forbidden) {
		V2i pacManTile = game().player().tile();
		List<Direction> escapes = new ArrayList<>(4);
		for (Direction dir : Direction.shuffled()) {
			if (forbidden.contains(dir)) {
				continue;
			}
			V2i neighbor = pacManTile.plus(dir.vec);
			if (game().player().canAccessTile(neighbor)) {
				escapes.add(dir);
			}
		}
		for (Direction escape : escapes) {
			V2i escapeTile = pacManTile.plus(escape.vec);
			if (game().currentLevel().world.isTunnel(escapeTile)) {
				return escape;
			}
		}
		return escapes.isEmpty() ? null : escapes.get(0);
	}

	private List<V2i> findNearestFoodTiles() {
		long time = System.nanoTime();
		List<V2i> foodTiles = new ArrayList<>();
		V2i pacManTile = game().player().tile();
		double minDist = Double.MAX_VALUE;
		for (int x = 0; x < game().currentLevel().world.numCols(); ++x) {
			for (int y = 0; y < game().currentLevel().world.numRows(); ++y) {
				V2i tile = new V2i(x, y);
				if (!game().currentLevel().world.isFoodTile(tile) || game().currentLevel().isFoodRemoved(tile)) {
					continue;
				}
				if (game().currentLevel().world.isEnergizerTile(tile) && game().player().powerTimer.ticksRemaining() > 1 * 60
						&& game().currentLevel().foodRemaining > 1) {
					continue;
				}
				double dist = pacManTile.manhattanDistance(tile);
				if (dist < minDist) {
					minDist = dist;
					foodTiles.clear();
					foodTiles.add(tile);
				} else if (dist == minDist) {
					foodTiles.add(tile);
				}
			}
		}
		time = System.nanoTime() - time;
		log("Nearest food tiles from Pac-Man location %s: (time %.2f millis)", pacManTile, time / 1_000_000f);
		for (V2i t : foodTiles) {
			log("\t%s (%.2g tiles away from Pac-Man, %.2g tiles away from ghosts)", t, t.manhattanDistance(pacManTile),
					minDistanceFromGhosts());
		}
		return foodTiles;
	}

	private V2i findTileFarestFromGhosts(List<V2i> tiles) {
		V2i farestTile = null;
		double maxDist = -1;
		for (V2i tile : tiles) {
			double dist = minDistanceFromGhosts();
			if (dist > maxDist) {
				maxDist = dist;
				farestTile = tile;
			}
		}
		return farestTile;
	}

	private double minDistanceFromGhosts() {
		return game().ghosts().map(Ghost::tile).mapToDouble(game().player().tile()::manhattanDistance).min().getAsDouble();
	}
}
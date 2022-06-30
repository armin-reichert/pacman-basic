/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.controller.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;

/**
 * Controls automatic movement of the player.
 * 
 * @author Armin Reichert
 */
public class Autopilot implements Consumer<Creature> {

	private static final Logger logger = LogManager.getFormatterLogger();

	private static class AutopilotData {

		static final int MAX_GHOST_AHEAD_DETECTION_DIST = 4; // tiles
		static final int MAX_GHOST_BEHIND_DETECTION_DIST = 1; // tiles
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
			StringBuilder s = new StringBuilder("-- Begin autopilot info%n");
			if (hunterAhead != null) {
				s.append("Hunter ahead:  %s, distance: %.2g%n".formatted(hunterAhead.name, hunterAheadDistance));
			} else {
				s.append("No hunter ahead%n");
			}
			if (hunterBehind != null) {
				s.append("Hunter behind: %s, distance: %.2g%n".formatted(hunterBehind.name, hunterBehindDistance));
			} else {
				s.append("No hunter behind%n");
			}
			for (int i = 0; i < frightenedGhosts.size(); ++i) {
				Ghost ghost = frightenedGhosts.get(i);
				s.append("Prey: %s, distance: %.2g%n".formatted(ghost.name, frightenedGhostsDistance.get(i)));
			}
			if (frightenedGhosts.isEmpty()) {
				s.append("No prey%n");
			}
			s.append("-- End autopilot info");
			return s.toString();
		}
	}

	private final Supplier<GameModel> gameSupplier;

	public Autopilot(Supplier<GameModel> gameSupplier) {
		this.gameSupplier = gameSupplier;
	}

	private GameModel game() {
		return gameSupplier.get();
	}

	@Override
	public void accept(Creature guy) {
		if (!guy.stuck && !guy.newTileEntered) {
			return;
		}
		AutopilotData data = collectData();
		if (data.hunterAhead != null || data.hunterBehind != null || !data.frightenedGhosts.isEmpty()) {
			logger.trace("%n%s", data);
		}
		takeAction(data);
	}

	private AutopilotData collectData() {
		AutopilotData data = new AutopilotData();
		Ghost hunterAhead = findHuntingGhostAhead(); // Where is Hunter?
		if (hunterAhead != null) {
			data.hunterAhead = hunterAhead;
			data.hunterAheadDistance = game().pac.tile().manhattanDistance(hunterAhead.tile());
		}
		Ghost hunterBehind = findHuntingGhostBehind();
		if (hunterBehind != null) {
			data.hunterBehind = hunterBehind;
			data.hunterBehindDistance = game().pac.tile().manhattanDistance(hunterBehind.tile());
		}
		data.frightenedGhosts = game().ghosts(GhostState.FRIGHTENED)
				.filter(ghost -> ghost.tile().manhattanDistance(game().pac.tile()) <= AutopilotData.MAX_GHOST_CHASE_DIST)
				.collect(Collectors.toList());
		data.frightenedGhostsDistance = data.frightenedGhosts.stream()
				.map(ghost -> ghost.tile().manhattanDistance(game().pac.tile())).collect(Collectors.toList());
		return data;
	}

	private void takeAction(AutopilotData data) {
		if (data.hunterAhead != null) {
			Direction escapeDir = null;
			if (data.hunterBehind != null) {
				escapeDir = findEscapeDirectionExcluding(EnumSet.of(game().pac.moveDir(), game().pac.moveDir().opposite()));
				logger.trace("Detected ghost %s behind, escape direction is %s", data.hunterAhead.name, escapeDir);
			} else {
				escapeDir = findEscapeDirectionExcluding(EnumSet.of(game().pac.moveDir()));
				logger.trace("Detected ghost %s ahead, escape direction is %s", data.hunterAhead.name, escapeDir);
			}
			if (escapeDir != null) {
				game().pac.setWishDir(escapeDir);
			}
			return;
		}

		// when not escaping ghost, keep move direction at least until next intersection
		if (!game().pac.stuck && !game().level.world.isIntersection(game().pac.tile()))
			return;

		if (!data.frightenedGhosts.isEmpty() && game().powerTimer.remaining() >= 1 * 60) {
			Ghost prey = data.frightenedGhosts.get(0);
			logger.trace("Detected frightened ghost %s %.0g tiles away", prey.name,
					prey.tile().manhattanDistance(game().pac.tile()));
			game().pac.targetTile = prey.tile();
		} else if (game().bonus() != null && game().bonus().state() == BonusState.EDIBLE && game().bonus().entity().tile()
				.manhattanDistance(game().pac.tile()) <= AutopilotData.MAX_BONUS_HARVEST_DIST) {
			logger.trace("Detected active bonus");
			game().pac.targetTile = game().bonus().entity().tile();
		} else {
			V2i foodTile = findTileFarestFromGhosts(findNearestFoodTiles());
			game().pac.targetTile = foodTile;
		}
		game().pac.computeDirectionTowardsTarget();
	}

	private Ghost findHuntingGhostAhead() {
		V2i pacManTile = game().pac.tile();
		boolean energizerFound = false;
		for (int i = 1; i <= AutopilotData.MAX_GHOST_AHEAD_DETECTION_DIST; ++i) {
			V2i ahead = pacManTile.plus(game().pac.moveDir().vec.scaled(i));
			if (!game().pac.canAccessTile(ahead)) {
				break;
			}
			if (game().level.world.isEnergizerTile(ahead) && !game().level.world.containsEatenFood(ahead)) {
				energizerFound = true;
			}
			V2i aheadLeft = ahead.plus(game().pac.moveDir().succAntiClockwise().vec);
			V2i aheadRight = ahead.plus(game().pac.moveDir().succClockwise().vec);
			for (Ghost ghost : game().ghosts(GhostState.HUNTING_PAC).toArray(Ghost[]::new)) {
				if (ghost.tile().equals(ahead) || ghost.tile().equals(aheadLeft) || ghost.tile().equals(aheadRight)) {
					if (energizerFound) {
						logger.trace("Ignore hunting ghost ahead, energizer comes first!");
						return null;
					}
					return ghost;
				}
			}
		}
		return null;
	}

	private Ghost findHuntingGhostBehind() {
		V2i pacManTile = game().pac.tile();
		for (int i = 1; i <= AutopilotData.MAX_GHOST_BEHIND_DETECTION_DIST; ++i) {
			V2i behind = pacManTile.plus(game().pac.moveDir().opposite().vec.scaled(i));
			if (!game().pac.canAccessTile(behind)) {
				break;
			}
			for (Ghost ghost : game().ghosts().toArray(Ghost[]::new)) {
				if (ghost.is(GhostState.HUNTING_PAC) && ghost.tile().equals(behind)) {
					return ghost;
				}
			}
		}
		return null;
	}

	private Direction findEscapeDirectionExcluding(Collection<Direction> forbidden) {
		V2i pacManTile = game().pac.tile();
		List<Direction> escapes = new ArrayList<>(4);
		for (Direction dir : Direction.shuffled()) {
			if (forbidden.contains(dir)) {
				continue;
			}
			V2i neighbor = pacManTile.plus(dir.vec);
			if (game().pac.canAccessTile(neighbor)) {
				escapes.add(dir);
			}
		}
		for (Direction escape : escapes) {
			V2i escapeTile = pacManTile.plus(escape.vec);
			if (game().level.world.isTunnel(escapeTile)) {
				return escape;
			}
		}
		return escapes.isEmpty() ? null : escapes.get(0);
	}

	private List<V2i> findNearestFoodTiles() {
		long time = System.nanoTime();
		List<V2i> foodTiles = new ArrayList<>();
		V2i pacManTile = game().pac.tile();
		double minDist = Double.MAX_VALUE;
		for (int x = 0; x < game().level.world.numCols(); ++x) {
			for (int y = 0; y < game().level.world.numRows(); ++y) {
				V2i tile = new V2i(x, y);
				if (!game().level.world.isFoodTile(tile) || game().level.world.containsEatenFood(tile)) {
					continue;
				}
				if (game().level.world.isEnergizerTile(tile) && game().powerTimer.remaining() > 2 * 60
						&& game().level.world.foodRemaining() > 1) {
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
		logger.trace("Nearest food tiles from Pac-Man location %s: (time %.2f millis)", pacManTile, time / 1_000_000f);
		for (V2i t : foodTiles) {
			logger.trace("\t%s (%.2g tiles away from Pac-Man, %.2g tiles away from ghosts)", t,
					t.manhattanDistance(pacManTile), minDistanceFromGhosts());
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
		return game().ghosts().map(Ghost::tile).mapToDouble(game().pac.tile()::manhattanDistance).min().getAsDouble();
	}
}
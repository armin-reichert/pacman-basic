/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.lib.steering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.Steering;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.World;

/**
 * Pac-Man steering based on a set of rules.
 * 
 * @author Armin Reichert
 */
public class RuleBasedSteering implements Steering {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static class CollectedData {

		static final int MAX_GHOST_AHEAD_DETECTION_DIST = 4; // tiles
		static final int MAX_GHOST_BEHIND_DETECTION_DIST = 1; // tiles
		static final int MAX_GHOST_CHASE_DIST = 10; // tiles
		static final int MAX_BONUS_HARVEST_DIST = 20; // tiles

		Ghost hunterAhead;
		float hunterAheadDistance;
		Ghost hunterBehind;
		float hunterBehindDistance;
		List<Ghost> frightenedGhosts;
		List<Float> frightenedGhostsDistance;

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder("-- Begin autopilot info%n");
			if (hunterAhead != null) {
				s.append("Hunter ahead:  %s, distance: %.2g%n".formatted(hunterAhead.name(), hunterAheadDistance));
			} else {
				s.append("No hunter ahead%n");
			}
			if (hunterBehind != null) {
				s.append("Hunter behind: %s, distance: %.2g%n".formatted(hunterBehind.name(), hunterBehindDistance));
			} else {
				s.append("No hunter behind%n");
			}
			for (int i = 0; i < frightenedGhosts.size(); ++i) {
				Ghost ghost = frightenedGhosts.get(i);
				s.append("Prey: %s, distance: %.2g%n".formatted(ghost.name(), frightenedGhostsDistance.get(i)));
			}
			if (frightenedGhosts.isEmpty()) {
				s.append("No prey%n");
			}
			s.append("-- End autopilot info");
			return s.toString();
		}
	}

	@Override
	public void init() {
		// nothing to do
	}

	@Override
	public void steer(GameLevel level, Creature guy) {
		if (!guy.isStuck() && !guy.isNewTileEntered()) {
			return;
		}
		var data = collectData(level);
		if (data.hunterAhead != null || data.hunterBehind != null || !data.frightenedGhosts.isEmpty()) {
			LOG.trace("%n%s", data);
		}
		takeAction(level, data);
	}

	private CollectedData collectData(GameLevel level) {
		var pac = level.pac();
		var data = new CollectedData();
		Ghost hunterAhead = findHuntingGhostAhead(level); // Where is Hunter?
		if (hunterAhead != null) {
			data.hunterAhead = hunterAhead;
			data.hunterAheadDistance = pac.tile().manhattanDistance(hunterAhead.tile());
		}
		Ghost hunterBehind = findHuntingGhostBehind(level);
		if (hunterBehind != null) {
			data.hunterBehind = hunterBehind;
			data.hunterBehindDistance = pac.tile().manhattanDistance(hunterBehind.tile());
		}
		data.frightenedGhosts = level.ghosts(GhostState.FRIGHTENED)
				.filter(ghost -> ghost.tile().manhattanDistance(pac.tile()) <= CollectedData.MAX_GHOST_CHASE_DIST).toList();
		data.frightenedGhostsDistance = data.frightenedGhosts.stream()
				.map(ghost -> ghost.tile().manhattanDistance(pac.tile())).toList();
		return data;
	}

	private void takeAction(GameLevel level, CollectedData data) {
		var pac = level.pac();
		if (data.hunterAhead != null) {
			Direction escapeDir = null;
			if (data.hunterBehind != null) {
				escapeDir = findEscapeDirectionExcluding(level, EnumSet.of(pac.moveDir(), pac.moveDir().opposite()));
				LOG.trace("Detected ghost %s behind, escape direction is %s", data.hunterAhead.name(), escapeDir);
			} else {
				escapeDir = findEscapeDirectionExcluding(level, EnumSet.of(pac.moveDir()));
				LOG.trace("Detected ghost %s ahead, escape direction is %s", data.hunterAhead.name(), escapeDir);
			}
			if (escapeDir != null) {
				pac.setWishDir(escapeDir);
			}
			return;
		}

		// when not escaping ghost, keep move direction at least until next intersection
		if (!pac.isStuck() && !level.world().isIntersection(pac.tile()))
			return;

		if (!data.frightenedGhosts.isEmpty() && pac.powerTimer().remaining() >= 1 * 60) {
			Ghost prey = data.frightenedGhosts.get(0);
			LOG.trace("Detected frightened ghost %s %.0g tiles away", prey.name(), prey.tile().manhattanDistance(pac.tile()));
			pac.setTargetTile(prey.tile());
		} else if (level.bonus() != null && level.bonus().state() == Bonus.STATE_EDIBLE
				&& World.tileAt(level.bonus().entity().position())
						.manhattanDistance(pac.tile()) <= CollectedData.MAX_BONUS_HARVEST_DIST) {
			LOG.trace("Detected active bonus");
			pac.setTargetTile(World.tileAt(level.bonus().entity().position()));
		} else {
			Vector2i foodTile = findTileFarestFromGhosts(level, findNearestFoodTiles(level));
			pac.setTargetTile(foodTile);
		}
		pac.navigateTowardsTarget(level);
	}

	private Ghost findHuntingGhostAhead(GameLevel level) {
		var pac = level.pac();
		Vector2i pacManTile = pac.tile();
		boolean energizerFound = false;
		for (int i = 1; i <= CollectedData.MAX_GHOST_AHEAD_DETECTION_DIST; ++i) {
			Vector2i ahead = pacManTile.plus(pac.moveDir().vector().scaled(i));
			if (!pac.canAccessTile(ahead, level)) {
				break;
			}
			if (level.world().isEnergizerTile(ahead) && !level.world().containsEatenFood(ahead)) {
				energizerFound = true;
			}
			Vector2i aheadLeft = ahead.plus(pac.moveDir().succAntiClockwise().vector());
			Vector2i aheadRight = ahead.plus(pac.moveDir().succClockwise().vector());
			for (Ghost ghost : level.ghosts(GhostState.HUNTING_PAC).toArray(Ghost[]::new)) {
				if (ghost.tile().equals(ahead) || ghost.tile().equals(aheadLeft) || ghost.tile().equals(aheadRight)) {
					if (energizerFound) {
						LOG.trace("Ignore hunting ghost ahead, energizer comes first!");
						return null;
					}
					return ghost;
				}
			}
		}
		return null;
	}

	private Ghost findHuntingGhostBehind(GameLevel level) {
		var pac = level.pac();
		Vector2i pacManTile = pac.tile();
		for (int i = 1; i <= CollectedData.MAX_GHOST_BEHIND_DETECTION_DIST; ++i) {
			Vector2i behind = pacManTile.plus(pac.moveDir().opposite().vector().scaled(i));
			if (!pac.canAccessTile(behind, level)) {
				break;
			}
			for (Ghost ghost : level.ghosts().toArray(Ghost[]::new)) {
				if (ghost.is(GhostState.HUNTING_PAC) && ghost.tile().equals(behind)) {
					return ghost;
				}
			}
		}
		return null;
	}

	private Direction findEscapeDirectionExcluding(GameLevel level, Collection<Direction> forbidden) {
		var pac = level.pac();
		Vector2i pacManTile = pac.tile();
		List<Direction> escapes = new ArrayList<>(4);
		for (Direction dir : Direction.shuffled()) {
			if (forbidden.contains(dir)) {
				continue;
			}
			Vector2i neighbor = pacManTile.plus(dir.vector());
			if (pac.canAccessTile(neighbor, level)) {
				escapes.add(dir);
			}
		}
		for (Direction escape : escapes) {
			Vector2i escapeTile = pacManTile.plus(escape.vector());
			if (level.world().isTunnel(escapeTile)) {
				return escape;
			}
		}
		return escapes.isEmpty() ? null : escapes.get(0);
	}

	private List<Vector2i> findNearestFoodTiles(GameLevel level) {
		long time = System.nanoTime();
		var pac = level.pac();
		List<Vector2i> foodTiles = new ArrayList<>();
		Vector2i pacManTile = pac.tile();
		float minDist = Float.MAX_VALUE;
		for (int x = 0; x < level.world().numCols(); ++x) {
			for (int y = 0; y < level.world().numRows(); ++y) {
				Vector2i tile = new Vector2i(x, y);
				if (!level.world().isFoodTile(tile) || level.world().containsEatenFood(tile)) {
					continue;
				}
				if (level.world().isEnergizerTile(tile) && pac.powerTimer().remaining() > 2 * 60
						&& level.world().uneatenFoodCount() > 1) {
					continue;
				}
				float dist = pacManTile.manhattanDistance(tile);
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
		LOG.trace("Nearest food tiles from Pac-Man location %s: (time %.2f millis)", pacManTile, time / 1_000_000f);
		for (Vector2i t : foodTiles) {
			LOG.trace("\t%s (%.2g tiles away from Pac-Man, %.2g tiles away from ghosts)", t, t.manhattanDistance(pacManTile),
					minDistanceFromGhosts(level));
		}
		return foodTiles;
	}

	private Vector2i findTileFarestFromGhosts(GameLevel level, List<Vector2i> tiles) {
		Vector2i farestTile = null;
		float maxDist = -1;
		for (Vector2i tile : tiles) {
			float dist = minDistanceFromGhosts(level);
			if (dist > maxDist) {
				maxDist = dist;
				farestTile = tile;
			}
		}
		return farestTile;
	}

	private float minDistanceFromGhosts(GameLevel level) {
		var pac = level.pac();
		return (float) level.ghosts().map(Ghost::tile).mapToDouble(pac.tile()::manhattanDistance).min().getAsDouble();
	}
}
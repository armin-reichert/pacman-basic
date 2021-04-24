package de.amr.games.pacman.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;

/**
 * Controls automatic movement of the player.
 * 
 * @author Armin Reichert
 */
public class Autopilot {

	public boolean logEnabled;

	public void steer(AbstractGameModel game) {
		if (game.player.forced) {
			game.player.forced = false;
			return;
		}
		if (!game.player.stuck && !game.player.newTileEntered) {
			return;
		}
		AutopilotData data = collectData(game);
		if (data.hunterAhead != null || data.hunterBehind != null || !data.frightenedGhosts.isEmpty()) {
			log("\n%s", data);
		}
		takeAction(game, data);
	}

	private void log(String msg, Object... args) {
		if (logEnabled) {
			Logging.log(msg, args);
		}
	}

	private AutopilotData collectData(AbstractGameModel game) {
		AutopilotData data = new AutopilotData();
		Ghost hunterAhead = findHuntingGhostAhead(game); // Where is Hunter?
		if (hunterAhead != null) {
			data.hunterAhead = hunterAhead;
			data.hunterAheadDistance = game.player.tile().manhattanDistance(hunterAhead.tile());
		}
		Ghost hunterBehind = findHuntingGhostBehind(game);
		if (hunterBehind != null) {
			data.hunterBehind = hunterBehind;
			data.hunterBehindDistance = game.player.tile().manhattanDistance(hunterBehind.tile());
		}
		data.frightenedGhosts = Stream.of(game.ghosts).filter(ghost -> ghost.is(GhostState.FRIGHTENED))
				.filter(ghost -> ghost.tile().manhattanDistance(game.player.tile()) <= AutopilotData.MAX_GHOST_CHASE_DIST)
				.collect(Collectors.toList());
		data.frightenedGhostsDistance = data.frightenedGhosts.stream()
				.map(ghost -> ghost.tile().manhattanDistance(game.player.tile())).collect(Collectors.toList());
		return data;
	}

	private void takeAction(AbstractGameModel game, AutopilotData data) {
		if (data.hunterAhead != null) {
			Direction escapeDir = null;
			if (data.hunterBehind != null) {
				escapeDir = findEscapeDirectionExcluding(game, EnumSet.of(game.player.dir(), game.player.dir().opposite()));
				log("Detected ghost %s behind, escape direction is %s", data.hunterAhead.name, escapeDir);
			} else {
				escapeDir = findEscapeDirectionExcluding(game, EnumSet.of(game.player.dir()));
				log("Detected ghost %s ahead, escape direction is %s", data.hunterAhead.name, escapeDir);
			}
			if (escapeDir != null) {
				game.player.setWishDir(escapeDir);
				game.player.forced = true;
			}
			return;
		}

		// when not escaping ghost, keep move direction at least until next intersection
		if (!game.player.stuck && !game.currentLevel.world.isIntersection(game.player.tile()))
			return;

		if (data.frightenedGhosts.size() != 0 && game.player.powerTimer.ticksRemaining() >= 1 * 60) {
			Ghost prey = data.frightenedGhosts.get(0);
			log("Detected frightened ghost %s %.0g tiles away", prey.name, prey.tile().manhattanDistance(game.player.tile()));
			game.player.targetTile = prey.tile();
		} else if (game.bonus.edibleTicksLeft > 0
				&& game.bonus.tile().manhattanDistance(game.player.tile()) <= AutopilotData.MAX_BONUS_HARVEST_DIST) {
			log("Detected active bonus");
			game.player.targetTile = game.bonus.tile();
		} else {
			V2i foodTile = findTileFarestFromGhosts(game, findNearestFoodTiles(game));
			game.player.targetTile = foodTile;
		}
		if (game.player.targetTile != null) {
			game.player.selectDirectionTowardsTarget();
		}
	}

	private Ghost findHuntingGhostAhead(AbstractGameModel game) {
		V2i pacManTile = game.player.tile();
		boolean energizerFound = false;
		for (int i = 1; i <= AutopilotData.MAX_GHOST_AHEAD_DETECTION_DIST; ++i) {
			V2i ahead = pacManTile.plus(game.player.dir().vec.scaled(i));
			if (!game.player.canAccessTile(ahead)) {
				break;
			}
			if (game.currentLevel.world.isEnergizerTile(ahead) && !game.currentLevel.isFoodRemoved(ahead)) {
				energizerFound = true;
			}
			V2i aheadLeft = ahead.plus(game.player.dir().turnLeft().vec),
					aheadRight = ahead.plus(game.player.dir().turnRight().vec);
			for (Ghost ghost : game.ghosts) {
				if (ghost.state != GhostState.HUNTING_PAC) {
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

	private Ghost findHuntingGhostBehind(AbstractGameModel game) {
		V2i pacManTile = game.player.tile();
		for (int i = 1; i <= AutopilotData.MAX_GHOST_BEHIND_DETECTION_DIST; ++i) {
			V2i behind = pacManTile.plus(game.player.dir().opposite().vec.scaled(i));
			if (!game.player.canAccessTile(behind)) {
				break;
			}
			for (Ghost ghost : game.ghosts) {
				if (ghost.state == GhostState.HUNTING_PAC && ghost.tile().equals(behind)) {
					return ghost;
				}
			}
		}
		return null;
	}

	private Direction findEscapeDirectionExcluding(AbstractGameModel game, Collection<Direction> forbidden) {
		V2i pacManTile = game.player.tile();
		List<Direction> escapes = new ArrayList<>(4);
		for (Direction dir : Direction.shuffled()) {
			if (forbidden.contains(dir)) {
				continue;
			}
			V2i neighbor = pacManTile.plus(dir.vec);
			if (game.player.canAccessTile(neighbor)) {
				escapes.add(dir);
			}
		}
		for (Direction escape : escapes) {
			V2i escapeTile = pacManTile.plus(escape.vec);
			if (game.currentLevel.world.isTunnel(escapeTile)) {
				return escape;
			}
		}
		return escapes.isEmpty() ? null : escapes.get(0);
	}

	private List<V2i> findNearestFoodTiles(AbstractGameModel game) {
		long time = System.nanoTime();
		List<V2i> foodTiles = new ArrayList<>();
		V2i pacManTile = game.player.tile();
		double minDist = Double.MAX_VALUE;
		for (int x = 0; x < game.currentLevel.world.numCols(); ++x) {
			for (int y = 0; y < game.currentLevel.world.numRows(); ++y) {
				V2i tile = new V2i(x, y);
				if (!game.currentLevel.world.isFoodTile(tile) || game.currentLevel.isFoodRemoved(tile)) {
					continue;
				}
				if (game.currentLevel.world.isEnergizerTile(tile) && game.player.powerTimer.ticksRemaining() > 1 * 60
						&& game.currentLevel.foodRemaining > 1) {
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
					minDistanceFromGhosts(game));
		}
		return foodTiles;
	}

	private V2i findTileFarestFromGhosts(AbstractGameModel game, List<V2i> tiles) {
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

	private double minDistanceFromGhosts(AbstractGameModel game) {
		return Stream.of(game.ghosts).map(Ghost::tile).mapToDouble(game.player.tile()::manhattanDistance).min()
				.getAsDouble();
	}
}
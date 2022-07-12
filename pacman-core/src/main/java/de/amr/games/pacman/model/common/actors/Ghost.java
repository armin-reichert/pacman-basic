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
package de.amr.games.pacman.model.common.actors;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.actors.GhostState.EATEN;
import static de.amr.games.pacman.model.common.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;
import static de.amr.games.pacman.model.common.actors.GhostState.RETURNING_TO_HOUSE;
import static de.amr.games.pacman.model.common.world.World.HTS;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.model.common.GameModel;

/**
 * There are 4 ghosts with different "personalities".
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getFormatterLogger();

	/** ID of red */
	public static final int RED_GHOST = 0;

	/** ID of pink */
	public static final int PINK_GHOST = 1;

	/** ID of cyan */
	public static final int CYAN_GHOST = 2;

	/** ID of orange */
	public static final int ORANGE_GHOST = 3;

	/** The ID of the ghost, see {@link GameModel#RED_GHOST} etc. */
	public final int id;

	/** The current state of the */
	private GhostState state;

	/** The home position of the ghost. */
	public V2d homePosition;

	/** The revival tile inside the house. For the red ghost, this is different from the home location. */
	public V2i revivalTile;

	/** Scatter target. */
	public V2i scatterTile;

	/** Value from <code>0..4</code>. Ghosts killed by same energizer are indexed in order. */
	public int killedIndex;

	/** Individual food counter, used to determine when the ghost can leave the house. */
	public int dotCounter;

	/** "Cruise Elroy" mode. Values: 0 (off), 1, -1, 2, -2 (negative means disabled). */
	public int elroy;

	/** Tiles where the ghost cannot move upwards when in chasing or scattering mode. */
	public List<V2i> upwardsBlockedTiles = List.of();

	public Ghost(int id, String name) {
		super(name);
		this.id = id;
	}

	@Override
	public String toString() {
		return String.format("[Ghost %s: state=%s, position=%s, tile=%s, offset=%s, velocity=%s, dir=%s, wishDir=%s]", name,
				state, position, tile(), offset(), velocity, moveDir, wishDir);
	}

	public GhostState getState() {
		return state;
	}

	public boolean is(GhostState... alternatives) {
		return U.oneOf(state, alternatives);
	}

	public void update(GameModel game) {
		switch (state) {
		case LOCKED -> doLocked(game);
		case LEAVING_HOUSE -> doLeavingHouse(game);
		case HUNTING_PAC -> doHunting(game);
		case FRIGHTENED -> doFrightened(game);
		case EATEN -> doEaten(game);
		case RETURNING_TO_HOUSE -> doReturningToHouse(game);
		case ENTERING_HOUSE -> doEnteringHouse(game);
		}
		advanceAnimation();
	}

	public void doLocked(GameModel game) {
		if (state != LOCKED) {
			state = LOCKED;
			setAnimation(AnimKeys.GHOST_COLOR).ifPresent(EntityAnimation::reset);
			return;
		}
		bounce();
		animationSet().ifPresent(animSet -> {
			if (game.powerTimer.isRunning()) {
				if (animSet.selected().equals(AnimKeys.GHOST_COLOR)) {
					setAnimation(AnimKeys.GHOST_BLUE);
				}
				ensureFlashingWhenPowerCeases(game);
			} else {
				setAnimation(AnimKeys.GHOST_COLOR);
			}
		});
	}

	private void bounce() {
		if (position.y() <= homePosition.y() - HTS) {
			setBothDirs(DOWN);
		} else if (position.y() >= homePosition.y() + HTS) {
			setBothDirs(UP);
		}
		move();
	}

	public void doLeavingHouse(GameModel game) {
		if (state != LEAVING_HOUSE) {
			state = LEAVING_HOUSE;
			setAbsSpeed(0.5); // not sure
			setAnimation(AnimKeys.GHOST_COLOR);
			GameEvents.publish(new GameEvent(game, GameEventType.GHOST_STARTS_LEAVING_HOUSE, this, tile()));
			return;
		}
		boolean outside = world.ghostHouse().leadGuestOutOfHouse(this);
		if (outside) {
			setBothDirs(LEFT);
			newTileEntered = false; // move left into next tile before changing direction
			doHunting(game);
			GameEvents.publish(new GameEvent(game, GameEventType.GHOST_COMPLETES_LEAVING_HOUSE, this, tile()));
		}
		ensureFlashingWhenPowerCeases(game);
	}

	/*
	 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say, the original intention
	 * had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man but because of a bug, only the scatter
	 * target of Blinky and Pinky would have been affected. Who knows?
	 */
	public void doHunting(GameModel game) {
		if (state != HUNTING_PAC) {
			state = HUNTING_PAC;
			setAnimation(AnimKeys.GHOST_COLOR);
			return;
		}
		if (world.isTunnel(tile())) {
			setRelSpeed(game.level.ghostSpeedTunnel);
		} else if (elroy == 1) {
			setRelSpeed(game.level.elroy1Speed);
		} else if (elroy == 2) {
			setRelSpeed(game.level.elroy2Speed);
		} else {
			setRelSpeed(game.level.ghostSpeed);
		}
		if (game.variant == MS_PACMAN && game.huntingTimer.scatterPhase() == 0 && (id == RED_GHOST || id == PINK_GHOST)) {
			roam();
		} else if (game.huntingTimer.inChasingPhase() || elroy > 0) {
			targetTile = chasingTile(game);
			tryReachingTargetTile();
		} else {
			targetTile = scatterTile;
			tryReachingTargetTile();
		}
		setAnimation(AnimKeys.GHOST_COLOR);
	}

	private V2i chasingTile(GameModel game) {
		return switch (id) {
		case RED_GHOST -> game.pac.tile();
		case PINK_GHOST -> game.pac.tilesAheadWithBug(4);
		case CYAN_GHOST -> game.pac.tilesAheadWithBug(2).scaled(2).minus(game.theGhosts[RED_GHOST].tile());
		case ORANGE_GHOST -> tile().euclideanDistance(game.pac.tile()) < 8 ? scatterTile : game.pac.tile();
		default -> null;
		};
	}

	private void roam() {
		if (newTileEntered) {
			Direction.shuffled().stream()//
					.filter(dir -> dir != moveDir.opposite())//
					.filter(dir -> canAccessTile(tile().plus(dir.vec)))//
					.findAny()//
					.ifPresent(this::setWishDir);
		}
		tryMoving();
	}

	public void doFrightened(GameModel game) {
		if (state != FRIGHTENED) {
			state = FRIGHTENED;
			setAnimation(AnimKeys.GHOST_BLUE);
			return;
		}
		if (world.isTunnel(tile())) {
			setRelSpeed(game.level.ghostSpeedTunnel);
		} else {
			setRelSpeed(game.level.ghostSpeedFrightened);
		}
		roam();
		ensureFlashingWhenPowerCeases(game);
	}

	public void doEaten(GameModel game) {
		if (state != EATEN) {
			state = EATEN;
			targetTile = game.world().ghostHouse().entryTile();
			setAnimation(AnimKeys.GHOST_VALUE);
			// display ghost value (200, 400, 800, 1600)
			animation().ifPresent(anim -> anim.setFrameIndex(killedIndex));
		}
	}

	public void doReturningToHouse(GameModel game) {
		if (state != RETURNING_TO_HOUSE) {
			state = RETURNING_TO_HOUSE;
			targetTile = world.ghostHouse().entryTile();
			setAnimation(AnimKeys.GHOST_EYES);
			return;
		}
		if (world.ghostHouse().atHouseEntry(this)) {
			doEnteringHouse(game);
		} else {
			setRelSpeed(2 * game.level.ghostSpeed); // not sure
			tryReachingTargetTile();
		}
	}

	public void doEnteringHouse(GameModel game) {
		if (state != ENTERING_HOUSE) {
			state = ENTERING_HOUSE;
			setBothDirs(DOWN);
			targetTile = revivalTile;
			GameEvents.publish(new GameEvent(game, GameEventType.GHOST_ENTERS_HOUSE, this, tile()));
			return;
		}
		boolean arrived = world.ghostHouse().leadGuestToTile(this, targetTile);
		if (arrived) {
			doLeavingHouse(game);
		}
	}

	@Override
	public boolean canAccessTile(V2i tile) {
		if (world == null) {
			return false;
		}
		if (world.ghostHouse().isDoorTile(tile)) {
			return is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		return super.canAccessTile(tile);
	}

	@Override
	protected boolean isForbiddenDirection(Direction dir) {
		if (dir == moveDir.opposite()) {
			return true;
		}
		return state == HUNTING_PAC && dir == UP && upwardsBlockedTiles.contains(tile());
	}

	private void ensureFlashingWhenPowerCeases(GameModel game) {
		if (game.powerTimer.remaining() == GameModel.PAC_POWER_FADING_TICKS) {
			startFlashing(game.level.numFlashes);
		}
	}

	public void setFlashingStopped(boolean stopped) {
		animation(AnimKeys.GHOST_FLASHING).ifPresent(flashing -> {
			if (stopped) {
				flashing.stop();
				// this is dependent on the animation implementation: display white with red eyes
				flashing.setFrameIndex(2);
			} else {
				flashing.run();
			}
		});
	}

	private void startFlashing(int numFlashes) {
		animationSet().ifPresent(animSet -> {
			if (animSet.selected().equals(AnimKeys.GHOST_FLASHING)) {
				animSet.selectedAnimation().ensureRunning();
			} else {
				animSet.select(AnimKeys.GHOST_FLASHING);
				var flashing = animSet.selectedAnimation();
				long frameTicks = GameModel.PAC_POWER_FADING_TICKS / (numFlashes * flashing.numFrames());
				flashing.setFrameDuration(frameTicks);
				flashing.setRepetions(numFlashes);
				flashing.restart();
			}
		});
	}
}
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
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

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
import de.amr.games.pacman.model.common.world.GhostHouse;

/**
 * There are 4 ghosts with different "personalities".
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature {

	private static final Logger logger = LogManager.getFormatterLogger();

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

	/** The kill index. First ghost killed by an energizer has index 0 etc. */
	public int killIndex;

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

	public void lock() {
		enterStateLocked();
	}

	public void unlock(GameModel game) {
		if (id == RED_GHOST) {
			enterStateHunting();
		} else {
			enterStateLeavingHouse(game);
		}
	}

	public void update(GameModel game) {
		switch (state) {
		case LOCKED -> updateStateLocked(game);
		case LEAVING_HOUSE -> updateStateLeavingHouse(game);
		case HUNTING_PAC -> updateStateHunting(game);
		case FRIGHTENED -> updateStateFrightened(game);
		case DEAD -> updateStateDead(game);
		case ENTERING_HOUSE -> updateStateEnteringHouse(game);
		}
		advance();
	}

	private void enterStateLocked() {
		state = GhostState.LOCKED;
		setAnimation(AnimKeys.GHOST_COLOR).ifPresent(EntityAnimation::reset);
	}

	private void updateStateLocked(GameModel game) {
		bounce();
		animationSet().ifPresent(animSet -> {
			if (game.powerTimer.isRunning()) {
				if (animSet.selected().equals(AnimKeys.GHOST_COLOR)) {
					setAnimation(AnimKeys.GHOST_BLUE);
				}
				checkFlashing(game);
			} else {
				setAnimation(AnimKeys.GHOST_COLOR);
			}
		});
	}

	private void bounce() {
		if (position.y <= homePosition.y - HTS) {
			setBothDirs(DOWN);
		} else if (position.y >= homePosition.y + HTS) {
			setBothDirs(UP);
		}
		move();
	}

	private void enterStateLeavingHouse(GameModel game) {
		state = LEAVING_HOUSE;
		setAnimation(AnimKeys.GHOST_COLOR);
		checkFlashing(game);
		GameEvents.publish(new GameEvent(game, GameEventType.GHOST_STARTS_LEAVING_HOUSE, this, tile()));
	}

	private void updateStateLeavingHouse(GameModel game) {
		boolean outside = leaveHouse(world.ghostHouse());
		if (outside) {
			enterStateHunting();
			setBothDirs(LEFT);
			newTileEntered = false;
			GameEvents.publish(new GameEvent(game, GameEventType.GHOST_COMPLETES_LEAVING_HOUSE, this, tile()));
		}
	}

	/**
	 * Lets the ghost leave the house from its home position towards the middle of the house and then upwards towards the
	 * house door.
	 * 
	 * @param house the ghost house
	 * @return {@code true} if the ghost left the house
	 */
	private boolean leaveHouse(GhostHouse house) {
		var outsidePosition = new V2d(house.entry()).scaled(TS).plus(HTS, 0);
		if (position.x == outsidePosition.x && position.y <= outsidePosition.y) {
			setPosition(outsidePosition);
			return true;
		}
		var center = house.middleSeatCenter();
		if (U.insideRange(position.x, center.x, 1)) {
			setOffset(HTS, offset().y); // center horizontally before rising
			setBothDirs(UP);
		} else {
			setBothDirs(position.x < center.x ? RIGHT : LEFT);
		}
		move();
		return false;
	}

	public void enterStateHunting() {
		state = HUNTING_PAC;
		setAnimation(AnimKeys.GHOST_COLOR);
	}

	/*
	 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say, the original intention
	 * had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man but because of a bug, only the scatter
	 * target of Blinky and Pinky would have been affected. Who knows?
	 */
	private void updateStateHunting(GameModel game) {
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
			tryReachingTile(chasingTile(game));
		} else {
			tryReachingTile(scatterTile);
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

	public void enterStateFrightened() {
		state = FRIGHTENED;
		setAnimation(AnimKeys.GHOST_BLUE);
	}

	private void updateStateFrightened(GameModel game) {
		if (world.isTunnel(tile())) {
			setRelSpeed(game.level.ghostSpeedTunnel);
		} else {
			setRelSpeed(game.level.ghostSpeedFrightened);
		}
		roam();
		checkFlashing(game);
	}

	public void enterStateDead() {
		state = GhostState.DEAD;
		setAnimation(AnimKeys.GHOST_VALUE);
		// display ghost value sprite (200, 400, 800, 1600)
		animation().ifPresent(anim -> anim.setFrameIndex(killIndex));
	}

	private void updateStateDead(GameModel game) {
		if (atGhostHouseDoor(world.ghostHouse())) {
			enterStateEnteringHouse(game);
		} else {
			setRelSpeed(2 * game.level.ghostSpeed); // not sure
			tryReachingTile(world.ghostHouse().entry());
			setAnimation(AnimKeys.GHOST_EYES);
		}
	}

	private void enterStateEnteringHouse(GameModel game) {
		state = ENTERING_HOUSE;
		setBothDirs(DOWN);
		targetTile = revivalTile;
		GameEvents.publish(new GameEvent(game, GameEventType.GHOST_ENTERS_HOUSE, this, tile()));
	}

	private void updateStateEnteringHouse(GameModel game) {
		boolean arrived = enterHouse(world.ghostHouse());
		if (arrived) {
			setAbsSpeed(0.5);
			enterStateLeavingHouse(game);
		}
	}

	/**
	 * @return {@code true} if the ghost is at the ghosthouse door.
	 */
	private boolean atGhostHouseDoor(GhostHouse house) {
		return tile().equals(house.entry()) && U.insideRange(offset().x, HTS, 1);
	}

	/**
	 * Lets the ghost enter the house and moving to its target position.
	 * 
	 * @param house the ghost house
	 * @return {@code true} if the ghost has reached its target position
	 */
	private boolean enterHouse(GhostHouse house) {
		var tile = tile();
		if (tile.equals(targetTile) && offset().y >= 0) {
			return true;
		}
		var middle = house.seatMiddle();
		if (tile.equals(middle) && offset().y >= 0) {
			if (targetTile.x < middle.x) {
				setBothDirs(LEFT);
			} else if (targetTile.x > middle.x) {
				setBothDirs(RIGHT);
			}
		}
		move();
		return false;
	}

	public void forceTurningBack() {
		if (state == FRIGHTENED || state == HUNTING_PAC) {
			logger.info("%s got signal to reverse direction", name);
			reverse = true;
		}
	}

	@Override
	public boolean canAccessTile(V2i tile) {
		if (world == null) {
			return false;
		}
		V2i leftDoor = world.ghostHouse().doorTileLeft();
		V2i rightDoor = world.ghostHouse().doorTileRight();
		if (leftDoor.equals(tile) || rightDoor.equals(tile)) {
			return is(ENTERING_HOUSE) || is(LEAVING_HOUSE);
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

	private void checkFlashing(GameModel game) {
		animationSet().ifPresent(animSet -> {
			if (game.powerTimer.tick() == 0) {
				animSet.byName(AnimKeys.GHOST_FLASHING).stop();
			} else if (game.powerTimer.remaining() == GameModel.PAC_POWER_FADING_TICKS) {
				startFlashing(game.level.numFlashes);
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
				flashing.frameDuration(frameTicks);
				flashing.repetions(numFlashes);
				flashing.restart();
			}
		});
	}
}
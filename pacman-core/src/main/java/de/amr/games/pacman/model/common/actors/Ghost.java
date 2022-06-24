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
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.SingleSpriteAnimation;
import de.amr.games.pacman.lib.animation.SpriteAnimation;
import de.amr.games.pacman.lib.animation.SpriteAnimations;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.GhostHouse;
import de.amr.games.pacman.model.common.world.World;

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

	/** The home tile of the */
	public V2i homeTile;

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

	public void reset() {
		show();
		placeAt(homeTile, HTS, 0);
		setAbsSpeed(id == RED_GHOST ? 0 : 0.5);
		setBothDirs(switch (id) {
		case RED_GHOST -> Direction.LEFT;
		case PINK_GHOST -> Direction.DOWN;
		case CYAN_GHOST, ORANGE_GHOST -> Direction.UP;
		default -> null;
		});
		targetTile = null;
		stuck = false;
		killIndex = -1;
		enterLockedState();
	}

	public void update(GameModel game) {
		switch (state) {
		case LOCKED -> updateLockedState(game);
		case LEAVING_HOUSE -> updateLeavingHouseState(game);
		case HUNTING_PAC -> updateHuntingState(game);
		case FRIGHTENED -> updateFrightenedState(game);
		case DEAD -> updateDeadState(game);
		case ENTERING_HOUSE -> updateEnteringHouseState(game);
		}
	}

	private void updateLockedState(GameModel game) {
		bounce();
		if (game.pac.hasPower()) {
			animations().ifPresent(anims -> {
				if (anims.selected().equals(AnimKeys.GHOST_COLOR)) {
					anims.select(AnimKeys.GHOST_BLUE);
				}
			});
			updateFlashingAnimation(game);
		} else {
			selectAnimation(AnimKeys.GHOST_COLOR);
		}
	}

	private void bounce() {
		var zeroLevel = t(homeTile.y);
		if (position.y <= zeroLevel - HTS || position.y >= zeroLevel + HTS) {
			setBothDirs(moveDir.opposite());
		}
		move();
	}

	private void updateLeavingHouseState(GameModel game) {
		var world = game.level.world;
		boolean houseLeft = leaveHouse(world.ghostHouse());
		if (houseLeft) {
			state = HUNTING_PAC;
			selectAnimation(AnimKeys.GHOST_COLOR);
			// TODO Inky behaves differently. Why?
			setBothDirs(LEFT);
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
		if (tile().equals(house.entry()) && offset().y <= 1) {
			setOffset(offset().x, 0); // place exactly at house entry to avoid getting stuck
			return true;
		}
		var center = house.middleSeatCenter();
		if (U.insideRange(position.x, center.x, 1)) {
			setOffset(HTS, offset().y); // center horizontally before rising
			setBothDirs(UP);
		} else if (position.y < center.y) {
			setBothDirs(DOWN); // sink below zero level before moving to the center
		} else {
			setBothDirs(position.x < center.x ? RIGHT : LEFT);
		}
		move();
		return false;
	}

	/*
	 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say, the original intention
	 * had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man but because of a bug, only the scatter
	 * target of Blinky and Pinky would have been affected. Who knows?
	 */
	private void updateHuntingState(GameModel game) {
		var world = game.level.world;
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
			roam(game);
		} else if (game.huntingTimer.inChasingPhase() || elroy > 0) {
			chase(game);
		} else {
			scatter(game);
		}
		animations().ifPresent(anims -> anims.select(AnimKeys.GHOST_COLOR));
	}

	private void scatter(GameModel game) {
		targetTile = scatterTile;
		computeDirectionTowardsTarget(game.level.world);
		tryMoving(game.level.world);
	}

	private void chase(GameModel game) {
		targetTile = switch (id) {
		case RED_GHOST -> game.pac.tile();
		case PINK_GHOST -> game.pac.tilesAheadWithBug(4);
		case CYAN_GHOST -> game.pac.tilesAheadWithBug(2).scaled(2).minus(game.theGhosts[RED_GHOST].tile());
		case ORANGE_GHOST -> tile().euclideanDistance(game.pac.tile()) < 8 ? scatterTile : game.pac.tile();
		default -> null;
		};
		computeDirectionTowardsTarget(game.level.world);
		tryMoving(game.level.world);
	}

	private void roam(GameModel game) {
		if (newTileEntered) {
			Direction.shuffled().stream()//
					.filter(dir -> dir != moveDir.opposite())//
					.filter(dir -> canAccessTile(game.level.world, tile().plus(dir.vec)))//
					.findAny()//
					.ifPresent(this::setWishDir);
		}
		tryMoving(game.level.world);
	}

	private void updateFrightenedState(GameModel game) {
		if (game.level.world.isTunnel(tile())) {
			setRelSpeed(game.level.ghostSpeedTunnel);
		} else {
			setRelSpeed(game.level.ghostSpeedFrightened);
		}
		roam(game);
		updateFlashingAnimation(game);
	}

	private void updateDeadState(GameModel game) {
		var world = game.level.world;
		boolean houseReached = returnToHouse(world, world.ghostHouse());
		if (houseReached) {
			setBothDirs(DOWN);
			targetTile = revivalTile;
			state = ENTERING_HOUSE;
			GameEvents.publish(new GameEvent(game, GameEventType.GHOST_ENTERS_HOUSE, this, tile()));
		} else {
			setRelSpeed(2 * game.level.ghostSpeed);
			targetTile = world.ghostHouse().entry();
			selectAnimation(AnimKeys.GHOST_EYES);
		}
	}

	/**
	 * Lets the ghost return back to the ghost house entry.
	 * 
	 * @param world the world
	 * @param house the ghost house
	 * @return {@code true} if the ghost has reached the house entry before he starts to enter
	 */
	private boolean returnToHouse(World world, GhostHouse house) {
		if (atGhostHouseDoor(house) && moveDir != DOWN) {
			return true;
		}
		computeDirectionTowardsTarget(world);
		tryMoving(world);
		return false;
	}

	private void updateEnteringHouseState(GameModel game) {
		boolean revivalTileReached = enterHouse(game.level.world.ghostHouse());
		if (revivalTileReached) {
			state = LEAVING_HOUSE;
			setBothDirs(moveDir.opposite());
			setAbsSpeed(0.5);
			if (game.pac.hasPower()) {
				selectAnimation(AnimKeys.GHOST_BLUE);
			} else {
				selectAnimation(AnimKeys.GHOST_COLOR);
			}
			GameEvents.publish(new GameEvent(game, GameEventType.GHOST_STARTS_LEAVING_HOUSE, this, tile()));
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

	public boolean is(GhostState... alternatives) {
		return U.oneOf(state, alternatives);
	}

	public GhostState getState() {
		return state;
	}

	public void enterLockedState() {
		state = GhostState.LOCKED;
		animations().ifPresent(anim -> {
			selectAnimation(AnimKeys.GHOST_COLOR);
			anim.selectedAnimation().reset(); // at first frame and stopped
		});
	}

	public void enterHuntingState() {
		state = HUNTING_PAC;
		selectAnimation(AnimKeys.GHOST_COLOR);
	}

	public void enterFrightenedState() {
		state = FRIGHTENED;
		animations().ifPresent(anims -> {
			anims.byName(AnimKeys.GHOST_FLASHING).stop();
			selectAnimation(AnimKeys.GHOST_BLUE);
		});
	}

	public void enterDeadState() {
		state = GhostState.DEAD;
		animations().ifPresent(anims -> {
			anims.select(AnimKeys.GHOST_VALUE);
			anims.selectedAnimation().setFrameIndex(killIndex);
		});
	}

	public void enterLeavingHouseState(GameModel game) {
		state = LEAVING_HOUSE;
		updateFlashingAnimation(game);
	}

	@Override
	public String toString() {
		return String.format("[Ghost %s: state=%s, position=%s, tile=%s, offset=%s, velocity=%s, dir=%s, wishDir=%s]", name,
				state, position, tile(), offset(), velocity, moveDir, wishDir);
	}

	@Override
	public boolean canAccessTile(World world, V2i tile) {
		V2i leftDoor = world.ghostHouse().doorTileLeft();
		V2i rightDoor = world.ghostHouse().doorTileRight();
		if (leftDoor.equals(tile) || rightDoor.equals(tile)) {
			return is(ENTERING_HOUSE) || is(LEAVING_HOUSE);
		}
		return super.canAccessTile(world, tile);
	}

	public void checkCruiseElroyStart(GameLevel level) {
		if (level.world.foodRemaining() == level.elroy1DotsLeft) {
			elroy = 1;
			logger.info("%s becomes Cruise Elroy 1", name);
		} else if (level.world.foodRemaining() == level.elroy2DotsLeft) {
			elroy = 2;
			logger.info("%s becomes Cruise Elroy 2", name);
		}
	}

	public void stopCruiseElroyMode() {
		if (elroy > 0) {
			elroy = -elroy; // negative value means "disabled"
			logger.info("Cruise Elroy %d for %s stops", elroy, name);
		}
	}

	@Override
	protected boolean isForbiddenDirection(Direction dir) {
		if (dir == moveDir.opposite()) {
			return true;
		}
		return state == HUNTING_PAC && dir == UP && upwardsBlockedTiles.contains(tile());
	}

	// Animations

	private SpriteAnimations<Ghost> animations;

	public void setAnimations(SpriteAnimations<Ghost> animations) {
		this.animations = animations;
	}

	public Optional<SpriteAnimations<Ghost>> animations() {
		return Optional.ofNullable(animations);
	}

	public Optional<SpriteAnimation> animation(String name) {
		return animations().map(anim -> anim.byName(name));
	}

	public void selectAnimation(String name) {
		selectAnimation(name, true);
	}

	public void selectAnimation(String name, boolean ensureRunning) {
		animations().ifPresent(anim -> {
			anim.select(name);
			if (ensureRunning) {
				anim.selectedAnimation().ensureRunning();
			}
		});
	}

	private void updateFlashingAnimation(GameModel game) {
		if (game.pac.powerTimer.remaining() == 1) {
			ensureFlashingStopped();
		} else if (game.pac.powerTimer.remaining() == GameModel.PAC_POWER_FADING) {
			ensureFlashingStarted(game.level.numFlashes);
		}
	}

	private void ensureFlashingStarted(int numFlashes) {
		animations().ifPresent(anim -> {
			if (anim.selected().equals(AnimKeys.GHOST_FLASHING)) {
				anim.selectedAnimation().ensureRunning();
			} else {
				anim.select(AnimKeys.GHOST_FLASHING);
				var flashing = (SingleSpriteAnimation<?>) anim.selectedAnimation();
				long frameTicks = GameModel.PAC_POWER_FADING / (numFlashes * flashing.numFrames());
				flashing.frameDuration(frameTicks);
				flashing.repetions(numFlashes);
				flashing.restart();
			}
		});
	}

	private void ensureFlashingStopped() {
		animations().ifPresent(anim -> {
			if (anim.selected().equals(AnimKeys.GHOST_FLASHING)) {
				anim.selectedAnimation().stop();
				anim.select(AnimKeys.GHOST_COLOR);
			}
		});
	}
}
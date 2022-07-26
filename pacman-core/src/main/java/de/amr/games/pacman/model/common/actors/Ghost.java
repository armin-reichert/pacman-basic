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
import static de.amr.games.pacman.model.common.actors.GhostState.EATEN;
import static de.amr.games.pacman.model.common.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;
import static de.amr.games.pacman.model.common.actors.GhostState.RETURNING_TO_HOUSE;
import static de.amr.games.pacman.model.common.world.World.HTS;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.AnimatedEntity;
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.model.common.GameModel;

/**
 * There are 4 ghosts with different "personalities".
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature implements AnimatedEntity {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	public static final int RED_GHOST = 0;
	public static final int PINK_GHOST = 1;
	public static final int CYAN_GHOST = 2;
	public static final int ORANGE_GHOST = 3;

	/** The ID of the ghost, see {@link #RED_GHOST} etc. */
	public final int id;

	/** The current state of this ghost. */
	private GhostState state;

	/** The position of this ghost when the game starts. */
	public V2d homePosition;

	/** The tile inside the house where this ghosts get revived. Amen. */
	public V2i revivalTile;

	/** The (unreachable) tile in some corner of the world which is targetted during the scatter phase. */
	public V2i scatterTile;

	/** Ghosts killed using the same energizer are indexed in order <code>0..4</code>. */
	public int killedIndex;

	/** Ghost-specific food counter, used to determine when the ghost can leave the house. */
	public int dotCounter;

	/** "Cruise Elroy" mode. Values: <code>0 (off), 1, -1 (disabled), 2, -2 (disabled)</code>. */
	public int elroy;

	/** Tiles where the ghost cannot move upwards when in chasing or scattering mode. */
	public List<V2i> upwardsBlockedTiles = List.of();

	/** Function computing the chasing target of this ghost. */
	public Supplier<V2i> fnChasingTarget = () -> null;

	private EntityAnimationSet animationSet;

	public void setAnimationSet(EntityAnimationSet animationSet) {
		this.animationSet = animationSet;
	}

	@Override
	public Optional<EntityAnimationSet> animationSet() {
		return Optional.ofNullable(animationSet);
	}

	public Ghost(int id, String name) {
		super(name);
		if (id < 0 || id > 3) {
			throw new IllegalArgumentException("Ghost ID must be in range 0..3");
		}
		this.id = id;
	}

	/**
	 * Executes a single simulation step for this ghost in the specified game.
	 * 
	 * @param game the game
	 */
	public void update(GameModel game) {
		switch (state) {
		case LOCKED -> doLocked(game);
		case LEAVING_HOUSE -> doLeavingHouse(game);
		case HUNTING_PAC -> doHuntingPac(game);
		case FRIGHTENED -> doFrightened(game);
		case EATEN -> doEaten(game);
		case RETURNING_TO_HOUSE -> doReturningToHouse(game);
		case ENTERING_HOUSE -> doEnteringHouse(game);
		}
		advanceAnimation();
	}

	public void setState(GhostState state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "[Ghost %s: state=%s, position=%s, tile=%s, offset=%s, velocity=%s, dir=%s, wishDir=%s]".formatted(name,
				state, position, tile(), offset(), velocity, moveDir, wishDir);
	}

	public GhostState getState() {
		return state;
	}

	public boolean is(GhostState... alternatives) {
		return U.oneOf(state, alternatives);
	}

	/**
	 * In locked state, ghosts inside the house are bouncing up and down. They become blue and blink if Pac-Man gets/loses
	 * power. After that, they return to their normal color.
	 * 
	 * @param game the game
	 */
	public void doLocked(GameModel game) {
		if (state != LOCKED) {
			state = LOCKED;
			selectAndResetAnimation(AnimKeys.GHOST_COLOR);
			return;
		}
		bounce();
		updateGhostInHouseAnimation(game);
	}

	private void updateGhostInHouseAnimation(GameModel game) {
		if (inDanger(game) && killedIndex == -1) {
			if (!isAnimationSelected(AnimKeys.GHOST_FLASHING)) {
				selectAndRunAnimation(AnimKeys.GHOST_BLUE);
			}
			ensureFlashingWhenPowerCeases(game);
		} else {
			selectAndRunAnimation(AnimKeys.GHOST_COLOR);
		}
	}

	private void bounce() {
		if (position.y() <= homePosition.y() - HTS) {
			setBothDirs(DOWN);
		} else if (position.y() >= homePosition.y() + HTS) {
			setBothDirs(UP);
		}
		move();
	}

	/**
	 * When a ghost leaves the house, he follows a specific route from his home/revival position to the house exit. This
	 * logic is house-specific so it is placed in the house implementation. In the Arcade versions of Pac-Man and Ms.
	 * Pac-Man, the ghost first moves towards the vertical center of the house and then raises up until he has passed the
	 * door on top of the house.
	 * <p>
	 * The ghost speed is slower than outside but I do not know yet the exact value.
	 * 
	 * @param game the game
	 */
	public void doLeavingHouse(GameModel game) {
		if (state != LEAVING_HOUSE) {
			state = LEAVING_HOUSE;
			setAbsSpeed(0.55);
			selectAndRunAnimation(inDanger(game) && killedIndex == -1 ? AnimKeys.GHOST_BLUE : AnimKeys.GHOST_COLOR);
			GameEvents.publish(new GameEvent(game, GameEventType.GHOST_STARTS_LEAVING_HOUSE, this, tile()));
			return;
		}
		if (world.ghostHouse().leadGuyOutOfHouse(this)) {
			newTileEntered = false; // move left into next tile before changing direction
			setBothDirs(LEFT);
			if (inDanger(game) && killedIndex == -1) {
				doFrightened(game);
			} else {
				doHuntingPac(game);
			}
			killedIndex = -1;
			GameEvents.publish(new GameEvent(game, GameEventType.GHOST_COMPLETES_LEAVING_HOUSE, this, tile()));
		} else {
			updateGhostInHouseAnimation(game);
		}
	}

	/**
	 * There are 4 hunting phases of different duration at each level. A hunting phase always starts with a "scatter"
	 * phase where the ghosts retreat to their maze corners. After some time they start chasing Pac-Man according to their
	 * character ("Shadow", "Speedy", "Bashful", "Pokey"). The 4th hunting phase at each level has an "infinite" chasing
	 * phase.
	 * <p>
	 * 
	 * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say, the original intention
	 * had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man but because of a bug, only the scatter
	 * target of Blinky and Pinky would have been affected. Who knows?
	 */
	public void doHuntingPac(GameModel game) {
		if (state != HUNTING_PAC) {
			state = HUNTING_PAC;
			selectAndRunAnimation(AnimKeys.GHOST_COLOR);
			return;
		}
		if (insideTunnel()) {
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
			targetTile = fnChasingTarget.get();
			tryReachingTargetTile();
		} else {
			targetTile = scatterTile;
			tryReachingTargetTile();
		}
	}

	private void roam() {
		tryMoving();
		if (pseudoRandomMode) {
			setPseudoRandomWishDir();
		} else {
			if (newTileEntered || stuck) {
				Direction.shuffled().stream()//
						.filter(dir -> dir != moveDir.opposite())//
						.filter(dir -> canAccessTile(tile().plus(dir.vec)))//
						.findAny()//
						.ifPresent(this::setWishDir);
			}
		}
	}

	/**
	 * When frightened, a ghost moves randomly through the world, at each new tile he randomly decides where to move next.
	 * Reversing the move direction is not allowed in this state either.
	 * <p>
	 * A frightened ghost has a blue color and starts flashing blue/white shortly (how long exactly?) before Pac-Man loses
	 * his power.
	 * <p>
	 * Speed is about half of the normal speed.
	 * 
	 * @param game the game
	 */
	public void doFrightened(GameModel game) {
		if (state != FRIGHTENED) {
			state = FRIGHTENED;
			selectAndRunAnimation(AnimKeys.GHOST_BLUE);
			return;
		}
		setRelSpeed(insideTunnel() ? game.level.ghostSpeedTunnel : game.level.ghostSpeedFrightened);
		roam();
		ensureFlashingWhenPowerCeases(game);
	}

	/**
	 * After a ghost is eaten by Pac-Man he is displayed for a short time as the number of points earned for eating him.
	 * The value doubles for each ghost eaten using the power of the same energizer.
	 * 
	 * @param game the game
	 */
	public void doEaten(GameModel game) {
		if (state != EATEN) {
			state = EATEN;
			// display ghost value (200, 400, 800, 1600)
			selectAndRunAnimation(AnimKeys.GHOST_VALUE).ifPresent(anim -> anim.setFrameIndex(killedIndex));
		}
	}

	/**
	 * After the short time being displayed by his value, the eaten ghost is displayed by his eyes only and returns to the
	 * ghost house to be revived. Hallelujah!
	 * 
	 * @param game the game
	 */
	public void doReturningToHouse(GameModel game) {
		if (state != RETURNING_TO_HOUSE) {
			state = RETURNING_TO_HOUSE;
			targetTile = world.ghostHouse().entryTile();
			selectAndRunAnimation(AnimKeys.GHOST_EYES);
			return;
		}
		if (world.ghostHouse().atHouseEntry(this)) {
			doEnteringHouse(game);
		} else {
			setRelSpeed(2 * game.level.ghostSpeed); // not sure
			tryReachingTargetTile();
		}
	}

	/**
	 * When the eaten ghost has reached the ghost house, he starts entering it to reach his revival position. Because the
	 * exact route from the ghost house entry to the revival tile is house-specific, this logic is put into the ghost
	 * house implementation.
	 * 
	 * @param game the game
	 */
	public void doEnteringHouse(GameModel game) {
		if (state != ENTERING_HOUSE) {
			state = ENTERING_HOUSE;
			targetTile = revivalTile;
			GameEvents.publish(new GameEvent(game, GameEventType.GHOST_ENTERS_HOUSE, this, tile()));
			return;
		}
		boolean arrivedAtRevivalTile = world.ghostHouse().leadGuyToTile(this, revivalTile);
		if (arrivedAtRevivalTile) {
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
		if (inDanger(game) && game.powerTimer.remaining() <= GameModel.PAC_POWER_FADING_TICKS) {
			animationSet().ifPresent(anims -> {
				if (isAnimationSelected(AnimKeys.GHOST_FLASHING)) {
					anims.selectedAnimation().ensureRunning();
				} else {
					anims.select(AnimKeys.GHOST_FLASHING);
					var flashing = anims.selectedAnimation();
					var numFlashes = game.level.numFlashes;
					long frameTicks = GameModel.PAC_POWER_FADING_TICKS / (numFlashes * flashing.numFrames());
					flashing.setFrameDuration(frameTicks);
					flashing.setRepetions(numFlashes);
					flashing.restart();
				}
			});
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

	public boolean inDanger(GameModel game) {
		return game.powerTimer.isRunning();
	}

	public boolean insideTunnel() {
		return world.isTunnel(tile());
	}

	// HACK zone

	private boolean pseudoRandomMode;

	private int pseudoRandomIndex;

	private Direction[][] pseudoRandomDirs = { //
			{ DOWN, DOWN, RIGHT, LEFT, DOWN, RIGHT, DOWN, DOWN, RIGHT, UP }, //
			{ UP, DOWN, LEFT, UP, RIGHT, LEFT, UP, UP, LEFT, UP, LEFT, DOWN }, //
			{ RIGHT, UP, LEFT, LEFT, LEFT, LEFT, LEFT, DOWN, /* 2nd */ RIGHT, UP, /* eaten...leave house */ RIGHT }, //
			{ RIGHT, RIGHT, LEFT, UP }, //
	};

	private void setPseudoRandomWishDir() {
		if (newTileEntered && world.isIntersection(tile())) {
			setWishDir(pseudoRandomDirs[id][pseudoRandomIndex]);
			LOGGER.info("%s has new wishdir %s at tile %s, index=%d", name, wishDir, tile(), pseudoRandomIndex);
			if (++pseudoRandomIndex == pseudoRandomDirs[id].length) {
				pseudoRandomIndex = 0;
			}
		}
	}

	public void setPseudoRandomMode(boolean pseudoRandomMode) {
		this.pseudoRandomMode = pseudoRandomMode;
		pseudoRandomIndex = 0;
	}

}
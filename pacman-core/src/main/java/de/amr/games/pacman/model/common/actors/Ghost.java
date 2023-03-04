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

import static de.amr.games.pacman.event.GameEvents.publishGameEvent;
import static de.amr.games.pacman.lib.steering.Direction.DOWN;
import static de.amr.games.pacman.lib.steering.Direction.LEFT;
import static de.amr.games.pacman.lib.steering.Direction.UP;
import static de.amr.games.pacman.model.common.actors.GhostState.EATEN;
import static de.amr.games.pacman.model.common.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LOCKED;
import static de.amr.games.pacman.model.common.actors.GhostState.RETURNING_TO_HOUSE;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GhostEvent;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.lib.anim.AnimationKey;
import de.amr.games.pacman.lib.anim.AnimatedEntity;
import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.anim.EntityAnimationMap;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.World;

/**
 * There are 4 ghosts with different "personalities".
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature implements AnimatedEntity {

	public static final byte ID_RED_GHOST = 0;
	public static final byte ID_PINK_GHOST = 1;
	public static final byte ID_CYAN_GHOST = 2;
	public static final byte ID_ORANGE_GHOST = 3;

	private final byte id;
	private GhostState state;
	private Supplier<Vector2i> fnChasingTarget = () -> null;
	private EntityAnimationMap animations;
	private int killedIndex;

	public Ghost(byte id, String name) {
		super(name);
		this.id = GameModel.checkGhostID(id);
		reset();
	}

	/**
	 * The ghost ID. One of {@link #ID_RED_GHOST}, {@link #ID_PINK_GHOST}, {@link #ID_CYAN_GHOST},
	 * {@link #ID_ORANGE_GHOST}.
	 */
	public byte id() {
		return id;
	}

	@Override
	public void reset() {
		super.reset();
		setKilledIndex(-1);
	}

	/**
	 * Sets the function that provides the target tile of this ghost when chasing Pac-Man.
	 * 
	 * @param fnChasingTarget function providing the chasing target tile
	 */
	public void setChasingTarget(Supplier<Vector2i> fnChasingTarget) {
		this.fnChasingTarget = Objects.requireNonNull(fnChasingTarget);
	}

	/**
	 * @return Index <code>(0,1,2,3)</code> telling when this ghost was killed during Pac-Man power phase. If not killed,
	 *         value is -1.
	 */
	public int killedIndex() {
		return killedIndex;
	}

	public void setKilledIndex(int index) {
		if (index < -1 || index > 3) {
			throw new IllegalArgumentException("Killed index must be one of -1, 0, 1, 2, 3, but is: " + index);
		}
		this.killedIndex = index;
	}

	@Override
	public boolean canAccessTile(Vector2i tile, GameLevel level) {
		Objects.requireNonNull(tile, MSG_TILE_NULL);
		Objects.requireNonNull(level, MSG_LEVEL_NULL);
		var currentTile = tile();
		if (tile.equals(currentTile.plus(UP.vector())) && !level.isSteeringAllowed(this, UP)) {
			LOG.trace("%s cannot access tile %s because he cannot move UP at %s", name(), tile, currentTile);
			return false;
		}
		if (level.world().ghostHouse().door().contains(tile)) {
			return is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		return super.canAccessTile(tile, level);
	}

	@Override
	protected boolean canReverse(GameLevel level) {
		return isNewTileEntered() && is(HUNTING_PAC, FRIGHTENED);
	}

	/**
	 * While "scattering", a ghost aims to "his" corner in the maze and circles around the wall block in that corner
	 * 
	 * @param level game level
	 */
	public void scatter(GameLevel level) {
		Objects.requireNonNull(level, MSG_LEVEL_NULL);
		setTargetTile(level.world().ghostScatterTargetTile(id));
		navigateTowardsTarget(level);
		tryMoving(level);
	}

	/**
	 * While chasing Pac-Man, a ghost aims toward his chasing target tile
	 * 
	 * @param level game level
	 */
	public void chase(GameLevel level) {
		Objects.requireNonNull(level, MSG_LEVEL_NULL);
		setTargetTile(fnChasingTarget.get());
		navigateTowardsTarget(level);
		tryMoving(level);
	}

	/**
	 * While frightened, a ghost walks randomly through the maze.
	 * 
	 * @param level game level
	 */
	public void roam(GameLevel level) {
		Objects.requireNonNull(level, MSG_LEVEL_NULL);
		moveRandomly(level);
	}

	private void moveRandomly(GameLevel level) {
		if (isNewTileEntered() || isStuck()) {
			Direction.shuffled().stream() //
					.filter(dir -> dir != moveDir().opposite()) //
					.filter(dir -> canAccessTile(tile().plus(dir.vector()), level)) //
					.findAny() //
					.ifPresent(this::setWishDir);
		}
		tryMoving(level);
	}

	@SuppressWarnings("unused")
//	private void movePseudoRandomly(GameLevel level) {
//		var route = level.game().getDemoLevelGhostRoute(id);
//		if (route.isEmpty()) {
//			moveRandomly(level);
//		} else if (tile().equals(route.get(attractRouteIndex).tile())) {
//			var navPoint = route.get(attractRouteIndex);
//			if (atTurnPositionTo(navPoint.dir())) {
//				setWishDir(navPoint.dir());
//				LOG.trace("New wish dir %s at nav point %s for %s", navPoint.dir(), navPoint.tile(), this);
//				++attractRouteIndex;
//			}
//			tryMoving(level);
//		} else {
//			navigateTowardsTarget(level);
//			tryMoving(level);
//		}
//	}

	@Override
	public String toString() {
		return "Ghost[%-6s %s tile=%s pos=%s offset=%s velocity=%s dir=%s wishDir=%s stuck=%s reverse=%s]".formatted(name(),
				state, tile(), position, offset(), velocity, moveDir(), wishDir(), isStuck(), shouldReverse);
	}

	// Here begins the state machine part

	/** The current state of this ghost. */
	public GhostState state() {
		return state;
	}

	/**
	 * @param alternatives ghost states to be checked
	 * @return <code>true</code> if this ghost is in any of the given states. If no alternatives are given, returns
	 *         <code>false</code>
	 */
	public boolean is(GhostState... alternatives) {
		return U.oneOf(state, alternatives);
	}

	/**
	 * Executes a single simulation step for this ghost in the specified game level.
	 * 
	 * @param game the game
	 */
	public void update(GameLevel level) {
		Objects.requireNonNull(level, MSG_LEVEL_NULL);
		switch (state) {
		case LOCKED -> updateStateLocked(level);
		case LEAVING_HOUSE -> updateStateLeavingHouse(level);
		case HUNTING_PAC -> updateStateHuntingPac(level);
		case FRIGHTENED -> updateStateFrightened(level);
		case EATEN -> updateStateEaten();
		case RETURNING_TO_HOUSE -> updateStateReturningToHouse(level);
		case ENTERING_HOUSE -> updateStateEnteringHouse(level);
		default -> throw new IllegalArgumentException("Unknown state: " + state);
		}
		animate();
	}

	// --- LOCKED ---

	/**
	 * In locked state, ghosts inside the house are bouncing up and down. They become blue and blink if Pac-Man gets/loses
	 * power. After that, they return to their normal color.
	 */
	public void enterStateLocked() {
		state = LOCKED;
		setPixelSpeed(0);
		selectAndResetAnimation(AnimationKey.GHOST_COLOR);
	}

	private void updateStateLocked(GameLevel level) {
		var initialPosition = level.world().ghostInitialPosition(id);
		if (level.world().ghostHouse().contains(this)) {
			if (position.y() <= initialPosition.y() - World.HTS) {
				setMoveAndWishDir(DOWN);
			} else if (position.y() >= initialPosition.y() + World.HTS) {
				setMoveAndWishDir(UP);
			}
			setPixelSpeed(GameModel.SPEED_GHOST_INSIDE_HOUSE_PX);
			move();
		}
		boolean endangered = level.pac().powerTimer().isRunning() && killedIndex == -1;
		if (endangered) {
			updateFrightenedAnimation(level);
		} else {
			selectAndRunAnimation(AnimationKey.GHOST_COLOR);
		}
	}

	// --- LEAVING_HOUSE ---

	/**
	 * When a ghost leaves the house, he follows a specific route from his home/revival position to the house exit. This
	 * logic is house-specific so it is placed in the house implementation. In the Arcade versions of Pac-Man and Ms.
	 * Pac-Man, the ghost first moves towards the vertical center of the house and then raises up until he has passed the
	 * door on top of the house.
	 * <p>
	 * The ghost speed is slower than outside but I do not know yet the exact value.
	 */
	public void enterStateLeavingHouse(GameLevel level) {
		Objects.requireNonNull(level, MSG_LEVEL_NULL);
		state = LEAVING_HOUSE;
		setPixelSpeed(GameModel.SPEED_GHOST_INSIDE_HOUSE_PX);
		publishGameEvent(new GhostEvent(level.game(), GameEventType.GHOST_STARTS_LEAVING_HOUSE, this));
	}

	private void updateStateLeavingHouse(GameLevel level) {
		boolean endangered = level.pac().powerTimer().isRunning() && killedIndex == -1;
		if (endangered) {
			updateFrightenedAnimation(level);
		} else {
			selectAndRunAnimation(AnimationKey.GHOST_COLOR);
		}
		var outOfHouse = level.world().ghostHouse().leadOutside(this);
		if (outOfHouse) {
			setNewTileEntered(false);
			setMoveAndWishDir(LEFT);
			if (endangered) {
				enterStateFrightened();
				LOG.trace("Ghost %s leaves house frightened", name());
			} else {
				killedIndex = -1;
				enterStateHuntingPac();
				LOG.trace("Ghost %s leaves house hunting", name());
			}
			publishGameEvent(new GhostEvent(level.game(), GameEventType.GHOST_COMPLETES_LEAVING_HOUSE, this));
		}
	}

	// --- HUNTING_PAC ---

	/**
	 * In each game level there are 4 alternating (scattering vs. chasing) hunting phases of different duration. The first
	 * hunting phase is always a "scatter" phase where the ghosts retreat to their maze corners. After some time they
	 * start chasing Pac-Man according to their character ("Shadow", "Speedy", "Bashful", "Pokey"). The 4th hunting phase
	 * is an "infinite" chasing phase.
	 * <p>
	 */
	public void enterStateHuntingPac() {
		state = HUNTING_PAC;
		selectAndRunAnimation(AnimationKey.GHOST_COLOR);
	}

	private void updateStateHuntingPac(GameLevel level) {
		setRelSpeed(level.huntingSpeed(this));
		level.game().doGhostHuntingAction(level, this);
	}

	// --- FRIGHTENED ---

	/**
	 * When frightened, a ghost moves randomly through the world, at each new tile he randomly decides where to move next.
	 * Reversing the move direction is not allowed in this state either.
	 * <p>
	 * A frightened ghost has a blue color and starts flashing blue/white shortly (how long exactly?) before Pac-Man loses
	 * his power. Speed is about half of the normal speed.
	 */
	public void enterStateFrightened() {
		state = FRIGHTENED;
		selectAndRunAnimation(AnimationKey.GHOST_BLUE);
	}

	private void updateStateFrightened(GameLevel level) {
		var speed = level.world().isTunnel(tile()) ? level.params().ghostSpeedTunnel()
				: level.params().ghostSpeedFrightened();
		setRelSpeed(speed);
		roam(level);
		updateFrightenedAnimation(level);
	}

	// --- EATEN ---

	/**
	 * After a ghost is eaten by Pac-Man, he is displayed for a short time as the number of points earned for eating him.
	 * The value doubles for each ghost eaten using the power of the same energizer.
	 */
	public void enterStateEaten() {
		state = EATEN;
		selectAndRunAnimation(AnimationKey.GHOST_VALUE).ifPresent(anim -> anim.setFrameIndex(killedIndex));
	}

	private void updateStateEaten() {
		// nothing to do
	}

	// --- RETURNING_TO_HOUSE ---

	/**
	 * After the short time being displayed by his value, the eaten ghost is displayed by his eyes only and returns to the
	 * ghost house to be revived. Hallelujah!
	 * 
	 * @param level the game level
	 */
	public void enterStateReturningToHouse(GameLevel level) {
		Objects.requireNonNull(level, MSG_LEVEL_NULL);
		state = RETURNING_TO_HOUSE;
		setTargetTile(level.world().ghostHouse().door().entryTile());
		selectAndRunAnimation(AnimationKey.GHOST_EYES);
	}

	private void updateStateReturningToHouse(GameLevel level) {
		var houseEntry = level.world().ghostHouse().door().entryPosition();
		if (position().almostEquals(houseEntry, 1, 0)) {
			setPosition(houseEntry);
			enterStateEnteringHouse(level);
		} else {
			setPixelSpeed(GameModel.SPEED_GHOST_RETURNING_TO_HOUSE_PX);
			navigateTowardsTarget(level);
			tryMoving(level);
		}
	}

	// --- ENTERING_HOUSE ---

	/**
	 * When an eaten ghost reaches the ghost house, he enters and moves (is lead) to his revival position. Because the
	 * exact route from the house entry to the revival tile is house-specific, this logic is in the house implementation.
	 * 
	 * @param level the game level
	 */
	public void enterStateEnteringHouse(GameLevel level) {
		Objects.requireNonNull(level, MSG_LEVEL_NULL);
		state = ENTERING_HOUSE;
		setTargetTile(null);
		setPixelSpeed(GameModel.SPEED_GHOST_ENTERING_HOUSE_PX);
		publishGameEvent(new GhostEvent(level.game(), GameEventType.GHOST_ENTERS_HOUSE, this));
	}

	private void updateStateEnteringHouse(GameLevel level) {
		boolean atRevivalPosition = level.world().ghostHouse().leadInside(this, level.world().ghostRevivalPosition(id));
		if (atRevivalPosition) {
			setMoveAndWishDir(UP);
			enterStateLocked();
		}
	}

	// Animation

	public void setAnimations(EntityAnimationMap animationSet) {
		this.animations = animationSet;
	}

	@Override
	public Optional<EntityAnimationMap> animations() {
		return Optional.ofNullable(animations);
	}

	private void updateFrightenedAnimation(GameLevel level) {
		if (level.pac().powerTimer().remaining() > GameModel.TICKS_PAC_POWER_FADES) {
			selectAndRunAnimation(AnimationKey.GHOST_BLUE);
		} else {
			animations().ifPresent(anims -> {
				if (!anims.isSelected(AnimationKey.GHOST_FLASHING)) {
					selectAndRunAnimation(AnimationKey.GHOST_FLASHING)
							.ifPresent(flashing -> startFlashing(level.params().numFlashes(), flashing));
				}
			});
		}
	}

	private void startFlashing(int numFlashes, EntityAnimation flashing) {
		long frameTicks = GameModel.TICKS_PAC_POWER_FADES / (numFlashes * flashing.numFrames());
		flashing.setFrameDuration(frameTicks);
		flashing.setRepetitions(numFlashes);
		flashing.restart();
	}

	public void stopFlashing(boolean stopped) {
		animation(AnimationKey.GHOST_FLASHING).ifPresent(flashing -> {
			if (stopped) {
				flashing.stop();
				flashing.setFrameIndex(0);
			} else {
				flashing.start();
			}
		});
	}
}
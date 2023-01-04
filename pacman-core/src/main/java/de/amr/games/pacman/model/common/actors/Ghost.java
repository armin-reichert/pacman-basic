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
import static de.amr.games.pacman.model.common.world.World.tileAt;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GhostEvent;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.lib.anim.AnimatedEntity;
import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.anim.EntityAnimationSet;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.steering.NavigationPoint;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.pacman.PacManGameAttractModeRoutes;

/**
 * There are 4 ghosts with different "personalities".
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature implements AnimatedEntity<AnimKeys> {

	public static final byte ID_RED_GHOST = 0;
	public static final byte ID_PINK_GHOST = 1;
	public static final byte ID_CYAN_GHOST = 2;
	public static final byte ID_ORANGE_GHOST = 3;

	private final byte id;
	private GhostState state;
	private Supplier<Vector2i> fnChasingTarget = () -> null;
	private EntityAnimationSet<AnimKeys> animationSet;
	private int killedIndex;
	private int attractRouteIndex;

	public static byte checkID(byte id) {
		if (id < 0 || id > 3) {
			throw new IllegalArgumentException("Ghost ID must be in range 0..3");
		}
		return id;
	}

	public Ghost(byte id, String name) {
		super(name);
		this.id = checkID(id);
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
		attractRouteIndex = 0;
		killedIndex = -1;
	}

	public void setChasingBehavior(Supplier<Vector2i> fnTargetTile) {
		this.fnChasingTarget = Objects.requireNonNull(fnTargetTile);
	}

	/**
	 * @return Order in which ghost was killed using the same energizer (power pill). First killed ghost has index 0,
	 *         second 1 and so on. If not killed, value is -1.
	 */
	public int killedIndex() {
		return killedIndex;
	}

	public void setKilledIndex(int killedIndex) {
		if (killedIndex < -1 || killedIndex > 3) {
			throw new IllegalArgumentException("Killed index must be one of -1, 0, 1, 2, 3, but is: " + killedIndex);
		}
		this.killedIndex = killedIndex;
	}

	@Override
	public boolean canAccessTile(Vector2i tile, GameLevel level) {
		Objects.requireNonNull(tile, MSG_TILE_NULL);
		Objects.requireNonNull(level, MSG_LEVEL_NULL);
		var currentTile = tile();
		if (tile.equals(currentTile.plus(UP.vec)) && !level.isSteeringAllowed(this, UP)) {
			LOGGER.trace("%s cannot access tile %s because he cannot move UP at %s", name(), tile, currentTile);
			return false;
		}
		if (level.world().ghostHouse().isDoorTile(tile)) {
			return is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		return super.canAccessTile(tile, level);
	}

	@Override
	protected boolean canReverse(GameLevel level) {
		return isNewTileEntered() && is(HUNTING_PAC, FRIGHTENED);
	}

	public void scatter(GameLevel level) {
		setTargetTile(level.world().ghostScatterTargetTile(id));
		navigateTowardsTarget(level);
		tryMoving(level);
	}

	public void chase(GameLevel level) {
		setTargetTile(fnChasingTarget.get());
		navigateTowardsTarget(level);
		tryMoving(level);
	}

	public void roam(GameLevel level) {
		moveRandomly(level);
	}

	@SuppressWarnings("unused")
	// TODO not used yet
	private void movePseudoRandomly(GameLevel level) {
		var route = getAttractRoute(level.game().variant());
		if (route.isEmpty()) {
			moveRandomly(level);
		} else if (tile().equals(route.get(attractRouteIndex).tile())) {
			var navPoint = route.get(attractRouteIndex);
			if (atTurnPositionTo(navPoint.dir())) {
				setWishDir(navPoint.dir());
				LOGGER.trace("New wish dir %s at nav point %s for %s", navPoint.dir(), navPoint.tile(), this);
				++attractRouteIndex;
			}
			tryMoving(level);
		} else {
			navigateTowardsTarget(level);
			tryMoving(level);
		}
	}

	private void moveRandomly(GameLevel level) {
		if (isNewTileEntered() || isStuck()) {
			Direction.shuffled().stream()//
					.filter(dir -> dir != moveDir().opposite())//
					.filter(dir -> canAccessTile(tile().plus(dir.vec), level))//
					.findAny()//
					.ifPresent(this::setWishDir);
		}
		tryMoving(level);
	}

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
		default -> throw new IllegalArgumentException("Unexpected value: " + state);
		}
		animate();
	}

	// --- LOCKED ---

	public void enterStateLocked() {
		state = LOCKED;
		setAbsSpeed(0);
		selectAndResetAnimation(AnimKeys.GHOST_COLOR);
	}

	/**
	 * In locked state, ghosts inside the house are bouncing up and down. They become blue and blink if Pac-Man gets/loses
	 * power. After that, they return to their normal color.
	 * 
	 * @param level the level
	 */
	private void updateStateLocked(GameLevel level) {
		var initialPosition = level.world().ghostInitialPosition(id);
		if (level.world().ghostHouse().contains(this)) {
			if (position.y() <= initialPosition.y() - World.HTS) {
				setMoveAndWishDir(DOWN);
			} else if (position.y() >= initialPosition.y() + World.HTS) {
				setMoveAndWishDir(UP);
			}
			setAbsSpeed(GameModel.SPEED_GHOST_INSIDE_HOUSE_PX);
			move();
		}
		selectColoredAnimation(level);
	}

	// --- LEAVING_HOUSE ---

	public void enterStateLeavingHouse(GameLevel level) {
		Objects.requireNonNull(level, MSG_LEVEL_NULL);
		state = LEAVING_HOUSE;
		setAbsSpeed(GameModel.SPEED_GHOST_INSIDE_HOUSE_PX);
		GameEvents.publish(new GhostEvent(level.game(), GameEventType.GHOST_STARTS_LEAVING_HOUSE, this));
	}

	/**
	 * When a ghost leaves the house, he follows a specific route from his home/revival position to the house exit. This
	 * logic is house-specific so it is placed in the house implementation. In the Arcade versions of Pac-Man and Ms.
	 * Pac-Man, the ghost first moves towards the vertical center of the house and then raises up until he has passed the
	 * door on top of the house.
	 * <p>
	 * The ghost speed is slower than outside but I do not know yet the exact value.
	 * 
	 * @param level the level
	 */
	private void updateStateLeavingHouse(GameLevel level) {
		selectColoredAnimation(level);
		var outOfHouse = level.world().ghostHouse().leadOut(this);
		if (outOfHouse) {
			setNewTileEntered(false);
			setMoveAndWishDir(LEFT);
			if (level.pac().powerTimer().isRunning() && killedIndex == -1) {
				enterStateFrightened();
			} else {
				killedIndex = -1;
				enterStateHuntingPac();
			}
			GameEvents.publish(new GhostEvent(level.game(), GameEventType.GHOST_COMPLETES_LEAVING_HOUSE, this));
		}
	}

	// --- HUNTING_PAC ---

	/**
	 * @param level the game level
	 */
	public void enterStateHuntingPac() {
		state = HUNTING_PAC;
		selectAndRunAnimation(AnimKeys.GHOST_COLOR);
	}

	/*
	 * There are 4 hunting phases of different duration at each level. A hunting phase always starts with a "scatter"
	 * phase where the ghosts retreat to their maze corners. After some time they start chasing Pac-Man according to their
	 * character ("Shadow", "Speedy", "Bashful", "Pokey"). The 4th hunting phase at each level has an "infinite" chasing
	 * phase. <p>
	 */
	private void updateStateHuntingPac(GameLevel level) {
		setRelSpeed(level.huntingSpeed(this));
		level.game().doGhostHuntingAction(level, this);
		selectColoredAnimation(level);
	}

	// --- FRIGHTENED ---

	/**
	 * @param level the game level
	 */
	public void enterStateFrightened() {
		state = FRIGHTENED;
		attractRouteIndex = 0;
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
	 * @param level the game level
	 */
	private void updateStateFrightened(GameLevel level) {
		var speed = level.world().isTunnel(tile()) ? level.params().ghostSpeedTunnel()
				: level.params().ghostSpeedFrightened();
		setRelSpeed(speed);
		roam(level);
		selectColoredAnimation(level);
	}

	private List<NavigationPoint> getAttractRoute(GameVariant variant) {
		return switch (variant) {
		case PACMAN -> switch (id) {
		case ID_RED_GHOST -> PacManGameAttractModeRoutes.RED_GHOST;
		case ID_PINK_GHOST -> PacManGameAttractModeRoutes.PINK_GHOST;
		case ID_CYAN_GHOST -> PacManGameAttractModeRoutes.CYAN_GHOST;
		case ID_ORANGE_GHOST -> PacManGameAttractModeRoutes.ORANGE_GHOST;
		default -> throw new IllegalArgumentException();
		};
		case MS_PACMAN -> List.of();
		};
	}

	// --- EATEN ---

	/**
	 * After a ghost is eaten by Pac-Man he is displayed for a short time as the number of points earned for eating him.
	 * The value doubles for each ghost eaten using the power of the same energizer.
	 * 
	 * @param level the level
	 */
	public void enterStateEaten() {
		state = EATEN;
		selectAndRunAnimation(AnimKeys.GHOST_VALUE).ifPresent(anim -> anim.setFrameIndex(killedIndex));
	}

	private void updateStateEaten() {
		// nothing to do
	}

	// --- RETURNING_HOUSE ---

	public void enterStateReturningToHouse(GameLevel level) {
		Objects.requireNonNull(level, MSG_LEVEL_NULL);
		state = RETURNING_TO_HOUSE;
		setTargetTile(level.world().ghostHouse().entryTile());
		selectAndRunAnimation(AnimKeys.GHOST_EYES);
	}

	/**
	 * After the short time being displayed by his value, the eaten ghost is displayed by his eyes only and returns to the
	 * ghost house to be revived. Hallelujah!
	 * 
	 * @param game the game
	 */
	private void updateStateReturningToHouse(GameLevel level) {
		if (level.world().ghostHouse().atDoor(this)) {
			enterStateEnteringHouse(level);
		} else {
			setAbsSpeed(GameModel.SPEED_GHOST_RETURNING_TO_HOUSE_PX);
			navigateTowardsTarget(level);
			tryMoving(level);
		}
	}

	// ENTERING_HOUSE state

	public void enterStateEnteringHouse(GameLevel level) {
		Objects.requireNonNull(level, MSG_LEVEL_NULL);
		state = ENTERING_HOUSE;
		setTargetTile(tileAt(level.world().ghostRevivalPosition(id)));
		GameEvents.publish(new GhostEvent(level.game(), GameEventType.GHOST_ENTERS_HOUSE, this));
	}

	/**
	 * When an eaten ghost reaches the ghost house, he enters and moves to his revival position. Because the exact route
	 * from the house entry to the revival tile is house-specific, this logic is in the house implementation.
	 * 
	 * @param level the level
	 */
	private void updateStateEnteringHouse(GameLevel level) {
		setAbsSpeed(GameModel.SPEED_GHOST_ENTERING_HOUSE_PX);
		boolean atRevivalTile = level.world().ghostHouse().leadInside(this, level.world().ghostRevivalPosition(id));
		if (atRevivalTile) {
			setMoveAndWishDir(UP);
			enterStateLocked();
		}
	}

	// Animations

	public void setAnimationSet(EntityAnimationSet<AnimKeys> animationSet) {
		this.animationSet = animationSet;
	}

	@Override
	public Optional<EntityAnimationSet<AnimKeys>> animationSet() {
		return Optional.ofNullable(animationSet);
	}

	private void selectColoredAnimation(GameLevel level) {
		var pac = level.pac();
		if (!pac.powerTimer().isRunning()) {
			selectAndRunAnimation(AnimKeys.GHOST_COLOR);
		} else if (pac.powerTimer().remaining() > pac.powerFadingTicks()) {
			selectAndRunAnimation(AnimKeys.GHOST_BLUE);
		} else {
			animationSet().ifPresent(animations -> {
				if (!animations.isSelected(AnimKeys.GHOST_FLASHING)) {
					selectAndRunAnimation(AnimKeys.GHOST_FLASHING).ifPresent(flashing -> startFlashingAnimation(level, flashing));
				}
			});
		}
	}

	private void startFlashingAnimation(GameLevel level, EntityAnimation flashing) {
		int numFlashes = level.params().numFlashes();
		long frameTicks = level.pac().powerFadingTicks() / (numFlashes * flashing.numFrames());
		flashing.setFrameDuration(frameTicks);
		flashing.setRepetitions(numFlashes);
		flashing.restart();
	}

	public void setFlashingPaused(boolean paused) {
		animation(AnimKeys.GHOST_FLASHING).ifPresent(flashing -> {
			if (paused) {
				flashing.stop();
				// this is dependent on the animation implementation: display white with red eyes
				flashing.setFrameIndex(2);
			} else {
				flashing.start();
			}
		});
	}
}
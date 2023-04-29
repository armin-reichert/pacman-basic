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
package de.amr.games.pacman.model.actors;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.checkGhostID;
import static de.amr.games.pacman.lib.Globals.checkLevelNotNull;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.checkTileNotNull;
import static de.amr.games.pacman.lib.Globals.differsAtMost;
import static de.amr.games.pacman.lib.Globals.oneOf;
import static de.amr.games.pacman.lib.steering.Direction.DOWN;
import static de.amr.games.pacman.lib.steering.Direction.LEFT;
import static de.amr.games.pacman.lib.steering.Direction.RIGHT;
import static de.amr.games.pacman.lib.steering.Direction.UP;
import static de.amr.games.pacman.model.actors.GhostState.EATEN;
import static de.amr.games.pacman.model.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.model.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.actors.GhostState.LOCKED;
import static de.amr.games.pacman.model.actors.GhostState.RETURNING_TO_HOUSE;

import java.util.Optional;
import java.util.function.Supplier;

import org.tinylog.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GhostEvent;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.AnimatedEntity;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.House;

/**
 * There are 4 ghosts with different "personalities".
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature implements AnimatedEntity {

	private final byte id;
	private GhostState state;
	private Supplier<Vector2i> fnChasingTarget = () -> null;
	private Vector2f initialPosition = Vector2f.ZERO;
	private Vector2f revivalPosition = Vector2f.ZERO;
	private Vector2i scatterTile = Vector2i.ZERO;
	private Direction initialDirection = Direction.UP;

	private AnimationMap animations;
	private int killedIndex;

	public Ghost(byte id, String name) {
		super(name);
		checkGhostID(id);
		this.id = id;
		reset();
	}

	@Override
	public String toString() {
		return "[%-6s (%s) position=%s tile=%s offset=%s velocity=%s dir=%s wishDir=%s reverse=%s]".formatted(name(), state,
				position, tile(), offset(), velocity, moveDir(), wishDir(), gotReverseCommand);
	}

	/**
	 * The ghost ID. One of {@link GameModel#RED_GHOST}, {@link GameModel#PINK_GHOST}, {@link GameModel#CYAN_GHOST},
	 * {@link GameModel#ORANGE_GHOST}.
	 */
	public byte id() {
		return id;
	}

	@Override
	public void reset() {
		super.reset();
		setKilledIndex(-1);
	}

	@Override
	public Entity entity() {
		return this;
	}

	/**
	 * Sets the function that provides the target tile of this ghost when chasing Pac-Man.
	 * 
	 * @param fnChasingTarget function providing the chasing target tile
	 */
	public void setChasingTarget(Supplier<Vector2i> fnChasingTarget) {
		checkNotNull(fnChasingTarget);
		this.fnChasingTarget = fnChasingTarget;
	}

	public Vector2f initialPosition() {
		return initialPosition;
	}

	public void setInitialPosition(Vector2f pos) {
		this.initialPosition = pos;
	}

	public Vector2f revivalPosition() {
		return revivalPosition;
	}

	public void setRevivalPosition(Vector2f pos) {
		this.revivalPosition = pos;
	}

	public Vector2i scatterTile() {
		return scatterTile;
	}

	public void setScatterTile(Vector2i pos) {
		this.scatterTile = pos;
	}

	public Direction initialDirection() {
		return initialDirection;
	}

	public void setInitialDirection(Direction dir) {
		this.initialDirection = dir;
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
		checkTileNotNull(tile);
		checkLevelNotNull(level);
		var currentTile = tile();
		if (tile.equals(currentTile.plus(UP.vector())) && !level.isSteeringAllowed(this, UP)) {
			Logger.trace("{} cannot access tile {} because he cannot move UP at {}", name(), tile, currentTile);
			return false;
		}
		if (level.world().house().door().occupies(tile)) {
			return is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		return super.canAccessTile(tile, level);
	}

	@Override
	public boolean canReverse(GameLevel level) {
		checkLevelNotNull(level);
		return isNewTileEntered() && is(HUNTING_PAC, FRIGHTENED);
	}

	public void leaveHouse(GameLevel level) {
		if (level.world().house().contains(tile())) {
			enterStateLeavingHouse(level);
		} else {
			setMoveAndWishDir(LEFT);
			enterStateHuntingPac();
		}
	}

	/**
	 * While "scattering", a ghost aims to "his" corner in the maze and circles around the wall block in that corner
	 * 
	 * @param level game level
	 */
	public void scatter(GameLevel level) {
		checkLevelNotNull(level);
		setTargetTile(scatterTile);
		navigateTowardsTarget(level);
		tryMoving(level);
	}

	/**
	 * While chasing Pac-Man, a ghost aims toward his chasing target tile
	 * 
	 * @param level game level
	 */
	public void chase(GameLevel level) {
		checkLevelNotNull(level);
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
		checkLevelNotNull(level);
		moveRandomly(level);
	}

	private void moveRandomly(GameLevel level) {
		if (isNewTileEntered() || !moved()) {
			Direction.shuffled().stream() //
					.filter(dir -> dir != moveDir().opposite()) //
					.filter(dir -> canAccessTile(tile().plus(dir.vector()), level)) //
					.findAny() //
					.ifPresent(this::setWishDir);
		}
		tryMoving(level);
	}

//	private void movePseudoRandomly(GameLevel level) {
//		var route = level.game().getDemoLevelGhostRoute(id);
//		if (route.isEmpty()) {
//			moveRandomly(level);
//		} else if (tile().equals(route.get(attractRouteIndex).tile())) {
//			var navPoint = route.get(attractRouteIndex);
//			if (atTurnPositionTo(navPoint.dir())) {
//				setWishDir(navPoint.dir());
//				Logger.trace("New wish dir {} at nav point {} for {}", navPoint.dir(), navPoint.tile(), this);
//				++attractRouteIndex;
//			}
//			tryMoving(level);
//		} else {
//			navigateTowardsTarget(level);
//			tryMoving(level);
//		}
//	}

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
		return oneOf(state, alternatives);
	}

	/**
	 * Executes a single simulation step for this ghost in the specified game level.
	 * 
	 * @param game the game
	 */
	public void update(GameLevel level) {
		checkLevelNotNull(level);
		switch (state) {
		case LOCKED -> updateStateLocked(level);
		case LEAVING_HOUSE -> updateStateLeavingHouse(level);
		case HUNTING_PAC -> updateStateHuntingPac(level);
		case FRIGHTENED -> updateStateFrightened(level);
		case EATEN -> updateStateEaten();
		case RETURNING_TO_HOUSE -> updateStateReturningToHouse(level);
		case ENTERING_HOUSE -> updateStateEnteringHouse(level);
		default -> throw new IllegalArgumentException("Unknown ghost state: '%s'".formatted(state));
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
		selectAndResetAnimation(GameModel.AK_GHOST_COLOR);
	}

	private void updateStateLocked(GameLevel level) {
		var baseLevel = initialPosition.y();
		if (level.world().house().contains(tile())) {
			if (position.y() <= baseLevel - HTS) {
				setMoveAndWishDir(DOWN);
			} else if (position.y() >= baseLevel + HTS) {
				setMoveAndWishDir(UP);
			}
			setPixelSpeed(GameModel.SPEED_PX_INSIDE_HOUSE);
			move();
		}
		boolean frightened = level.pac().powerTimer().isRunning() && killedIndex == -1;
		if (frightened) {
			updateFrightenedAnimation(level);
		} else {
			// got already killed in this power phase, not frightened anymore
			selectAndRunAnimation(GameModel.AK_GHOST_COLOR);
		}
	}

	// --- LEAVING_HOUSE ---

	/**
	 * When a ghost leaves the house, he follows a specific route from his home/revival position to the house exit. This
	 * logic is house-specific so it is placed in the house implementation. In the Arcade versions of Pac-Man and Ms.
	 * Pac-Man, the ghost first moves towards the vertical center of the house and then raises up until he has passed the
	 * door on top of the house.
	 * <p>
	 * The ghost speed is slower than outside but I do not know the exact value.
	 * 
	 * @param level the game level
	 */
	public void enterStateLeavingHouse(GameLevel level) {
		checkLevelNotNull(level);
		state = LEAVING_HOUSE;
		setPixelSpeed(GameModel.SPEED_PX_INSIDE_HOUSE);
		// TODO is this event needed/handled at all?
		GameEvents.publishGameEvent(new GhostEvent(level.game(), GameEventType.GHOST_STARTS_LEAVING_HOUSE, this));
	}

	private void updateStateLeavingHouse(GameLevel level) {
		boolean frightened = level.pac().powerTimer().isRunning() && killedIndex == -1;
		if (frightened) {
			updateFrightenedAnimation(level);
		} else {
			selectAndRunAnimation(GameModel.AK_GHOST_COLOR);
		}
		var outOfHouse = moveOutsideHouse(level.world().house());
		if (outOfHouse) {
			setMoveAndWishDir(LEFT);
			newTileEntered = false; // force moving left until next tile is entered
			if (frightened) {
				enterStateFrightened();
				Logger.trace("Ghost {} leaves house frightened", name());
			} else {
				killedIndex = -1;
				enterStateHuntingPac();
				Logger.trace("Ghost {} leaves house hunting", name());
			}
			// TODO is this event needed/handled at all?
			GameEvents.publishGameEvent(new GhostEvent(level.game(), GameEventType.GHOST_COMPLETES_LEAVING_HOUSE, this));
		}
	}

	/**
	 * Ghosts first move sidewards to the center, then they raise until the house entry/exit position outside is reached.
	 */
	private boolean moveOutsideHouse(House house) {
		var endPosition = house.door().entryPosition();
		if (position().y() <= endPosition.y()) {
			setPosition(endPosition); // valign at house entry
			return true;
		}
		if (differsAtMost(velocity().length() / 2, position().x(), house.center().x())) {
			// center reached: halign and start rising
			setPosition(house.center().x(), position().y());
			setMoveAndWishDir(UP);
		} else {
			// move sidewards until center axis is reached
			setMoveAndWishDir(position().x() < house.center().x() ? RIGHT : LEFT);
		}
		move();
		return false;
	}

	/**
	 * Ghost moves down on the vertical axis to the center, then returns or moves sidewards to its seat.
	 */
	private boolean moveInsideHouse(House house, Vector2f targetPosition) {
		var entryPosition = house.door().entryPosition();
		if (position().almostEquals(entryPosition, velocity().length() / 2, 0) && moveDir() != Direction.DOWN) {
			// near entry, start entering
			setPosition(entryPosition);
			setMoveAndWishDir(Direction.DOWN);
		} else if (position().y() >= house.center().y()) {
			setPosition(position().x(), house.center().y());
			if (targetPosition.x() < house.center().x()) {
				setMoveAndWishDir(LEFT);
			} else if (targetPosition.x() > house.center().x()) {
				setMoveAndWishDir(RIGHT);
			}
		}
		move();
		boolean reachedTarget = differsAtMost(1, position().x(), targetPosition.x())
				&& position().y() >= targetPosition.y();
		if (reachedTarget) {
			setPosition(targetPosition);
		}
		return reachedTarget;
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
		selectAndRunAnimation(GameModel.AK_GHOST_COLOR);
	}

	private void updateStateHuntingPac(GameLevel level) {
		setRelSpeed(level.huntingSpeed(this));
		level.doGhostHuntingAction(this);
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
		selectAndRunAnimation(GameModel.AK_GHOST_BLUE);
	}

	private void updateStateFrightened(GameLevel level) {
		var speed = level.world().isTunnel(tile()) ? level.ghostSpeedTunnel : level.ghostSpeedFrightened;
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
		selectAndRunAnimation(GameModel.AK_GHOST_VALUE).ifPresent(anim -> anim.setFrameIndex(killedIndex));
	}

	private void updateStateEaten() {
		// wait for timeout
	}

	// --- RETURNING_TO_HOUSE ---

	/**
	 * After the short time being displayed by his value, the eaten ghost is displayed by his eyes only and returns to the
	 * ghost house to be revived. Hallelujah!
	 * 
	 * @param level the game level
	 */
	public void enterStateReturningToHouse(GameLevel level) {
		checkLevelNotNull(level);
		state = RETURNING_TO_HOUSE;
		setTargetTile(level.world().house().door().leftWing());
		selectAndRunAnimation(GameModel.AK_GHOST_EYES);
	}

	private void updateStateReturningToHouse(GameLevel level) {
		var houseEntry = level.world().house().door().entryPosition();
		// TODO should this check for difference by speed instead of 1?
		if (position().almostEquals(houseEntry, 1, 0)) {
			setPosition(houseEntry);
			enterStateEnteringHouse(level);
		} else {
			setPixelSpeed(GameModel.SPEED_PX_RETURNING_TO_HOUSE);
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
		checkLevelNotNull(level);
		state = ENTERING_HOUSE;
		setTargetTile(null);
		setPixelSpeed(GameModel.SPEED_PX_ENTERING_HOUSE);
		// TODO is this event needed/handled at all?
		GameEvents.publishGameEvent(new GhostEvent(level.game(), GameEventType.GHOST_ENTERS_HOUSE, this));
	}

	private void updateStateEnteringHouse(GameLevel level) {
		boolean atRevivalPosition = moveInsideHouse(level.world().house(), revivalPosition);
		if (atRevivalPosition) {
			setMoveAndWishDir(UP);
			enterStateLocked();
		}
	}

	// Animation

	public void setAnimations(AnimationMap animationSet) {
		this.animations = animationSet;
	}

	@Override
	public Optional<AnimationMap> animations() {
		return Optional.ofNullable(animations);
	}

	private void updateFrightenedAnimation(GameLevel level) {
		if (animations == null) {
			return;
		}
		var timer = level.pac().powerTimer();
		if (timer.remaining() == GameModel.PAC_POWER_FADES_TICKS
				|| timer.duration() < GameModel.PAC_POWER_FADES_TICKS && timer.tick() == 1) {
			startFlashing(level.numFlashes, timer.remaining());
		} else if (timer.remaining() > GameModel.PAC_POWER_FADES_TICKS) {
			selectAndRunAnimation(GameModel.AK_GHOST_BLUE);
		}
	}

	private void startFlashing(int numFlashes, long totalTicks) {
		animations.animation(GameModel.AK_GHOST_FLASHING).ifPresent(flashing -> {
			selectAndResetAnimation(GameModel.AK_GHOST_FLASHING);
			long frameTicks = totalTicks / (numFlashes * flashing.numFrames());
			flashing.setFrameDuration(frameTicks);
			flashing.setRepetitions(numFlashes);
			flashing.restart();
			Logger.trace("{}: Start flashing for {} ticks: {} flashes, {} ticks per flash", name(), totalTicks, numFlashes,
					frameTicks);
		});
	}

	public void stopFlashing(boolean stop) {
		animation(GameModel.AK_GHOST_FLASHING).ifPresent(flashing -> {
			if (stop) {
				flashing.stop();
				flashing.setFrameIndex(0);
			} else {
				flashing.start();
			}
		});
	}
}
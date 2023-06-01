/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import static de.amr.games.pacman.lib.Globals.checkLevelNotNull;

import java.util.Optional;

import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;

/**
 * Pac-Man / Ms. Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Pac extends Creature {

	public static final long REST_FOREVER = -1;

	private final TickTimer powerTimer;
	private boolean dead;
	private long restingTicks;
	private long starvingTicks;
	private PacAnimations<?, ?> animations;

	public Pac(String name) {
		super(name);
		powerTimer = new TickTimer("PacPower");
		reset();
	}

	@Override
	public String toString() {
		return String.format(
				"['%s' position=%s offset=%s tile=%s velocity=%s speed=%.2f moveDir=%s wishDir=%s dead=%s restingTicks=%d starvingTicks=%d]",
				name(), position, offset(), tile(), velocity, velocity.length(), moveDir(), wishDir(), dead, restingTicks,
				starvingTicks);
	}

	@Override
	public boolean canReverse(GameLevel level) {
		return isNewTileEntered();
	}

	@Override
	public void reset() {
		super.reset();
		dead = false;
		restingTicks = 0;
		starvingTicks = 0;
		corneringSpeedUp = 1.5f; // TODO experimental
		selectAnimation(PacAnimations.MUNCHING);
		powerTimer.reset(0);
	}

	public void update(GameLevel level) {
		checkLevelNotNull(level);
		if (dead) {
			updateDead();
		} else {
			updateAlive(level);
		}
	}

	private void updateAlive(GameLevel level) {
		if (restingTicks == REST_FOREVER) {
			return;
		}
		if (restingTicks == 0) {
			var speed = powerTimer.isRunning() ? level.pacSpeedPowered : level.pacSpeed;
			setRelSpeed(speed);
			tryMoving(level);
			selectAnimation(PacAnimations.MUNCHING);
			if (moved()) {
				startAnimation();
			} else {
				stopAnimation();
			}
		} else {
			--restingTicks;
		}
		powerTimer.advance();
	}

	private void updateDead() {
		setPixelSpeed(0);
	}

	public void killed() {
		stopAnimation();
		setPixelSpeed(0);
		dead = true;
		starvingTicks = 0;
		restingTicks = 0;
	}

	public boolean isPowerFading(GameLevel level) {
		checkLevelNotNull(level);
		return powerTimer.isRunning() && powerTimer.remaining() <= GameModel.PAC_POWER_FADES_TICKS;
	}

	public TickTimer powerTimer() {
		return powerTimer;
	}

	public boolean isDead() {
		return dead;
	}

	/* Number of ticks Pac is resting and not moving. */
	public long restingTicks() {
		return restingTicks;
	}

	public void rest(long ticks) {
		if (ticks != REST_FOREVER && ticks < 0) {
			throw new IllegalArgumentException(String.format("Resting time cannot be negative, but is: %d", ticks));
		}
		restingTicks = ticks;
	}

	/* Number of ticks since Pac has has eaten a pellet or energizer. */
	public long starvingTicks() {
		return starvingTicks;
	}

	public void starve() {
		++starvingTicks;
	}

	public void endStarving() {
		starvingTicks = 0;
	}

	public boolean isStandingStill() {
		return velocity().length() == 0 || !moved() || restingTicks == REST_FOREVER;
	}

	// Animation

	public void setAnimations(PacAnimations<?, ?> animations) {
		this.animations = animations;
	}

	public Optional<PacAnimations<?, ?>> animations() {
		return Optional.ofNullable(animations);
	}

	public void selectAnimation(String name) {
		if (animations != null) {
			animations.select(name);
		}
	}

	public void startAnimation() {
		if (animations != null) {
			animations.startSelected();
		}
	}

	public void stopAnimation() {
		if (animations != null) {
			animations.stopSelected();
		}
	}

	public void resetAnimation() {
		if (animations != null) {
			animations.resetSelected();
		}
	}
}
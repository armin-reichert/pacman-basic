/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import java.util.Optional;

import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.GameModel;

/**
 * Pac-Man / Ms. Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Pac extends Creature implements AnimationDirector<PacAnimations<?, ?>> {

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
		return "Pac [powerTimer=" + powerTimer + ", dead=" + dead + ", restingTicks=" + restingTicks + ", starvingTicks="
				+ starvingTicks + ", newTileEntered=" + newTileEntered + ", gotReverseCommand=" + gotReverseCommand
				+ ", canTeleport=" + canTeleport + ", corneringSpeedUp=" + corneringSpeedUp + ", name=" + name + ", visible="
				+ visible + ", position=" + position + ", velocity=" + velocity + ", acceleration=" + acceleration + "]";
	}

	@Override
	public boolean canReverse() {
		return isNewTileEntered();
	}

	@Override
	public void reset() {
		super.reset();
		dead = false;
		restingTicks = 0;
		starvingTicks = 0;
		corneringSpeedUp = 1.5f; // TODO experimental
		powerTimer.reset(0);
		selectAnimation(PacAnimations.MUNCHING);
	}

	public void update() {
		if (dead || restingTicks == REST_FOREVER) {
			return;
		}
		if (restingTicks == 0) {
			var speed = powerTimer.isRunning() ? level.pacSpeedPowered : level.pacSpeed;
			setRelSpeed(speed);
			tryMoving();
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

	public void killed() {
		stopAnimation();
		setPixelSpeed(0);
		dead = true;
		starvingTicks = 0;
		restingTicks = 0;
	}

	public boolean isPowerFading() {
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
			throw new IllegalArgumentException("Resting time cannot be negative, but is: " + ticks);
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

	// Animations

	public void setAnimations(PacAnimations<?, ?> animations) {
		this.animations = animations;
	}

	@Override
	public Optional<PacAnimations<?, ?>> animations() {
		return Optional.ofNullable(animations);
	}
}
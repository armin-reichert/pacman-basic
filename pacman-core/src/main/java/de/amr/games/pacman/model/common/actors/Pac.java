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
package de.amr.games.pacman.model.common.actors;

import java.util.Optional;

import de.amr.games.pacman.lib.anim.AnimatedEntity;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;

/**
 * (Ms.) Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Pac extends Creature implements AnimatedEntity {

	public static final int REST_INDEFINITE = -1;

	private final TickTimer powerTimer;
	private boolean dead;
	private int restingTicks;
	private int starvingTicks;
	private AnimationMap animationMap;

	public Pac(String name) {
		super(name);
		powerTimer = new TickTimer("PacPower");
		reset();
	}

	@Override
	public void reset() {
		super.reset();
		dead = false;
		restingTicks = 0;
		starvingTicks = 0;
		selectAndResetAnimation(GameModel.AK_PAC_MUNCHING);
		powerTimer.reset(0);
	}

	public void update(GameLevel level) {
		GameModel.checkLevelNotNull(level);
		if (dead) {
			updateDead();
		} else {
			updateAlive(level);
		}
	}

	private void updateAlive(GameLevel level) {
		switch (restingTicks) {
		case 0 -> {
			var speed = powerTimer.isRunning() ? level.pacSpeedPowered : level.pacSpeed;
			setRelSpeed(speed);
			tryMoving(level);
			selectAndRunAnimation(GameModel.AK_PAC_MUNCHING);
			if (!isStuck()) {
				animate();
			}
		}
		case REST_INDEFINITE -> {
			// Warten auf Godot
		}
		default -> --restingTicks;
		}
		powerTimer.advance();
	}

	private void updateDead() {
		setPixelSpeed(0);
		animate();
	}

	public void die() {
		stopAnimation();
		setPixelSpeed(0);
		dead = true;
	}

	public boolean isPowerFading(GameLevel level) {
		GameModel.checkLevelNotNull(level);
		return powerTimer.isRunning() && powerTimer.remaining() <= GameModel.TICKS_PAC_POWER_FADES;
	}

	@Override
	public String toString() {
		return "[Pac: name='%s' position=%s offset=%s tile=%s velocity=%s speed=%.2f moveDir=%s wishDir=%s dead=%s restingTicks=%d starvingTicks=%d]"
				.formatted(name(), position, offset(), tile(), velocity, velocity.length(), moveDir(), wishDir(), dead,
						restingTicks, starvingTicks);
	}

	/** Timer controlling how long Pac-Man has power. */
	public TickTimer powerTimer() {
		return powerTimer;
	}

	@Override
	public Optional<AnimationMap> animations() {
		return Optional.ofNullable(animationMap);
	}

	public void setAnimations(AnimationMap animationMap) {
		this.animationMap = animationMap;
	}

	public boolean isDead() {
		return dead;
	}

	/* Number of ticks Pac is resting and not moving. */
	public int restingTicks() {
		return restingTicks;
	}

	public void rest(int ticks) {
		if (ticks != REST_INDEFINITE && ticks < 0) {
			throw new IllegalArgumentException("Resting time cannot be negative, but is: %d".formatted(ticks));
		}
		restingTicks = ticks;
	}

	/* Number of ticks since Pac has has eaten a pellet or energizer. */
	public int starvingTicks() {
		return starvingTicks;
	}

	public void starve() {
		++starvingTicks;
	}

	public void endStarving() {
		starvingTicks = 0;
	}
}
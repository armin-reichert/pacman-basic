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

import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.lib.animation.AnimatedEntity;
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.model.common.GameModel;

/**
 * (Ms.) Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Pac extends Creature implements AnimatedEntity<AnimKeys> {

	private boolean autoControlled;

	private boolean dead;

	/* Number of ticks Pac is resting and not moving. */
	private int restingTicks;

	/* Number of ticks since Pac has has eaten a pellet or energizer. */
	private int starvingTicks;

	private EntityAnimationSet<AnimKeys> animationSet;

	public Pac(String name) {
		super(name);
	}

	@Override
	public String toString() {
		return "[Pac: name='%s' position=%s offset=%s tile=%s velocity=%s speed=%.2f moveDir=%s wishDir=%s dead=%s restingTicks=%d starvingTicks=%d]"
				.formatted(name, position, offset(), tile(), velocity, velocity.length(), moveDir(), wishDir(), dead,
						restingTicks, starvingTicks);
	}

	public void update(GameModel game) {
		Objects.requireNonNull(game, "Game must not be null");
		if (dead) {
			animate();
			return;
		}
		switch (restingTicks) {
		case 0 -> {
			var speed = game.powerTimer().isRunning() ? game.level().playerSpeedPowered() : game.level().playerSpeed();
			setRelSpeed(speed);
			tryMoving(game);
			if (!stuck) {
				animate();
			}
		}
		case Integer.MAX_VALUE -> {
			// rest in peace
		}
		default -> --restingTicks;
		}
	}

	public void setAnimationSet(EntityAnimationSet<AnimKeys> animationSet) {
		this.animationSet = animationSet;
	}

	@Override
	public Optional<EntityAnimationSet<AnimKeys>> animationSet() {
		return Optional.ofNullable(animationSet);
	}

	@Override
	public void reset() {
		super.reset();
		dead = false;
		restingTicks = 0;
		starvingTicks = 0;
		selectAndResetAnimation(AnimKeys.PAC_MUNCHING);
	}

	public boolean isDead() {
		return dead;
	}

	public boolean isAutoControlled() {
		return autoControlled;
	}

	public void setAutoControlled(boolean autoControlled) {
		this.autoControlled = autoControlled;
	}

	public void rest(int ticks) {
		if (ticks < 0) {
			throw new IllegalArgumentException("Resting time cannot be negative, but is: %d".formatted(ticks));
		}
		restingTicks = ticks;
	}

	public int restingTicks() {
		return restingTicks;
	}

	public void starve() {
		++starvingTicks;
	}

	public void endStarving() {
		starvingTicks = 0;
	}

	public int starvingTicks() {
		return starvingTicks;
	}

	public void die() {
		dead = true;
		setRelSpeed(0);
		stopAnimation();
	}
}
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

import java.util.Optional;

import de.amr.games.pacman.lib.animation.AnimatedEntity;
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.model.common.GameModel;

/**
 * Pac-Man or Ms. Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Pac extends Creature implements AnimatedEntity<AnimKeys> {

	/** If Pac has been killed. */
	private boolean dead = false;

	/** Number of clock ticks Pac has to rest and can not move. */
	private int restingTicks = 0;

	/** Number of clock ticks Pac has not eaten any pellet. */
	private int starvingTicks = 0;

	private EntityAnimationSet<AnimKeys> animationSet;

	public void setAnimationSet(EntityAnimationSet<AnimKeys> animationSet) {
		this.animationSet = animationSet;
	}

	@Override
	public Optional<EntityAnimationSet<AnimKeys>> animationSet() {
		return Optional.ofNullable(animationSet);
	}

	public Pac(String name) {
		super(name);
	}

	@Override
	public void reset() {
		super.reset();
		dead = false;
		restingTicks = 0;
		starvingTicks = 0;
	}

	public void rest(int ticks) {
		restingTicks = ticks;
	}

	public void starve() {
		++starvingTicks;
	}

	public void endStarving() {
		starvingTicks = 0;
	}

	public int starvingTime() {
		return starvingTicks;
	}

	public void die() {
		dead = true;
		setRelSpeed(0);
	}

	public void update(GameModel game) {
		if (dead) {
			advanceAnimation();
		} else if (restingTicks > 0) {
			--restingTicks;
		} else {
			setRelSpeed(game.powerTimer.isRunning() ? game.level.playerSpeedPowered() : game.level.playerSpeed());
			tryMoving(game);
			updateMouthAnimation();
		}
	}

	public void updateMouthAnimation() {
		animation().ifPresent(animation -> {
			if (stuck) {
				animation.stop();
			} else {
				animation.run();
			}
			animation.advance();
		});
	}
}
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

import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.animation.ThingAnimation;
import de.amr.games.pacman.lib.animation.Animations;
import de.amr.games.pacman.model.common.GameLevel;

/**
 * Pac-Man or Ms. Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Pac extends Creature {

	/** If Pac has been killed. */
	public boolean killed = false;

	/** Controls the time Pac has power. */
	public final TickTimer powerTimer = new TickTimer("Pac-power-timer");

	/** Number of clock ticks Pac is still resting and will not move. */
	public int resting = 0;

	/** Number of clock ticks Pac has not eaten any pellet. */
	public int starvingTicks = 0;

	public Pac(String name) {
		super(name);
	}

	@Override
	public String toString() {
		return String.format("%s: pos=%s, velocity=%s, speed=%.2f, dir=%s, wishDir=%s", name, position, velocity,
				velocity.length(), moveDir(), wishDir());
	}

	public boolean hasPower() {
		return powerTimer.isRunning();
	}

	public void update(GameLevel level) {
		if (resting == 0) {
			setRelSpeed(hasPower() ? level.playerSpeedPowered : level.playerSpeed);
			tryMoving(level.world);
			updateMunchAnimation();
		} else {
			--resting;
		}
		powerTimer.advance();
	}

	// Animations

	private Animations<Pac, String> animations;

	public Optional<Animations<Pac, String>> animations() {
		return Optional.ofNullable(animations);
	}

	public Optional<ThingAnimation<?>> animation(String key) {
		return animations().map(anim -> anim.byName(key));
	}

	public void setAnimations(Animations<Pac, String> animations) {
		this.animations = animations;
	}

	private void updateMunchAnimation() {
		animation("munching").ifPresent(munching -> {
			if (stuck) {
				munching.stop();
			} else {
				munching.run();
			}
		});
	}
}
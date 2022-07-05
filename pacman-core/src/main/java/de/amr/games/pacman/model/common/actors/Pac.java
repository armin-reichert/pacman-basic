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

import static de.amr.games.pacman.lib.V2i.v;
import static de.amr.games.pacman.model.common.world.World.HTS;

import java.util.Optional;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.SpriteAnimation;
import de.amr.games.pacman.lib.animation.SpriteAnimations;
import de.amr.games.pacman.model.common.GameModel;

/**
 * Pac-Man or Ms. Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Pac extends Creature {

	/** If Pac has been killed. */
	public boolean killed = false;

	/** Number of clock ticks Pac has to rest and can not move. */
	public int restingTicks = 0;

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

	public void reset() {
		show();
		placeAt(v(13, 26), HTS, 0);
		setBothDirs(Direction.LEFT);
		setAbsSpeed(0);
		targetTile = null; // used in autopilot mode
		stuck = false;
		killed = false;
		restingTicks = 0;
		starvingTicks = 0;
		animations().ifPresent(anim -> {
			anim.select(AnimKeys.PAC_MUNCHING);
			anim.selectedAnimation().reset();
		});
	}

	public void update(GameModel game) {
		if (killed) {
			setRelSpeed(0);
		} else if (restingTicks == 0) {
			setRelSpeed(game.powerTimer.isRunning() ? game.level.playerSpeedPowered : game.level.playerSpeed);
			tryMoving();
			if (stuck) {
				animation(AnimKeys.PAC_MUNCHING).ifPresent(SpriteAnimation::stop);
			} else {
				animation(AnimKeys.PAC_MUNCHING).ifPresent(SpriteAnimation::run);
			}
		} else {
			--restingTicks;
		}
		animations().map(SpriteAnimations::selectedAnimation).ifPresent(SpriteAnimation::advance);
	}

	private SpriteAnimations animations;

	public Optional<SpriteAnimations> animations() {
		return Optional.ofNullable(animations);
	}

	public Optional<SpriteAnimation> animation(String key) {
		return animations().map(anim -> anim.byName(key));
	}

	public void setAnimations(SpriteAnimations animations) {
		this.animations = animations;
	}

	public void selectAnimation(String name) {
		selectAnimation(name, true);
	}

	public void selectAnimation(String name, boolean ensureRunning) {
		animations().ifPresent(anim -> {
			anim.select(name);
			if (ensureRunning) {
				anim.selectedAnimation().ensureRunning();
			}
		});
	}

	public void animate() {
		animations().map(SpriteAnimations::selectedAnimation).ifPresent(SpriteAnimation::advance);
	}
}
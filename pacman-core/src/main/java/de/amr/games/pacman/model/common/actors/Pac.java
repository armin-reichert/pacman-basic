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

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.animation.EntityAnimations;
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
		targetTile = null; // used in autopilot mode
		stuck = false;
		killed = false;
		restingTicks = 0;
		starvingTicks = 0;
		setAbsSpeed(0);
		placeAtTile(v(13, 26), HTS, 0);
		setBothDirs(Direction.LEFT);
		selectAnimation(AnimKeys.PAC_MUNCHING);
		animations().map(EntityAnimations::selectedAnimation).ifPresent(EntityAnimation::reset);
		show();
	}

	public void update(GameModel game) {
		if (killed) {
			setRelSpeed(0);
		} else if (restingTicks == 0) {
			setRelSpeed(game.powerTimer.isRunning() ? game.level.playerSpeedPowered : game.level.playerSpeed);
			tryMoving();
			if (stuck) {
				animation(AnimKeys.PAC_MUNCHING).ifPresent(EntityAnimation::stop);
			} else {
				animation(AnimKeys.PAC_MUNCHING).ifPresent(EntityAnimation::run);
			}
		} else {
			--restingTicks;
		}
		animate();
	}

}
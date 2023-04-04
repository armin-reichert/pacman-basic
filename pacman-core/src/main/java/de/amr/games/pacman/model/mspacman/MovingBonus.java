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
package de.amr.games.pacman.model.mspacman;

import static de.amr.games.pacman.event.GameEvents.publishGameEvent;
import static de.amr.games.pacman.event.GameEvents.publishSoundEvent;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.anim.SimpleAnimation;
import de.amr.games.pacman.lib.steering.NavigationPoint;
import de.amr.games.pacman.lib.steering.RouteBasedSteering;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * A bonus that tumbles through the world, starting at some portal, making one round around the ghost house and leaving
 * the maze at some portal at the other border.
 * 
 * <p>
 * That's however not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 * 
 * @author Armin Reichert
 */
public class MovingBonus implements Bonus {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private final Creature bonusCreature = new Creature("MovingBonus") {
		{
			canTeleport = false;
		}

		@Override
		public boolean canReverse(GameLevel level) {
			return false;
		}
	};

	private final byte symbol;
	private final int points;
	private long timer;
	private byte state;
	private final SimpleAnimation<Float> jumpAnimation;
	private final RouteBasedSteering steering = new RouteBasedSteering();

	public MovingBonus(int symbol, int points) {
		this.symbol = (byte) symbol;
		this.points = points;
		bonusCreature.reset();
		jumpAnimation = new SimpleAnimation<>(1.5f, -1.5f);
		jumpAnimation.setFrameDuration(10);
		jumpAnimation.repeatForever();
		setInactive();
	}

	public void setRoute(List<NavigationPoint> route) {
		steering.setRoute(route);
		LOG.info("New route of moving bonus: %s", route);
	}

	@Override
	public Creature entity() {
		return bonusCreature;
	}

	@Override
	public String toString() {
		return "[MovingBonus state=%s symbol=%d value=%d timer=%d tile=%s]".formatted(state, symbol, points, timer,
				bonusCreature.tile());
	}

	@Override
	public byte state() {
		return state;
	}

	@Override
	public byte symbol() {
		return symbol;
	}

	@Override
	public int points() {
		return points;
	}

	@Override
	public void setInactive() {
		state = Bonus.STATE_INACTIVE;
		jumpAnimation.stop();
		bonusCreature.hide();
		bonusCreature.setPixelSpeed(0);
	}

	@Override
	public void setEdible(long ticks) {
		state = Bonus.STATE_EDIBLE;
		timer = ticks;
		jumpAnimation.restart();
		bonusCreature.show();
		bonusCreature.setPixelSpeed(0.5f); // how fast in the original game?
		bonusCreature.setTargetTile(null);
		LOG.info("Bonus gets edible: %s", this);
	}

	@Override
	public void eat() {
		state = Bonus.STATE_EATEN;
		timer = GameModel.TICKS_BONUS_POINTS_SHOWN;
		LOG.info("Bonus eaten: %s", this);
		jumpAnimation.stop();
		publishGameEvent(GameEventType.BONUS_GETS_EATEN, bonusCreature.tile());
		publishSoundEvent(GameModel.SE_BONUS_EATEN);
	}

	public float dy() {
		return jumpAnimation.isRunning() ? jumpAnimation.frame() : 0;
	}

	@Override
	public void update(GameLevel level) {
		switch (state) {
		case STATE_INACTIVE -> { // nothing to do
		}
		case STATE_EDIBLE -> {
			if (bonusCreature.sameTile(level.pac())) {
				level.game().scorePoints(points);
				eat();
				return;
			}
			steering.steer(level, bonusCreature);
			if (steering.isComplete()) {
				LOG.info("Bonus reached target: %s", this);
				publishGameEvent(GameEventType.BONUS_EXPIRES, bonusCreature.tile());
				setInactive();
				return;
			}
			bonusCreature.navigateTowardsTarget(level);
			bonusCreature.tryMoving(level);
			jumpAnimation.animate();
		}
		case STATE_EATEN -> {
			if (--timer == 0) {
				setInactive();
				LOG.info("Bonus expired: %s", this);
				publishGameEvent(GameEventType.BONUS_EXPIRES, bonusCreature.tile());
			}
		}
		default -> throw new IllegalStateException();
		}
	}
}
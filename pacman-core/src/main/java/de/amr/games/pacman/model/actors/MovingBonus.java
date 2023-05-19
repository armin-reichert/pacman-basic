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

import static de.amr.games.pacman.event.GameEvents.publishGameEvent;
import static de.amr.games.pacman.event.GameEvents.publishSoundEvent;

import java.util.List;

import org.tinylog.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.anim.SimpleAnimation;
import de.amr.games.pacman.lib.steering.NavigationPoint;
import de.amr.games.pacman.lib.steering.RouteBasedSteering;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;

/**
 * A bonus that tumbles through the world, starting at some portal, making one round around the ghost house and leaving
 * the maze at some portal at the other border.
 * 
 * <p>
 * That's however not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 * 
 * @author Armin Reichert
 */
public class MovingBonus extends Creature implements Bonus {

	private final byte symbol;
	private long timer;
	private byte state;

	private final SimpleAnimation<Float> jumpAnimation;
	private final RouteBasedSteering steering = new RouteBasedSteering();

	public MovingBonus(byte symbol) {
		super("MovingBonus-%d".formatted(symbol));
		super.reset(); // TODO check this

		this.symbol = symbol;
		this.canTeleport = false; // override setting from reset()
		this.timer = 0;
		this.state = Bonus.STATE_INACTIVE;

		jumpAnimation = new SimpleAnimation<>(1.5f, -1.5f);
		jumpAnimation.setFrameDuration(10);
		jumpAnimation.repeatForever();
	}

	@Override
	public boolean canReverse(GameLevel level) {
		return false;
	}

	@Override
	public Creature entity() {
		return this;
	}

	@Override
	public String toString() {
		return "[MovingBonus state=%s symbol=%d value=%d timer=%d tile=%s]".formatted(state, symbol(), points(), timer,
				tile());
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
		return GameModel.BONUS_VALUES_MS_PACMAN[symbol] * 100;
	}

	@Override
	public void setInactive() {
		state = Bonus.STATE_INACTIVE;
		jumpAnimation.stop();
		hide();
		setPixelSpeed(0);
	}

	@Override
	public void setEdible(long ticks) {
		state = Bonus.STATE_EDIBLE;
		timer = ticks;
		jumpAnimation.restart();
		show();
		setPixelSpeed(0.5f); // how fast in the original game?
		setTargetTile(null);
	}

	@Override
	public void eat() {
		state = Bonus.STATE_EATEN;
		timer = GameModel.BONUS_POINTS_SHOWN_TICKS;
		jumpAnimation.stop();
		Logger.info("Bonus eaten: {}", this);
		publishGameEvent(GameEventType.BONUS_GETS_EATEN, tile());
		publishSoundEvent(GameModel.SE_BONUS_EATEN);
	}

	public void setRoute(List<NavigationPoint> route) {
		steering.setRoute(route);
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
			if (sameTile(level.pac())) {
				level.game().scorePoints(points());
				eat();
				return;
			}
			steering.steer(level, this);
			if (steering.isComplete()) {
				Logger.trace("Bonus reached target: {}", this);
				publishGameEvent(GameEventType.BONUS_EXPIRES, tile());
				setInactive();
				return;
			}
			navigateTowardsTarget(level);
			tryMoving(level);
			jumpAnimation.animate();
		}
		case STATE_EATEN -> {
			if (--timer == 0) {
				setInactive();
				Logger.trace("Bonus expired: {}", this);
				publishGameEvent(GameEventType.BONUS_EXPIRES, tile());
			}
		}
		default -> throw new IllegalStateException();
		}
	}
}
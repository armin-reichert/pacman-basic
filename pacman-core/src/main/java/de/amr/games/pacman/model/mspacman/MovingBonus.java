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
package de.amr.games.pacman.model.mspacman;

import static de.amr.games.pacman.event.GameEvents.publishGameEvent;
import static de.amr.games.pacman.event.GameEvents.publishSoundEvent;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.anim.SingleEntityAnimation;
import de.amr.games.pacman.lib.steering.NavigationPoint;
import de.amr.games.pacman.lib.steering.RouteBasedSteering;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Entity;

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

	private static final Logger LOG = LogManager.getFormatterLogger();

	private final byte symbol;
	private final int points;
	private long timer;
	private BonusState state;
	private final SingleEntityAnimation<Float> jumpAnimation;
	private final RouteBasedSteering steering = new RouteBasedSteering();

	public MovingBonus(byte symbol, int points) {
		super("MovingBonus");
		this.symbol = symbol;
		this.points = points;
		reset();
		canTeleport = false;
		jumpAnimation = new SingleEntityAnimation<>(1.5f, -1.5f);
		jumpAnimation.setFrameDuration(10);
		jumpAnimation.repeatForever();
		setInactive();
	}

	public void setRoute(List<NavigationPoint> route) {
		steering.setRoute(route);
		LOG.info("New route of moving bonus: %s", route);
	}

	@Override
	public Entity entity() {
		return this;
	}

	@Override
	public String toString() {
		return "[MovingBonus state=%s symbol=%d value=%d timer=%d tile=%s]".formatted(state, symbol, points, timer, tile());
	}

	@Override
	public BonusState state() {
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
		visible = false;
		canTeleport = false;
		state = BonusState.INACTIVE;
		jumpAnimation.stop();
		setPixelSpeed(0);
	}

	@Override
	public void setEdible(long ticks) {
		state = BonusState.EDIBLE;
		timer = ticks;
		visible = true;
		jumpAnimation.restart();
		setPixelSpeed(0.5f); // how fast in the original game?
		setTargetTile(null);
		LOG.info("Bonus gets edible: %s", this);
	}

	@Override
	public void eat() {
		state = BonusState.EATEN;
		timer = GameModel.TICKS_BONUS_POINTS_SHOWN;
		LOG.info("Bonus eaten: %s", this);
		jumpAnimation.stop();
		publishGameEvent(GameEventType.BONUS_GETS_EATEN, tile());
		publishSoundEvent(GameModel.SE_BONUS_EATEN);
	}

	public float dy() {
		return jumpAnimation.isRunning() ? jumpAnimation.frame() : 0;
	}

	@Override
	public void update(GameLevel level) {
		switch (state) {
		case INACTIVE -> { // nothing to do
		}
		case EDIBLE -> {
			if (sameTile(level.pac())) {
				level.game().scorePoints(points);
				eat();
				return;
			}
			steering.steer(level, this);
			if (steering.isComplete()) {
				LOG.info("Bonus reached target: %s", this);
				publishGameEvent(GameEventType.BONUS_EXPIRES, tile());
				setInactive();
				return;
			}
			navigateTowardsTarget(level);
			tryMoving(level);
			jumpAnimation.animate();
		}
		case EATEN -> {
			if (--timer == 0) {
				setInactive();
				LOG.info("Bonus expired: %s", this);
				publishGameEvent(GameEventType.BONUS_EXPIRES, tile());
			}
		}
		default -> throw new IllegalStateException();
		}
	}
}
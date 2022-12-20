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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.FollowRoute;
import de.amr.games.pacman.lib.NavigationPoint;
import de.amr.games.pacman.lib.animation.SingleEntityAnimation;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Entity;

/**
 * A bonus that tumbles through the world, starting at some portal, making one round around the ghost house and leaving
 * the maze at some portal at the other border.
 * 
 * TODO: That's not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 * 
 * @author Armin Reichert
 */
public class MovingBonus extends Creature implements Bonus {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private final int symbol;
	private final int points;
	private long timer;
	private BonusState state;
	private final SingleEntityAnimation<Integer> jumpAnimation;
	private final FollowRoute steering = new FollowRoute();

	public MovingBonus(int symbol, int points) {
		super("MovingBonus");
		this.symbol = symbol;
		this.points = points;
		canTeleport = false;
		jumpAnimation = new SingleEntityAnimation<>(2, -2);
		jumpAnimation.setFrameDuration(10);
		jumpAnimation.repeatForever();
		setInactive();
	}

	public void setRoute(List<NavigationPoint> route) {
		steering.setRoute(route);
		LOGGER.info("New route of moving bonus: %s", route);
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
	public int symbol() {
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
		setAbsSpeed(0);
	}

	@Override
	public void setEdible(long ticks) {
		state = BonusState.EDIBLE;
		timer = ticks;
		visible = true;
		jumpAnimation.restart();
		setAbsSpeed(0.5); // how fast in the original game?
		setTargetTile(null);
		LOGGER.info("Bonus gets edible: %s", this);
	}

	@Override
	public void eat() {
		state = BonusState.EATEN;
		timer = GameModel.BONUS_EATEN_TICKS;
		LOGGER.info("Bonus eaten: %s", this);
		jumpAnimation.stop();
	}

	public int dy() {
		return jumpAnimation.isRunning() ? jumpAnimation.frame() : 0;
	}

	@Override
	public void update(GameModel game) {
		switch (state) {
		case INACTIVE -> { // nothing to do
		}
		case EDIBLE -> {
			if (sameTile(game.pac())) {
				eat();
				game.scorePoints(points);
				GameEvents.publish(GameEventType.BONUS_GETS_EATEN, tile());
				return;
			}
			steering.steer(game, this);
			if (steering.isComplete()) {
				LOGGER.info("Bonus reached target: %s", this);
				GameEvents.publish(GameEventType.BONUS_EXPIRES, tile());
				setInactive();
				return;
			}
			navigateTowardsTarget(game);
			tryMoving(game);
			jumpAnimation.animate();
		}
		case EATEN -> {
			if (--timer == 0) {
				setInactive();
				LOGGER.info("Bonus expired: %s", this);
				GameEvents.publish(GameEventType.BONUS_EXPIRES, tile());
			}
		}
		}
	}
}
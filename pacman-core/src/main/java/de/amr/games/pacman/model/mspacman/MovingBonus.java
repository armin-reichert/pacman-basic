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
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FixedRouteSteering;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.SingleSpriteAnimation;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.world.World;

/**
 * A bonus that tumbles through the world, starting at some portal, making one round around the ghost house and leaving
 * the maze at some portal at the other border.
 * 
 * TODO: That's not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 * 
 * @author Armin Reichert
 */
public class MovingBonus extends Creature implements Bonus {

	private static final Logger logger = LogManager.getFormatterLogger();

	private BonusState state;
	private int symbol;
	private int value;
	private long timer;
	private final SingleSpriteAnimation<Integer> jumpAnimation;
	private FixedRouteSteering steering;

	public MovingBonus() {
		super("MovingBonus");
		jumpAnimation = new SingleSpriteAnimation<>(2, -2);
		jumpAnimation.frameDuration(10);
		jumpAnimation.repeatForever();
		setInactive();
	}

	public void setRoute(World world, List<V2i> route, Direction startDir) {
		if (route == null || route.isEmpty()) {
			throw new IllegalArgumentException("Route must contain at least one tile");
		}
		var startTile = route.get(0);
		targetTile = startTile;
		placeAt(startTile, 0, 0);
		setBothDirs(startDir);
		steering = new FixedRouteSteering(route);
	}

	@Override
	public Entity entity() {
		return this;
	}

	@Override
	public String toString() {
		return "[MovingBonus state=%s symbol=%d value=%d timer=%d creature=%s]".formatted(state, symbol, value, timer,
				super.toString());
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
	public int value() {
		return value;
	}

	@Override
	public void setInactive() {
		visible = false;
		state = BonusState.INACTIVE;
		jumpAnimation.stop();
		setAbsSpeed(0);
	}

	@Override
	public void setEdible(int symbol, int value, long ticks) {
		this.symbol = symbol;
		this.value = value;
		state = BonusState.EDIBLE;
		timer = ticks;
		visible = true;
		jumpAnimation.restart();
		setAbsSpeed(0.4); // TODO how fast in the original game?
		logger.info("%s gets edible", this);
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
			if (game.pac.tile().equals(tile())) {
				state = BonusState.EATEN;
				timer = Bonus.EATEN_DURATION;
				jumpAnimation.stop();
				game.scores.addPoints(value);
				logger.info("Bonus eaten: %s", this);
				GameEvents.publish(GameEventType.BONUS_GETS_EATEN, tile());
				return;
			}
			steering.accept(this);
			if (steering.isComplete()) {
				logger.info("%s reached target", this);
				GameEvents.publish(GameEventType.BONUS_EXPIRES, tile());
				setInactive();
				return;
			}
			jumpAnimation.advance();
		}
		case EATEN -> {
			if (--timer == 0) {
				logger.info("%s expired", this);
				setInactive();
				GameEvents.publish(GameEventType.BONUS_EXPIRES, tile());
			}
		}
		}
	}
}
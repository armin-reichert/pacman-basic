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
package de.amr.games.pacman.model.pacman;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Entity;

/**
 * Bonus that appears at a static position.
 * 
 * @author Armin Reichert
 */
public class StaticBonus extends Entity implements Bonus {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private final byte symbol;
	private final int points;
	private long timer;
	private BonusState state;

	public StaticBonus(byte symbol, int points) {
		this.symbol = symbol;
		this.points = points;
		this.timer = 0;
		this.state = BonusState.INACTIVE;
	}

	@Override
	public Entity entity() {
		return this;
	}

	@Override
	public String toString() {
		return "[StaticBonus symbol=%d value=%d state=%s position=%s timer=%d]".formatted(symbol, points, state, position(),
				timer);
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
		timer = 0;
		state = BonusState.INACTIVE;
		hide();
	}

	@Override
	public void setEdible(long ticks) {
		if (ticks <= 0) {
			throw new IllegalArgumentException("Bonus edible time must be larger than zero");
		}
		timer = ticks;
		state = BonusState.EDIBLE;
		show();
	}

	@Override
	public void eat() {
		timer = GameModel.TICKS_BONUS_POINTS_SHOWN;
		state = BonusState.EATEN;
		LOGGER.info("Bonus eaten: %s", this);
		GameEvents.publish(GameEventType.BONUS_GETS_EATEN, tile());
	}

	private void expire() {
		setInactive();
		LOGGER.info("Bonus expired: %s", this);
		GameEvents.publish(GameEventType.BONUS_EXPIRES, tile());
	}

	@Override
	public void update(GameLevel level) {
		switch (state) {
		case INACTIVE -> {
			// stay inactive
		}
		case EDIBLE -> {
			if (sameTile(level.pac())) {
				level.game().scorePoints(points);
				eat();
			} else if (timer == 0) {
				expire();
			} else {
				--timer;
			}
		}
		case EATEN -> {
			if (timer == 0) {
				expire();
			} else {
				--timer;
			}
		}
		}
	}
}
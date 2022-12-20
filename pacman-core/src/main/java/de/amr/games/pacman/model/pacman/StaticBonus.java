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
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Entity;

/**
 * Bonus that appears at a static position.
 * 
 * @author Armin Reichert
 */
public class StaticBonus implements Bonus {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private final int symbol;
	private final int points;
	private Entity entity;
	private BonusState state;
	private long timer;

	public StaticBonus(int symbol, int points) {
		this.symbol = symbol;
		this.points = points;
		entity = new Entity();
		entity.show();
		state = BonusState.INACTIVE;
	}

	@Override
	public Entity entity() {
		return entity;
	}

	@Override
	public String toString() {
		return "[StaticBonus symbol=%d value=%d state=%s position=%s timer=%d]".formatted(symbol, points, state,
				entity.position(), timer);
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
		state = BonusState.INACTIVE;
	}

	@Override
	public void setEdible(long ticks) {
		state = BonusState.EDIBLE;
		timer = ticks;
	}

	@Override
	public void eat() {
		state = BonusState.EATEN;
		timer = GameModel.BONUS_EATEN_TICKS;
		LOGGER.info("Bonus eaten: %s", this);
	}

	@Override
	public void update(GameModel game) {
		switch (state) {
		case INACTIVE -> {
			// nothing to do
		}
		case EDIBLE -> {
			if (entity.sameTile(game.pac())) {
				eat();
				game.scorePoints(points());
				GameEvents.publish(GameEventType.BONUS_GETS_EATEN, entity.tile());
				return;
			}
			if (--timer == 0) {
				state = BonusState.INACTIVE;
				LOGGER.info("Bonus expired: %s", this);
				GameEvents.publish(GameEventType.BONUS_EXPIRES, entity.tile());
			}
		}
		case EATEN -> {
			if (--timer == 0) {
				setInactive();
				LOGGER.info("Bonus expired: %s", this);
				GameEvents.publish(GameEventType.BONUS_EXPIRES, entity.tile());
			}
		}
		}
	}
}
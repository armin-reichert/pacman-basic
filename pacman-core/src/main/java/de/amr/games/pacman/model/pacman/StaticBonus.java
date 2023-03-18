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
package de.amr.games.pacman.model.pacman;

import static de.amr.games.pacman.event.GameEvents.publishGameEvent;
import static de.amr.games.pacman.event.GameEvents.publishSoundEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Entity;

/**
 * Bonus that appears at a static position.
 * 
 * @author Armin Reichert
 */
public class StaticBonus implements Bonus {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private final Entity entity;
	private final byte symbol;
	private final int points;
	private long timer;
	private byte state;

	public StaticBonus(byte symbol, int points) {
		this.entity = new Entity();
		this.symbol = symbol;
		this.points = points;
		this.timer = 0;
		this.state = Bonus.STATE_INACTIVE;
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
		timer = 0;
		state = Bonus.STATE_INACTIVE;
		entity.hide();
	}

	@Override
	public void setEdible(long ticks) {
		if (ticks <= 0) {
			throw new IllegalArgumentException("Bonus edible time must be larger than zero");
		}
		timer = ticks;
		state = Bonus.STATE_EDIBLE;
		entity.show();
	}

	@Override
	public void eat() {
		timer = GameModel.TICKS_BONUS_POINTS_SHOWN;
		state = Bonus.STATE_EATEN;
		LOG.info("Bonus eaten: %s", this);
		publishGameEvent(GameEventType.BONUS_GETS_EATEN, entity.tile());
		publishSoundEvent(GameModel.SE_BONUS_EATEN);
	}

	private void expire() {
		setInactive();
		LOG.info("Bonus expired: %s", this);
		publishGameEvent(GameEventType.BONUS_EXPIRES, entity.tile());
	}

	@Override
	public void update(GameLevel level) {
		switch (state) {
		case Bonus.STATE_INACTIVE -> {
			// stay inactive
		}
		case Bonus.STATE_EDIBLE -> {
			if (entity.sameTile(level.pac())) {
				level.game().scorePoints(points);
				eat();
			} else if (timer == 0) {
				expire();
			} else {
				--timer;
			}
		}
		case Bonus.STATE_EATEN -> {
			if (timer == 0) {
				expire();
			} else {
				--timer;
			}
		}
		default -> throw new IllegalStateException();
		}
	}
}
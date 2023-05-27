/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import static de.amr.games.pacman.event.GameEvents.publishGameEvent;
import static de.amr.games.pacman.event.GameEvents.publishSoundEvent;

import org.tinylog.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;

/**
 * Bonus that appears at a static position.
 * 
 * @author Armin Reichert
 */
public class StaticBonus extends Entity implements Bonus {

	private final byte symbol;
	private long timer;
	private byte state;

	public StaticBonus(byte symbol) {
		this.symbol = symbol;
		this.timer = 0;
		this.state = Bonus.STATE_INACTIVE;
	}

	@Override
	public Entity entity() {
		return this;
	}

	@Override
	public String toString() {
		return String.format("[StaticBonus symbol=%d value=%d state=%s position=%s timer=%d]", symbol, points(), state,
				position, timer);
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
		return GameModel.BONUS_VALUES_PACMAN[symbol] * 100;
	}

	@Override
	public void setInactive() {
		timer = 0;
		state = Bonus.STATE_INACTIVE;
		hide();
	}

	@Override
	public void setEdible(long ticks) {
		if (ticks <= 0) {
			throw new IllegalArgumentException("Bonus edible time must be larger than zero");
		}
		timer = ticks;
		state = Bonus.STATE_EDIBLE;
		show();
	}

	@Override
	public void eat() {
		timer = GameModel.BONUS_POINTS_SHOWN_TICKS;
		state = Bonus.STATE_EATEN;
		Logger.info("Bonus eaten: {}", this);
		publishGameEvent(GameEventType.BONUS_GETS_EATEN, tile());
		publishSoundEvent(GameModel.SE_BONUS_EATEN);
	}

	private void expire() {
		setInactive();
		Logger.info("Bonus expired: {}", this);
		publishGameEvent(GameEventType.BONUS_EXPIRES, tile());
	}

	@Override
	public void update(GameLevel level) {
		switch (state) {
		case Bonus.STATE_INACTIVE: {
			// stay inactive
			break;
		}
		case Bonus.STATE_EDIBLE: {
			if (sameTile(level.pac())) {
				level.game().scorePoints(points());
				eat();
			} else if (timer == 0) {
				expire();
			} else {
				--timer;
			}
			break;
		}
		case Bonus.STATE_EATEN: {
			if (timer == 0) {
				expire();
			} else {
				--timer;
			}
			break;
		}
		default:
			throw new IllegalStateException();
		}
	}
}
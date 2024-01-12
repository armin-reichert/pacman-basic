/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import org.tinylog.Logger;

/**
 * Bonus that appears for some time at a fixed position before it gets eaten or vanishes.
 * 
 * @author Armin Reichert
 */
public class StaticBonus extends Entity implements Bonus {

	private final byte symbol;
	private final int points;
	private long timer;
	private byte state;

	public StaticBonus(byte symbol, int points) {
		this.symbol = symbol;
		this.points = points;
		this.timer = 0;
		this.state = Bonus.STATE_INACTIVE;
	}

	@Override
	public StaticBonus entity() {
		return this;
	}

	@Override
	public String toString() {
		return "StaticBonus{" +
			"symbol=" + symbol +
			", points=" + points +
			", timer=" + timer +
			", state=" + state +
			'}';
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
	public void setEaten(long ticks) {
		if (ticks <= 0) {
			throw new IllegalArgumentException("Bonus edible time must be larger than zero");
		}
		timer = ticks;
		state = Bonus.STATE_EATEN;
		Logger.info("Bonus eaten: {}", this);
		GameController.it().publishGameEvent(GameEventType.BONUS_EATEN, tile());
	}

	private void expire() {
		setInactive();
		Logger.info("Bonus expired: {}", this);
		GameController.it().publishGameEvent(GameEventType.BONUS_EXPIRED, tile());
	}

	@Override
	public void update(GameLevel level) {
		switch (state) {
		case Bonus.STATE_INACTIVE: {
			// stay inactive
			break;
		}
		case Bonus.STATE_EDIBLE: {
			// TODO does this belong here? I doubt it.
			if (sameTile(level.pac())) {
				level.game().scorePoints(points());
				setEaten(GameModel.BONUS_POINTS_SHOWN_TICKS);
				Logger.info("Scored {} points for eating bonus {}", points(), this);
				GameController.it().publishGameEvent(GameEventType.BONUS_EATEN);
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
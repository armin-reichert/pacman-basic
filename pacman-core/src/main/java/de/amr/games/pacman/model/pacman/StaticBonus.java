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

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;

import de.amr.games.pacman.event.GameEventing;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.world.World;

/**
 * Bonus that appears at a static position.
 * 
 * @author Armin Reichert
 */
public class StaticBonus extends Entity implements Bonus {

	private BonusState state;
	private int symbol;
	private int value;
	private long timer;

	public StaticBonus(V2d position) {
		this.position = position;
	}

	@Override
	public String toString() {
		return "[StaticBonus symbol=%d value=%d state=%s position=%s timer=%d]".formatted(symbol, value, state, position,
				timer);
	}

	@Override
	public BonusState state() {
		return state;
	}

	@Override
	public V2d position() {
		return position;
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
	public void init() {
		state = BonusState.INACTIVE;
	}

	@Override
	public void activate(World world, int symbol, int value, long ticks) {
		state = BonusState.EDIBLE;
		this.symbol = symbol;
		this.value = value;
		timer = ticks;
		log("%s activated", this);
	}

	@Override
	public void eat(long ticks) {
		state = BonusState.EATEN;
		timer = ticks;
	}

	@Override
	public void update(GameModel game) {
		switch (state) {
		case INACTIVE -> {
		}
		case EDIBLE -> {
			if (game.pac.tile().equals(tile())) {
				log("%s found bonus: %s", game.pac.name, this);
				game.scoring().addPoints(value());
				eat(sec_to_ticks(2));
				GameEventing.publish(GameEventType.BONUS_GETS_EATEN, tile());
			} else {
				boolean expired = tick();
				if (expired) {
					log("Bonus expired: %s", this);
					init();
					GameEventing.publish(GameEventType.BONUS_EXPIRES, tile());
				}
			}
		}
		case EATEN -> {
			boolean expired = tick();
			if (expired) {
				log("Bonus expired: %s", this);
				init();
				GameEventing.publish(GameEventType.BONUS_EXPIRES, tile());
			}
		}
		}
	}

	private boolean tick() {
		if (timer > 0) {
			--timer;
			if (timer == 0) {
				return true;
			}
		}
		return false;
	}
}
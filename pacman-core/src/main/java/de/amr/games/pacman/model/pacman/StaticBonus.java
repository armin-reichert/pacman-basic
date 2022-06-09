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

import java.util.Optional;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEventing;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.animation.GenericAnimationCollection;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusAnimationKey;
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
	private GenericAnimationCollection<Bonus, BonusAnimationKey, ?> animations;

	public StaticBonus(V2d position) {
		this.position = position;
		visible = true;
	}

	@Override
	public Entity entity() {
		return this;
	}

	@Override
	public void setAnimations(GenericAnimationCollection<Bonus, BonusAnimationKey, ?> animations) {
		this.animations = animations;
	}

	@Override
	public Optional<GenericAnimationCollection<Bonus, BonusAnimationKey, ?>> animations() {
		return Optional.of(animations);
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
	public int symbol() {
		return symbol;
	}

	@Override
	public int value() {
		return value;
	}

	@Override
	public void setInactive() {
		state = BonusState.INACTIVE;
	}

	@Override
	public void setEdible(World world, int symbol, int value, long ticks) {
		state = BonusState.EDIBLE;
		this.symbol = symbol;
		this.value = value;
		timer = ticks;
		animations.select(BonusAnimationKey.ANIM_SYMBOL);
		log("%s activated", this);
	}

	@Override
	public void setEaten(long ticks) {
		state = BonusState.EATEN;
		timer = ticks;
		animations.select(BonusAnimationKey.ANIM_VALUE);
	}

	@Override
	public void update(GameModel game) {
		switch (state) {
		case INACTIVE -> {
			animations().ifPresent(anim -> anim.select(BonusAnimationKey.ANIM_NONE));
		}
		case EDIBLE -> {
			if (game.pac.tile().equals(tile())) {
				log("%s found bonus: %s", game.pac.name, this);
				game.scores().addPoints(value());
				setEaten(sec_to_ticks(2));
				GameEventing.publish(GameEventType.BONUS_GETS_EATEN, tile());
			} else {
				boolean expired = tick();
				if (expired) {
					log("Bonus expired: %s", this);
					setInactive();
					GameEventing.publish(GameEventType.BONUS_EXPIRES, tile());
				}
			}
		}
		case EATEN -> {
			boolean expired = tick();
			if (expired) {
				log("Bonus expired: %s", this);
				setInactive();
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
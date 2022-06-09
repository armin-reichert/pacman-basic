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

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.lib.TickTimer.sec_to_ticks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEventing;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.GenericAnimationCollection;
import de.amr.games.pacman.lib.animation.SingleGenericAnimation;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusAnimationKey;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.world.Portal;
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

	private BonusState state;
	private int symbol;
	private int value;
	private long timer;
	private GenericAnimationCollection<Bonus, BonusAnimationKey, ?> animations;
	private final List<V2i> route = new ArrayList<>();
	private final SingleGenericAnimation<Integer> jumpAnimation;

	public MovingBonus() {
		super("MovingBonus");
		jumpAnimation = new SingleGenericAnimation<>(2, -2);
		jumpAnimation.frameDuration(10);
		jumpAnimation.repeatForever();
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
		state = BonusState.INACTIVE;
		animations.select(BonusAnimationKey.ANIM_NONE);
		timer = TickTimer.INDEFINITE;
		symbol = 0;
		value = 0;
		route.clear();
		jumpAnimation.stop();
	}

	@Override
	public void setEdible(World world, int symbol, int value, long ticks) {
		this.symbol = symbol;
		this.value = value;
		route.clear();
		newTileEntered = true;
		stuck = false;
		int numPortals = world.portals().size();
		if (numPortals > 0) {
			Portal entryPortal = world.portals().get(new Random().nextInt(numPortals));
			Portal exitPortal = world.portals().get(new Random().nextInt(numPortals));
			computeNewRoute(world, entryPortal, exitPortal);
			state = BonusState.EDIBLE;
			animations.select(BonusAnimationKey.ANIM_SYMBOL);
			timer = ticks;
			setAbsSpeed(0.4); // TODO how fast should it walk?
			jumpAnimation.restart();
			log("MovingBonus symbol=%d, value=%d position=%s activated", symbol, value, position);
		}
	}

	@Override
	public void setEaten(long ticks) {
		state = BonusState.EATEN;
		animations.select(BonusAnimationKey.ANIM_VALUE);
		timer = ticks;
		jumpAnimation.stop();
	}

	public void stopJumping() {
		jumpAnimation.stop();
	}

	public int dy() {
		return jumpAnimation.isRunning() ? jumpAnimation.frame() : 0;
	}

	private void computeNewRoute(World world, Portal entryPortal, Portal exitPortal) {
		V2i houseEntry = world.ghostHouse().doorTileLeft().plus(Direction.UP.vec);
		Direction travelDir = new Random().nextBoolean() ? Direction.LEFT : Direction.RIGHT;
		route.add(houseEntry);
		route.add(houseEntry.plus(Direction.DOWN.vec.scaled(world.ghostHouse().size().y + 2)));
		route.add(houseEntry);
		route.add(travelDir == Direction.RIGHT ? exitPortal.right : exitPortal.left);
		placeAt(travelDir == Direction.RIGHT ? entryPortal.left : entryPortal.right, 0, 0);
		setBothDirs(travelDir);
	}

	public boolean followRoute(World world) {
		targetTile = route.get(0);
		if (tile().equals(targetTile)) {
			route.remove(0);
			if (route.isEmpty()) {
				return true;
			}
		}
		computeDirectionTowardsTarget(world);
		tryMoving(world);
		return false;
	}

	@Override
	public void update(GameModel game) {
		switch (state) {
		case INACTIVE -> {
		}
		case EDIBLE -> {
			boolean leftWorld = followRoute(game.level.world);
			if (leftWorld) {
				log("%s expired (left level.world)", this);
				position = V2d.NULL;
				state = BonusState.INACTIVE;
				GameEventing.publish(GameEventType.BONUS_EXPIRES, tile());
				return;
			}
			if (game.pac.tile().equals(tile())) {
				log("%s found bonus %s", game.pac.name, this);
				game.scores().addPoints(value());
				setEaten(sec_to_ticks(2));
				GameEventing.publish(GameEventType.BONUS_GETS_EATEN, tile());
			}
			jumpAnimation.advance();
		}
		case EATEN -> {
			boolean expired = tick();
			if (expired) {
				log("%s expired", this);
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
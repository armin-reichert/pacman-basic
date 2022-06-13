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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.FixedRouteSteering;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.SimpleThingAnimation;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.Bonus;
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
	private List<?> symbolList;
	private List<?> valueList;
	private final SimpleThingAnimation<Integer> jumpAnimation;
	private FixedRouteSteering steering;

	public MovingBonus() {
		super("MovingBonus");
		jumpAnimation = new SimpleThingAnimation<>(2, -2);
		jumpAnimation.frameDuration(10);
		jumpAnimation.repeatForever();
		setAbsSpeed(0.4); // TODO how fast in the original game?
		visible = false;
		state = BonusState.INACTIVE;
	}

	public void setWorld(World world) {
		steering = new FixedRouteSteering(world, computeRoute(world));
	}

	private List<V2i> computeRoute(World world) {
		List<V2i> route = new ArrayList<>();
		int numPortals = world.portals().size();
		if (numPortals > 0) {
			Portal entryPortal = world.portals().get(new Random().nextInt(numPortals));
			Portal exitPortal = world.portals().get(new Random().nextInt(numPortals));
			V2i houseEntry = world.ghostHouse().doorTileLeft().plus(Direction.UP.vec);
			var travelDir = new Random().nextBoolean() ? Direction.LEFT : Direction.RIGHT;
			route.add(houseEntry);
			route.add(houseEntry.plus(Direction.DOWN.vec.scaled(world.ghostHouse().size().y + 2)));
			route.add(houseEntry);
			route.add(travelDir == Direction.RIGHT ? exitPortal.right : exitPortal.left);
			placeAt(travelDir == Direction.RIGHT ? entryPortal.left : entryPortal.right, 0, 0);
			setBothDirs(travelDir);
		}
		return route;
	}

	@Override
	public Entity entity() {
		return this;
	}

	@Override
	public void setSymbolList(List<?> symbolList) {
		this.symbolList = symbolList;
	}

	@Override
	public void setValueList(List<?> valueList) {
		this.valueList = valueList;
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
		jumpAnimation.stop();
	}

	@Override
	public void setEdible(World world, int symbol, int value, long ticks) {
		state = BonusState.EDIBLE;
		timer = ticks;
		this.symbol = symbol;
		this.value = value;
		visible = true;
		jumpAnimation.restart();
		log("MovingBonus symbol=%d, value=%d position=%s activated", symbol, value, position);
	}

	public int dy() {
		return jumpAnimation.isRunning() ? jumpAnimation.frame() : 0;
	}

	@Override
	public void update(GameModel game) {
		switch (state) {
		case INACTIVE -> {
		}
		case EDIBLE -> {
			if (game.pac.tile().equals(tile())) {
				log("%s found bonus %s", game.pac.name, this);
				game.scores.addPoints(value());
				state = BonusState.EATEN;
				timer = Bonus.EATEN_DURATION;
				jumpAnimation.stop();
				game.sounds().ifPresent(snd -> snd.play(GameSound.BONUS_EATEN));
				GameEvents.publish(GameEventType.BONUS_GETS_EATEN, tile());
				return;
			}
			steering.accept(this);
			if (steering.isComplete()) {
				log("%s expired (left world)", this);
				position = V2d.NULL;
				setInactive();
				GameEvents.publish(GameEventType.BONUS_EXPIRES, tile());
				return;
			}
			jumpAnimation.advance();
		}
		case EATEN -> {
			if (--timer == 0) {
				log("%s expired", this);
				setInactive();
				GameEvents.publish(GameEventType.BONUS_EXPIRES, tile());
			}
		}
		}
	}

	@Override
	public Object getSprite() {
		return switch (state) {
		case INACTIVE -> null;
		case EATEN -> valueList.get(symbol);
		case EDIBLE -> symbolList.get(symbol);
		};
	}
}
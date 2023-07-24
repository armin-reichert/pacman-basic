/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import java.util.List;

import de.amr.games.pacman.model.GameLevel;
import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.SoundEvent;
import de.amr.games.pacman.lib.NavigationPoint;
import de.amr.games.pacman.lib.Pulse;
import de.amr.games.pacman.lib.RouteBasedSteering;
import de.amr.games.pacman.model.GameModel;

/**
 * A bonus that tumbles through the world, starting at some portal, making one round around the ghost house and leaving
 * the maze at some portal at the other border.
 * 
 * <p>
 * That's however not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 * 
 * @author Armin Reichert
 */
public class MovingBonus extends Creature implements Bonus {

	private final Pulse jumpAnimation;
	private final RouteBasedSteering steering = new RouteBasedSteering();
	private final byte symbol;
	private final int points;
	private long eatenTimer;
	private byte state;

	public MovingBonus(byte symbol, int points) {
		super("MovingBonus-" + symbol);
		reset();
		this.symbol = symbol;
		this.points = points;
		jumpAnimation = new Pulse(10, false);
		canTeleport = false; // override default from Creature
		eatenTimer = 0;
		state = Bonus.STATE_INACTIVE;
	}

	@Override
	public boolean canReverse() {
		return false;
	}

	@Override
	public Creature entity() {
		return this;
	}

	@Override
	public String toString() {
		return String.format("[MovingBonus state=%s symbol=%d value=%d eatenTimer=%d tile=%s]",
				state, symbol(), points, eatenTimer, tile());
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
		state = Bonus.STATE_INACTIVE;
		jumpAnimation.stop();
		hide();
		setPixelSpeed(0);
	}

	@Override
	public void setEdible(long ticks) {
		state = Bonus.STATE_EDIBLE;
		jumpAnimation.restart();
		show();
		setPixelSpeed(0.5f); // how fast in the original game?
		setTargetTile(null);
	}

	@Override
	public void eat() {
		state = Bonus.STATE_EATEN;
		eatenTimer = GameModel.BONUS_POINTS_SHOWN_TICKS;
		jumpAnimation.stop();
		Logger.info("Bonus eaten: {}", this);
		GameController.publishGameEvent(GameEvent.BONUS_GETS_EATEN, tile());
	}

	public void setRoute(List<NavigationPoint> route) {
		steering.setRoute(route);
	}

	public float dy() {
		if (!jumpAnimation.isRunning()) {
			return 0;
		}
		return jumpAnimation.on() ? -3f : 3f;
	}

	@Override
	public void update(GameLevel level) {
		switch (state) {
		case STATE_INACTIVE:
			// nothing to do
			break;
		case STATE_EDIBLE: {
			if (sameTile(level.pac())) {
				level.game().scorePoints(points());
				eat();
				GameController.publishSoundEvent(SoundEvent.BONUS_EATEN);
				return;
			}
			steering.steer(level, this);
			if (steering.isComplete()) {
				setInactive();
				Logger.trace("Bonus reached target: {}", this);
				GameController.publishGameEvent(GameEvent.BONUS_EXPIRES, tile());
			} else {
				navigateTowardsTarget();
				tryMoving();
				jumpAnimation.tick();
			}
			break;
		}
		case STATE_EATEN: {
			if (--eatenTimer == 0) {
				setInactive();
				Logger.trace("Bonus expired: {}", this);
				GameController.publishGameEvent(GameEvent.BONUS_EXPIRES, tile());
			}
			break;
		}
		default:
			throw new IllegalStateException();
		}
	}
}
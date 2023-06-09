/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import java.util.List;

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

	private final byte symbol;
	private long timer;
	private byte state;

	private final Pulse jumpAnimation;
	private final RouteBasedSteering steering = new RouteBasedSteering();

	public MovingBonus(byte symbol) {
		super(String.format("MovingBonus-%d", symbol));
		super.reset(); // TODO check this

		this.symbol = symbol;
		this.canTeleport = false; // override setting from reset()
		this.timer = 0;
		this.state = Bonus.STATE_INACTIVE;

		jumpAnimation = new Pulse(10, false);
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
		return String.format("[MovingBonus state=%s symbol=%d value=%d timer=%d tile=%s]", state, symbol(), points(), timer,
				tile());
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
		return GameModel.BONUS_VALUES_MS_PACMAN[symbol] * 100;
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
		timer = ticks;
		jumpAnimation.restart();
		show();
		setPixelSpeed(0.5f); // how fast in the original game?
		setTargetTile(null);
	}

	@Override
	public void eat() {
		state = Bonus.STATE_EATEN;
		timer = GameModel.BONUS_POINTS_SHOWN_TICKS;
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
	public void update() {
		switch (state) {
		case STATE_INACTIVE:
			// nothing to do
			break;
		case STATE_EDIBLE: {
			if (sameTile(level().pac())) {
				level().game().scorePoints(points());
				eat();
				GameController.publishSoundEvent(SoundEvent.BONUS_EATEN);
				return;
			}
			steering.steer(level(), this);
			if (steering.isComplete()) {
				Logger.trace("Bonus reached target: {}", this);
				GameController.publishGameEvent(GameEvent.BONUS_EXPIRES, tile());
				setInactive();
				return;
			}
			navigateTowardsTarget();
			tryMoving();
			jumpAnimation.tick();
			break;
		}
		case STATE_EATEN: {
			if (--timer == 0) {
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
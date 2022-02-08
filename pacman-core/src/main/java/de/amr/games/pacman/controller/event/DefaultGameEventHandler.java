/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.controller.event;

/**
 * Default implementation of the {@link GameEventListener} interface providing an empty handler method for each game
 * event such that a class implementing this interface just needs to override the needed method(s).
 * 
 * @author Armin Reichert
 */
public abstract class DefaultGameEventHandler implements GameEventListener {

	@Override
	public void onGameEvent(GameEvent event) {
		if (event instanceof ScatterPhaseStartedEvent) {
			onScatterPhaseStarted((ScatterPhaseStartedEvent) event);
		} else if (event instanceof GameStateChangeEvent) {
			onGameStateChange((GameStateChangeEvent) event);
		} else {
			switch (event.info) {
			case BONUS_ACTIVATED -> onBonusActivated(event);
			case BONUS_EATEN -> onBonusEaten(event);
			case BONUS_EXPIRED -> onBonusExpired(event);
			case EXTRA_LIFE -> onExtraLife(event);
			case GHOST_ENTERS_HOUSE -> onGhostEntersHouse(event);
			case GHOST_REVIVED -> onGhostRevived(event);
			case GHOST_LEAVING_HOUSE -> onGhostLeavingHouse(event);
			case GHOST_LEFT_HOUSE -> onGhostLeftHouse(event);
			case GHOST_RETURNS_HOME -> onGhostReturnsHome(event);
			case PLAYER_FOUND_FOOD -> onPlayerFoundFood(event);
			case PLAYER_GAINS_POWER -> onPlayerGainsPower(event);
			case PLAYER_LOSING_POWER -> onPlayerLosingPower(event);
			case PLAYER_LOST_POWER -> onPlayerLostPower(event);
			default -> throw new IllegalArgumentException("Unexpected value: " + event.info);
			}
		}
	}

	public void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
	}

	public void onGameStateChange(GameStateChangeEvent e) {
	}

	public void onBonusActivated(GameEvent e) {
	}

	public void onBonusEaten(GameEvent e) {
	}

	public void onBonusExpired(GameEvent e) {
	}

	public void onExtraLife(GameEvent e) {
	}

	public void onGhostEntersHouse(GameEvent e) {
	}

	public void onGhostReturnsHome(GameEvent e) {
	}

	public void onGhostRevived(GameEvent e) {
	}

	public void onGhostLeavingHouse(GameEvent e) {
	}

	public void onGhostLeftHouse(GameEvent e) {
	}

	public void onPlayerFoundFood(GameEvent e) {
	}

	public void onPlayerLosingPower(GameEvent e) {
	}

	public void onPlayerLostPower(GameEvent e) {
	}

	public void onPlayerGainsPower(GameEvent e) {
	}

}
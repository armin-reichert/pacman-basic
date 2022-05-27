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
package de.amr.games.pacman.event;

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
			switch (event.type) {
			case BONUS_ACTIVATED -> onBonusActivated(event);
			case BONUS_EATEN -> onBonusEaten(event);
			case BONUS_EXPIRED -> onBonusExpired(event);
			case GHOST_ENTERED_HOUSE -> onGhostEnteredHouse(event);
			case GHOST_REVIVED -> onGhostRevived(event);
			case GHOST_STARTED_LEAVING_HOUSE -> onGhostStartedLeavingHouse(event);
			case GHOST_FINISHED_LEAVING_HOUSE -> onGhostFinishedLeavingHouse(event);
			case GHOST_STARTED_RETURNING_HOME -> onGhostStartedReturningHome(event);
			case PLAYER_FOUND_FOOD -> onPlayerFoundFood(event);
			case PLAYER_GOT_EXTRA_LIFE -> onPlayerGotExtraLife(event);
			case PLAYER_GOT_POWER -> onPlayerGotPower(event);
			case PLAYER_STARTED_LOSING_POWER -> onPlayerStartedLosingPower(event);
			case PLAYER_LOST_POWER -> onPlayerLostPower(event);
			case UI_CHANGE -> onUIChange(event);
			default -> throw new IllegalArgumentException("Unknown event type: " + event.type);
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

	public void onPlayerGotExtraLife(GameEvent e) {
	}

	public void onGhostEnteredHouse(GameEvent e) {
	}

	public void onGhostStartedReturningHome(GameEvent e) {
	}

	public void onGhostRevived(GameEvent e) {
	}

	public void onGhostStartedLeavingHouse(GameEvent e) {
	}

	public void onGhostFinishedLeavingHouse(GameEvent e) {
	}

	public void onPlayerFoundFood(GameEvent e) {
	}

	public void onPlayerStartedLosingPower(GameEvent e) {
	}

	public void onPlayerLostPower(GameEvent e) {
	}

	public void onPlayerGotPower(GameEvent e) {
	}

	public void onUIChange(GameEvent e) {
	}
}
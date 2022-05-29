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
 * Implementation of the {@link GameEventListener} interface providing an empty handler method for each game event such
 * that a class implementing this interface just needs to override the needed method(s).
 * 
 * @author Armin Reichert
 */
public abstract class GameEventAdapter implements GameEventListener {

	@Override
	public void onGameEvent(GameEvent event) {
		if (event instanceof ScatterPhaseStartsEvent) {
			onScatterPhaseStarts((ScatterPhaseStartsEvent) event);
		} else if (event instanceof GameStateChangeEvent) {
			onGameStateChange((GameStateChangeEvent) event);
		} else {
			switch (event.type) {
			case BONUS_GETS_ACTIVE -> onBonusGetsActivate(event);
			case BONUS_GETS_EATEN -> onBonusGetsEaten(event);
			case BONUS_EXPIRES -> onBonusExpires(event);
			case GHOST_ENTERS_HOUSE -> onGhostEntersHouse(event);
			case GHOST_STARTS_LEAVING_HOUSE -> onGhostStartsLeavingHouse(event);
			case GHOST_COMPLETES_LEAVING_HOUSE -> onGhostCompletesLeavingHouse(event);
			case GHOST_STARTS_RETURNING_HOME -> onGhostStartsReturningHome(event);
			case PLAYER_FINDS_FOOD -> onPlayerFindsFood(event);
			case PLAYER_GETS_EXTRA_LIFE -> onPlayerGetsExtraLife(event);
			case PLAYER_GETS_POWER -> onPlayerGetsPower(event);
			case PLAYER_STARTS_LOSING_POWER -> onPlayerStartsLosingPower(event);
			case PLAYER_LOSES_POWER -> onPlayerLosesPower(event);
			case UI_FORCE_UPDATE -> onUIForceUpdate(event);
			default -> throw new IllegalArgumentException("Unknown event type: " + event.type);
			}
		}
	}

	public void onScatterPhaseStarts(ScatterPhaseStartsEvent e) {
	}

	public void onGameStateChange(GameStateChangeEvent e) {
	}

	public void onBonusGetsActivate(GameEvent e) {
	}

	public void onBonusGetsEaten(GameEvent e) {
	}

	public void onBonusExpires(GameEvent e) {
	}

	public void onPlayerGetsExtraLife(GameEvent e) {
	}

	public void onGhostEntersHouse(GameEvent e) {
	}

	public void onGhostStartsReturningHome(GameEvent e) {
	}

	public void onGhostStartsLeavingHouse(GameEvent e) {
	}

	public void onGhostCompletesLeavingHouse(GameEvent e) {
	}

	public void onPlayerFindsFood(GameEvent e) {
	}

	public void onPlayerStartsLosingPower(GameEvent e) {
	}

	public void onPlayerLosesPower(GameEvent e) {
	}

	public void onPlayerGetsPower(GameEvent e) {
	}

	public void onUIForceUpdate(GameEvent e) {
	}
}
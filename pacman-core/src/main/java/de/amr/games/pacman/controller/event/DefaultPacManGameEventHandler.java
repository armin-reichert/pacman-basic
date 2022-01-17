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
 * Provides an empty default handler for each game event such that a class implementing this interface just needs to
 * override the needed method(s).
 * 
 * @author Armin Reichert
 */
public interface DefaultPacManGameEventHandler extends PacManGameEventListener {

	@Override
	default void onGameEvent(PacManGameEvent event) {
		switch (event.info) {
		case BONUS_ACTIVATED:
			onBonusActivated(event);
			return;
		case BONUS_EATEN:
			onBonusEaten(event);
			return;
		case BONUS_EXPIRED:
			onBonusExpired(event);
			return;
		case EXTRA_LIFE:
			onExtraLife(event);
			return;
		case GHOST_ENTERS_HOUSE:
			onGhostEntersHouse(event);
			return;
		case GHOST_LEAVING_HOUSE:
			onGhostLeavingHouse(event);
			return;
		case GHOST_LEFT_HOUSE:
			onGhostLeftHouse(event);
			return;
		case GHOST_RETURNS_HOME:
			onGhostReturnsHome(event);
			return;
		case PLAYER_FOUND_FOOD:
			onPlayerFoundFood(event);
			return;
		case PLAYER_GAINS_POWER:
			onPlayerGainsPower(event);
			return;
		case PLAYER_LOSING_POWER:
			onPlayerLosingPower(event);
			return;
		case PLAYER_LOST_POWER:
			onPlayerLostPower(event);
			return;
		case TICK:
			onTick();
		default:
			if (event instanceof ScatterPhaseStartedEvent) {
				onScatterPhaseStarted((ScatterPhaseStartedEvent) event);
				return;
			}
			if (event instanceof PacManGameStateChangeEvent) {
				onPacManGameStateChange((PacManGameStateChangeEvent) event);
				return;
			}
		}
	}

	default void onBonusActivated(PacManGameEvent e) {
	}

	default void onBonusEaten(PacManGameEvent e) {
	}

	default void onBonusExpired(PacManGameEvent e) {
	}

	default void onExtraLife(PacManGameEvent e) {
	}

	default void onGhostEntersHouse(PacManGameEvent e) {
	}

	default void onGhostReturnsHome(PacManGameEvent e) {
	}

	default void onGhostLeavingHouse(PacManGameEvent e) {
	}

	default void onGhostLeftHouse(PacManGameEvent e) {
	}

	default void onPlayerFoundFood(PacManGameEvent e) {
	}

	default void onPlayerLosingPower(PacManGameEvent e) {
	}

	default void onPlayerLostPower(PacManGameEvent e) {
	}

	default void onPlayerGainsPower(PacManGameEvent e) {
	}

	default void onPacManGameStateChange(PacManGameStateChangeEvent e) {
	}

	default void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
	}

	default void onTick() {
	}
}
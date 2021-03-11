package de.amr.games.pacman.controller;

import static de.amr.games.pacman.controller.PacManGameState.INTRO;
import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.lib.Logging;

/**
 * Finite-state machine controlling the Pac-Man and Ms. Pac-Man game play.
 * 
 * @author Armin Reichert
 */
public class PacManGameStateMachine {

	public boolean logging = true;
	public PacManGameState previousState;
	public PacManGameState state;

	public void init() {
		state = INTRO;
		previousState = null;
		if (logging) {
			log("Initialize game state machine, enter state %s", state);
		}
		state.onEnter.run(); // assuming INTRO state has onEnter action
		state.timer.start();
	}

	public PacManGameState changeState(PacManGameState newState) {
		if (state == null) {
			throw new IllegalStateException("State machine not initialized");
		}
		if (logging) {
			log("Exit game state %s", state);
		}
		if (state.onExit != null) {
			state.onExit.run();
		}
		previousState = state;
		state = newState;
		if (logging) {
			log("Enter game state %s", newState);
		}
		if (newState.onEnter != null) {
			newState.onEnter.run();
		}
		newState.timer.start();
		// TODO fire change event to listeners
		return newState;
	}

	public void updateState() {
		try {
			if (state.onUpdate != null) {
				state.onUpdate.run();
			}
			state.timer.tick();
		} catch (Exception x) {
			Logging.log("Error updating state %s", state);
			x.printStackTrace();
		}
	}

	public void resumePreviousState() {
		if (previousState == null) {
			throw new IllegalStateException("State machine cannot resume previous state because there is none");
		}
		if (logging) {
			log("Resume game state %s", previousState);
		}
		changeState(previousState);
	}

	public String stateDescription() {
		return state.name();
	}
}
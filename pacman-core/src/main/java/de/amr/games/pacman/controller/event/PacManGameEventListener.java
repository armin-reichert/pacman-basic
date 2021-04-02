package de.amr.games.pacman.controller.event;

@FunctionalInterface
public interface PacManGameEventListener {

	void onGameEvent(PacManGameEvent event);

}

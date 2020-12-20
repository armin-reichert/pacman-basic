package de.amr.games.pacman.core;

/**
 * The different states of the game. Each state has a timer.
 * 
 * @author Armin Reichert
 *
 */
public enum GameState {

	INTRO, READY, HUNTING, CHANGING_LEVEL, PACMAN_DYING, GHOST_DYING, GAME_OVER;

	private long duration;
	private long ticksRemaining;

	public void tick() {
		if (ticksRemaining != Long.MAX_VALUE && ticksRemaining > 0) {
			--ticksRemaining;
		}
	}

	public void setDuration(long ticks) {
		ticksRemaining = duration = ticks;
	}

	public long ticksRemaining() {
		return ticksRemaining;
	}

	public long duration() {
		return duration;
	}

	public long running() {
		return duration - ticksRemaining;
	}

	public boolean expired() {
		return ticksRemaining == 0;
	}
}
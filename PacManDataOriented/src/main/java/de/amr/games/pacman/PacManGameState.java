package de.amr.games.pacman;

public enum PacManGameState {

	INTRO, READY, HUNTING, CHANGING_LEVEL, PACMAN_DYING, GHOST_DYING, GAME_OVER;

	private long ticksRemaining;

	public void tick() {
		if (ticksRemaining != Long.MAX_VALUE && ticksRemaining > 0) {
			--ticksRemaining;
		}
	}

	public void setDuration(long duration) {
		ticksRemaining = duration;
	}

	public long ticksRemaining() {
		return ticksRemaining;
	}

	public boolean expired() {
		return ticksRemaining == 0;
	}
}
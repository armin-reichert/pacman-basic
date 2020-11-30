package de.amr.games.pacman;

public enum GameState {

	READY, SCATTERING, CHASING, CHANGING_LEVEL, PACMAN_DYING, GHOST_DYING, GAME_OVER;

	private long timer;

	public void tick() {
		if (timer != Long.MAX_VALUE) {
			--timer;
		}
	}

	public void setTimer(long duration) {
		timer = duration;
	}

	public long ticksRemaining() {
		return timer;
	}
}
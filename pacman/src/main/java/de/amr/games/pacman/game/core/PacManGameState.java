package de.amr.games.pacman.game.core;

import java.util.function.Predicate;

/**
 * The different states of the game. Each state has a timer.
 * 
 * @author Armin Reichert
 */
public enum PacManGameState {

	INTRO, READY, HUNTING, CHANGING_LEVEL, PACMAN_DYING, GHOST_DYING, GAME_OVER;

	private long duration;
	private long running;

	public PacManGameState doTick() {
		++running;
		return this;
	}

	public void setDuration(long ticks) {
		duration = ticks;
		running = 0;
	}

	public void resetTimer() {
		running = 0;
	}

	public long ticksRemaining() {
		return duration == Long.MAX_VALUE ? Long.MAX_VALUE : Math.max(duration - running, 0);
	}

	public long duration() {
		return duration;
	}

	public long tick() {
		return running;
	}

	public boolean expired() {
		return ticksRemaining() == 0;
	}

	public void runAfter(long tick, Runnable code) {
		if (running > tick) {
			code.run();
		}
	}

	public void runBefore(long tick, Runnable code) {
		if (running < tick) {
			code.run();
		}
	}

	public void runBetween(long startTick, long endTick, Runnable code) {
		if (running >= startTick && running <= endTick) {
			code.run();
		}
	}

	public void runAt(long tick, Runnable code) {
		if (running == tick) {
			code.run();
		}
	}

	public void runWhile(Predicate<Long> timerCondition, Runnable code) {
		if (timerCondition.test(running)) {
			code.run();
		}
	}
}
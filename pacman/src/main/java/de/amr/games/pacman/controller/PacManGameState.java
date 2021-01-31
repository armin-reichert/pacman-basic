package de.amr.games.pacman.controller;

import java.util.function.Predicate;

/**
 * The different states of the game. Each state has a timer.
 * 
 * @author Armin Reichert
 */
public enum PacManGameState {

	INTRO, READY, HUNTING, CHANGING_LEVEL, PACMAN_DYING, GHOST_DYING, GAME_OVER;

	private long duration;
	private long ticksRun;

	public long ticksRun() {
		return ticksRun;
	}

	public boolean ticksLeft(int ticks) {
		return duration - ticksRun == ticks;
	}

	public long durationTicks() {
		return duration;
	}

	public PacManGameState run() {
		++ticksRun;
		return this;
	}

	public void setDuration(long durationTicks) {
		duration = durationTicks;
		ticksRun = 0;
	}

	public void resetTimer() {
		ticksRun = 0;
	}

	public long remaining() {
		return duration == Long.MAX_VALUE ? Long.MAX_VALUE : Math.max(duration - ticksRun, 0);
	}

	public boolean hasExpired() {
		return remaining() == 0;
	}

	public void runAfter(long tick, Runnable code) {
		if (ticksRun > tick) {
			code.run();
		}
	}

	public void runBefore(long tick, Runnable code) {
		if (ticksRun < tick) {
			code.run();
		}
	}

	public void runBetween(long startTick, long endTick, Runnable code) {
		if (ticksRun >= startTick && ticksRun <= endTick) {
			code.run();
		}
	}

	public void runAt(long tick, Runnable code) {
		if (ticksRun == tick) {
			code.run();
		}
	}

	public void runWhile(Predicate<Long> timerCondition, Runnable code) {
		if (timerCondition.test(ticksRun)) {
			code.run();
		}
	}
}
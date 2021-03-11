package de.amr.games.pacman.lib;

import static de.amr.games.pacman.lib.TickTimer.TickTimerState.EXPIRED;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.READY;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.RUNNING;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.STOPPED;

/**
 * A simple, but useful, passive timer counting ticks.
 * 
 * @author Armin Reichert
 */
public class TickTimer {

	public enum TickTimerState {
		READY, RUNNING, STOPPED, EXPIRED;
	}

	private TickTimerState state;
	private long duration;
	private long ticked; // 0 .. duration - 1

	public TickTimer() {
		reset();
	}

	public void reset() {
		reset(Long.MAX_VALUE);
	}

	public void reset(long durationTicks) {
		state = READY;
		ticked = 0;
		duration = durationTicks;
	}

	public void start() {
		if (state == STOPPED || state == READY) {
			state = RUNNING;
		} else {
			throw new IllegalStateException("Timer cannot be started from state " + state);
		}
	}

	public void stop() {
		if (state == STOPPED) {
			return;
		}
		if (state == RUNNING) {
			state = STOPPED;
		}
	}

	public void tick() {
		if (state == STOPPED) {
			return;
		}
		if (state != RUNNING) {
			throw new IllegalStateException(state == READY ? "Timer has not been started" : "Timer has expired");
		}
		++ticked;
		if (ticked == duration) {
			state = EXPIRED;
			return;
		}
	}

	public void forceExpiration() {
		state = EXPIRED;
	}

	public boolean hasExpired() {
		return state == EXPIRED;
	}

	public boolean isRunning() {
		return state == RUNNING;
	}

	public boolean isStopped() {
		return state == STOPPED;
	}

	public long duration() {
		return duration;
	}

	public long ticked() {
		return ticked;
	}

	public long ticksRemaining() {
		return duration == Long.MAX_VALUE ? Long.MAX_VALUE : duration - ticked;
	}
}
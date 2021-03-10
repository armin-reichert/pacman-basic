package de.amr.games.pacman.lib;

import static de.amr.games.pacman.lib.TickTimer.TickTimerState.EXPIRED;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.IDLE;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.RUNNING;

//TODO unit test
public class TickTimer {

	public enum TickTimerState {
		IDLE, RUNNING, EXPIRED;
	}

	private TickTimerState state;
	private long duration;
	private long ticked; // 0 .. duration - 1

	public TickTimer() {
		reset();
	}

	public void reset() {
		state = IDLE;
		ticked = 0;
		duration = Long.MAX_VALUE;
	}

	public void setDuration(long durationTicks) {
		if (state != IDLE) {
			throw new IllegalStateException("Duration can only be set when timer is IDLE");
		}
		ticked = 0;
		duration = durationTicks;
	}

	public void start() {
		if (state != IDLE) {
			throw new IllegalStateException("Timer is not IDLE and cannot be started. State=" + state);
		}
		state = RUNNING;
		ticked = 0;
	}

	public void tick() {
		if (state != RUNNING) {
			throw new IllegalStateException("Timer has not been started");
		}
		++ticked;
		if (ticked == duration) {
			state = EXPIRED;
			return;
		}
	}

	public void forceExpiration() {
		ticked = duration;
		state = EXPIRED;
	}

	public boolean expired() {
		return state == EXPIRED;
	}

	public boolean running() {
		return state == RUNNING;
	}

	public long getDuration() {
		return duration;
	}

	public long ticksRunning() {
		return ticked;
	}

	public long ticksRemaining() {
		return duration == Long.MAX_VALUE ? Long.MAX_VALUE : duration - ticked;
	}
}
package de.amr.games.pacman.lib;

import static de.amr.games.pacman.lib.TickTimer.TickTimerState.EXPIRED;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.READY;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.RUNNING;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.STOPPED;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * A simple, but useful, passive timer counting ticks.
 * 
 * @author Armin Reichert
 */
public class TickTimer {

	public enum TickTimerState {
		READY, RUNNING, STOPPED, EXPIRED;
	}

	public enum TimerEvent {
		RESET, STARTED, STOPPED, EXPIRED;
	}

	private final Collection<Consumer<TimerEvent>> subscribers = new HashSet<>();
	private TickTimerState state;
	private long duration;
	private long ticked; // 0 .. duration - 1

	public TickTimer() {
		reset();
	}

	public void addEventListener(Consumer<TimerEvent> subscriber) {
		subscribers.add(subscriber);
	}

	public void removeEventListener(Consumer<TimerEvent> subscriber) {
		subscribers.remove(subscriber);
	}

	private void fireEvent(TimerEvent e) {
		subscribers.forEach(subscriber -> subscriber.accept(e));
	}

	public void reset(long durationTicks) {
		state = READY;
		ticked = 0;
		duration = durationTicks;
		fireEvent(TimerEvent.RESET);
	}

	public void reset() {
		reset(Long.MAX_VALUE);
	}

	public void resetSeconds(double seconds) {
		reset((long) (seconds * 60));
	}

	public void start() {
		if (state == RUNNING) {
			return;
		}
		if (state == STOPPED || state == READY) {
			state = RUNNING;
			fireEvent(TimerEvent.STARTED);
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
			fireEvent(TimerEvent.STOPPED);
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
			fireEvent(TimerEvent.EXPIRED);
			return;
		}
	}

	public void forceExpiration() {
		state = EXPIRED;
		fireEvent(TimerEvent.EXPIRED);
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

	public boolean isRunningSeconds(double seconds) {
		return ticked == (long) (seconds * 60);
	}

	public boolean hasJustStarted() {
		return ticked == 1;
	}
}
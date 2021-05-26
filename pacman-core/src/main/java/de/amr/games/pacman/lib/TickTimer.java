package de.amr.games.pacman.lib;

import static de.amr.games.pacman.lib.TickTimer.TickTimerState.EXPIRED;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.READY;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.RUNNING;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.STOPPED;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

import de.amr.games.pacman.lib.TickTimerEvent.Type;

/**
 * A simple, but useful, passive timer counting ticks.
 * 
 * @author Armin Reichert
 */
public class TickTimer {

	public static final long sec_to_ticks(double sec) {
		return Math.round(sec * 60);
	}

	public enum TickTimerState {
		READY, RUNNING, STOPPED, EXPIRED;
	}

	private final Collection<Consumer<TickTimerEvent>> subscribers = new HashSet<>();
	private final String name;
	private TickTimerState state;
	private long duration;
	private long ticked; // 0 .. duration - 1

	public TickTimer(String name) {
		this.name = name;
		reset();
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("TickTimer %s: ticked: %d remaining: %d", name, ticked, ticksRemaining());
	}

	public void addEventListener(Consumer<TickTimerEvent> subscriber) {
		subscribers.add(subscriber);
	}

	public void removeEventListener(Consumer<TickTimerEvent> subscriber) {
		subscribers.remove(subscriber);
	}

	private void fireEvent(TickTimerEvent e) {
		subscribers.forEach(subscriber -> subscriber.accept(e));
	}

	public void reset(long durationTicks) {
		state = READY;
		ticked = 0;
		duration = durationTicks;
		fireEvent(new TickTimerEvent(Type.RESET, duration));
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
			fireEvent(new TickTimerEvent(Type.STARTED));
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
			fireEvent(new TickTimerEvent(Type.STOPPED));
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
		if (ticked == duration / 2) {
			fireEvent(new TickTimerEvent(Type.HALF_EXPIRED, ticked));
		}
		if (ticked == duration) {
			state = EXPIRED;
			fireEvent(new TickTimerEvent(Type.EXPIRED, ticked));
			return;
		}
	}

	public void forceExpiration() {
		state = EXPIRED;
		fireEvent(new TickTimerEvent(Type.EXPIRED, ticked));
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
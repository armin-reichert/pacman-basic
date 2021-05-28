package de.amr.games.pacman.lib;

import static de.amr.games.pacman.lib.TickTimer.TickTimerState.EXPIRED;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.READY;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.RUNNING;
import static de.amr.games.pacman.lib.TickTimer.TickTimerState.STOPPED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import de.amr.games.pacman.lib.TickTimerEvent.Type;

/**
 * A simple, but useful, passive timer counting ticks.
 * 
 * @author Armin Reichert
 */
public class TickTimer {

	public static final long INDEFINITE = Long.MAX_VALUE;

	public static final long sec_to_ticks(double sec) {
		return Math.round(sec * 60);
	}

	public enum TickTimerState {
		READY, RUNNING, STOPPED, EXPIRED;
	}

	public static boolean trace = false;

	private void trace(String msg, Object... args) {
		if (trace) {
			Logging.log(msg, args);
		}
	}

	private Collection<Consumer<TickTimerEvent>> subscribers;
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

	private void ensureSubscribersCreated() {
		if (subscribers == null) {
			subscribers = new ArrayList<>(3);
		}
	}

	public void addEventListener(Consumer<TickTimerEvent> subscriber) {
		ensureSubscribersCreated();
		subscribers.add(subscriber);
	}

	public void removeEventListener(Consumer<TickTimerEvent> subscriber) {
		if (subscribers != null) {
			subscribers.remove(subscriber);
		}
	}

	private void fireEvent(TickTimerEvent e) {
		if (subscribers != null) {
			subscribers.forEach(subscriber -> subscriber.accept(e));
		}
	}

	public void reset(long durationTicks) {
		state = READY;
		trace("%s reset", name);
		ticked = 0;
		duration = durationTicks;
		fireEvent(new TickTimerEvent(Type.RESET, duration));
	}

	public void reset() {
		reset(INDEFINITE);
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
			trace("%s started", name);
			fireEvent(new TickTimerEvent(Type.STARTED));
		} else {
			throw new IllegalStateException(String.format("Timer %s cannot be started from state %s", name, state));
		}
	}

	public void stop() {
		if (state == STOPPED) {
			return;
		}
		if (state == RUNNING) {
			state = STOPPED;
			trace("%s stopped", name);
			fireEvent(new TickTimerEvent(Type.STOPPED));
		}
	}

	public void tick() {
		if (state == STOPPED) {
			return;
		}
		if (state == READY) {
			throw new IllegalStateException(String.format("Timer %s has not been started", name));
		}
		if (state == EXPIRED) {
			throw new IllegalStateException(String.format("Timer %s has expired", name));
		}
		++ticked;
		trace("%s ticked", this);
		if (ticked == duration / 2) {
			fireEvent(new TickTimerEvent(Type.HALF_EXPIRED, ticked));
		}
		if (ticked == duration) {
			state = EXPIRED;
			trace("%s expired", name);
			fireEvent(new TickTimerEvent(Type.EXPIRED, ticked));
			return;
		}
	}

	public void forceExpiration() {
		state = EXPIRED;
		trace("%s expired", name);
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
		return duration == INDEFINITE ? INDEFINITE : duration - ticked;
	}

	public boolean isRunningSeconds(double seconds) {
		return ticked == (long) (seconds * 60);
	}

	public boolean hasJustStarted() {
		return ticked == 1;
	}
}
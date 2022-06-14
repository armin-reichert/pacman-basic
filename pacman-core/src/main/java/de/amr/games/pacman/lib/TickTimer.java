/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.lib;

import static de.amr.games.pacman.lib.TickTimer.State.EXPIRED;
import static de.amr.games.pacman.lib.TickTimer.State.READY;
import static de.amr.games.pacman.lib.TickTimer.State.RUNNING;
import static de.amr.games.pacman.lib.TickTimer.State.STOPPED;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.amr.games.pacman.lib.TickTimerEvent.Type;

/**
 * A simple, but useful, passive timer counting ticks.
 * 
 * @author Armin Reichert
 */
public class TickTimer {

	public static boolean trace = false;

	public enum State {
		READY, RUNNING, STOPPED, EXPIRED;
	}

	public static final long INDEFINITE = Long.MAX_VALUE;
	private static final int TICKS_PER_SEC = 60;

	private void trace(String msg, Object... args) {
		if (trace) {
			Logging.log(msg, args);
		}
	}

	/**
	 * @param sec seconds
	 * @return number of ticks representing given seconds at 60Hz
	 */
	public static final long sec_to_ticks(double sec) {
		return Math.round(sec * TICKS_PER_SEC);
	}

	public static String ticksToString(long ticks) {
		return ticks == INDEFINITE ? "indefinite" : "%d".formatted(ticks);
	}

	private final String name;
	private State state;
	private long duration;
	private long tick; // 0..(duration - 1)
	private List<Consumer<TickTimerEvent>> subscribers;

	public TickTimer(String name) {
		this.name = name;
		resetIndefinitely();
	}

	public void addEventListener(Consumer<TickTimerEvent> subscriber) {
		if (subscribers == null) {
			subscribers = new ArrayList<>(3);
		}
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

	@Override
	public String toString() {
		return "[%s %s tick:%s remaining:%s]".formatted(name, state, ticksToString(tick), ticksToString(remaining()));
	}

	public State state() {
		return state;
	}

	public String name() {
		return name;
	}

	/**
	 * Sets the timer to given duration and its state to {@link State#READY}. The timer is not running after this call!
	 * 
	 * @param ticks timer duration in ticks
	 */
	public void reset(long ticks) {
		duration = ticks;
		tick = 0;
		state = READY;
		trace("%s reset", this);
		fireEvent(new TickTimerEvent(Type.RESET, ticks));
	}

	/**
	 * Sets the timer duration in seconds.
	 * 
	 * @param seconds number of seconds
	 */
	public void resetSeconds(double seconds) {
		reset(sec_to_ticks(seconds));
	}

	/**
	 * Sets the timer to run forever.
	 */
	public void resetIndefinitely() {
		reset(INDEFINITE);
	}

	/**
	 * Starts the timer. If the timer is already running, does nothing.
	 */
	public void start() {
		switch (state) {
		case STOPPED, READY -> {
			state = RUNNING;
			trace("%s started", this);
			fireEvent(new TickTimerEvent(Type.STARTED));
		}
		case RUNNING -> {
			trace("%s not started, already running", this);
		}
		case EXPIRED -> {
			// TODO throw exception?
			trace("%s not started, timer has expired", this);
		}
		}
	}

	/**
	 * Stops the timer. If the timer is not running, does nothing.
	 */
	public void stop() {
		switch (state) {
		case RUNNING -> {
			state = STOPPED;
			trace("%s stopped", this);
			fireEvent(new TickTimerEvent(Type.STOPPED));
		}
		case STOPPED -> {
			trace("%s already stopped", this);
		}
		case READY -> {
			trace("%s not stopped, was not running", this);
		}
		case EXPIRED -> {
			trace("%s not stopped, has expired", this);
		}
		}
	}

	/**
	 * Advances the timer by one step, if it is running. Does nothing, else.
	 */
	public void advance() {
		if (state == RUNNING) {
			if (tick == duration) {
				expire();
			} else {
				++tick;
			}
		}
	}

	/**
	 * Forces the timer to expire.
	 */
	public void expire() {
		if (state != EXPIRED) {
			state = EXPIRED;
			trace("%s expired", this);
			fireEvent(new TickTimerEvent(Type.EXPIRED, tick));
		}
	}

	public boolean hasExpired() {
		return state == EXPIRED;
	}

	public boolean isReady() {
		return state == READY;
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

	public long tick() {
		return tick;
	}

	public boolean atSecond(double seconds) {
		return tick == sec_to_ticks(seconds);
	}

	public long remaining() {
		return duration == INDEFINITE ? INDEFINITE : duration - tick;
	}
}
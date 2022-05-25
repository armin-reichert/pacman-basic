/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;

import de.amr.games.pacman.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.TickTimer;

/**
 * @author Armin Reichert
 */
public class HuntingTimer {

	/** Tells if the current hunting phase is "scattering". */
	public static boolean isScatteringPhase(int phase) {
		return phase % 2 == 0;
	}

	private final TickTimer timer = new TickTimer("HuntingTimer");
	private int phase;

	@Override
	public String toString() {
		return "%s Phase %d (%s)".formatted(timer, phase, getPhaseName());
	}

	public int getPhase() {
		return phase;
	}

	public long getTick() {
		return timer.tick();
	}

	public long getTicksRemaining() {
		return timer.remaining();
	}

	public int getScatteringPhase() {
		return phase % 2 == 0 ? phase / 2 : -1;
	}

	public int getChasingPhase() {
		return phase % 2 == 1 ? phase / 2 : -1;
	}

	public String getPhaseName() {
		return phase % 2 == 0 ? "Scattering" : "Chasing";
	}

	public boolean hasExpired() {
		return timer.hasExpired();
	}

	public boolean isStopped() {
		return timer.isStopped();
	}

	public void advance() {
		timer.advance();
	}

	public void stop() {
		timer.stop();
		log("%s: stopped", this);
	}

	public void start() {
		timer.start();
		log("%s: started", this);
	}

	public void startPhase(GameModel game, int phase) {
		timer.setDurationTicks(game.huntingPhaseTicks(phase)).start();
	}

	public void startNextHuntingPhase(GameModel game) {
		startHuntingPhase(game, ++phase);
	}

	public void startHuntingPhase(GameModel game, int phase) {
		this.phase = phase;
		startPhase(game, phase);
		log("%s: Phase %d has been started", this, phase);
		if (phase > 0) {
			game.ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED)).forEach(Ghost::forceTurningBack);
		}
		if (isScatteringPhase(phase)) {
			game.publishEvent(new ScatterPhaseStartedEvent(game, phase / 2));
		}
	}
}
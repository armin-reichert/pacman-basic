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

package de.amr.games.pacman.controller.common;

import java.util.stream.Stream;

import de.amr.games.pacman.model.common.GameSound;

/**
 * @author Armin Reichert
 */
public class NoSound implements GameSoundController {

	@Override
	public void play(GameSound snd) {
		// do nothing
	}

	@Override
	public void ensurePlaying(GameSound snd) {
		// do nothing
	}

	@Override
	public boolean isPlaying(GameSound sound) {
		return false;
	}

	@Override
	public void loop(GameSound snd, int repetitions) {
		// do nothing
	}

	@Override
	public void setSilent(boolean silent) {
		// do nothing
	}

	@Override
	public boolean isMuted() {
		return false;
	}

	@Override
	public void setMuted(boolean muted) {
		// do nothing
	}

	@Override
	public void stopAll() {
		// do nothing
	}

	@Override
	public void stop(GameSound snd) {
		// do nothing
	}

	@Override
	public void ensureLoop(GameSound snd, int repetitions) {
		// do nothing
	}

	@Override
	public Stream<GameSound> sirens() {
		return Stream.empty();
	}

	@Override
	public void startSiren(int sirenIndex) {
		// do nothing
	}

	@Override
	public void stopSirens() {
		// do nothing
	}

	@Override
	public void ensureSirenStarted(int siren) {
		// do nothing
	}
}
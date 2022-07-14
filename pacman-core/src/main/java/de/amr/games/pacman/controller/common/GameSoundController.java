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
public interface GameSoundController {

	public static final GameSoundController NO_SOUND = new NoSound();

	public static final int LOOP_FOREVER = -1;

	void play(GameSound snd);

	void ensurePlaying(GameSound snd);

	boolean isPlaying(GameSound sound);

	void loop(GameSound snd, int repetitions);

	boolean isMuted();

	void setMuted(boolean muted);

	void stopAll();

	void stop(GameSound snd);

	void ensureLoop(GameSound snd, int repetitions);

	Stream<GameSound> sirens();

	void startSiren(int sirenIndex);

	void stopSirens();

	void ensureSirenStarted(int siren);
}
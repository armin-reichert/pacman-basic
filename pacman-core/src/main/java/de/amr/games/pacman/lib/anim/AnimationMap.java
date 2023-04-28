/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

package de.amr.games.pacman.lib.anim;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class AnimationMap {

	public static final byte NO_SELECTION = -1;

	private final Map<Byte, Animated> animationsByKey = new HashMap<>(5);
	protected byte selectedKey;

	public final Optional<Animated> animation(byte key) {
		return Optional.ofNullable(animationsByKey.get(key));
	}

	public void put(byte key, Animated animation) {
		checkNotNull(key);
		checkNotNull(animation);
		animationsByKey.put(key, animation);
	}

	public void select(byte key) {
		checkNotNull(key);
		selectedKey = key;
	}

	public void selectAndRestart(byte key) {
		checkNotNull(key);
		select(key);
		animation(selectedKey).ifPresent(Animated::restart);
	}

	public boolean isSelected(byte key) {
		checkNotNull(key);
		return selectedKey == key;
	}

	public byte selectedKey() {
		return selectedKey;
	}

	public Optional<Animated> selectedAnimation() {
		return animation(selectedKey);
	}

	public final Stream<Animated> all() {
		return animationsByKey.values().stream();
	}

	public void animate() {
		all().forEach(Animated::animate);
	}

	public void reset() {
		all().forEach(Animated::reset);
	}

	public void restart() {
		all().forEach(Animated::restart);
	}

	public void stop() {
		all().forEach(Animated::stop);
	}

	public void start() {
		all().forEach(Animated::start);
	}

	public void ensureRunning() {
		all().forEach(Animated::ensureRunning);
	}
}
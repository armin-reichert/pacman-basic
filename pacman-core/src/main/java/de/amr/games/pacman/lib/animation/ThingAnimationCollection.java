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

package de.amr.games.pacman.lib.animation;

import java.util.stream.Stream;

/**
 * @author Armin Reichert
 * 
 * @param <E> entity type for which these animations are defined
 * @param <K> key type
 * @param <T> thing type (Rectangle, Image)
 */
public abstract class ThingAnimationCollection<E, K, T> {

	protected K selectedKey;

	public abstract ThingAnimation<T> byKey(K key);

	public abstract Stream<ThingAnimation<T>> all();

	public abstract T current(E entity);

	public K selectedKey() {
		return selectedKey;
	}

	public ThingAnimation<T> selectedAnimation() {
		return byKey(selectedKey());
	}

	public void select(K key) {
		selectedKey = key;
	}

	public void reset() {
		all().forEach(ThingAnimation::reset);
	}

	public void stop() {
		all().forEach(ThingAnimation::stop);
	}

	public void run() {
		all().forEach(ThingAnimation::run);
	}

	public void ensureRunning() {
		all().forEach(ThingAnimation::ensureRunning);
	}

	public void restart() {
		all().forEach(ThingAnimation::restart);
	}
}
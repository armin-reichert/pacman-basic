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
package de.amr.games.pacman.lib.animation;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author Armin Reichert
 *
 * @param <K> key type of map (enum)
 * @param <S> sprite type (Image, Rectangle)
 */
public class GenericAnimationMap<K extends Enum<K>, S> implements GenericAnimation {

	private final Map<K, SingleGenericAnimation<S>> animationMap;

	public GenericAnimationMap(Class<K> keyClass) {
		animationMap = new EnumMap<>(keyClass);
	}

	public void put(K key, SingleGenericAnimation<S> animation) {
		animationMap.put(key, animation);
	}

	public SingleGenericAnimation<S> get(K key) {
		return animationMap.get(key);
	}

	public Collection<SingleGenericAnimation<S>> allAnimations() {
		return animationMap.values();
	}

	@Override
	public void reset() {
		allAnimations().forEach(SingleGenericAnimation::reset);
	}

	@Override
	public void restart() {
		allAnimations().forEach(SingleGenericAnimation::restart);
	}

	@Override
	public void stop() {
		allAnimations().forEach(SingleGenericAnimation::stop);
	}

	@Override
	public void run() {
		allAnimations().forEach(SingleGenericAnimation::run);
	}

	@Override
	public void ensureRunning() {
		allAnimations().forEach(SingleGenericAnimation::ensureRunning);
	}

	@Override
	public void setFrameIndex(int index) {
		throw new UnsupportedOperationException();
	}
}
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
public class GenericAnimationMap<K extends Enum<K>, S> implements GenericAnimationAPI {

	private final Map<K, GenericAnimation<S>> animationMap;

	public GenericAnimationMap(Class<K> keyClass) {
		animationMap = new EnumMap<>(keyClass);
	}

	public void put(K key, GenericAnimation<S> animation) {
		animationMap.put(key, animation);
	}

	public GenericAnimation<S> get(K key) {
		return animationMap.get(key);
	}

	public Collection<GenericAnimation<S>> allAnimations() {
		return animationMap.values();
	}

	@Override
	public void reset() {
		allAnimations().forEach(GenericAnimation::reset);
	}

	@Override
	public void restart() {
		allAnimations().forEach(GenericAnimation::restart);
	}

	@Override
	public void stop() {
		allAnimations().forEach(GenericAnimation::stop);
	}

	@Override
	public void run() {
		allAnimations().forEach(GenericAnimation::run);
	}

	@Override
	public void ensureRunning() {
		allAnimations().forEach(GenericAnimation::ensureRunning);
	}

	@Override
	public void setFrameIndex(int index) {
		throw new UnsupportedOperationException();
	}
}
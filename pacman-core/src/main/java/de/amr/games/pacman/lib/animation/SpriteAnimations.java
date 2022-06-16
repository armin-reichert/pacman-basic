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

import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 * 
 * @param <E> entity type for which these animations are defined
 */
public abstract class SpriteAnimations<E> {

	protected Map<String, SpriteAnimation<?>> animationsByName;
	protected String selected;

	public final SpriteAnimation<?> byName(String name) {
		return animationsByName.get(name);
	}

	public final Stream<SpriteAnimation<?>> all() {
		return animationsByName.values().stream();
	}

	public void put(String name, SpriteAnimation<?> animation) {
		animationsByName.put(name, animation);
	}

	public abstract Object current(E entity);

	public void select(String name) {
		selected = name;
	}

	public String selected() {
		return selected;
	}

	public SpriteAnimation<?> selectedAnimation() {
		return byName(selected);
	}

	public void reset() {
		all().forEach(SpriteAnimation::reset);
	}

	public void stop() {
		all().forEach(SpriteAnimation::stop);
	}

	public void run() {
		all().forEach(SpriteAnimation::run);
	}

	public void ensureRunning() {
		all().forEach(SpriteAnimation::ensureRunning);
	}

	public void restart() {
		all().forEach(SpriteAnimation::restart);
	}
}
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

package de.amr.games.pacman.lib.anim;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class EntityAnimationSet<K> {

	private final Map<K, EntityAnimation> animationsByKey;
	protected K selectedKey;

	public EntityAnimationSet(int initialSize) {
		animationsByKey = new HashMap<>(initialSize);
	}

	public final Optional<EntityAnimation> animation(K key) {
		return Optional.ofNullable(animationsByKey.get(key));
	}

	public void put(K key, EntityAnimation animation) {
		animationsByKey.put(key, Objects.requireNonNull(animation));
	}

	public void select(K key) {
		selectedKey = Objects.requireNonNull(key);
	}

	public boolean isSelected(K key) {
		if (selectedKey == null) {
			return false;
		}
		return selectedKey.equals(Objects.requireNonNull(key));
	}

	public K selectedKey() {
		return selectedKey;
	}

	public Optional<EntityAnimation> selectedAnimation() {
		return animation(selectedKey);
	}

	public final Stream<EntityAnimation> all() {
		return animationsByKey.values().stream();
	}

	public void reset() {
		all().forEach(EntityAnimation::reset);
	}

	public void stop() {
		all().forEach(EntityAnimation::stop);
	}

	public void run() {
		all().forEach(EntityAnimation::start);
	}

	public void ensureRunning() {
		all().forEach(EntityAnimation::ensureRunning);
	}

	public void restart() {
		all().forEach(EntityAnimation::restart);
	}
}
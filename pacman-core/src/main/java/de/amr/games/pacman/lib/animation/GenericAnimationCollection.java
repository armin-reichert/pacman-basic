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
 * @param <E> entity type, for example Ghost, Pac etc.
 * @param <K> Key key type
 * @param <S> Sprite type (Rectangle, Image)
 */
public abstract class GenericAnimationCollection<E, K, S> implements GenericAnimation<S> {

	protected K selectedKey;

	public abstract GenericAnimation<S> getByKey(K key);

	public abstract Stream<GenericAnimation<S>> all();

	public abstract S currentSprite(E entity);

	@Override
	public S frame(int i) {
		return null;
	}

	public K selectedKey() {
		return selectedKey;
	}

	public void select(K key) {
		selectedKey = key;
	}

	public GenericAnimation<S> selectedAnimation() {
		return getByKey(selectedKey());
	}

	public void stop(K key) {
		getByKey(key).stop();
	}

	public void run(K key) {
		getByKey(key).run();
	}

	public void restart(K key) {
		getByKey(key).restart();
	}

	@Override
	public void reset() {
		all().forEach(GenericAnimation::reset);
	}

	@Override
	public void stop() {
		all().forEach(GenericAnimation::stop);
	}

	@Override
	public void run() {
		all().forEach(GenericAnimation::run);
	}

	@Override
	public void ensureRunning() {
		all().forEach(GenericAnimation::ensureRunning);
	}

	@Override
	public void restart() {
		all().forEach(GenericAnimation::restart);
	}
}
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
 * @param <ENTITY> entity type, for example Ghost, Pac etc.
 * @param <KEY>    Key key type
 * @param <SPRITE> Sprite type (Rectangle, Image)
 */
public interface GenericAnimationCollection<ENTITY, KEY, SPRITE> extends GenericAnimation {

	public GenericAnimation getByKey(KEY key);

	public Stream<GenericAnimation> all();

	public SPRITE currentSprite(ENTITY entity);

	public void select(KEY key);

	public KEY selectedKey();

	default GenericAnimation selectedAnimation() {
		return getByKey(selectedKey());
	}

	default void stop(KEY key) {
		getByKey(key).stop();
	}

	default void run(KEY key) {
		getByKey(key).run();
	}

	default void restart(KEY key) {
		getByKey(key).restart();
	}

	@Override
	default void reset() {
		all().forEach(GenericAnimation::reset);
	}

	@Override
	default void stop() {
		all().forEach(GenericAnimation::stop);
	}

	@Override
	default void run() {
		all().forEach(GenericAnimation::run);
	}

	@Override
	default void restart() {
		all().forEach(GenericAnimation::restart);
	}
}
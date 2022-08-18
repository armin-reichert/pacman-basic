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

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import de.amr.games.pacman.lib.Direction;

/**
 * @author Armin Reichert
 */
public class EntityAnimationByDirection implements EntityAnimation {

	private final Map<Direction, EntityAnimation> map = new EnumMap<>(Direction.class);
	private final Supplier<Direction> fnDirection;

	public EntityAnimationByDirection(Supplier<Direction> fnDirection) {
		this.fnDirection = fnDirection;
	}

	public void put(Direction dir, EntityAnimation animation) {
		map.put(dir, animation);
	}

	public EntityAnimation get(Direction dir) {
		return map.get(dir);
	}

	private EntityAnimation currentAnimation() {
		return map.get(fnDirection.get());
	}

	@Override
	public void advance() {
		currentAnimation().advance();
	}

	@Override
	public Object animate() {
		return currentAnimation().animate();
	}

	@Override
	public void ensureRunning() {
		map.values().forEach(EntityAnimation::ensureRunning);
	}

	@Override
	public Object frame() {
		return currentAnimation().frame();
	}

	@Override
	public void setFrameDuration(long frameTicks) {
		currentAnimation().setFrameDuration(frameTicks);
	}

	@Override
	public int numFrames() {
		return currentAnimation().numFrames();
	}

	@Override
	public Object frame(int i) {
		return currentAnimation().frame(i);
	}

	@Override
	public boolean isRunning() {
		return currentAnimation().isRunning();
	}

	@Override
	public void reset() {
		map.values().forEach(EntityAnimation::reset);
	}

	@Override
	public void restart() {
		map.values().forEach(EntityAnimation::restart);
	}

	@Override
	public void setRepetitions(int n) {
		currentAnimation().setRepetitions(n);
	}

	@Override
	public void run() {
		map.values().forEach(EntityAnimation::run);
	}

	@Override
	public void setFrameIndex(int i) {
		currentAnimation().setFrameIndex(i);
	}

	@Override
	public void stop() {
		map.values().forEach(EntityAnimation::stop);
	}
}
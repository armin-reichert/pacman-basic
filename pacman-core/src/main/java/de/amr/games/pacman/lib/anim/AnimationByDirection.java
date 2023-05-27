/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.anim;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import de.amr.games.pacman.lib.steering.Direction;

/**
 * @author Armin Reichert
 */
public class AnimationByDirection implements Animated {

	private final Map<Direction, Animated> map = new EnumMap<>(Direction.class);
	private final Supplier<Direction> fnDirection;

	public AnimationByDirection(Supplier<Direction> fnDirection) {
		this.fnDirection = fnDirection;
	}

	public void put(Direction dir, Animated animation) {
		map.put(dir, animation);
	}

	public Animated get(Direction dir) {
		return map.get(dir);
	}

	private Animated currentAnimation() {
		return map.get(fnDirection.get());
	}

	@Override
	public Object animate() {
		return currentAnimation().animate();
	}

	@Override
	public void ensureRunning() {
		map.values().forEach(Animated::ensureRunning);
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
	public int frameIndex() {
		return currentAnimation().frameIndex();
	}

	@Override
	public boolean isRunning() {
		return currentAnimation().isRunning();
	}

	@Override
	public void reset() {
		map.values().forEach(Animated::reset);
	}

	@Override
	public void restart() {
		map.values().forEach(Animated::restart);
	}

	@Override
	public void setRepetitions(int n) {
		currentAnimation().setRepetitions(n);
	}

	@Override
	public void start() {
		map.values().forEach(Animated::start);
	}

	@Override
	public void setFrameIndex(int i) {
		currentAnimation().setFrameIndex(i);
	}

	@Override
	public void stop() {
		map.values().forEach(Animated::stop);
	}
}
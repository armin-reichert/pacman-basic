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

import java.util.Optional;

/**
 * Mix-in with useful methods for an animated entity.
 * 
 * @author Armin Reichert
 */
public interface AnimatedEntity {

	public Optional<AnimationMap> animations();

	/**
	 * @param key key identifying animation in set
	 * @return (optional) animation specified by given key
	 */
	default Optional<Animated> animation(String key) {
		return animations().flatMap(am -> am.animation(key));
	}

	/**
	 * @return (optional) selected animation
	 */
	default Optional<Animated> animation() {
		return animations().flatMap(AnimationMap::selectedAnimation);
	}

	/**
	 * Selects animation by given key and ensures it is running.
	 * 
	 * @param key key identifying animation in set
	 * @return (optional) selected animation
	 */
	default Optional<Animated> selectAndRunAnimation(String key) {
		animations().ifPresent(anims -> {
			anims.select(key);
			anims.selectedAnimation().ifPresent(Animated::ensureRunning);
		});
		return animation();
	}

	/**
	 * Selects animation by given key and resets it without starting it.
	 * 
	 * @param key key identifying animation in set
	 * @return (optional) selected animation
	 */
	default Optional<Animated> selectAndResetAnimation(String key) {
		animations().ifPresent(anims -> {
			anims.select(key);
			anims.selectedAnimation().ifPresent(Animated::reset);
		});
		return animation();
	}

	/**
	 * Advances the currently selected animation by a single frame.
	 */
	default void animate() {
		animation().ifPresent(Animated::animate);
	}

	default void startAnimation() {
		animation().ifPresent(Animated::start);
	}

	default void stopAnimation() {
		animation().ifPresent(Animated::stop);
	}

	default boolean isAnimationSelected(String key) {
		var animations = animations();
		if (animations.isPresent()) {
			return animations.get().isSelected(key);
		}
		return false;
	}
}
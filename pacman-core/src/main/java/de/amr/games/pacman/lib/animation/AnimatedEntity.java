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

import java.util.Optional;

/**
 * Mix-in with useful methods for an animated entity.
 * 
 * @param <K> key type of animation set, most probably an enumeration type
 * @author Armin Reichert
 */
public interface AnimatedEntity<K> {

	public Optional<EntityAnimationSet<K>> animationSet();

	/**
	 * @param key key identifying animation in set
	 * @return (optional) animation specified by given key
	 */
	default Optional<EntityAnimation> animation(K key) {
		var animSet = animationSet();
		return animSet.isPresent() ? animSet.get().animation(key) : Optional.empty();
	}

	/**
	 * @return (optional) selected animation
	 */
	default Optional<EntityAnimation> selectedAnimation() {
		var animSet = animationSet();
		return animSet.isPresent() ? animSet.get().selectedAnimation() : Optional.empty();
	}

	/**
	 * Selects animation by given key and ensures it is running.
	 * 
	 * @param key key identifying animation in set
	 * @return (optional) selected animation
	 */
	default Optional<EntityAnimation> selectAndEnsureRunningAnimation(K key) {
		animationSet().ifPresent(animSet -> {
			animSet.select(key);
			animSet.selectedAnimation().ifPresent(EntityAnimation::ensureRunning);
		});
		return selectedAnimation();
	}

	/**
	 * Selects animation by given key and resets it without starting it.
	 * 
	 * @param key key identifying animation in set
	 * @return (optional) selected animation
	 */
	default Optional<EntityAnimation> selectAndResetAnimation(K key) {
		animationSet().ifPresent(animSet -> {
			animSet.select(key);
			animSet.selectedAnimation().ifPresent(EntityAnimation::reset);
		});
		return selectedAnimation();
	}

	/**
	 * Advances the currently selected animation by a single frame.
	 */
	default void advanceAnimation() {
		selectedAnimation().ifPresent(EntityAnimation::advance);
	}
}
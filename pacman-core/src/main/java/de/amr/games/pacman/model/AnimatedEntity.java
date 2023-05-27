/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import java.util.Optional;

import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.model.actors.Entity;

/**
 * Mix-in with useful methods for an animated entity.
 * 
 * @author Armin Reichert
 */
public interface AnimatedEntity {

	Entity entity();

	default void moveAndAnimate() {
		entity().move();
		animate();
	}

	Optional<AnimationMap> animations();

	/**
	 * @param key key identifying animation in set
	 * @return (optional) animation specified by given key
	 */
	default Optional<Animated> animation(byte key) {
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
	default Optional<Animated> selectAndRunAnimation(byte key) {
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
	default Optional<Animated> selectAndResetAnimation(byte key) {
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

	default boolean isAnimationSelected(byte key) {
		var animations = animations();
		if (animations.isPresent()) {
			return animations.get().isSelected(key);
		}
		return false;
	}
}
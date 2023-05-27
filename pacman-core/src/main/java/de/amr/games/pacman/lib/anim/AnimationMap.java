/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.anim;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class AnimationMap {

	public static final byte NO_SELECTION = -1;

	private final Animated[] animations;
	protected byte selectedKey = NO_SELECTION;

	public AnimationMap(int capacity) {
		animations = new Animated[capacity];
	}

	private void checkValidEntryKey(byte key) {
		if (key < 0 || key > animations.length - 1) {
			throw new IllegalArgumentException(String.format("Invalid animation map key: %d", key));
		}
	}

	private void checkValidSelectionKey(byte key) {
		if (key < -1 || key > animations.length - 1) {
			throw new IllegalArgumentException(String.format("Invalid animation map key: %d", key));
		}
	}

	public final Optional<Animated> animation(byte key) {
		checkValidSelectionKey(key);
		return key == NO_SELECTION ? Optional.empty() : Optional.ofNullable(animations[key]);
	}

	public void put(byte key, Animated animation) {
		checkValidEntryKey(key);
		checkNotNull(animation);
		animations[key] = animation;
	}

	public void select(byte key) {
		checkValidSelectionKey(key);
		selectedKey = key;
	}

	public void selectAndRestart(byte key) {
		select(key);
		animation(selectedKey).ifPresent(Animated::restart);
	}

	public boolean isSelected(byte key) {
		checkValidSelectionKey(key);
		return selectedKey == key;
	}

	public byte selectedKey() {
		return selectedKey;
	}

	public Optional<Animated> selectedAnimation() {
		return animation(selectedKey);
	}

	public final Stream<Animated> all() {
		return Arrays.stream(animations).filter(Objects::nonNull);
	}

	public void animate() {
		all().forEach(Animated::animate);
	}

	public void reset() {
		all().forEach(Animated::reset);
	}

	public void restart() {
		all().forEach(Animated::restart);
	}

	public void stop() {
		all().forEach(Animated::stop);
	}

	public void start() {
		all().forEach(Animated::start);
	}

	public void ensureRunning() {
		all().forEach(Animated::ensureRunning);
	}
}
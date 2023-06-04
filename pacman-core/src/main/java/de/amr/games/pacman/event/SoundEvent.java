/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.event;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

import de.amr.games.pacman.model.GameModel;

/**
 * @author Armin Reichert
 */
public class SoundEvent extends GameEvent {

	public static final byte BONUS_EATEN = 0;
	public static final byte CREDIT_ADDED = 1;
	public static final byte EXTRA_LIFE = 2;
	public static final byte GHOST_EATEN = 3;
	public static final byte HUNTING_PHASE_STARTED_0 = 4;
	public static final byte HUNTING_PHASE_STARTED_2 = 5;
	public static final byte HUNTING_PHASE_STARTED_4 = 6;
	public static final byte HUNTING_PHASE_STARTED_6 = 7;
	public static final byte PACMAN_DEATH = 8;
	public static final byte PACMAN_FOUND_FOOD = 9;
	public static final byte PACMAN_POWER_ENDS = 10;
	public static final byte PACMAN_POWER_STARTS = 11;
	public static final byte READY_TO_PLAY = 12;
	public static final byte START_INTERMISSION_1 = 13;
	public static final byte START_INTERMISSION_2 = 14;
	public static final byte START_INTERMISSION_3 = 15;
	public static final byte STOP_ALL_SOUNDS = 16;

	public final byte id;

	public SoundEvent(GameModel game, byte id) {
		super(game, GameEvent.SOUND_EVENT, null);
		checkNotNull(id);
		this.id = id;
	}

	@Override
	public String toString() {
		return String.format("SoundEvent('%s')", id);
	}
}
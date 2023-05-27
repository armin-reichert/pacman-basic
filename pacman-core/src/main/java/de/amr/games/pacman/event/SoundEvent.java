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

	public final byte id;

	public SoundEvent(GameModel game, byte id) {
		super(game, GameEventType.SOUND_EVENT, null);
		checkNotNull(id);
		this.id = id;
	}

	@Override
	public String toString() {
		return String.format("SoundEvent('%s')", id);
	}
}
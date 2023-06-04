/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.event;

import de.amr.games.pacman.model.GameModel;

/**
 * @author Armin Reichert
 */
public class UnspecifiedChangeEvent extends GameEvent {

	public UnspecifiedChangeEvent(GameModel game) {
		super(game, GameEvent.UNSPECIFIED_CHANGE, null);
	}
}

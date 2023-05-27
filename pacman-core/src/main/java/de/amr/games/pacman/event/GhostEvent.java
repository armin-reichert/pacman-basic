/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.event;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;

/**
 * @author Armin Reichert
 */
public class GhostEvent extends GameEvent {

	public final Ghost ghost;

	public GhostEvent(GameModel game, GameEventType type, Ghost ghost) {
		super(game, type, ghost.tile());
		checkNotNull(ghost);
		this.ghost = ghost;
	}

	@Override
	public String toString() {
		return String.format("%s: tile %s, ghost %s", type, tile, ghost);
	}
}
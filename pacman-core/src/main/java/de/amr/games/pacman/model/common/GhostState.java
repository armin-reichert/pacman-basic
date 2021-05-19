package de.amr.games.pacman.model.common;

/**
 * A ghost is exactly in one of these states at any point in time.
 * 
 * @author Armin Reichert
 */
public enum GhostState {
	LOCKED, DEAD, ENTERING_HOUSE, LEAVING_HOUSE, FRIGHTENED, HUNTING_PAC;
}
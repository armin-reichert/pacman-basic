package de.amr.games.pacman.lib;

/**
 * The move directions inside the world.
 * 
 * @author Armin Reichert
 */
public enum Direction {

	LEFT(-1, 0), RIGHT(1, 0), UP(0, -1), DOWN(0, 1);

	private static final Direction[] OPPOSITE = { RIGHT, LEFT, DOWN, UP };

	public final V2i vec;

	public Direction opposite() {
		return OPPOSITE[ordinal()];
	}

	private Direction(int x, int y) {
		vec = new V2i(x, y);
	}
}
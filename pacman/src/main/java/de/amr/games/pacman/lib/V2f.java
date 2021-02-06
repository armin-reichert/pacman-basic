package de.amr.games.pacman.lib;

import java.util.Objects;

/**
 * Immutable float 2D vector.
 * 
 * @author Armin Reichert
 */
public class V2f {

	public static final V2f NULL = new V2f(0, 0);

	private static float EPS = 0.000001f;

	private static boolean almostEquals(float x, float y) {
		return x >= y - EPS && x <= y + EPS;
	}

	public final float x;
	public final float y;

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		V2f other = (V2f) obj;
		return almostEquals(x, other.x) && almostEquals(y, other.y);
	}

	@Override
	public String toString() {
		return String.format("(%.2f, %.2f)", x, y);
	}

	public V2f(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public V2f(V2i v) {
		this(v.x, v.y);
	}

	public V2i toV2i() {
		return new V2i((int) x, (int) y);
	}

	public V2f sum(V2f v) {
		return new V2f(x + v.x, y + v.y);
	}

	public V2f scaled(float s) {
		return new V2f(s * x, s * y);
	}
}
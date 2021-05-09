package de.amr.games.pacman.model.world;

import java.util.Objects;

import de.amr.games.pacman.lib.V2i;

/**
 * A portal is a tunnel end that is connected to the tunnel end on the opposite side of the world.
 * 
 * @author Armin Reichert
 */
public class Portal {

	public final V2i left; // x == -1
	public final V2i right; // x == world.numCols()

	public Portal(V2i left, V2i right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public int hashCode() {
		return Objects.hash(left, right);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Portal other = (Portal) obj;
		return Objects.equals(left, other.left) && Objects.equals(right, other.right);
	}
}
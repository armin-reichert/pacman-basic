package de.amr.games.pacman.entities;

import de.amr.games.pacman.common.V2i;

public class Ghost extends Creature {

	public final V2i scatterTile;
	public V2i targetTile;
	public boolean frightened;
	public boolean enteringHouse;
	public boolean leavingHouse;
	public int bounty;

	public Ghost(String name, V2i homeTile, V2i scatterTile) {
		super(name, homeTile);
		this.scatterTile = scatterTile;
	}
}
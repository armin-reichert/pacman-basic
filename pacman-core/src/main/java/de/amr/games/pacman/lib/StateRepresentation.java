package de.amr.games.pacman.lib;

public class StateRepresentation {

	public final Object id;
	public final TickTimer timer;
	public Runnable onEnter, onUpdate, onExit;

	public StateRepresentation(Object id) {
		this.id = id;
		timer = new TickTimer();
	}
}
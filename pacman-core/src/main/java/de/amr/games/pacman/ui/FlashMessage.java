package de.amr.games.pacman.ui;

import de.amr.games.pacman.lib.TickTimer;

public class FlashMessage {

	public final TickTimer timer = new TickTimer("FlashMessage-timer");
	public String text;

	public FlashMessage(String text, long displayTicks) {
		this.text = text;
		timer.reset(displayTicks);
	}
}
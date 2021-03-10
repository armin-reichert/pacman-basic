package de.amr.games.pacman.ui;

import de.amr.games.pacman.lib.TickTimer;

public class FlashMessage {

	public final TickTimer timer = new TickTimer();
	public String text;

	public FlashMessage(String text, long displayDuration) {
		this.text = text;
		timer.setDuration(displayDuration);
	}
}
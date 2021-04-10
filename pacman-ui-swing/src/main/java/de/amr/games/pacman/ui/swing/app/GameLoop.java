package de.amr.games.pacman.ui.swing.app;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.controller.PacManGameController;

public class GameLoop {

	public final SpeedControl clock = new SpeedControl();
	private final PacManGameController controller;
	private Thread thread;
	private boolean running;

	public GameLoop(PacManGameController controller) {
		this.controller = controller;
	}

	private void run() {
		while (running) {
			clock.frame(controller::step);
			controller.getUI().update();
		}
	}

	public void start() {
		if (running) {
			log("Cannot start: Game loop is already running");
			return;
		}
		thread = new Thread(this::run, "GameLoop");
		thread.start();
		running = true;
	}

	public void end() {
		running = false;
		try {
			thread.join();
		} catch (Exception x) {
			x.printStackTrace();
		}
		log("Exit game and terminate VM");
		System.exit(0);
	}
}
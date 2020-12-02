package de.amr.games.pacman;

public class GameClock {

	public int targetFPS = 60;
	public long fps;
	public long frames;
	public long framesTotal;
	public long fpsCountStartTime;
	public long frameStartTime;
	public long frameEndTime;
	public long frameDuration;

	public int sec(float seconds) {
		return (int) (seconds * targetFPS);
	}

	public void tick(Runnable frame) {
		frameStartTime = System.nanoTime();
		frame.run();
		frameEndTime = System.nanoTime();
		frameDuration = frameEndTime - frameStartTime;
		++frames;
		++framesTotal;
		if (frameEndTime - fpsCountStartTime >= 1_000_000_000) {
			fps = frames;
			frames = 0;
			fpsCountStartTime = System.nanoTime();
		}
		long sleepTime = Math.max(1_000_000_000 / targetFPS - frameDuration, 0);
		if (sleepTime > 0) {
			try {
				Thread.sleep(sleepTime / 1_000_000); // milliseconds
			} catch (InterruptedException x) {
				x.printStackTrace();
			}
		}
	}
}
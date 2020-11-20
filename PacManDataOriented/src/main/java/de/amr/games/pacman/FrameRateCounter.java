package de.amr.games.pacman;

public class FrameRateCounter {

	public long fps;
	public long frames;
	public long framesTotal;
	public long fpsCountStartTime;
	public long frameStartTime;
	public long frameEndTime;
	public long frameDuration;

	public void update() {
		++frames;
		++framesTotal;
		if (frameEndTime - fpsCountStartTime >= 1_000_000_000) {
			fps = frames;
			frames = 0;
			fpsCountStartTime = System.nanoTime();
		}
	}
}

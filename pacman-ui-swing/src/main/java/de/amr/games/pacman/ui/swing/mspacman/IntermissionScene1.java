package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.GameScene;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;

public class IntermissionScene1 implements GameScene {

	enum Phase {

		FLAP, CHASED_BY_GHOSTS;

		long ticks;

		void tick() {
			if (ticks != Long.MAX_VALUE && ticks > 0) {
				--ticks;
			}
		}
	}

	private final V2i size;
	private final MsPacManGameRendering rendering;
	private final SoundManager soundManager;
	private final PacManGameModel game;

	private Phase phase;

	private Pac pac, msPac;
	private Ghost pinky, inky;
	private EnumMap<Direction, Animation<BufferedImage>> pacManMunching;

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.ticks = ticks;
	}

	public IntermissionScene1(V2i size, MsPacManGameRendering rendering, SoundManager soundManager,
			PacManGameModel game) {
		this.size = size;
		this.rendering = rendering;
		this.soundManager = soundManager;
		this.game = game;

		Spritesheet sheet = rendering.assets;
		sheet.setOrigin(456, 0);
		pacManMunching = new EnumMap<>(Direction.class);
		pacManMunching.put(Direction.RIGHT,
				Animation.of(sheet.spriteAt(0, 9), sheet.spriteAt(1, 9), sheet.spriteAt(2, 9)).endless().frameDuration(4));
		pacManMunching.put(Direction.LEFT,
				Animation.of(sheet.spriteAt(0, 10), sheet.spriteAt(1, 10), sheet.spriteAt(2, 9)).endless().frameDuration(4));
		pacManMunching.put(Direction.UP,
				Animation.of(sheet.spriteAt(0, 11), sheet.spriteAt(1, 11), sheet.spriteAt(2, 9)).endless().frameDuration(4));
		pacManMunching.put(Direction.DOWN,
				Animation.of(sheet.spriteAt(0, 12), sheet.spriteAt(1, 12), sheet.spriteAt(2, 9)).endless().frameDuration(4));
		sheet.setOrigin(0, 0);
	}

	@Override
	public V2i sizeInPixel() {
		return size;
	}

	@Override
	public void start() {

		pac = new Pac("Pac-Man", Direction.RIGHT);
		pac.position = new V2f(-t(2), t(8));
		pac.visible = true;
		pac.couldMove = true;
		pacManMunching.values().forEach(Animation::restart);

		inky = new Ghost(2, "Inky", Direction.RIGHT);
		inky.position = pac.position.sum(-t(3), 0);
		inky.visible = true;

		msPac = new Pac("Ms. Pac-Man", Direction.LEFT);
		msPac.position = new V2f(t(30), t(16));
		msPac.visible = true;
		msPac.couldMove = true;
		rendering.pacMunching(msPac).forEach(Animation::restart);

		pinky = new Ghost(1, "Pinky", Direction.LEFT);
		pinky.position = msPac.position.sum(t(3), 0);
		pinky.visible = true;

		rendering.ghostsKicking(Stream.of(inky, pinky)).forEach(Animation::restart);
		rendering.assets.flapAnim.restart();
		soundManager.loop(PacManGameSound.INTERMISSION_1, 1);

		enter(Phase.FLAP, clock.sec(1));
	}

	@Override
	public void update() {
		switch (phase) {
		case FLAP:
			if (phase.ticks == 0) {
				pac.speed = msPac.speed = 1;
				inky.speed = pinky.speed = 1.04f;
				enter(Phase.CHASED_BY_GHOSTS, Long.MAX_VALUE);
			}
			break;
		case CHASED_BY_GHOSTS:
			inky.move();
			pac.move();
			pinky.move();
			msPac.move();
			break;
		default:
			break;
		}
		if (game.state.ticksRun() == clock.sec(10)) {
			game.state.duration(0);
		}
		phase.tick();
//		log("%s %d ticks", phase, phase.ticks);
	}

	@Override
	public void render(Graphics2D g) {
		if (phase == Phase.FLAP) {
			drawFlapAnimation(g, t(3), t(10));
		}
		drawPacMan(g);
		rendering.drawGhost(g, inky, game);
		rendering.drawPac(g, msPac, game);
		rendering.drawGhost(g, pinky, game);
	}

	private void drawPacMan(Graphics2D g) {
		rendering.drawImage(g, pacManMunching.get(pac.dir).animate(), pac.position.x - 4, pac.position.y - 4, true);
	}

	private void drawFlapAnimation(Graphics2D g, int flapX, int flapY) {
		rendering.drawImage(g, rendering.assets.flapAnim.animate(), flapX, flapY, false);
		g.setColor(new Color(222, 222, 225));
		g.setFont(rendering.assets.getScoreFont());
		g.drawString("1", flapX + 20, flapY + 30);
		if (rendering.assets.flapAnim.isRunning()) {
			g.drawString("THEY MEET", flapX + 40, flapY + 20);
		}
	}
}
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

		FLAP, CHASED_BY_GHOSTS, COMING_TOGETHER, READY_TO_PLAY;

		long ticksRemaining;
		long ticksRun;

		void tick() {
			if (ticksRemaining != Long.MAX_VALUE && ticksRemaining > 0) {
				--ticksRemaining;
				++ticksRun;
			}
		}

		boolean running(int runningTicks) {
			return ticksRun == runningTicks;
		}
	}

	private final V2i size;
	private final MsPacManGameRendering rendering;
	private final SoundManager soundManager;
	private final PacManGameModel game;

	private Phase phase;

	private int upperY = t(12), lowerY = t(24), middleY = t(18);
	private Pac pac, msPac;
	private Ghost pinky, inky;
	private EnumMap<Direction, Animation<BufferedImage>> pacManMunching;
	private BufferedImage heart;

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.ticksRemaining = ticks;
		phase.ticksRun = 0;
	}

	public IntermissionScene1(V2i size, MsPacManGameRendering rendering, SoundManager soundManager,
			PacManGameModel game) {
		this.size = size;
		this.rendering = rendering;
		this.soundManager = soundManager;
		this.game = game;

		Spritesheet sheet = rendering.assets;
		sheet.setOrigin(456, 0);
		heart = sheet.spriteAt(2, 10);
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
		pac.position = new V2f(-t(2), upperY);
		pac.visible = true;
		pac.couldMove = true;
		pacManMunching.values().forEach(Animation::restart);

		inky = new Ghost(2, "Inky", Direction.RIGHT);
		inky.position = pac.position.sum(-t(3), 0);
		inky.visible = true;

		msPac = new Pac("Ms. Pac-Man", Direction.LEFT);
		msPac.position = new V2f(t(30), lowerY);
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
			if (phase.ticksRemaining == 0) {
				startChasedByGhosts();
			}
			break;
		case CHASED_BY_GHOSTS:
			inky.move();
			pac.move();
			pinky.move();
			msPac.move();
			if (inky.position.x > t(30)) {
				startComingTogether();
			}
			break;
		case COMING_TOGETHER:
			inky.move();
			pac.move();
			pinky.move();
			msPac.move();
			if (pac.position.x < t(15)) {
				pac.dir = msPac.dir = Direction.UP;
				pinky.speed = inky.speed = 0; // TODO almost collision and bouncing back
			}
			if (pac.position.y < upperY) {
				pac.speed = msPac.speed = 0;
				pac.dir = Direction.LEFT;
				msPac.dir = Direction.RIGHT;
				enter(Phase.READY_TO_PLAY, clock.sec(300));
			}
			break;
		case READY_TO_PLAY:
			if (phase.running(clock.sec(1))) {
				inky.visible = false;
				pinky.visible = false;
			}
			if (phase.ticksRemaining == 0) {
				game.state.duration(0);
			}
			break;
		default:
			break;
		}
		phase.tick();
//		log("%s %d ticks", phase, phase.ticks);
	}

	private void startChasedByGhosts() {
		pac.speed = msPac.speed = 1;
		inky.speed = pinky.speed = 1.04f;
		enter(Phase.CHASED_BY_GHOSTS, Long.MAX_VALUE);
	}

	private void startComingTogether() {
		pac.position = new V2f(t(30), middleY);
		inky.position = new V2f(t(33), middleY);
		pac.dir = Direction.LEFT;
		inky.dir = inky.wishDir = Direction.LEFT;
		pinky.position = new V2f(t(-5), middleY);
		msPac.position = new V2f(t(-2), middleY);
		msPac.dir = Direction.RIGHT;
		pinky.dir = pinky.wishDir = Direction.RIGHT;
		enter(Phase.COMING_TOGETHER, Long.MAX_VALUE);
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
		if (phase == Phase.READY_TO_PLAY) {
			rendering.drawImage(g, heart, msPac.position.x + 4, pac.position.y - 20, true);
		}
	}

	private void drawPacMan(Graphics2D g) {
		Animation<BufferedImage> munching = pacManMunching.get(pac.dir);
		if (pac.speed > 0) {
			rendering.drawImage(g, munching.animate(), pac.position.x - 4, pac.position.y - 4, true);
		} else {
			rendering.drawImage(g, munching.frame(1), pac.position.x - 4, pac.position.y - 4, true);
		}
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
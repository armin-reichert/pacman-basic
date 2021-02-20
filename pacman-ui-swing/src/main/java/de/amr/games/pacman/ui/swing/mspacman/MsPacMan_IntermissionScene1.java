package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacMan_GameRendering.assets;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;
import de.amr.games.pacman.ui.swing.common.AbstractGameScene;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they
 * quickly move upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms.
 * Pac-Man face each other at the top of the screen and a big pink heart appears above them. (Played
 * after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene1 extends AbstractGameScene {

	enum Phase {

		FLAP, CHASED_BY_GHOSTS, COMING_TOGETHER, READY_TO_PLAY;

		final CountdownTimer timer = new CountdownTimer();
	}

	private final MsPacMan_GameRendering rendering = PacManGameSwingUI.msPacManGameRendering;
	private final SoundManager sounds = PacManGameSwingUI.msPacManGameSounds;

	private Phase phase;

	private int upperY = t(12), lowerY = t(24), middleY = t(18);
	private Pac pac, msPac;
	private Ghost pinky, inky;
	private EnumMap<Direction, Animation<BufferedImage>> pacManMunching;
	private BufferedImage heart;
	private boolean heartVisible;
	private boolean ghostsMet;

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	public MsPacMan_IntermissionScene1(Dimension size) {
		super(size);

		assets.setOrigin(456, 0);
		heart = assets.spriteAt(2, 10);
		pacManMunching = new EnumMap<>(Direction.class);
		pacManMunching.put(Direction.RIGHT,
				Animation.of(assets.spriteAt(0, 9), assets.spriteAt(1, 9), assets.spriteAt(2, 9)).endless().frameDuration(4));
		pacManMunching.put(Direction.LEFT,
				Animation.of(assets.spriteAt(0, 10), assets.spriteAt(1, 10), assets.spriteAt(2, 9)).endless().frameDuration(4));
		pacManMunching.put(Direction.UP,
				Animation.of(assets.spriteAt(0, 11), assets.spriteAt(1, 11), assets.spriteAt(2, 9)).endless().frameDuration(4));
		pacManMunching.put(Direction.DOWN,
				Animation.of(assets.spriteAt(0, 12), assets.spriteAt(1, 12), assets.spriteAt(2, 9)).endless().frameDuration(4));
		assets.setOrigin(0, 0);
	}

	@Override
	public Dimension sizeInPixel() {
		return size;
	}

	@Override
	public void start() {

		pac = new Pac("Pac-Man", Direction.RIGHT);
		pac.setPosition(-t(2), upperY);
		pac.visible = true;
		pac.couldMove = true;
		pacManMunching.values().forEach(Animation::restart);

		inky = new Ghost(2, "Inky", Direction.RIGHT);
		inky.setPosition(pac.position.sum(-t(3), 0));
		inky.visible = true;

		msPac = new Pac("Ms. Pac-Man", Direction.LEFT);
		msPac.setPosition(t(30), lowerY);
		msPac.visible = true;
		msPac.couldMove = true;
		rendering.pacMunching(msPac).forEach(Animation::restart);

		pinky = new Ghost(1, "Pinky", Direction.LEFT);
		pinky.setPosition(msPac.position.sum(t(3), 0));
		pinky.visible = true;

		rendering.ghostsKicking(Stream.of(inky, pinky)).forEach(Animation::restart);
		assets.flapAnim.restart();
		sounds.loop(PacManGameSound.INTERMISSION_1, 1);

		heartVisible = false;
		ghostsMet = false;

		enter(Phase.FLAP, clock.sec(1));
	}

	@Override
	public void update() {
		switch (phase) {

		case FLAP:
			if (phase.timer.expired()) {
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
			pinky.move();
			pac.move();
			msPac.move();
			if (pac.dir == Direction.LEFT && pac.position.x < t(15)) {
				pac.dir = msPac.dir = Direction.UP;
			}
			if (pac.dir == Direction.UP && pac.position.y < upperY) {
				pac.speed = msPac.speed = 0;
				pac.dir = Direction.LEFT;
				msPac.dir = Direction.RIGHT;
				heartVisible = true;
				rendering.ghostKicking(inky).forEach(Animation::reset);
				rendering.ghostKicking(pinky).forEach(Animation::reset);
				enter(Phase.READY_TO_PLAY, clock.sec(3));
			}
			if (!ghostsMet && inky.position.x - pinky.position.x < 16) {
				ghostsMet = true;
				inky.dir = inky.wishDir = inky.dir.opposite();
				pinky.dir = pinky.wishDir = pinky.dir.opposite();
				inky.speed = pinky.speed = 0.2f;
			}
			break;

		case READY_TO_PLAY:
			if (phase.timer.running() == clock.sec(0.5)) {
				inky.visible = false;
				pinky.visible = false;
			}
			if (phase.timer.expired()) {
				game.state.timer.setDuration(0);
			}
			break;

		default:
			break;
		}
		phase.timer.run();
	}

	private void startChasedByGhosts() {
		pac.speed = msPac.speed = 1;
		inky.speed = pinky.speed = 1.04f;
		enter(Phase.CHASED_BY_GHOSTS, Long.MAX_VALUE);
	}

	private void startComingTogether() {
		pac.setPosition(t(30), middleY);
		inky.setPosition(t(33), middleY);
		pac.dir = Direction.LEFT;
		inky.dir = inky.wishDir = Direction.LEFT;
		pinky.setPosition(t(-5), middleY);
		msPac.setPosition(t(-2), middleY);
		msPac.dir = Direction.RIGHT;
		pinky.dir = pinky.wishDir = Direction.RIGHT;
		enter(Phase.COMING_TOGETHER, Long.MAX_VALUE);
	}

	@Override
	public void render(Graphics2D g) {
		if (phase == Phase.FLAP) {
			rendering.drawFlapAnimation(g, t(3), t(10), "1", "THEY MEET");
		}
		drawPacMan(g);
		rendering.drawGhost(g, inky, game);
		rendering.drawPac(g, msPac, game);
		rendering.drawGhost(g, pinky, game);
		if (heartVisible) {
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
}
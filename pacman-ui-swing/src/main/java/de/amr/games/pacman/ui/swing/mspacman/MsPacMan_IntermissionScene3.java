package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.common.JuniorBag;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.common.GameScene;
import de.amr.games.pacman.ui.swing.rendering.SwingRendering;
import de.amr.games.pacman.ui.swing.rendering.standard.FlapUI;
import de.amr.games.pacman.ui.swing.rendering.standard.StorkUI;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle.
 * The stork drops the bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and
 * finally opens up to reveal a tiny Pac-Man. (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene3 extends GameScene {

	enum Phase {

		FLAP, ACTION, READY_TO_PLAY;

		final CountdownTimer timer = new CountdownTimer();
	}

	private static final int BIRD_Y = t(12), GROUND_Y = t(24);

	private FlapUI flap;
	private Pac pacMan;
	private Pac msPacMan;
	private StorkUI stork;
	private JuniorBag bag;

	private Phase phase;

	public MsPacMan_IntermissionScene3(Dimension size, SwingRendering rendering, SoundManager sounds) {
		super(size, rendering, sounds);
	}

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	@Override
	public void start() {

		flap = new FlapUI(3, "JUNIOR");
		flap.setTilePosition(3, 10);
		flap.visible = true;
		flap.flapping.restart();

		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		pacMan.setPosition(t(3), GROUND_Y - 4);

		msPacMan = new Pac("Ms. Pac-Man", Direction.RIGHT);
		msPacMan.setPosition(t(5), GROUND_Y - 4);

		stork = new StorkUI();
		stork.setPosition(t(30), BIRD_Y);
		stork.flying.restart();

		bag = new JuniorBag();
		bag.setPositionRelativeTo(stork, -14, 3);

		enter(Phase.FLAP, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		switch (phase) {
		case FLAP:
			if (phase.timer.running() == clock.sec(2)) {
				flap.visible = false;
				sounds.play(PacManGameSound.INTERMISSION_3);
				enter(Phase.ACTION, Long.MAX_VALUE);
			}
			break;

		case ACTION:
			stork.move();
			bag.move();
			if (phase.timer.running() == 0) {
				pacMan.visible = msPacMan.visible = stork.visible = bag.visible = true;
				stork.velocity = bag.velocity = new V2f(-1.25f, 0);
			}
			// release bag?
			if (!bag.released && stork.position.x <= t(24)) {
				bag.released = true;
			}
			// closed bag reaches ground?
			if (!bag.open && bag.position.y > GROUND_Y) {
				++bag.bounces;
				if (bag.bounces < 5) {
					bag.velocity = new V2f(-0.2f, -1f / bag.bounces);
					bag.setPosition(bag.position.x, GROUND_Y);
				} else {
					bag.open = true;
					bag.velocity = V2f.NULL;
					enter(Phase.READY_TO_PLAY, clock.sec(3));
				}
			}
			break;
		case READY_TO_PLAY:
			stork.move();
			if (phase.timer.expired()) {
				game.state.timer.setDuration(0);
			}
			break;
		default:
			break;
		}
		phase.timer.run();
	}

	@Override
	public void render(Graphics2D g) {
		rendering.drawFlap(g, flap);
		rendering.drawPlayer(g, msPacMan);
		rendering.drawSpouse(g, pacMan);
		rendering.drawStork(g, stork);
		rendering.drawJuniorBag(g, bag);
	}
}